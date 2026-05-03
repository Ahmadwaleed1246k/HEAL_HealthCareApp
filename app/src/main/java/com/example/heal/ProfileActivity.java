package com.example.heal;

import android.app.DatePickerDialog;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etDob;
    private AutoCompleteTextView actvGender, actvMaritalStatus;
    private Button btnSave;
    private ProgressBar progressBar;
    
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        etName = findViewById(R.id.etProfileName);
        etPhone = findViewById(R.id.etProfilePhone);
        etDob = findViewById(R.id.etProfileDob);
        actvGender = findViewById(R.id.actvProfileGender);
        actvMaritalStatus = findViewById(R.id.actvProfileMaritalStatus);
        btnSave = findViewById(R.id.btnSaveProfile);
        progressBar = findViewById(R.id.profileProgressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupDropdowns();
        setupPhoneFormatting();
        fetchUserData();

        etDob.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> updateProfile());
    }

    private void setupDropdowns() {
        String[] genderOptions = {"Male", "Female"};
        actvGender.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, genderOptions));

        String[] maritalOptions = {"Married", "Unmarried"};
        actvMaritalStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, maritalOptions));
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

    private void fetchUserData() {
        if (currentUser != null) {
            mDatabase.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        etName.setText(snapshot.child("name").getValue(String.class));
                        etPhone.setText(snapshot.child("phone").getValue(String.class));
                        etDob.setText(snapshot.child("dob").getValue(String.class));
                        actvGender.setText(snapshot.child("gender").getValue(String.class), false);
                        actvMaritalStatus.setText(snapshot.child("maritalStatus").getValue(String.class), false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
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

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String gender = actvGender.getText().toString();
        String maritalStatus = actvMaritalStatus.getText().toString();

        if (TextUtils.isEmpty(name)) { etName.setError("Required"); return; }
        
        // Phone validation: 03xx-xxxxxxx
        if (!phone.matches("^03[0-9]{2}-[0-9]{7}$")) {
            etPhone.setError("Format must be 03xx-xxxxxxx");
            return;
        }

        if (TextUtils.isEmpty(dob)) { etDob.setError("Required"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.GONE);

        HashMap<String, Object> updateData = new HashMap<>();
        updateData.put("name", name);
        updateData.put("phone", phone);
        updateData.put("dob", dob);
        updateData.put("gender", gender);
        updateData.put("maritalStatus", maritalStatus);

        mDatabase.child("users").child(currentUser.getUid()).updateChildren(updateData)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
