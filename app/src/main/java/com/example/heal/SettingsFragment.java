package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    // Existing views
    private TextView tvName, tvEmail, tvPhone, tvDob, tvMemberSince, tvHealthRecordsTitle;
    private androidx.cardview.widget.CardView cvHealthRecords;
    private Button btnLogout;
    private LinearLayout llLabResults;

    // Doctor profile views
    private TextView tvDoctorProfileTitle;
    private androidx.cardview.widget.CardView cvDoctorProfile;
    private TextView tvDoctorSpecialization, tvDoctorHospital, tvDoctorHospitalLocation;
    private TextView tvDoctorExperience, tvDoctorRating, tvDoctorReviews;
    private TextView tvDoctorFee, tvDoctorAvailability, tvDoctorLanguages;
    private TextView tvSubSpecialtiesLabel, tvDoctorSubSpecialties;
    private View divSubSpecialties;
    private TextView tvDoctorQualifications, tvDoctorTimeSlots, tvDoctorAbout;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Existing bindings
        tvName = view.findViewById(R.id.tvSettingsName);
        tvEmail = view.findViewById(R.id.tvSettingsEmail);
        tvPhone = view.findViewById(R.id.tvSettingsPhone);
        tvDob = view.findViewById(R.id.tvSettingsDob);
        tvMemberSince = view.findViewById(R.id.tvSettingsMemberSince);
        tvHealthRecordsTitle = view.findViewById(R.id.tvHealthRecordsTitle);
        cvHealthRecords = view.findViewById(R.id.cvHealthRecords);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Doctor profile bindings
        tvDoctorProfileTitle = view.findViewById(R.id.tvDoctorProfileTitle);
        cvDoctorProfile = view.findViewById(R.id.cvDoctorProfile);
        tvDoctorSpecialization = view.findViewById(R.id.tvDoctorSpecialization);
        tvDoctorHospital = view.findViewById(R.id.tvDoctorHospital);
        tvDoctorHospitalLocation = view.findViewById(R.id.tvDoctorHospitalLocation);
        tvDoctorExperience = view.findViewById(R.id.tvDoctorExperience);
        tvDoctorRating = view.findViewById(R.id.tvDoctorRating);
        tvDoctorReviews = view.findViewById(R.id.tvDoctorReviews);
        tvDoctorFee = view.findViewById(R.id.tvDoctorFee);
        tvDoctorAvailability = view.findViewById(R.id.tvDoctorAvailability);
        tvDoctorLanguages = view.findViewById(R.id.tvDoctorLanguages);
        tvSubSpecialtiesLabel = view.findViewById(R.id.tvSubSpecialtiesLabel);
        tvDoctorSubSpecialties = view.findViewById(R.id.tvDoctorSubSpecialties);
        divSubSpecialties = view.findViewById(R.id.divSubSpecialties);
        tvDoctorQualifications = view.findViewById(R.id.tvDoctorQualifications);
        tvDoctorTimeSlots = view.findViewById(R.id.tvDoctorTimeSlots);
        tvDoctorAbout = view.findViewById(R.id.tvDoctorAbout);

        llLabResults = view.findViewById(R.id.llLabResults);
        llLabResults.setOnClickListener(v -> startActivity(
                new Intent(getActivity(), UserLabBookingsActivity.class)));

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(getActivity());

        // Immediate hide if doctor (from session)
        if ("Doctor".equalsIgnoreCase(sessionManager.getRole())) {
            tvHealthRecordsTitle.setVisibility(View.GONE);
            cvHealthRecords.setVisibility(View.GONE);
        }

        fetchUserProfile();

        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void fetchUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        tvEmail.setText(user.getEmail());

        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String name = snapshot.child("name").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String dob = snapshot.child("dob").getValue(String.class);
                String memberSince = snapshot.child("memberSince").getValue(String.class);

                if (name != null) tvName.setText(name);
                if (phone != null) tvPhone.setText(phone);
                if (dob != null) tvDob.setText(dob);
                if (memberSince != null) tvMemberSince.setText("MEMBER SINCE " + memberSince.toUpperCase());

                String role = snapshot.child("role").getValue(String.class);
                if ("Doctor".equalsIgnoreCase(role)) {
                    tvHealthRecordsTitle.setVisibility(View.GONE);
                    cvHealthRecords.setVisibility(View.GONE);
                    fetchDoctorProfile(user.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fetchDoctorProfile(String uid) {
        mDatabase.child("doctors").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                // Show doctor section
                tvDoctorProfileTitle.setVisibility(View.VISIBLE);
                cvDoctorProfile.setVisibility(View.VISIBLE);

                // Specialization
                String specialization = snapshot.child("specialization").getValue(String.class);
                if (specialization != null) tvDoctorSpecialization.setText(specialization);

                // Hospital
                String hospitalName = snapshot.child("hospital_name").getValue(String.class);
                String hospitalLocation = snapshot.child("hospital_location").getValue(String.class);
                if (hospitalName != null) tvDoctorHospital.setText(hospitalName);
                if (hospitalLocation != null) tvDoctorHospitalLocation.setText(hospitalLocation);

                // Experience
                Object expObj = snapshot.child("experience_years").getValue();
                if (expObj != null) {
                    tvDoctorExperience.setText(expObj + " yrs");
                }

                // Rating
                Object ratingObj = snapshot.child("rating").getValue();
                if (ratingObj != null) {
                    tvDoctorRating.setText("⭐ " + ratingObj);
                }

                // Reviews
                Object reviewsObj = snapshot.child("total_reviews").getValue();
                if (reviewsObj != null) {
                    tvDoctorReviews.setText(reviewsObj + " reviews");
                }

                // Consultation Fee
                Object feeObj = snapshot.child("consultation_fee").getValue();
                if (feeObj != null) {
                    tvDoctorFee.setText(feeObj.toString());
                }

                // Availability Status
                String availability = snapshot.child("availability_status").getValue(String.class);
                if (availability != null) {
                    tvDoctorAvailability.setText(availability.substring(0, 1).toUpperCase() + availability.substring(1));
                    tvDoctorAvailability.setTextColor("available".equalsIgnoreCase(availability)
                            ? 0xFF2E7D32 : 0xFFD32F2F);
                }

                // Languages
                List<String> languages = new ArrayList<>();
                for (DataSnapshot langSnap : snapshot.child("languages").getChildren()) {
                    String lang = langSnap.getValue(String.class);
                    if (lang != null) languages.add(lang);
                }
                if (!languages.isEmpty()) tvDoctorLanguages.setText(String.join(", ", languages));

                // Sub-specialties
                List<String> subSpecialties = new ArrayList<>();
                for (DataSnapshot subSnap : snapshot.child("sub_specialties").getChildren()) {
                    String sub = subSnap.getValue(String.class);
                    if (sub != null) subSpecialties.add(sub);
                }
                if (!subSpecialties.isEmpty()) {
                    tvSubSpecialtiesLabel.setVisibility(View.VISIBLE);
                    tvDoctorSubSpecialties.setVisibility(View.VISIBLE);
                    divSubSpecialties.setVisibility(View.VISIBLE);
                    tvDoctorSubSpecialties.setText(String.join("  •  ", subSpecialties));
                }

                // Qualifications
                StringBuilder qualBuilder = new StringBuilder();
                for (DataSnapshot qualSnap : snapshot.child("qualifications").getChildren()) {
                    String degree = qualSnap.child("degree").getValue(String.class);
                    String institution = qualSnap.child("institution").getValue(String.class);
                    Object year = qualSnap.child("year").getValue();
                    if (degree != null) {
                        if (qualBuilder.length() > 0) qualBuilder.append("\n");
                        qualBuilder.append(degree);
                        if (institution != null) qualBuilder.append(" — ").append(institution);
                        if (year != null) qualBuilder.append(" (").append(year).append(")");
                    }
                }
                if (qualBuilder.length() > 0) tvDoctorQualifications.setText(qualBuilder.toString());

                // Available Time Slots
                List<String> slots = new ArrayList<>();
                for (DataSnapshot slotSnap : snapshot.child("timings").child("available_slots").getChildren()) {
                    String slot = slotSnap.getValue(String.class);
                    if (slot != null) slots.add(slot);
                }
                if (!slots.isEmpty()) tvDoctorTimeSlots.setText(String.join("  •  ", slots));

                // About
                String about = snapshot.child("about").getValue(String.class);
                if (about != null && !about.isEmpty()) tvDoctorAbout.setText(about);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void logout() {
        mAuth.signOut();
        sessionManager.logoutUser();
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
