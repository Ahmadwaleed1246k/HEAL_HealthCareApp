package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class DoctorPrescriptionsFragment extends Fragment {

    private RecyclerView rvPrescriptions;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_appointments, container, false);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Prescriptions");
        view.findViewById(R.id.headerLayout).setVisibility(View.GONE);

        rvPrescriptions = view.findViewById(R.id.rvAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvPrescriptions.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList = new ArrayList<>();
        
        adapter = new AppointmentAdapter(getContext(), appointmentList, true, new AppointmentAdapter.OnAppointmentActionListener() {
            @Override public void onAccept(Appointment appointment) {}
            @Override public void onReject(Appointment appointment) {}
            @Override public void onReschedule(Appointment appointment) {}
            @Override public void onCancel(Appointment appointment) {}
            @Override public void onPrescribe(Appointment appointment) {
                Intent intent = new Intent(getContext(), PrescriptionActivity.class);
                intent.putExtra("appointmentId", appointment.getAppointmentId());
                intent.putExtra("patientId", appointment.getPatientId());
                intent.putExtra("patientName", appointment.getPatientName());
                startActivity(intent);
            }
        });
        rvPrescriptions.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("appointments");
        fetchAcceptedAppointments();

        return view;
    }

    private void fetchAcceptedAppointments() {
        String doctorId = FirebaseAuth.getInstance().getUid();
        if (doctorId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        mDatabase.orderByChild("doctorId").equalTo(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Appointment appointment = dataSnapshot.getValue(Appointment.class);
                    if (appointment != null && "accepted".equalsIgnoreCase(appointment.getStatus())) {
                        appointmentList.add(appointment);
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (appointmentList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No accepted appointments for prescription.");
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
