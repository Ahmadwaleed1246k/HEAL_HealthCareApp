package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private TextView tvUserName;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView rvTopSpecialists;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> topDoctors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvUserName = findViewById(R.id.tvUserName);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        setupTopSpecialists();
        setupClickListeners();
        fetchUserProfile();
        fetchTopSpecialists();
    }

    private void setupClickListeners() {
        findViewById(R.id.btnEmergency).setOnClickListener(v -> showToast("Emergency Service"));
        findViewById(R.id.btnBlood).setOnClickListener(v -> showToast("Blood Donation Service"));
        findViewById(R.id.btnDoctors).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SpecialistsActivity.class));
        });
        findViewById(R.id.btnPrescription).setOnClickListener(v -> showToast("Prescription Service"));
        findViewById(R.id.btnCheckup).setOnClickListener(v -> showToast("Check-up Service"));
        findViewById(R.id.btnLocation).setOnClickListener(v -> showToast("Location Service"));
        findViewById(R.id.btnHospital).setOnClickListener(v -> showToast("Hospital Service"));
        
        findViewById(R.id.tvViewAllSpecialists).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SpecialistsActivity.class));
        });
        
        findViewById(R.id.profileCard).setOnClickListener(v -> showToast("Opening Profile"));
    }

    private void setupTopSpecialists() {
        rvTopSpecialists = findViewById(R.id.rvTopSpecialists);
        rvTopSpecialists.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        topDoctors = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(this, topDoctors, true, doctor -> {
            Toast.makeText(this, "Doctor: " + doctor.getName(), Toast.LENGTH_SHORT).show();
        });
        rvTopSpecialists.setAdapter(doctorAdapter);
    }

    private void fetchTopSpecialists() {
        mDatabase.child("doctors").limitToFirst(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topDoctors.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Doctor doctor = postSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        topDoctors.add(doctor);
                    }
                }
                doctorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void fetchUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null) {
                            tvUserName.setText(name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

}
