package com.example.heal;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class UserLabBookingsActivity extends AppCompatActivity {

    private RecyclerView rvLabBookings;
    private UserLabBookingsAdapter adapter;
    private List<TestBooking> bookingList;
    private ProgressBar progressBar;
    private LinearLayout llEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_lab_bookings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvLabBookings = findViewById(R.id.rvLabBookings);
        progressBar = findViewById(R.id.progressBar);
        llEmpty = findViewById(R.id.llEmpty);

        bookingList = new ArrayList<>();
        adapter = new UserLabBookingsAdapter(this, bookingList);
        rvLabBookings.setLayoutManager(new LinearLayoutManager(this));
        rvLabBookings.setAdapter(adapter);

        fetchBookings();
    }

    private void fetchBookings() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            llEmpty.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("test_bookings");

        ref.orderByChild("user_id").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookingList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            TestBooking booking = ds.getValue(TestBooking.class);
                            if (booking != null) {
                                bookingList.add(booking);
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        if (bookingList.isEmpty()) {
                            llEmpty.setVisibility(View.VISIBLE);
                            rvLabBookings.setVisibility(View.GONE);
                        } else {
                            llEmpty.setVisibility(View.GONE);
                            rvLabBookings.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        llEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }
}
