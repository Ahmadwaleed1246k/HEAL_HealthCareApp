package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class SettingsFragment extends Fragment {

    private TextView tvName, tvEmail, tvPhone, tvDob, tvMemberSince;
    private Button btnLogout;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        tvName = view.findViewById(R.id.tvSettingsName);
        tvEmail = view.findViewById(R.id.tvSettingsEmail);
        tvPhone = view.findViewById(R.id.tvSettingsPhone);
        tvDob = view.findViewById(R.id.tvSettingsDob);
        tvMemberSince = view.findViewById(R.id.tvSettingsMemberSince);
        btnLogout = view.findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(getActivity());

        fetchUserProfile();

        btnLogout.setOnClickListener(v -> logout());

        return view;
    }

    private void fetchUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phone = snapshot.child("phone").getValue(String.class);
                        String dob = snapshot.child("dob").getValue(String.class);
                        String memberSince = snapshot.child("memberSince").getValue(String.class);

                        if (name != null) tvName.setText(name);
                        if (phone != null) tvPhone.setText(phone);
                        if (dob != null) tvDob.setText(dob);
                        if (memberSince != null) tvMemberSince.setText("MEMBER SINCE " + memberSince.toUpperCase());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
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
