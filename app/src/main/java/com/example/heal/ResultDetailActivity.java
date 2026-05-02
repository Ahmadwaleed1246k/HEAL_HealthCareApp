package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResultDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BOOKING_ID   = "booking_id";
    public static final String EXTRA_TEST_NAME    = "test_name";
    public static final String EXTRA_BOOKING_DATE = "booking_date";
    public static final String EXTRA_AI_RESULT    = "ai_result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_detail);

        String testName    = getIntent().getStringExtra(EXTRA_TEST_NAME);
        String bookingDate = getIntent().getStringExtra(EXTRA_BOOKING_DATE);
        String aiResult    = getIntent().getStringExtra(EXTRA_AI_RESULT);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTestName  = findViewById(R.id.tvResultTestName);
        TextView tvDate      = findViewById(R.id.tvResultDate);
        TextView tvAiResult  = findViewById(R.id.tvAiResult);
        TextView btnShare    = findViewById(R.id.btnShareResult);

        tvTestName.setText(testName != null ? testName : "Lab Test");
        tvDate.setText(bookingDate != null ? bookingDate : "—");
        tvAiResult.setText(aiResult != null ? aiResult : "No result available.");

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "HEAL AI Lab Result – " + testName);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "HEAL AI Lab Analysis\nTest: " + testName + "\nDate: " + bookingDate
                    + "\n\n" + aiResult);
            startActivity(Intent.createChooser(shareIntent, "Share Report"));
        });
    }
}
