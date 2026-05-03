package com.example.heal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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

public class DoctorAppointmentsFragment extends Fragment {

    private RecyclerView rvAppointments;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private DatabaseReference mDatabase, mDoctorDatabase;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvDoctorName, tvTotalAppointments, tvTotalRevenue;
    private android.widget.ImageView ivDoctorProfile;
    private androidx.cardview.widget.CardView profileCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_appointments, container, false);

        rvAppointments = view.findViewById(R.id.rvAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvDoctorName = view.findViewById(R.id.tvDoctorName);
        tvTotalAppointments = view.findViewById(R.id.tvTotalAppointments);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        ivDoctorProfile = view.findViewById(R.id.ivDoctorProfile);
        profileCard = view.findViewById(R.id.profileCard);

        SessionManager sessionManager = new SessionManager(getActivity());
        String cachedName = sessionManager.getName();
        if (!cachedName.isEmpty()) {
            tvDoctorName.setText(cachedName);
        }

        rvAppointments.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList = new ArrayList<>();
        
        adapter = new AppointmentAdapter(getContext(), appointmentList, true, false, new AppointmentAdapter.OnAppointmentActionListener() {
            @Override
            public void onAccept(Appointment appointment) {
                updateAppointmentStatus(appointment, "accepted");
            }

            @Override
            public void onReject(Appointment appointment) {
                updateAppointmentStatus(appointment, "cancelled_by_doctor");
            }

            @Override
            public void onReschedule(Appointment appointment) {
                showRescheduleDialog(appointment);
            }

            @Override
            public void onCancel(Appointment appointment) {
                // Doctors don't cancel from this button
            }

            @Override
            public void onPrescribe(Appointment appointment) {
                android.content.Intent intent = new android.content.Intent(getContext(), PrescriptionActivity.class);
                intent.putExtra("appointmentId", appointment.getAppointmentId());
                intent.putExtra("patientId", appointment.getPatientId());
                intent.putExtra("patientName", appointment.getPatientName());
                startActivity(intent);
            }

            @Override
            public void onItemClick(Appointment appointment) {
                showDetailDialog(appointment);
            }
        });
        rvAppointments.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("appointments");
        mDoctorDatabase = FirebaseDatabase.getInstance().getReference("doctors");
        fetchAppointments();
        fetchDoctorProfile();

        profileCard.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), DoctorProfileSetupActivity.class);
            intent.putExtra("isEditing", true);
            startActivity(intent);
        });

        return view;
    }

    private void fetchDoctorProfile() {
        String doctorId = FirebaseAuth.getInstance().getUid();
        if (doctorId == null) return;

        mDoctorDatabase.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profilePic = snapshot.child("profile_picture").getValue(String.class);
                    
                    if (name != null) {
                        tvDoctorName.setText(name);
                        new SessionManager(getActivity()).setName(name);
                    }
                    if (profilePic != null && !profilePic.isEmpty()) {
                        com.bumptech.glide.Glide.with(DoctorAppointmentsFragment.this)
                                .load(profilePic)
                                .placeholder(R.drawable.ic_person)
                                .into(ivDoctorProfile);
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchAppointments() {
        String doctorId = FirebaseAuth.getInstance().getUid();
        if (doctorId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        mDatabase.orderByChild("doctorId").equalTo(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                double totalRevenue = 0;
                int totalBookings = 0;
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Appointment appointment = dataSnapshot.getValue(Appointment.class);
                    if (appointment != null) {
                        appointmentList.add(appointment);
                        totalBookings++;
                        
                        String status = appointment.getStatus();
                        if ("accepted".equalsIgnoreCase(status) || "prescribed".equalsIgnoreCase(status)) {
                            totalRevenue += appointment.getConsultationFee();
                        }
                    }
                }
                
                tvTotalAppointments.setText(String.valueOf(totalBookings));
                tvTotalRevenue.setText("$" + (int)totalRevenue);
                
                progressBar.setVisibility(View.GONE);
                if (appointmentList.isEmpty()) {
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

    private void deleteAppointment(Appointment appointment) {
        mDatabase.child(appointment.getAppointmentId()).removeValue()
            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Appointment removed", Toast.LENGTH_SHORT).show());
    }

    private void updateAppointmentStatus(Appointment appointment, String status) {
        mDatabase.child(appointment.getAppointmentId()).child("status").setValue(status)
            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Appointment " + status, Toast.LENGTH_SHORT).show());
    }

    private void showRescheduleDialog(Appointment appointment) {
        com.google.android.material.timepicker.MaterialTimePicker picker =
                new com.google.android.material.timepicker.MaterialTimePicker.Builder()
                        .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
                        .setTitleText("Reschedule Time")
                        .build();

        picker.addOnPositiveButtonClickListener(v -> {
            int hour = picker.getHour();
            int minute = picker.getMinute();
            String amPm = hour >= 12 ? "PM" : "AM";
            int hour12 = hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour);
            String newTime = String.format("%02d:%02d %s", hour12, minute, amPm);
            
            java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
            updates.put("time", newTime);
            updates.put("status", "rescheduled");
            
            mDatabase.child(appointment.getAppointmentId()).updateChildren(updates);
        });

        picker.show(getChildFragmentManager(), "RESCHEDULE_PICKER");
    }

    private void showDetailDialog(Appointment appt) {
        StringBuilder detail = new StringBuilder();
        detail.append("Date: ").append(appt.getDate()).append("\n");
        detail.append("Time: ").append(appt.getTime()).append("\n");
        detail.append("Patient: ").append(appt.getPatientName()).append("\n");
        detail.append("Status: ").append(appt.getStatus().toUpperCase()).append("\n");
        if (appt.getType() != null && appt.getType().equals("room")) {
            detail.append("Hospital: ").append(appt.getHospitalName()).append("\n");
            detail.append("Room: ").append(appt.getRoomNumber()).append("\n");
        } else {
            detail.append("Consultation Fee: $").append(appt.getConsultationFee()).append("\n");
        }
        if (appt.getNotes() != null && !appt.getNotes().isEmpty()) {
            detail.append("\nNotes:\n").append(appt.getNotes());
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(getContext())
                .setTitle("Appointment Details")
                .setMessage(detail.toString())
                .setPositiveButton("Close", null)
                .show();
    }
}
