package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DoctorProfileSetupActivity extends AppCompatActivity {

    private AutoCompleteTextView etSpecialization;
    private TextInputEditText etExperience, etHospitalName, etHospitalLocation, etFee, etAbout, etLanguages, etQualifications;
    private Button btnCompleteSetup, btnAddSlot;
    private com.google.android.material.chip.ChipGroup cgTimeSlots;
    private ProgressBar progressBar;
    private List<String> selectedTimeSlots = new ArrayList<>();
    
    private final String[] departments = {
            "Cardiologist", "Dermatologist", "Gynecologist", "Pediatrician",
            "Psychiatrist", "Neurologist", "Orthopedic Surgeon", "ENT Specialist",
            "Ophthalmologist", "Urologist", "Gastroenterologist", "Endocrinologist",
            "Pulmonologist", "Nephrologist", "Oncologist", "Rheumatologist",
            "General Physician", "Dentist", "Physical Therapist", "Nutritionist"
    };

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile_setup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etSpecialization = findViewById(R.id.etSpecialization);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departments);
        etSpecialization.setAdapter(adapter);

        etExperience = findViewById(R.id.etExperience);
        etHospitalName = findViewById(R.id.etHospitalName);
        etHospitalLocation = findViewById(R.id.etHospitalLocation);
        etFee = findViewById(R.id.etFee);
        etAbout = findViewById(R.id.etAbout);
        etLanguages = findViewById(R.id.etLanguages);
        etQualifications = findViewById(R.id.etQualifications);
        btnCompleteSetup = findViewById(R.id.btnCompleteSetup);
        btnAddSlot = findViewById(R.id.btnAddSlot);
        cgTimeSlots = findViewById(R.id.cgTimeSlots);
        progressBar = findViewById(R.id.progressBar);

        btnAddSlot.setOnClickListener(v -> showTimePicker());
        btnCompleteSetup.setOnClickListener(v -> saveDoctorProfile());
    }

    private void showTimePicker() {
        com.google.android.material.timepicker.MaterialTimePicker picker =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                        .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
                        .setHour(9)
                        .setMinute(0)
                        .setTitleText("Select Appointment Slot")
                        .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();
            String amPm = hour >= 12 ? "PM" : "AM";
            int hour12 = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
            String time = String.format("%02d:%02d %s", hour12, minute, amPm);
            
            if (!selectedTimeSlots.contains(time)) {
                addTimeChip(time);
            }
        });

        picker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void addTimeChip(String time) {
        selectedTimeSlots.add(time);
        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
        chip.setText(time);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            selectedTimeSlots.remove(time);
            cgTimeSlots.removeView(chip);
        });
        cgTimeSlots.addView(chip);
    }

    private void saveDoctorProfile() {
        String specialization = etSpecialization.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String hospitalName = etHospitalName.getText().toString().trim();
        String hospitalLocation = etHospitalLocation.getText().toString().trim();
        String fee = etFee.getText().toString().trim();
        String about = etAbout.getText().toString().trim();
        String languagesStr = etLanguages.getText().toString().trim();
        String qualificationsStr = etQualifications.getText().toString().trim();

        if (TextUtils.isEmpty(specialization) || TextUtils.isEmpty(experience) || 
            TextUtils.isEmpty(hospitalName) || TextUtils.isEmpty(hospitalLocation) || 
            TextUtils.isEmpty(fee) || TextUtils.isEmpty(about)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCompleteSetup.setVisibility(View.GONE);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String name = user.getDisplayName(); // Usually set during signup
            if (name == null || name.isEmpty()) {
                // Fetch name from users node if not in FirebaseUser
                mDatabase.child("users").child(uid).child("name").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String fetchedName = String.valueOf(task.getResult().getValue());
                        completeSaving(uid, fetchedName, specialization, experience, hospitalName, hospitalLocation, fee, about, languagesStr, qualificationsStr);
                    } else {
                        completeSaving(uid, "Dr. User", specialization, experience, hospitalName, hospitalLocation, fee, about, languagesStr, qualificationsStr);
                    }
                });
            } else {
                completeSaving(uid, name, specialization, experience, hospitalName, hospitalLocation, fee, about, languagesStr, qualificationsStr);
            }
        }
    }

    private void completeSaving(String uid, String name, String specialization, String experience, String hospitalName, String hospitalLocation, String fee, String about, String languagesStr, String qualificationsStr) {
        
        List<String> languages = Arrays.asList(languagesStr.split("\\s*,\\s*"));
        
        HashMap<String, Object> qualificationMap = new HashMap<>();
        qualificationMap.put("degree", qualificationsStr);
        qualificationMap.put("institution", "Medical University");
        qualificationMap.put("year", 2024);
        List<HashMap<String, Object>> qualifications = new ArrayList<>();
        qualifications.add(qualificationMap);

        HashMap<String, Object> doctorData = new HashMap<>();
        doctorData.put("doctor_id", uid);
        doctorData.put("name", name);
        doctorData.put("specialization", specialization);
        doctorData.put("experience_years", Integer.parseInt(experience));
        doctorData.put("hospital_name", hospitalName);
        doctorData.put("hospital_location", hospitalLocation);
        doctorData.put("consultation_fee", Integer.parseInt(fee));
        doctorData.put("about", about);
        doctorData.put("languages", languages);
        doctorData.put("qualifications", qualifications);
        doctorData.put("availability_status", "available");
        doctorData.put("rating", 5.0);
        doctorData.put("total_reviews", 0);
        doctorData.put("profile_picture", ""); // Default or placeholder
        
        HashMap<String, Object> timingData = new HashMap<>();
        timingData.put("available_slots", selectedTimeSlots);
        doctorData.put("timings", timingData);

        // Update users node
        mDatabase.child("users").child(uid).updateChildren(doctorData);
        
        // Update doctors node for public listing
        mDatabase.child("doctors").child(uid).setValue(doctorData)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnCompleteSetup.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        Toast.makeText(DoctorProfileSetupActivity.this, "Profile Setup Complete!", Toast.LENGTH_SHORT).show();
                        
                        SessionManager sessionManager = new SessionManager(DoctorProfileSetupActivity.this);
                        sessionManager.setLogin(true);
                        sessionManager.setRole("Doctor");

                        startActivity(new Intent(DoctorProfileSetupActivity.this, DoctorHomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(DoctorProfileSetupActivity.this, "Setup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
