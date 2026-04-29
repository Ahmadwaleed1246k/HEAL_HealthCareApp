package com.example.heal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.heal.LabTest;
import com.example.heal.TestBooking;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class LabTestBookingBottomSheet extends BottomSheetDialogFragment {

    private LabTest labTest;
    private String appointmentType = "clinic_visit";
    private String timeSlot = "09:00 AM";
    private String preferredDate = "2026-05-12";

    public LabTestBookingBottomSheet(LabTest labTest) {
        this.labTest = labTest;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_bottom_sheet_booking, container, false);

        TextView tvPrepInstructions = view.findViewById(R.id.tvPrepInstructions);
        TextView btnConfirmAppointment = view.findViewById(R.id.btnConfirmAppointment);
        
        TextView btnClinicVisit = view.findViewById(R.id.btnClinicVisit);
        TextView btnHomeCollection = view.findViewById(R.id.btnHomeCollection);

        tvPrepInstructions.setText(labTest.getPreparation_instructions());
        btnConfirmAppointment.setText("Confirm Appointment + $" + String.format("%.2f", labTest.getPrice()));

        btnClinicVisit.setOnClickListener(v -> {
            appointmentType = "clinic_visit";
            btnClinicVisit.setBackgroundResource(R.drawable.bg_tab_active);
            btnClinicVisit.setTextColor(getResources().getColor(R.color.colorPrimary));
            btnHomeCollection.setBackgroundResource(R.drawable.bg_tab_inactive);
            btnHomeCollection.setTextColor(getResources().getColor(R.color.colorSecondary));
        });

        btnHomeCollection.setOnClickListener(v -> {
            if (!labTest.isAvailable_for_home_collection()) {
                Toast.makeText(getContext(), "Home collection not available for this test", Toast.LENGTH_SHORT).show();
                return;
            }
            appointmentType = "home_collection";
            btnHomeCollection.setBackgroundResource(R.drawable.bg_tab_active);
            btnHomeCollection.setTextColor(getResources().getColor(R.color.colorPrimary));
            btnClinicVisit.setBackgroundResource(R.drawable.bg_tab_inactive);
            btnClinicVisit.setTextColor(getResources().getColor(R.color.colorSecondary));
        });

        btnConfirmAppointment.setOnClickListener(v -> {
            saveBookingToFirebase();
        });

        return view;
    }

    private void saveBookingToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please log in to book a test", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // First get user name
        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = "";
                if (snapshot.exists()) {
                    userName = snapshot.child("name").getValue(String.class);
                }

                TestBooking booking = new TestBooking();
                booking.setBooking_id(UUID.randomUUID().toString());
                booking.setUser_id(user.getUid());
                booking.setUser_name(userName);
                booking.setTest_id(labTest.getTest_id());
                booking.setTest_name(labTest.getName());
                booking.setBooking_date(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                booking.setPreferred_date(preferredDate);
                booking.setTime_slot(timeSlot);
                booking.setAppointment_type(appointmentType);
                booking.setStatus("confirmed");
                booking.setTotal_amount(labTest.getPrice());
                booking.setPayment_status("pending");
                booking.setCreated_at(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()));
                booking.setPreparation_instructions(labTest.getPreparation_instructions());

                mDatabase.child("test_bookings").child(booking.getBooking_id()).setValue(booking)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Booking Confirmed!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to book: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
