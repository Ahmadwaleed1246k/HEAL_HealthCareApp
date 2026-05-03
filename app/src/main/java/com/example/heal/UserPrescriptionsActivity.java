package com.example.heal;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

public class UserPrescriptionsActivity extends AppCompatActivity implements PrescriptionAdapter.OnPrescriptionActionListener {

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

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvPrescriptions = findViewById(R.id.rvUserPrescriptions);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvPrescriptions.setLayoutManager(new LinearLayoutManager(this));
        prescriptionList = new ArrayList<>();
        adapter = new PrescriptionAdapter(this, prescriptionList, false, this);
        rvPrescriptions.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("prescriptions");
        fetchPrescriptions();
    }

    private void fetchPrescriptions() {
        String patientId = FirebaseAuth.getInstance().getUid();
        if (patientId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        // Only fetch active prescriptions (items that were moved to history are already deleted from this node)
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

    @Override
    public void onDismiss(Prescription prescription) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("medical_history")
                .child(userId).child("prescriptions").child(prescription.getPrescriptionId());

        // 1. Move to history
        historyRef.setValue(prescription).addOnSuccessListener(aVoid -> {
            // 2. Remove from active node
            mDatabase.child(prescription.getPrescriptionId()).removeValue()
                    .addOnSuccessListener(aVoid2 -> {
                        android.widget.Toast.makeText(UserPrescriptionsActivity.this, "Prescription archived to History", android.widget.Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            android.widget.Toast.makeText(UserPrescriptionsActivity.this, "Failed to archive: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onItemClick(Prescription prescription) {
        showDetailDialog(prescription);
    }

    private void showDetailDialog(Prescription pres) {
        StringBuilder detail = new StringBuilder();
        detail.append("Medicine: ").append(pres.getMedicineName()).append("\n");
        detail.append("Dosage: ").append(pres.getDosage()).append("\n");
        detail.append("Frequency: ").append(pres.getFrequency()).append("\n");
        detail.append("Doctor: ").append(pres.getDoctorName()).append("\n");
        detail.append("Date: ").append(pres.getDate()).append("\n");
        if (pres.getInstructions() != null && !pres.getInstructions().isEmpty()) {
            detail.append("\nInstructions:\n").append(pres.getInstructions());
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Prescription Details")
                .setMessage(detail.toString())
                .setPositiveButton("Close", null)
                .show();
    }
}
