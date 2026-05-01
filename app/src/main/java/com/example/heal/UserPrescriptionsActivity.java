package com.example.heal;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UserPrescriptionsActivity extends AppCompatActivity {

    private RecyclerView rvPrescriptions;
    private PrescriptionAdapter adapter;
    private List<Prescription> prescriptionList;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_prescriptions);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvPrescriptions = findViewById(R.id.rvUserPrescriptions);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvPrescriptions.setLayoutManager(new LinearLayoutManager(this));
        prescriptionList = new ArrayList<>();
        adapter = new PrescriptionAdapter(this, prescriptionList);
        rvPrescriptions.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("prescriptions");
        fetchPrescriptions();
    }

    private void fetchPrescriptions() {
        String patientId = FirebaseAuth.getInstance().getUid();
        if (patientId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        mDatabase.orderByChild("patientId").equalTo(patientId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                prescriptionList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Prescription prescription = dataSnapshot.getValue(Prescription.class);
                    if (prescription != null) {
                        prescriptionList.add(prescription);
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (prescriptionList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
