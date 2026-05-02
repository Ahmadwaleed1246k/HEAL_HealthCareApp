package com.example.heal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RoomBookingActivity extends AppCompatActivity {

    private Hospital hospital;
    private RecyclerView rvDates;
    private ChipGroup cgRooms;
    private TextView tvRoomCharges, tvAdvanceAmount;
    private Button btnConfirmBooking;
    private List<BookingDate> dateList;
    private String selectedDate;
    private Room selectedRoom;
    private DatabaseReference mDatabase;
    private com.google.android.material.textfield.TextInputEditText etCardNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_booking);

        hospital = (Hospital) getIntent().getSerializableExtra("hospital");
        if (hospital == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupHospitalInfo();
        setupDateSelection();
        setupRooms();

        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvDates = findViewById(R.id.rvDates);
        cgRooms = findViewById(R.id.cgRooms);
        tvRoomCharges = findViewById(R.id.tvRoomCharges);
        tvAdvanceAmount = findViewById(R.id.tvAdvanceAmount);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        etCardNumber = findViewById(R.id.etCardNumber);
        
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupHospitalInfo() {
        TextView tvName = findViewById(R.id.tvHospitalName);
        TextView tvAddress = findViewById(R.id.tvHospitalAddress);
        ImageView ivImage = findViewById(R.id.ivHospitalImage);

        tvName.setText(hospital.getName());
        tvAddress.setText(hospital.getAddress());
        Glide.with(this).load(hospital.getImageUrl()).placeholder(R.drawable.ic_hospital).into(ivImage);
    }

    private void setupDateSelection() {
        dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            dateList.add(new BookingDate(
                dateFormat.format(calendar.getTime()),
                dayNameFormat.format(calendar.getTime()),
                dayNumberFormat.format(calendar.getTime())
            ));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        selectedDate = dateList.get(0).getDate();
        BookingDateAdapter adapter = new BookingDateAdapter(this, dateList, bookingDate -> {
            selectedDate = bookingDate.getDate();
        });
        rvDates.setAdapter(adapter);
    }

    private void setupRooms() {
        if (hospital.getRooms() != null) {
            for (Room room : hospital.getRooms().values()) {
                Chip chip = new Chip(this);
                chip.setText("Room " + room.getRoomNumber() + " (" + room.getType() + ")");
                chip.setCheckable(true);
                chip.setClickable(true);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedRoom = room;
                        updatePaymentSummary();
                    }
                });
                cgRooms.addView(chip);
            }
        }
    }

    private void updatePaymentSummary() {
        if (selectedRoom != null) {
            double total = selectedRoom.getPricePerDay();
            double advance = total * 0.20;
            tvRoomCharges.setText("$" + total);
            tvAdvanceAmount.setText("$" + advance);
        }
    }

    private void confirmBooking() {
        if (selectedRoom == null) {
            Toast.makeText(this, "Please select a room", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardNumber = etCardNumber.getText() != null ? etCardNumber.getText().toString().trim() : "";
        if (cardNumber.isEmpty() || cardNumber.length() < 16) {
            etCardNumber.setError("Valid 16-digit card number is required");
            etCardNumber.requestFocus();
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        String bookingId = mDatabase.child("room_bookings").push().getKey();
        mDatabase.child("users").child(userId).get().addOnSuccessListener(snapshot -> {
            String userName = snapshot.child("name").getValue(String.class);
            
            RoomBooking booking = new RoomBooking(
                bookingId,
                hospital.getHospitalId(),
                hospital.getName(),
                userId,
                userName,
                selectedRoom.getRoomNumber(),
                selectedRoom.getType(),
                selectedDate,
                selectedRoom.getPricePerDay(),
                selectedRoom.getPricePerDay() * 0.20,
                "confirmed"
            );

            mDatabase.child("room_bookings").child(bookingId).setValue(booking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Room Booked Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Booking failed", Toast.LENGTH_SHORT).show());
        });
    }
}
