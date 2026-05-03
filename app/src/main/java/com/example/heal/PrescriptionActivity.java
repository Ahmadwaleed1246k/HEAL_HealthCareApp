package com.example.heal;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrescriptionActivity extends AppCompatActivity {

    private TextInputEditText etMedicineName, etDosage, etFrequency, etInstructions;
    private MaterialButton btnSubmit;
    private TextView tvPatientInfo;
    private DatabaseReference mDatabase;
    private String appointmentId, patientId, patientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_add);

        // Standardized Top Bar Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        appointmentId = getIntent().getStringExtra("appointmentId");
        patientId = getIntent().getStringExtra("patientId");
        patientName = getIntent().getStringExtra("patientName");

        etMedicineName = findViewById(R.id.etMedicineName);
        etDosage = findViewById(R.id.etDosage);
        etFrequency = findViewById(R.id.etFrequency);
        etInstructions = findViewById(R.id.etInstructions);
        btnSubmit = findViewById(R.id.btnSubmitPrescription);
        tvPatientInfo = findViewById(R.id.tvPatientInfo);

        tvPatientInfo.setText("Patient: " + patientName);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnSubmit.setOnClickListener(v -> submitPrescription());
    }

    private void submitPrescription() {
        String medicine = etMedicineName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String frequency = etFrequency.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();

        if (medicine.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String doctorId = FirebaseAuth.getInstance().getUid();
        String prescriptionId = mDatabase.child("prescriptions").push().getKey();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Fetching doctor name (we could pass it via intent, but for now we use a placeholder or fetch)
        // For simplicity, we'll assume we have it or use a placeholder.
        // Better: Fetch it from Session or Intent.
        
        Prescription prescription = new Prescription(
                prescriptionId, appointmentId, doctorId, "Dr. " + FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                patientId, patientName, medicine, dosage, frequency, instructions, date
        );

        mDatabase.child("prescriptions").child(prescriptionId).setValue(prescription)
                .addOnSuccessListener(aVoid -> {
                    // Update appointment status to completed/prescribed
                    mDatabase.child("appointments").child(appointmentId).child("status").setValue("prescribed");
                    Toast.makeText(this, "Prescription submitted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
