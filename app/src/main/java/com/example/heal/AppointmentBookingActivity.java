package com.example.heal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AppointmentBookingActivity extends AppCompatActivity {

    private Doctor doctor;
    private RecyclerView rvDates;
    private ChipGroup cgTimeSlots;
    private TextInputEditText etNotes, etCardNumber;
    private TextView tvConsultationFee;
    private Button btnConfirmBooking;
    private List<BookingDate> dateList;
    private String selectedDate;
    private String selectedTime;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booking);

        doctor = (Doctor) getIntent().getSerializableExtra("doctor");
        if (doctor == null) {
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        setupDoctorInfo();
        setupDateSelection();
        setupTimeSlots();

        btnConfirmBooking.setOnClickListener(v -> bookAppointment());
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        rvDates = findViewById(R.id.rvDates);
        cgTimeSlots = findViewById(R.id.cgTimeSlots);
        etNotes = findViewById(R.id.etNotes);
        etCardNumber = findViewById(R.id.etCardNumber);
        tvConsultationFee = findViewById(R.id.tvConsultationFee);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setupDoctorInfo() {
        TextView tvName = findViewById(R.id.tvDoctorName);
        TextView tvSpecialty = findViewById(R.id.tvDoctorSpecialty);
        ImageView ivProfile = findViewById(R.id.ivDoctorImage);

        tvName.setText(doctor.getName());
        tvSpecialty.setText(doctor.getSpecialization());
        tvConsultationFee.setText("Fee: $" + doctor.getConsultation_fee());
        
        Glide.with(this)
                .load(doctor.getProfile_picture())
                .placeholder(R.drawable.ic_person)
                .into(ivProfile);
    }

    private void setupDateSelection() {
        dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("dd", Locale.getDefault());

        int addedDays = 0;
        while (addedDays < 7) {
            if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                String fullDate = dateFormat.format(calendar.getTime());
                String dayName = dayNameFormat.format(calendar.getTime());
                String dayNumber = dayNumberFormat.format(calendar.getTime());
                
                dateList.add(new BookingDate(fullDate, dayName, dayNumber));
                addedDays++;
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        selectedDate = dateList.get(0).getDate();
        BookingDateAdapter adapter = new BookingDateAdapter(this, dateList, bookingDate -> {
            selectedDate = bookingDate.getDate();
        });
        rvDates.setAdapter(adapter);
    }

    private void setupTimeSlots() {
        if (doctor.getTimings() != null && doctor.getTimings().containsKey("available_slots")) {
            List<String> slots = (List<String>) doctor.getTimings().get("available_slots");
            for (String slot : slots) {
                Chip chip = new Chip(this);
                chip.setText(slot);
                chip.setCheckable(true);
                chip.setClickable(true);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) selectedTime = slot;
                });
                cgTimeSlots.addView(chip);
            }
        } else {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("No available slots for this doctor.");
            cgTimeSlots.addView(tvEmpty);
        }
    }

    private void bookAppointment() {
        if (selectedTime == null) {
            Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardNumber = etCardNumber.getText().toString().trim();
        if (cardNumber.length() != 14) {
            etCardNumber.setError("Please enter a valid 14-digit card number");
            return;
        }

        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        String appointmentId = mDatabase.child("appointments").push().getKey();
        String notes = etNotes.getText().toString().trim();

        mDatabase.child("users").child(userId).get().addOnSuccessListener(snapshot -> {
            String userName = snapshot.child("name").getValue(String.class);
            
            Appointment appointment = new Appointment(
                    appointmentId,
                    doctor.getDoctor_id(),
                    doctor.getName(),
                    userId,
                    userName,
                    selectedDate,
                    selectedTime,
                    "pending",
                    doctor.getConsultation_fee()
            );
            appointment.setNotes(notes);
            appointment.setCardNumber(cardNumber);
            appointment.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()));

            mDatabase.child("appointments").child(appointmentId).setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AppointmentBookingActivity.this, "Appointment Request Sent!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AppointmentBookingActivity.this, "Booking failed. Try again.", Toast.LENGTH_SHORT).show();
                });
        });
    }
}
