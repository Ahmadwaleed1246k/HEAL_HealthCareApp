package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

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

public class ChatDoctorsActivity extends AppCompatActivity {

    private RecyclerView rvDoctors;
    private ChatDoctorAdapter adapter;
    private List<Doctor> doctorsList;
    private DatabaseReference mDatabase;
    private String department;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_doctors);

        department = getIntent().getStringExtra("department");
        TextView tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setText(department + " Doctors");

        rvDoctors = findViewById(R.id.rvDoctors);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        doctorsList = new ArrayList<>();
        adapter = new ChatDoctorAdapter(this, doctorsList, doctor -> {
            Intent intent = new Intent(ChatDoctorsActivity.this, ChatInterfaceActivity.class);
            intent.putExtra("doctor", doctor);
            startActivity(intent);
        });
        rvDoctors.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("doctors");
        fetchDoctors();
    }

    private void fetchDoctors() {
        mDatabase.orderByChild("specialization").equalTo(department).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorsList.clear();
                for (DataSnapshot doctorSnapshot : snapshot.getChildren()) {
                    Doctor doctor = doctorSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        doctor.setDoctor_id(doctorSnapshot.getKey());
                        doctorsList.add(doctor);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
