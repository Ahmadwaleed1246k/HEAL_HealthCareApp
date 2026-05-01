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
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specialists);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvSpecialists = findViewById(R.id.rvSpecialists);
        rvSpecialists.setLayoutManager(new LinearLayoutManager(this));

        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(this, doctorList, false, doctor -> {
            android.content.Intent intent = new android.content.Intent(this, AppointmentBookingActivity.class);
            intent.putExtra("doctor", doctor);
            startActivity(intent);
        });
        rvSpecialists.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("doctors");
        fetchDoctors();
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpecialistsActivity.this, "Error fetching doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
