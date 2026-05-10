package com.example.heal;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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
import java.util.Locale;

public class SpecialistsActivity extends AppCompatActivity {

    private RecyclerView rvSpecialists;
    private DoctorAdapter adapter;
    private List<Doctor> doctorList;
    private List<Doctor> filteredList;
    private DatabaseReference mDatabase;
    private EditText etSearch;
    private String selectedSpecialty = "All";
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
        
        etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctors(selectedSpecialty, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupFilterButtons();
        fetchDoctors();
    }

    private void setupFilterButtons() {
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterCardiology = findViewById(R.id.btnFilterCardiology);
        btnFilterDermatology = findViewById(R.id.btnFilterDermatology);
        btnFilterDentistry = findViewById(R.id.btnFilterDentistry);
        btnFilterPediatrics = findViewById(R.id.btnFilterPediatrics);

        btnFilterAll.setOnClickListener(v -> filterDoctors("All", etSearch.getText().toString()));
        btnFilterCardiology.setOnClickListener(v -> filterDoctors("Cardiologist", etSearch.getText().toString()));
        btnFilterDermatology.setOnClickListener(v -> filterDoctors("Dermatologist", etSearch.getText().toString()));
        btnFilterDentistry.setOnClickListener(v -> filterDoctors("Dentist", etSearch.getText().toString()));
        btnFilterPediatrics.setOnClickListener(v -> filterDoctors("Pediatrician", etSearch.getText().toString()));
    }

    private void filterDoctors(String specialty, String query) {
        selectedSpecialty = specialty;
        filteredList.clear();

        for (Doctor doctor : doctorList) {
            boolean matchesSpecialty = specialty.equals("All") || 
                (doctor.getSpecialization() != null && doctor.getSpecialization().equalsIgnoreCase(specialty));
            
            boolean matchesQuery = query.isEmpty() || 
                (doctor.getName() != null && doctor.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) ||
                (doctor.getSpecialization() != null && doctor.getSpecialization().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)));

            if (matchesSpecialty && matchesQuery) {
                filteredList.add(doctor);
            }
        }

        if (specialty.equals("All")) updateButtonStyles(btnFilterAll);
        else if (specialty.equals("Cardiologist")) updateButtonStyles(btnFilterCardiology);
        else if (specialty.equals("Dermatologist")) updateButtonStyles(btnFilterDermatology);
        else if (specialty.equals("Dentist")) updateButtonStyles(btnFilterDentistry);
        else if (specialty.equals("Pediatrician")) updateButtonStyles(btnFilterPediatrics);

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
                filterDoctors("All", ""); // Default to show all
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpecialistsActivity.this, "Error fetching doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
