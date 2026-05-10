package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvUserName;
    private View notificationBadge;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView rvTopSpecialists;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> topDoctors;
    private List<Notification> notificationList = new ArrayList<>();
    private NotificationAdapter notificationAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        notificationBadge = view.findViewById(R.id.notificationBadge);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        SessionManager sessionManager = new SessionManager(getActivity());
        String cachedName = sessionManager.getName();
        if (!cachedName.isEmpty()) {
            tvUserName.setText(cachedName);
        }

        setupTopSpecialists(view);
        setupClickListeners(view);
        fetchUserProfile();
        fetchTopSpecialists();
        listenForNotifications();
        checkUpcomingReminders();

        return view;
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.btnEmergency).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EmergencyActivity.class));
        });
        view.findViewById(R.id.btnBlood).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), BloodDonationActivity.class));
        });
        view.findViewById(R.id.btnDoctors).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SpecialistsActivity.class));
        });
        view.findViewById(R.id.btnPrescription).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), UserPrescriptionsActivity.class));
        });
        view.findViewById(R.id.btnCheckup).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HeartRateActivity.class));
        });
        View btnLocation = view.findViewById(R.id.btnLocation);
        if (btnLocation != null) {
            btnLocation.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), FindCareActivity.class));
            });
        }
        view.findViewById(R.id.btnHospital).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HospitalListActivity.class));
        });
        view.findViewById(R.id.btnLabTests).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LabTestsActivity.class));
        });
        
        view.findViewById(R.id.btnChat).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChatDepartmentsActivity.class));
        });
        
        view.findViewById(R.id.tvViewAllSpecialists).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SpecialistsActivity.class));
        });
        
        view.findViewById(R.id.profileCard).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });

        view.findViewById(R.id.notificationCard).setOnClickListener(v -> {
            showNotificationsDialog();
        });
    }

    private void setupTopSpecialists(View view) {
        rvTopSpecialists = view.findViewById(R.id.rvTopSpecialists);
        rvTopSpecialists.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        topDoctors = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(getActivity(), topDoctors, true, doctor -> {
            Intent intent = new Intent(getActivity(), AppointmentBookingActivity.class);
            intent.putExtra("doctor", doctor);
            startActivity(intent);
        });
        rvTopSpecialists.setAdapter(doctorAdapter);
    }

    private void fetchTopSpecialists() {
        mDatabase.child("doctors").limitToFirst(5).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                topDoctors.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Doctor doctor = postSnapshot.getValue(Doctor.class);
                    if (doctor != null) {
                        topDoctors.add(doctor);
                    }
                }
                doctorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void checkUpcomingReminders() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Check Doctor Appointments
        mDatabase.child("appointments").orderByChild("patientId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            android.util.Log.d("HomeFragment", "No appointments found for user " + userId);
                        }
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Appointment appointment = ds.getValue(Appointment.class);
                            if (appointment != null && !"rejected".equals(appointment.getStatus())) {
                                processReminder(appointment.getAppointmentId(), appointment.getDate(), "appointment", appointment.getDoctorName());
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("HomeFragment", "Appointment fetch cancelled: " + error.getMessage());
                    }
                });

        // Check Room Bookings
        mDatabase.child("room_bookings").orderByChild("patientId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            android.util.Log.d("HomeFragment", "No room bookings found for user " + userId);
                        }
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            RoomBooking booking = ds.getValue(RoomBooking.class);
                            if (booking != null && !"cancelled".equals(booking.getStatus())) {
                                processReminder(booking.getBookingId(), booking.getDate(), "room", booking.getHospitalName());
                            }
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        android.util.Log.e("HomeFragment", "Room booking fetch cancelled: " + error.getMessage());
                    }
                });
    }

    private void processReminder(String refId, String dateStr, String type, String name) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date targetDate = dateFormat.parse(dateStr);
            if (targetDate == null) return;

            // Normalize target date to start of day
            Calendar targetCal = Calendar.getInstance();
            targetCal.setTime(targetDate);
            targetCal.set(Calendar.HOUR_OF_DAY, 0);
            targetCal.set(Calendar.MINUTE, 0);
            targetCal.set(Calendar.SECOND, 0);
            targetCal.set(Calendar.MILLISECOND, 0);

            // Normalize today to start of day
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diff = targetCal.getTimeInMillis() - today.getTimeInMillis();
            long daysLeft = diff / (24 * 60 * 60 * 1000);

            android.util.Log.d("HomeFragment", "Checking " + type + ": " + name + " on " + dateStr + ". Days left: " + daysLeft);

            if (daysLeft >= 0 && daysLeft <= 10) { // Increased to 10 for easier verification
                int milestone = (int) daysLeft;
                String userId = mAuth.getUid();
                if (userId == null) return;
                
                String notificationId = refId + "_" + milestone;

                // Check if already dismissed
                mDatabase.child("dismissed_reminders").child(userId).child(notificationId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dismissedSnapshot) {
                                if (dismissedSnapshot.exists()) return;

                                mDatabase.child("notifications").child(userId).child(notificationId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (!snapshot.exists()) {
                                                    String title = type.equals("room") ? "Room Booking" : "Appointment";
                                                    String message;
                                                    if (milestone == 0) {
                                                        message = "You have a " + (type.equals("room") ? "room booking at " : "appointment with ") + name + " today!";
                                                    } else {
                                                        message = "You have a " + (type.equals("room") ? "room booking at " : "appointment with ") + name + " in " + milestone + (milestone == 1 ? " day." : " days.");
                                                    }

                                                    Notification notification = new Notification(
                                                            notificationId, userId, title, message, type, refId, milestone, System.currentTimeMillis()
                                                    );
                                                    mDatabase.child("notifications").child(userId).child(notificationId).setValue(notification);
                                                }
                                            }
                                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                                        });
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }
        } catch (ParseException e) {
            android.util.Log.e("HomeFragment", "Error parsing date: " + dateStr, e);
        }
    }

    private void listenForNotifications() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        mDatabase.child("notifications").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Notification n = ds.getValue(Notification.class);
                    if (n != null) {
                        notificationList.add(n);
                    }
                }
                updateNotificationBadge();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateNotificationBadge() {
        if (notificationList.isEmpty()) {
            notificationBadge.setVisibility(View.GONE);
        } else {
            notificationBadge.setVisibility(View.VISIBLE);
        }
    }

    private void showNotificationsDialog() {
        if (getActivity() == null) return;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notifications, null);
        builder.setView(dialogView);

        RecyclerView rvNotifications = dialogView.findViewById(R.id.rvNotifications);
        TextView tvNoNotifications = dialogView.findViewById(R.id.tvNoNotifications);
        
        rvNotifications.setLayoutManager(new LinearLayoutManager(getActivity()));
        
        if (notificationList.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
        }

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        notificationAdapter = new NotificationAdapter(getActivity(), notificationList, notification -> {
            String userId = mAuth.getUid();
            if (userId != null) {
                // Remove from notifications AND mark as dismissed milestone
                mDatabase.child("notifications").child(userId).child(notification.getId()).removeValue();
                mDatabase.child("dismissed_reminders").child(userId).child(notification.getId()).setValue(true);
                
                if (notificationList.size() == 1) {
                    tvNoNotifications.setVisibility(View.VISIBLE);
                    rvNotifications.setVisibility(View.GONE);
                }
            }
        });
        rvNotifications.setAdapter(notificationAdapter);

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null) {
                            tvUserName.setText(name);
                            new SessionManager(getActivity()).setName(name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }
}
