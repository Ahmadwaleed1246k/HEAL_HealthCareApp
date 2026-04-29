package com.example.heal;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heal.LabTestAdapter;
import com.example.heal.LabTestPackageAdapter;
import com.example.heal.LabTest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LabTestsActivity extends AppCompatActivity {

    private RecyclerView rvTestCategories;
    private RecyclerView rvPopularPackages;
    private LabTestAdapter categoryAdapter;
    private LabTestPackageAdapter packageAdapter;

    private List<LabTest> allTestsList;
    private List<LabTest> popularPackagesList;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_tests);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvTestCategories = findViewById(R.id.rvTestCategories);
        rvPopularPackages = findViewById(R.id.rvPopularPackages);

        allTestsList = new ArrayList<>();
        popularPackagesList = new ArrayList<>();

        categoryAdapter = new LabTestAdapter(this, allTestsList, labTest -> openBookingSheet(labTest));
        rvTestCategories.setLayoutManager(new LinearLayoutManager(this));
        rvTestCategories.setAdapter(categoryAdapter);

        packageAdapter = new LabTestPackageAdapter(this, popularPackagesList, labTest -> openBookingSheet(labTest));
        rvPopularPackages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPopularPackages.setAdapter(packageAdapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("lab_tests");
        fetchLabTests();
    }

    private void fetchLabTests() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTestsList.clear();
                popularPackagesList.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    LabTest test = postSnapshot.getValue(LabTest.class);
                    if (test != null) {
                        allTestsList.add(test);
                        if (test.isPopular()) {
                            popularPackagesList.add(test);
                        }
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                packageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LabTestsActivity.this, "Failed to load lab tests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openBookingSheet(LabTest labTest) {
        LabTestBookingBottomSheet bottomSheet = new LabTestBookingBottomSheet(labTest);
        bottomSheet.show(getSupportFragmentManager(), "LabTestBookingBottomSheet");
    }
}
