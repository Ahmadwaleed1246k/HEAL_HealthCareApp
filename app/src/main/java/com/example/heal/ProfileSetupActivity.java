package com.example.heal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class ProfileSetupActivity extends AppCompatActivity {

    private AutoCompleteTextView actvMaritalStatus, actvGender;
    private TextInputEditText etPhone, etDob;
    private Button btnCompleteSetup;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        actvMaritalStatus = findViewById(R.id.actvMaritalStatus);
        actvGender = findViewById(R.id.actvGender);
        etPhone = findViewById(R.id.etPhone);
        etDob = findViewById(R.id.etDob);
        btnCompleteSetup = findViewById(R.id.btnCompleteSetup);
        progressBar = findViewById(R.id.progressBar);

        setupDropdowns();
        setupPhoneFormatting();

        etDob.setOnClickListener(v -> showDatePicker());
        btnCompleteSetup.setOnClickListener(v -> saveProfileInfo());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        String[] maritalOptions = {"Married", "Unmarried"};
        ArrayAdapter<String> maritalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, maritalOptions);
        actvMaritalStatus.setAdapter(maritalAdapter);

        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genderOptions);
        actvGender.setAdapter(genderAdapter);
    }

    private void setupPhoneFormatting() {
        etPhone.addTextChangedListener(new TextWatcher() {
            private boolean isDeleting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isDeleting) return;

                String text = s.toString();
                if (text.length() == 4 && !text.contains("-")) {
                    s.insert(4, "-");
                }
            }
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> etDob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                year, month, day);
        datePickerDialog.show();
    }

    private void saveProfileInfo() {
        String maritalStatus = actvMaritalStatus.getText().toString();
        String gender = actvGender.getText().toString();
        String phone = etPhone.getText().toString().trim();
        String dob = etDob.getText().toString().trim();

        if (TextUtils.isEmpty(maritalStatus)) {
            Toast.makeText(this, "Please select marital status", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Phone validation: 03xx-xxxxxxx
        if (!phone.matches("^03[0-9]{2}-[0-9]{7}$")) {
            etPhone.setError("Format must be 03xx-xxxxxxx");
            return;
        }

        if (TextUtils.isEmpty(dob)) {
            etDob.setError("Date of birth is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCompleteSetup.setVisibility(View.GONE);

        // Generate Member Since automatically
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String memberSince = sdf.format(Calendar.getInstance().getTime());

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            HashMap<String, Object> profileData = new HashMap<>();
            profileData.put("maritalStatus", maritalStatus);
            profileData.put("gender", gender);
            profileData.put("phone", phone);
            profileData.put("dob", dob);
            profileData.put("memberSince", memberSince);

            mDatabase.child("users").child(uid).updateChildren(profileData)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        btnCompleteSetup.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileSetupActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            
                            SessionManager sessionManager = new SessionManager(ProfileSetupActivity.this);
                            sessionManager.setLogin(true);

                            startActivity(new Intent(ProfileSetupActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(ProfileSetupActivity.this, "Update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
