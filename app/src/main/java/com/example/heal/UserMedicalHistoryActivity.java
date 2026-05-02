package com.example.heal;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserMedicalHistoryActivity extends AppCompatActivity {

    private MaterialButton btnAppointments, btnPrescriptions;
    private RecyclerView rvHistory;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private AppointmentAdapter appointmentAdapter;
    private PrescriptionAdapter prescriptionAdapter;
    private List<Appointment> appointmentList;
    private List<Prescription> prescriptionList;

    private boolean isShowingAppointments = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnAppointments = findViewById(R.id.btnTabAppointments);
        btnPrescriptions = findViewById(R.id.btnTabPrescriptions);
        rvHistory = findViewById(R.id.rvHistory);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        appointmentList = new ArrayList<>();
        prescriptionList = new ArrayList<>();

        // We don't need dismiss callbacks in the history view, or maybe we just pass null.
        // Wait, for Appointments, the third parameter might be needed. Let's see AppointmentAdapter constructor.
        // AppointmentAdapter has constructor: AppointmentAdapter(Context, List<Appointment>) or AppointmentAdapter(Context, List<Appointment>, boolean) or listener?
        // Let's use the simplest one, maybe we need to adjust the adapter initialization if the signature changed.
        
        btnAppointments.setOnClickListener(v -> showAppointments());
        btnPrescriptions.setOnClickListener(v -> showPrescriptions());

        // We will initialize adapters after verifying their constructors
        initAdapters();

        // Start with appointments
        updateTabStyles(true);
        showAppointments();
    }

    private void initAdapters() {
        appointmentAdapter = new AppointmentAdapter(this, appointmentList, false, true, new AppointmentAdapter.OnAppointmentActionListener() {
            @Override public void onAccept(Appointment appointment) {}
            @Override public void onReject(Appointment appointment) {}
            @Override public void onReschedule(Appointment appointment) {}
            @Override public void onCancel(Appointment appointment) {}
            @Override public void onPrescribe(Appointment appointment) {}
            
            @Override
            public void onItemClick(Appointment appointment) {
                showAppointmentDetail(appointment);
            }
        });

        prescriptionAdapter = new PrescriptionAdapter(this, prescriptionList, true, new PrescriptionAdapter.OnPrescriptionActionListener() {
            @Override
            public void onDismiss(Prescription prescription) {}

            @Override
            public void onItemClick(Prescription prescription) {
                showPrescriptionDetail(prescription);
            }
        });
    }

    private void showAppointmentDetail(Appointment appt) {
        StringBuilder detail = new StringBuilder();
        detail.append("Date: ").append(appt.getDate()).append("\n");
        detail.append("Time: ").append(appt.getTime()).append("\n");
        if (appt.getType() != null && appt.getType().equals("room")) {
            detail.append("Hospital: ").append(appt.getHospitalName()).append("\n");
            detail.append("Room: ").append(appt.getRoomNumber()).append("\n");
        } else {
            detail.append("Doctor: ").append(appt.getDoctorName()).append("\n");
        }
        detail.append("Status: ").append(appt.getStatus().toUpperCase()).append("\n");
        if (appt.getNotes() != null && !appt.getNotes().isEmpty()) {
            detail.append("\nNotes:\n").append(appt.getNotes());
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Appointment Detail")
                .setMessage(detail.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    private void showPrescriptionDetail(Prescription pres) {
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
                .setTitle("Prescription Detail")
                .setMessage(detail.toString())
                .setPositiveButton("Close", null)
                .show();
    }

    private void showAppointments() {
        isShowingAppointments = true;
        updateTabStyles(true);
        rvHistory.setAdapter(appointmentAdapter);
        fetchAppointmentsHistory();
    }

    private void showPrescriptions() {
        isShowingAppointments = false;
        updateTabStyles(false);
        rvHistory.setAdapter(prescriptionAdapter);
        fetchPrescriptionsHistory();
    }

    private void updateTabStyles(boolean isAppointments) {
        String primaryColor = "#0E6858";
        if (isAppointments) {
            // Appointments selected: Filled style
            btnAppointments.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(primaryColor)));
            btnAppointments.setTextColor(android.graphics.Color.WHITE);
            btnAppointments.setStrokeWidth(0);
            btnAppointments.setElevation(dpToPx(4));

            // Prescriptions unselected: Outlined style
            btnPrescriptions.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
            btnPrescriptions.setTextColor(android.graphics.Color.parseColor(primaryColor));
            btnPrescriptions.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(primaryColor)));
            btnPrescriptions.setStrokeWidth(dpToPx(1));
            btnPrescriptions.setElevation(0);
        } else {
            // Appointments unselected: Outlined style
            btnAppointments.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
            btnAppointments.setTextColor(android.graphics.Color.parseColor(primaryColor));
            btnAppointments.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(primaryColor)));
            btnAppointments.setStrokeWidth(dpToPx(1));
            btnAppointments.setElevation(0);

            // Prescriptions selected: Filled style
            btnPrescriptions.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(primaryColor)));
            btnPrescriptions.setTextColor(android.graphics.Color.WHITE);
            btnPrescriptions.setStrokeWidth(0);
            btnPrescriptions.setElevation(dpToPx(4));
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void fetchAppointmentsHistory() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        appointmentList.clear();

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("medical_history")
                .child(userId).child("appointments");

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Appointment appt = data.getValue(Appointment.class);
                    if (appt != null) {
                        appointmentList.add(appt);
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (appointmentList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No appointment history found");
                }
                appointmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void fetchPrescriptionsHistory() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        prescriptionList.clear();

        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("medical_history")
                .child(userId).child("prescriptions");

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    Prescription pres = data.getValue(Prescription.class);
                    if (pres != null) {
                        prescriptionList.add(pres);
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (prescriptionList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No prescription history found");
                }
                prescriptionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
