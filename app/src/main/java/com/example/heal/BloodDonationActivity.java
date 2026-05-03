package com.example.heal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BloodDonationActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private GridLayout glSupplyIndex;
    private LinearLayout llNearbyCenters;
    private GridLayout glTimeSlots;
    private TextView tvSelectedDate;
    
    // For slot selection logic
    private View selectedSlotView = null;
    private String selectedTimeSlot = null;
    
    // For center selection logic
    private View selectedCenterView = null;
    private String selectedCenterId = null;
    private String selectedCenterName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_donation);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("blood_donation");

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        glSupplyIndex = findViewById(R.id.glSupplyIndex);
        llNearbyCenters = findViewById(R.id.llNearbyCenters);
        glTimeSlots = findViewById(R.id.glTimeSlots);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);

        findViewById(R.id.btnEligibilityCheck).setOnClickListener(v -> {
            fetchAndShowEligibility();
        });

        findViewById(R.id.btnConfirmBooking).setOnClickListener(v -> {
            if (selectedCenterId == null) {
                Toast.makeText(this, "Please select a donation center", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedTimeSlot == null) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
                return;
            }
            showBookingConfirmationDialog();
        });

        fetchBloodInventory();
        setupNearbyCenters();
        fetchDonationSlots();
    }

    private void setupNearbyCenters() {
        llNearbyCenters.removeAllViews();
        addCenterCard("indus_hosp", "Indus Hospital Blood Bank", "Korangi, Karachi • 1.2 miles", true);
        addCenterCard("chughtai_lab", "Chughtai Lab Blood Bank", "DHA Phase 2, Karachi • 3.5 miles", false);
        addCenterCard("fatimid_found", "Fatimid Foundation", "Soldier Bazaar, Karachi • 5.0 miles", true);
    }

    private void fetchBloodInventory() {
        mDatabase.child("inventory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                glSupplyIndex.removeAllViews();
                
                // Only showing specific types from the design: A+, B-, O-, AB+
                String[] targetTypes = {"A+", "B-", "O-", "AB+"};
                
                for (String type : targetTypes) {
                    DataSnapshot typeSnapshot = snapshot.child(type);
                    if (typeSnapshot.exists()) {
                        String status = typeSnapshot.child("status").getValue(String.class);
                        addSupplyCard(type, status);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BloodDonationActivity.this, "Failed to load inventory", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSupplyCard(String bloodType, String status) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_blood_supply, glSupplyIndex, false);
        
        TextView tvBloodType = view.findViewById(R.id.tvBloodType);
        TextView tvStatus = view.findViewById(R.id.tvStatus);
        View vStatusLine = view.findViewById(R.id.vStatusLine);
        
        tvBloodType.setText(bloodType);
        tvStatus.setText(status);
        
        if (status != null) {
            if (status.equalsIgnoreCase("Sufficient") || status.equalsIgnoreCase("Adequate")) {
                vStatusLine.setBackgroundColor(android.graphics.Color.parseColor("#00897B")); // Teal
                tvStatus.setTextColor(android.graphics.Color.parseColor("#757575")); // Grey
            } else if (status.equalsIgnoreCase("Critical") || status.equalsIgnoreCase("Urgent")) {
                vStatusLine.setBackgroundColor(android.graphics.Color.parseColor("#E53935")); // Red
                tvStatus.setTextColor(android.graphics.Color.parseColor("#E53935")); // Red
            } else {
                vStatusLine.setBackgroundColor(android.graphics.Color.parseColor("#00897B")); // Default Teal
                tvStatus.setTextColor(android.graphics.Color.parseColor("#212121")); // Dark Grey
            }
        }
        
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        view.setLayoutParams(params);
        
        glSupplyIndex.addView(view);
    }

    private void addCenterCard(String centerId, String name, String addressDistance, boolean isFastTrack) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_donation_center, llNearbyCenters, false);
        
        TextView tvCenterName = view.findViewById(R.id.tvCenterName);
        TextView tvAddressDistance = view.findViewById(R.id.tvAddressDistance);
        TextView tvFastTrack = view.findViewById(R.id.tvFastTrack);
        
        tvCenterName.setText(name);
        tvAddressDistance.setText(addressDistance);
        
        if (isFastTrack) {
            tvFastTrack.setVisibility(View.VISIBLE);
        } else {
            tvFastTrack.setVisibility(View.GONE);
        }
        
        view.setOnClickListener(v -> {
            if (selectedCenterView != null) {
                selectedCenterView.setBackgroundResource(R.drawable.bg_card_white_rounded);
            }
            selectedCenterView = view;
            selectedCenterId = centerId;
            selectedCenterName = name;
            view.setBackgroundResource(R.drawable.bg_card_selected);
        });
        
        llNearbyCenters.addView(view);
    }


    private void fetchDonationSlots() {
        mDatabase.child("donation_slots").child("slot_001").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                glTimeSlots.removeAllViews();
                if (snapshot.exists()) {
                    String date = snapshot.child("date").getValue(String.class);
                    tvSelectedDate.setText(date); // Ideally format this to "Tomorrow, Oct 14"
                    
                    DataSnapshot timesSnap = snapshot.child("available_times");
                    for (DataSnapshot timeSnap : timesSnap.getChildren()) {
                        String time = timeSnap.getValue(String.class);
                        addTimeSlot(time);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addTimeSlot(String timeString) {
        // timeString example: "09:30 AM"
        View view = LayoutInflater.from(this).inflate(R.layout.item_donation_time_slot, glTimeSlots, false);
        LinearLayout llContainer = view.findViewById(R.id.llTimeSlotContainer);
        TextView tvTimePeriod = view.findViewById(R.id.tvTimePeriod);
        TextView tvTime = view.findViewById(R.id.tvTime);
        
        String period = "Morning";
        if (timeString != null && timeString.contains("PM")) {
            period = "Afternoon";
        }
        
        tvTimePeriod.setText(period);
        if (timeString != null) {
            tvTime.setText(timeString.replace(" ", "\n"));
        }
        
        llContainer.setOnClickListener(v -> {
            if (selectedSlotView != null) {
                // Deselect previous
                selectedSlotView.setBackgroundResource(R.drawable.bg_card_white_rounded);
                TextView prevPeriod = selectedSlotView.findViewById(R.id.tvTimePeriod);
                TextView prevTime = selectedSlotView.findViewById(R.id.tvTime);
                prevPeriod.setTextColor(getResources().getColor(R.color.colorSecondary));
                prevTime.setTextColor(android.graphics.Color.parseColor("#212121"));
            }
            
            // Select current
            selectedSlotView = llContainer;
            selectedTimeSlot = timeString;
            llContainer.setBackgroundResource(R.drawable.bg_tab_active);
            tvTimePeriod.setTextColor(getResources().getColor(R.color.white));
            tvTime.setTextColor(getResources().getColor(R.color.white));
        });

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        view.setLayoutParams(params);

        glTimeSlots.addView(view);
    }

    private void fetchAndShowEligibility() {
        mDatabase.child("eligibility_criteria").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder criteriaText = new StringBuilder();
                if (snapshot.exists()) {
                    Long minAge = snapshot.child("age_range").child("min").getValue(Long.class);
                    Long maxAge = snapshot.child("age_range").child("max").getValue(Long.class);
                    if (minAge != null && maxAge != null) {
                        criteriaText.append("• Age between ").append(minAge).append(" and ").append(maxAge).append(" years\n");
                    }
                    
                    Long minWeight = snapshot.child("min_weight_kg").getValue(Long.class);
                    if (minWeight != null) {
                        criteriaText.append("• Weight minimum ").append(minWeight).append(" kg\n");
                    }
                    
                    DataSnapshot addReq = snapshot.child("additional_requirements");
                    if (addReq.exists()) {
                        for (DataSnapshot req : addReq.getChildren()) {
                            criteriaText.append("• ").append(req.getValue(String.class)).append("\n");
                        }
                    }
                } else {
                    criteriaText.append("Failed to load criteria.");
                }

                showEligibilityDialog(criteriaText.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BloodDonationActivity.this, "Error loading criteria", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEligibilityDialog(String criteria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_eligibility, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvCriteria = dialogView.findViewById(R.id.tvCriteria);
        tvCriteria.setText(criteria);

        dialogView.findViewById(R.id.btnCloseEligibility).setOnClickListener(v -> dialog.dismiss());
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void showBookingConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_blood_booking, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView tvBookingSummary = dialogView.findViewById(R.id.tvBookingSummary);
        Spinner spinnerBloodType = dialogView.findViewById(R.id.spinnerBloodType);
        CheckBox cbConsent = dialogView.findViewById(R.id.cbConsent);
        TextView btnFinalBook = dialogView.findViewById(R.id.btnFinalBook);

        String dateStr = tvSelectedDate.getText().toString();
        tvBookingSummary.setText("Center: " + selectedCenterName + "\nDate: " + dateStr + "\nTime: " + selectedTimeSlot);

        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Don't know"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bloodTypes);
        spinnerBloodType.setAdapter(adapter);

        btnFinalBook.setOnClickListener(v -> {
            if (!cbConsent.isChecked()) {
                Toast.makeText(this, "Please check the consent box to proceed", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String selectedBloodType = spinnerBloodType.getSelectedItem().toString();
            saveBookingToFirebase(selectedBloodType, dateStr, dialog);
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void saveBookingToFirebase(String bloodType, String dateStr, AlertDialog dialog) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to book a donation slot", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference rootDb = FirebaseDatabase.getInstance().getReference();
        
        rootDb.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = "";
                if (snapshot.exists()) {
                    userName = snapshot.child("name").getValue(String.class);
                }

                String bookingId = UUID.randomUUID().toString();
                String currentDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());
                
                Map<String, Object> bookingData = new HashMap<>();
                bookingData.put("booking_id", bookingId);
                bookingData.put("user_id", user.getUid());
                bookingData.put("user_name", userName);
                bookingData.put("blood_type", bloodType);
                bookingData.put("center_id", selectedCenterId);
                bookingData.put("center_name", selectedCenterName);
                bookingData.put("preferred_date", dateStr);
                bookingData.put("preferred_time_slot", selectedTimeSlot);
                bookingData.put("booking_status", "confirmed");
                bookingData.put("created_at", currentDate);
                bookingData.put("notes", "Booked via app");

                mDatabase.child("donation_bookings").child(bookingId).setValue(bookingData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(BloodDonationActivity.this, "Booking Confirmed!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(BloodDonationActivity.this, "Failed to book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BloodDonationActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
