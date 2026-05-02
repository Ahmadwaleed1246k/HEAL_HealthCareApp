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

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvUserName;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private RecyclerView rvTopSpecialists;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> topDoctors;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
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

        return view;
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.btnEmergency).setOnClickListener(v -> showToast("Emergency Service"));
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
        view.findViewById(R.id.btnHospital).setOnClickListener(v -> showToast("Hospital Service"));
        view.findViewById(R.id.btnLabTests).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), LabTestsActivity.class));
        });
        
        view.findViewById(R.id.tvViewAllSpecialists).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SpecialistsActivity.class));
        });
        
        view.findViewById(R.id.profileCard).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
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
