package com.example.heal;

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

public class ScheduleFragment extends Fragment {

    private RecyclerView rvSchedule;
    private AppointmentAdapter adapter;
    private List<Appointment> appointmentList;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_appointments, container, false);

        // Reusing the same layout as doctor appointments fragment
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("My Schedule");

        rvSchedule = view.findViewById(R.id.rvAppointments);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList = new ArrayList<>();
        
        // Patients can't accept/reject/reschedule from here in this version
        adapter = new AppointmentAdapter(getContext(), appointmentList, false, new AppointmentAdapter.OnAppointmentActionListener() {
            @Override public void onAccept(Appointment appointment) {}
            @Override public void onReject(Appointment appointment) {}
            @Override public void onReschedule(Appointment appointment) {}
            @Override public void onCancel(Appointment appointment) {
                cancelAppointment(appointment);
            }
            @Override public void onPrescribe(Appointment appointment) {}
        });
        rvSchedule.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("appointments");
        fetchSchedule();

        return view;
    }

    private void cancelAppointment(Appointment appointment) {
        mDatabase.child(appointment.getAppointmentId()).removeValue()
            .addOnSuccessListener(aVoid -> android.widget.Toast.makeText(getContext(), "Appointment cancelled and removed", android.widget.Toast.LENGTH_SHORT).show());
    }

    private void fetchSchedule() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        mDatabase.orderByChild("patientId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Appointment appointment = dataSnapshot.getValue(Appointment.class);
                    if (appointment != null) {
                        appointmentList.add(appointment);
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (appointmentList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No appointments scheduled.");
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
