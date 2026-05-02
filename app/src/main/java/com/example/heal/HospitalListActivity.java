package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class HospitalListActivity extends AppCompatActivity {

    private RecyclerView rvHospitals;
    private HospitalAdapter adapter;
    private List<Hospital> hospitalList;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvHospitals = findViewById(R.id.rvHospitals);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvHospitals.setLayoutManager(new LinearLayoutManager(this));
        hospitalList = new ArrayList<>();
        adapter = new HospitalAdapter(this, hospitalList, new HospitalAdapter.OnHospitalClickListener() {
            @Override
            public void onHospitalClick(Hospital hospital) {
                // Future: Hospital detail page
            }

            @Override
            public void onBookRoomClick(Hospital hospital) {
                Intent intent = new Intent(HospitalListActivity.this, RoomBookingActivity.class);
                intent.putExtra("hospital", hospital);
                startActivity(intent);
            }
        });
        rvHospitals.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference("hospitals");
        fetchHospitals();
    }

    private void fetchHospitals() {
        progressBar.setVisibility(View.VISIBLE);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hospitalList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Hospital hospital = dataSnapshot.getValue(Hospital.class);
                    if (hospital != null) {
                        hospitalList.add(hospital);
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (hospitalList.isEmpty()) {
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
}
