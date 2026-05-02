package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class LabTestsActivity extends AppCompatActivity {

    // ── Browse tab views ──────────────────────────────────────────────────────
    private RecyclerView rvTestCategories;
    private RecyclerView rvPopularPackages;
    private LabTestAdapter categoryAdapter;
    private LabTestPackageAdapter packageAdapter;
    private List<LabTest> allTestsList;
    private List<LabTest> popularPackagesList;
    private LinearLayout llBrowseContent;

    // ── My Results tab views ──────────────────────────────────────────────────
    private RecyclerView rvMyResults;
    private MyResultsAdapter myResultsAdapter;
    private List<TestBooking> myBookingsList;
    private LinearLayout llResultsContent;
    private LinearLayout llResultsEmpty;

    // ── Shared ────────────────────────────────────────────────────────────────
    private TextView tabMyResults;
    private TextView tabBookNewTest;
    private DatabaseReference mDatabase;

    private static final int REQ_AI_BOOKING = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab_tests);

        // Back & profile
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        androidx.cardview.widget.CardView profileCard = findViewById(R.id.profileCard);
        profileCard.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        // Tabs
        tabMyResults  = findViewById(R.id.tabMyResults);
        tabBookNewTest = findViewById(R.id.tabBookNewTest);

        // Browse content panel
        llBrowseContent = findViewById(R.id.llBrowseContent);
        rvTestCategories = findViewById(R.id.rvTestCategories);
        rvPopularPackages = findViewById(R.id.rvPopularPackages);

        // Results content panel
        llResultsContent = findViewById(R.id.llResultsContent);
        rvMyResults = findViewById(R.id.rvMyResults);
        llResultsEmpty = findViewById(R.id.llResultsEmpty);

        // Init lists & adapters
        allTestsList = new ArrayList<>();
        popularPackagesList = new ArrayList<>();

        categoryAdapter = new LabTestAdapter(this, allTestsList, this::openAiQuestionnaire);
        rvTestCategories.setLayoutManager(new LinearLayoutManager(this));
        rvTestCategories.setAdapter(categoryAdapter);

        packageAdapter = new LabTestPackageAdapter(this, popularPackagesList, this::openAiQuestionnaire);
        rvPopularPackages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPopularPackages.setAdapter(packageAdapter);

        myBookingsList = new ArrayList<>();
        myResultsAdapter = new MyResultsAdapter(this, myBookingsList, this::onResultCardClick);
        rvMyResults.setLayoutManager(new LinearLayoutManager(this));
        rvMyResults.setAdapter(myResultsAdapter);

        // Tab click listeners
        tabMyResults.setOnClickListener(v -> switchTab(true));
        tabBookNewTest.setOnClickListener(v -> switchTab(false));

        mDatabase = FirebaseDatabase.getInstance().getReference();
        fetchLabTests();

        // Start on Browse tab
        switchTab(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_AI_BOOKING && resultCode == RESULT_OK) {
            // Booking confirmed – switch to My Results
            switchTab(true);
        }
    }

    // ─────────────────────────────────────────────
    //  Tab switching
    // ─────────────────────────────────────────────

    private void switchTab(boolean showResults) {
        if (showResults) {
            tabMyResults.setBackgroundResource(R.drawable.bg_tab_active);
            tabMyResults.setTextColor(getResources().getColor(R.color.white));
            tabBookNewTest.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabBookNewTest.setTextColor(getResources().getColor(R.color.colorSecondary));

            llBrowseContent.setVisibility(View.GONE);
            llResultsContent.setVisibility(View.VISIBLE);
            fetchMyResults();
        } else {
            tabBookNewTest.setBackgroundResource(R.drawable.bg_tab_active);
            tabBookNewTest.setTextColor(getResources().getColor(R.color.white));
            tabMyResults.setBackgroundResource(R.drawable.bg_tab_inactive);
            tabMyResults.setTextColor(getResources().getColor(R.color.colorSecondary));

            llBrowseContent.setVisibility(View.VISIBLE);
            llResultsContent.setVisibility(View.GONE);
        }
    }

    // ─────────────────────────────────────────────
    //  Firebase – lab tests (browse tab)
    // ─────────────────────────────────────────────

    private void fetchLabTests() {
        mDatabase.child("lab_tests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTestsList.clear();
                popularPackagesList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    LabTest test = ds.getValue(LabTest.class);
                    if (test != null) {
                        allTestsList.add(test);
                        if (test.isPopular()) popularPackagesList.add(test);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
                packageAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LabTestsActivity.this, "Failed to load lab tests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─────────────────────────────────────────────
    //  Firebase – my results tab
    // ─────────────────────────────────────────────

    private void fetchMyResults() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            llResultsEmpty.setVisibility(View.VISIBLE);
            rvMyResults.setVisibility(View.GONE);
            return;
        }

        mDatabase.child("test_bookings").orderByChild("user_id").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myBookingsList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            TestBooking b = ds.getValue(TestBooking.class);
                            if (b != null) myBookingsList.add(b);
                        }
                        myResultsAdapter.notifyDataSetChanged();
                        if (myBookingsList.isEmpty()) {
                            llResultsEmpty.setVisibility(View.VISIBLE);
                            rvMyResults.setVisibility(View.GONE);
                        } else {
                            llResultsEmpty.setVisibility(View.GONE);
                            rvMyResults.setVisibility(View.VISIBLE);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        llResultsEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ─────────────────────────────────────────────
    //  Navigation helpers
    // ─────────────────────────────────────────────

    /** Launch AI questionnaire for a selected test */
    private void openAiQuestionnaire(LabTest test) {
        Intent intent = new Intent(this, AiLabQuestionsActivity.class);
        intent.putExtra(AiLabQuestionsActivity.EXTRA_TEST_ID, test.getTest_id());
        intent.putExtra(AiLabQuestionsActivity.EXTRA_TEST_NAME, test.getName());
        intent.putExtra(AiLabQuestionsActivity.EXTRA_TEST_CATEGORY, test.getCategory());
        intent.putExtra(AiLabQuestionsActivity.EXTRA_TEST_PRICE, test.getPrice());
        intent.putExtra(AiLabQuestionsActivity.EXTRA_PREP_INSTRUCTIONS, test.getPreparation_instructions());
        // Flatten markers list to a comma-separated string
        if (test.getMarkers() != null) {
            intent.putExtra(AiLabQuestionsActivity.EXTRA_TEST_MARKERS,
                    android.text.TextUtils.join(", ", test.getMarkers()));
        }
        startActivityForResult(intent, REQ_AI_BOOKING);
    }

    /** Handle click on a result card – show payment sheet or result screen */
    private void onResultCardClick(TestBooking booking) {
        if ("paid".equalsIgnoreCase(booking.getPayment_status())) {
            // Already paid → open result detail
            openResultDetail(booking);
        } else {
            // Need to pay first
            PaymentBottomSheet sheet = new PaymentBottomSheet(booking, paidBookingId -> {
                // After payment: re-fetch & open
                mDatabase.child("test_bookings").child(paidBookingId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                TestBooking updated = snapshot.getValue(TestBooking.class);
                                if (updated != null) openResultDetail(updated);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            });
            sheet.show(getSupportFragmentManager(), "PaymentSheet");
        }
    }

    private void openResultDetail(TestBooking booking) {
        Intent intent = new Intent(this, ResultDetailActivity.class);
        intent.putExtra(ResultDetailActivity.EXTRA_BOOKING_ID, booking.getBooking_id());
        intent.putExtra(ResultDetailActivity.EXTRA_TEST_NAME, booking.getTest_name());
        intent.putExtra(ResultDetailActivity.EXTRA_BOOKING_DATE, booking.getBooking_date());
        intent.putExtra(ResultDetailActivity.EXTRA_AI_RESULT, booking.getAi_result());
        startActivity(intent);
    }
}
