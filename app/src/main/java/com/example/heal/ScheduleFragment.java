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
        view.findViewById(R.id.statsCard).setVisibility(View.GONE);

        TextView tvName = view.findViewById(R.id.tvDoctorName);
        SessionManager sessionManager = new SessionManager(getActivity());
        String cachedName = sessionManager.getName();
        if (!cachedName.isEmpty()) {
            tvName.setText(cachedName);
        }

        rvSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentList = new ArrayList<>();
        
        // Patients can't accept/reject/reschedule from here in this version
        adapter = new AppointmentAdapter(getContext(), appointmentList, false, new AppointmentAdapter.OnAppointmentActionListener() {
            @Override public void onAccept(Appointment appointment) {}
            @Override public void onReject(Appointment appointment) {}
            @Override public void onReschedule(Appointment appointment) {}
            @Override public void onCancel(Appointment appointment) {
                cancelItem(appointment);
            }
            @Override public void onPrescribe(Appointment appointment) {}
        });
        rvSchedule.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("appointments");
        fetchSchedule();

        return view;
    }

    private void cancelItem(Appointment item) {
        String node = item.getType().equals("room") ? "room_bookings" : "appointments";
        FirebaseDatabase.getInstance().getReference(node).child(item.getAppointmentId()).removeValue()
            .addOnSuccessListener(aVoid -> android.widget.Toast.makeText(getContext(), item.getType().equals("room") ? "Booking cancelled" : "Appointment cancelled", android.widget.Toast.LENGTH_SHORT).show());
    }

    private void fetchSchedule() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        
        // Fetch Doctor Appointments
        FirebaseDatabase.getInstance().getReference("appointments")
            .orderByChild("patientId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear and re-populate unified list
                appointmentList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Appointment appointment = dataSnapshot.getValue(Appointment.class);
                    if (appointment != null) {
                        appointment.setType("doctor");
                        appointmentList.add(appointment);
                    }
                }
                
                // Fetch Room Bookings
                fetchRoomBookings(userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchRoomBookings(String userId) {
        FirebaseDatabase.getInstance().getReference("room_bookings")
            .orderByChild("patientId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    RoomBooking rb = dataSnapshot.getValue(RoomBooking.class);
                    if (rb != null) {
                        Appointment app = new Appointment();
                        app.setAppointmentId(rb.getBookingId());
                        app.setHospitalName(rb.getHospitalName());
                        app.setDoctorName(rb.getHospitalName()); // Reuse doctorName field for UI
                        app.setRoomNumber(rb.getRoomNumber());
                        app.setDate(rb.getDate());
                        app.setTime("Room: " + rb.getRoomNumber() + " (" + rb.getRoomType() + ")");
                        app.setStatus(rb.getStatus());
                        app.setType("room");
                        appointmentList.add(app);
                    }
                }
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateUI() {
        progressBar.setVisibility(View.GONE);
        if (appointmentList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No appointments or bookings scheduled.");
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
        // Sort by date could be added here
        adapter.notifyDataSetChanged();
    }
}
