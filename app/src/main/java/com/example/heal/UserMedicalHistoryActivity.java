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

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_medical_detail);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTitle = dialog.findViewById(R.id.tvDetailTitle);
        TextView tvSubtitle = dialog.findViewById(R.id.tvDetailSubtitle);
        TextView tvContent = dialog.findViewById(R.id.tvDetailContent);
        com.google.android.material.button.MaterialButton btnClose = dialog.findViewById(R.id.btnDetailClose);

        tvTitle.setText("Appointment Detail");
        tvSubtitle.setText(appt.getDate() + " at " + appt.getTime());

        StringBuilder detail = new StringBuilder();
        if (appt.getType() != null && appt.getType().equals("room")) {
            detail.append("Hospital: ").append(appt.getHospitalName()).append("\n\n");
            detail.append("Room: ").append(appt.getRoomNumber()).append("\n\n");
        } else {
            detail.append("Doctor: ").append(appt.getDoctorName()).append("\n\n");
        }
        detail.append("Status: ").append(appt.getStatus().toUpperCase()).append("\n\n");
        if (appt.getNotes() != null && !appt.getNotes().isEmpty()) {
            detail.append("Notes:\n").append(appt.getNotes());
        }

        tvContent.setText(detail.toString());
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPrescriptionDetail(Prescription pres) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_medical_detail);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tvTitle = dialog.findViewById(R.id.tvDetailTitle);
        TextView tvSubtitle = dialog.findViewById(R.id.tvDetailSubtitle);
        TextView tvContent = dialog.findViewById(R.id.tvDetailContent);
        com.google.android.material.button.MaterialButton btnClose = dialog.findViewById(R.id.btnDetailClose);

        tvTitle.setText("Prescription Detail");
        tvSubtitle.setText("Date: " + pres.getDate());

        StringBuilder detail = new StringBuilder();
        detail.append("Medicine: ").append(pres.getMedicineName()).append("\n\n");
        detail.append("Dosage: ").append(pres.getDosage()).append("\n\n");
        detail.append("Frequency: ").append(pres.getFrequency()).append("\n\n");
        detail.append("Doctor: ").append(pres.getDoctorName()).append("\n\n");
        if (pres.getInstructions() != null && !pres.getInstructions().isEmpty()) {
            detail.append("Instructions:\n").append(pres.getInstructions());
        }

        tvContent.setText(detail.toString());
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
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
        int primaryColor = android.graphics.Color.parseColor("#0E6858");
        int white = android.graphics.Color.WHITE;

        if (isAppointments) {
            btnAppointments.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnAppointments.setTextColor(white);

            btnPrescriptions.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
            btnPrescriptions.setTextColor(primaryColor);
        } else {
            btnAppointments.setBackgroundTintList(android.content.res.ColorStateList.valueOf(white));
            btnAppointments.setTextColor(primaryColor);

            btnPrescriptions.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));
            btnPrescriptions.setTextColor(white);
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
