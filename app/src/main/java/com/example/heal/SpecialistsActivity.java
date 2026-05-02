package com.example.heal;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SpecialistsActivity extends AppCompatActivity {

    private RecyclerView rvSpecialists;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList;
    private List<Doctor> filteredList;
    private DatabaseReference mDatabase;
    private androidx.appcompat.widget.AppCompatButton btnFilterAll, btnFilterCardiology, btnFilterDermatology, btnFilterDentistry, btnFilterPediatrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specialists);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvSpecialists = findViewById(R.id.rvSpecialists);
        rvSpecialists.setLayoutManager(new LinearLayoutManager(this));

        doctorList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new DoctorAdapter(this, filteredList, false, doctor -> {
            android.content.Intent intent = new android.content.Intent(this, AppointmentBookingActivity.class);
            intent.putExtra("doctor", doctor);
            startActivity(intent);
        });
        rvSpecialists.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("doctors");
        
        setupFilterButtons();
        fetchDoctors();
    }

    private void setupFilterButtons() {
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterCardiology = findViewById(R.id.btnFilterCardiology);
        btnFilterDermatology = findViewById(R.id.btnFilterDermatology);
        btnFilterDentistry = findViewById(R.id.btnFilterDentistry);
        btnFilterPediatrics = findViewById(R.id.btnFilterPediatrics);

        btnFilterAll.setOnClickListener(v -> filterDoctors("All"));
        btnFilterCardiology.setOnClickListener(v -> filterDoctors("Cardiologist"));
        btnFilterDermatology.setOnClickListener(v -> filterDoctors("Dermatologist"));
        btnFilterDentistry.setOnClickListener(v -> filterDoctors("Dentist"));
        btnFilterPediatrics.setOnClickListener(v -> filterDoctors("Pediatrician"));
    }

    private void filterDoctors(String specialty) {
        filteredList.clear();
        if (specialty.equals("All")) {
            filteredList.addAll(doctorList);
            updateButtonStyles(btnFilterAll);
        } else {
            for (Doctor doctor : doctorList) {
                if (doctor.getSpecialization() != null && doctor.getSpecialization().equalsIgnoreCase(specialty)) {
                    filteredList.add(doctor);
                }
            }
            if (specialty.equals("Cardiologist")) updateButtonStyles(btnFilterCardiology);
            else if (specialty.equals("Dermatologist")) updateButtonStyles(btnFilterDermatology);
            else if (specialty.equals("Dentist")) updateButtonStyles(btnFilterDentistry);
            else if (specialty.equals("Pediatrician")) updateButtonStyles(btnFilterPediatrics);
        }
        adapter.notifyDataSetChanged();
    }

    private void updateButtonStyles(androidx.appcompat.widget.AppCompatButton selectedButton) {
        // Reset all buttons
        androidx.appcompat.widget.AppCompatButton[] buttons = {btnFilterAll, btnFilterCardiology, btnFilterDermatology, btnFilterDentistry, btnFilterPediatrics};
        for (androidx.appcompat.widget.AppCompatButton btn : buttons) {
            btn.setBackgroundTintList(null);
            btn.setTextColor(getResources().getColor(R.color.colorSecondary));
        }
        // Highlight selected
        selectedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        selectedButton.setTextColor(getResources().getColor(R.color.white));
    }

    private void fetchDoctors() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Doctor doctor = postSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        doctorList.add(doctor);
                    }
                }
                filterDoctors("All"); // Default to show all
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpecialistsActivity.this, "Error fetching doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
