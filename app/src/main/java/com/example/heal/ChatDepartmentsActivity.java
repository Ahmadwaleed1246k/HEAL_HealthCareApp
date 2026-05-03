package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatDepartmentsActivity extends AppCompatActivity {

    private RecyclerView rvDepartments;
    private DepartmentAdapter adapter;
    private List<String> departmentsList;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_departments);

        rvDepartments = findViewById(R.id.rvDepartments);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvDepartments.setLayoutManager(new LinearLayoutManager(this));
        departmentsList = new ArrayList<>();
        adapter = new DepartmentAdapter(departmentsList, department -> {
            Intent intent = new Intent(ChatDepartmentsActivity.this, ChatDoctorsActivity.class);
            intent.putExtra("department", department);
            startActivity(intent);
        });
        rvDepartments.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("doctors");
        fetchDepartments();
    }

    private void fetchDepartments() {
        String[] allDepartments = {
                "Cardiologist", "Dermatologist", "Gynecologist", "Pediatrician",
                "Psychiatrist", "Neurologist", "Orthopedic Surgeon", "ENT Specialist",
                "Ophthalmologist", "Urologist", "Gastroenterologist", "Endocrinologist",
                "Pulmonologist", "Nephrologist", "Oncologist", "Rheumatologist",
                "General Physician", "Dentist", "Physical Therapist", "Nutritionist"
        };

        departmentsList.clear();
        for (String dept : allDepartments) {
            departmentsList.add(dept);
        }
        adapter.notifyDataSetChanged();
    }
}
