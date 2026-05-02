package com.example.heal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DoctorHomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);

        bottomNavigationView = findViewById(R.id.doctorBottomNavigation);
        
        // Initial fragment
        loadFragment(new DoctorAppointmentsFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_appointments) {
                fragment = new DoctorAppointmentsFragment();
            } else if (itemId == R.id.nav_chat) {
                fragment = new DoctorChatFragment();
            } else if (itemId == R.id.nav_settings) {
                fragment = new SettingsFragment();
            }
            
            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.doctor_fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
