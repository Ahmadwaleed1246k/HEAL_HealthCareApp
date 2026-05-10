package com.example.heal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiLabQuestionsActivity extends AppCompatActivity {

    public static final String EXTRA_TEST_ID = "test_id";
    public static final String EXTRA_TEST_NAME = "test_name";
    public static final String EXTRA_TEST_CATEGORY = "test_category";
    public static final String EXTRA_TEST_PRICE = "test_price";
    public static final String EXTRA_TEST_MARKERS = "test_markers";
    public static final String EXTRA_PREP_INSTRUCTIONS = "prep_instructions";

    private static final String OPENROUTER_API_KEY = "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";
    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    private LinearLayout llQuestionsContainer;
    private TextView tvTestName, tvCategory, tvPrice, btnConfirmBooking;
    private android.app.ProgressDialog progressDialog;

    // Each question: label → chosen answer (radio) or typed text (edit)
    private final List<QuestionView> questionViews = new ArrayList<>();

    private String testId, testName, testCategory, testMarkers, prepInstructions;
    private double testPrice;
    private String userGender = "Not specified";
    private int userAge = 0;
    private String userFullName = "Patient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_lab_questions);

        testId = getIntent().getStringExtra(EXTRA_TEST_ID);
        testName = getIntent().getStringExtra(EXTRA_TEST_NAME);
        testCategory = getIntent().getStringExtra(EXTRA_TEST_CATEGORY);
        testPrice = getIntent().getDoubleExtra(EXTRA_TEST_PRICE, 0);
        testMarkers = getIntent().getStringExtra(EXTRA_TEST_MARKERS);
        prepInstructions = getIntent().getStringExtra(EXTRA_PREP_INSTRUCTIONS);

        tvTestName = findViewById(R.id.tvTestName);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        llQuestionsContainer = findViewById(R.id.llQuestionsContainer);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        tvTestName.setText(testName);
        tvCategory.setText(testCategory);
        tvPrice.setText("$" + (int) testPrice);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        fetchUserProfileAndBuildQuestions();

        btnConfirmBooking.setOnClickListener(v -> {
            if (!allAnswered()) {
                Toast.makeText(this, "Please answer all questions before continuing.", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmBooking();
        });
    }

    private void fetchUserProfileAndBuildQuestions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            buildQuestionsFor(testId);
            return;
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userFullName = snapshot.child("name").getValue(String.class);
                    userGender = snapshot.child("gender").getValue(String.class);
                    String dob = snapshot.child("dob").getValue(String.class);
                    if (dob != null) {
                        userAge = calculateAge(dob);
                    }
                }
                // Build questions AFTER fetching user profile
                buildQuestionsFor(testId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                buildQuestionsFor(testId);
            }
        });
    }

    private int calculateAge(String dob) {
        try {
            // Expected format: d/M/yyyy or dd/MM/yyyy
            String[] parts = dob.split("/");
            if (parts.length != 3) return 0;
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            java.util.Calendar birth = java.util.Calendar.getInstance();
            birth.set(year, month - 1, day);
            java.util.Calendar today = java.util.Calendar.getInstance();

            int age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR);
            if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (Exception e) {
            return 0;
        }
    }


    // ─────────────────────────────────────────────
    //  Per-test question definitions
    // ─────────────────────────────────────────────

    private void buildQuestionsFor(String id) {
        if (id == null) return;
        switch (id) {
            case "comprehensive_blood_work":
                addRadio("Have you fasted (no food or drink except water) for at least 8-12 hours?", "Yes", "No");
                addText("Are you experiencing symptoms like chronic fatigue, persistent weakness, or unexplained weight changes?");
                addText("Are you currently taking any medications, including blood thinners, antibiotics, or hormone therapy?");
                addRadio("Any history of chronic conditions like Diabetes, Anemia, or Hypertension?", "Yes", "No", "Not sure");
                break;

            case "lipid_profile_basic":
                addRadio("Have you fasted for 12 hours before this test? (Crucial for Triglyceride accuracy)", "Yes", "No");
                addRadio("Do you have a personal or family history of heart disease or stroke?", "Yes", "No", "Not sure");
                addText("Describe your typical weekly physical activity and diet (e.g., high-fat, balanced, vegetarian).");
                addRadio("Do you currently smoke or use tobacco products?", "Yes", "No", "Occasionally");
                break;

            case "thyroid_profile":
                addText("Are you experiencing sensitivity to cold, hair loss, or changes in heart rate?");
                addRadio("Are you currently on any thyroid-related medications or taking Biotin supplements?", "Yes", "No");
                addRadio("Any history of thyroid nodules, Goiter, or previous thyroid surgery?", "Yes", "No");
                addRadio("If applicable, are you currently pregnant or postpartum?", "Pregnant", "Postpartum", "N/A");
                break;

            case "liver_function_test":
                addRadio("Have you consumed alcohol in the last 48-72 hours?", "Yes", "No");
                addRadio("Are you experiencing jaundice (yellowing of eyes/skin) or upper abdominal pain?", "Yes", "No");
                addRadio("Are you taking any over-the-counter painkillers like Acetaminophen/Paracetamol regularly?", "Yes", "No");
                addRadio("Known history of Hepatitis or fatty liver disease?", "Yes", "No", "Not sure");
                break;

            case "vitamin_d_b12_panel":
                addText("Describe your daily sun exposure and any dietary restrictions (e.g., Vegan, Vegetarian).");
                addRadio("Are you experiencing numbness, tingling in hands/feet, or memory difficulties?", "Yes", "No");
                addRadio("Are you currently taking any Vitamin D or B12 supplements or injections?", "Yes", "No");
                addRadio("Any history of malabsorption issues (like Celiac or Crohn's disease)?", "Yes", "No", "Not sure");
                break;

            case "cardiac_wellness":
                addRadio("Do you experience chest pain, shortness of breath, or palpitations during physical exertion?", "Yes", "No");
                addText("What is your most recent known blood pressure reading (if known)?");
                addRadio("Do you have a family history of early-onset heart disease (before age 55)?", "Yes", "No", "Not sure");
                addText("Describe your current stress levels and sleep patterns.");
                break;

            case "executive_health_panel":
                addText("What is your primary health goal or concern for this comprehensive check-up?");
                addText("Do you have any existing chronic conditions like Diabetes, High BP, or Thyroid issues?");
                addRadio("Are you under significant professional or personal stress lately?", "Yes", "No");
                addRadio("When was your last comprehensive medical screening?", "< 6 months", "6-12 months", "1-2 years", "> 2 years", "Never");
                break;

            case "dna_genetic_screening":
                addText("What specific health risks or ancestry traits are you most interested in investigating?");
                addText("Do you have a family history of hereditary conditions (e.g., specific cancers, cystic fibrosis)?");
                addRadio("Have you previously consulted a genetic counselor?", "Yes", "No");
                break;

            case "imaging_radiology":
                addText("Exactly where is the pain or concern located, and how long has it persisted?");
                addRadio("Do you have any metal implants, pacemakers, or known allergies to contrast dye?", "Yes", "No");
                addRadio("Is there any possibility of pregnancy? (For X-ray/CT/MRI safety)", "Yes", "No", "N/A");
                addRadio("Urgency?", "Routine", "Urgent (doctor advised)");
                break;

            default:
                addText("Describe any symptoms or health concerns relevant to this test");
                addRadio("Known medical conditions?", "Diabetes", "Hypertension", "Heart disease", "None");
                break;
        }
    }

    // ─────────────────────────────────────────────
    //  Question builder helpers
    // ─────────────────────────────────────────────

    private void addRadio(String question, String... options) {
        View card = buildQuestionCard(question);
        LinearLayout container = card.findViewWithTag("answer_container");

        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);
        LinearLayout.LayoutParams rgParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rgParams.topMargin = dp(8);
        rg.setLayoutParams(rgParams);

        for (String option : options) {
            RadioButton rb = new RadioButton(this);
            rb.setText(option);
            rb.setTextSize(14f);
            rb.setTextColor(Color.parseColor("#333333"));
            rb.setPadding(dp(4), dp(6), dp(4), dp(6));
            rg.addView(rb);
        }
        container.addView(rg);

        QuestionView qv = new QuestionView(question, null, rg);
        questionViews.add(qv);
        llQuestionsContainer.addView(card);
    }

    private void addText(String question) {
        View card = buildQuestionCard(question);
        LinearLayout container = card.findViewWithTag("answer_container");

        EditText et = new EditText(this);
        et.setHint("Type your answer here...");
        et.setTextSize(14f);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        et.setLines(2);
        et.setMaxLines(4);
        et.setGravity(Gravity.TOP | Gravity.START);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etParams.topMargin = dp(8);
        et.setLayoutParams(etParams);
        et.setBackgroundColor(Color.parseColor("#F5F5F5"));
        et.setPadding(dp(12), dp(10), dp(12), dp(10));
        container.addView(et);

        QuestionView qv = new QuestionView(question, et, null);
        questionViews.add(qv);
        llQuestionsContainer.addView(card);
    }

    private View buildQuestionCard(String question) {
        // Card shell
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(16);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(14));
        card.setCardElevation(dp(2));
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(18), dp(16), dp(18), dp(18));

        // Question label
        TextView tvQ = new TextView(this);
        tvQ.setText(question);
        tvQ.setTextSize(15f);
        tvQ.setTextColor(Color.parseColor("#1A1A1A"));
        tvQ.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        inner.addView(tvQ);

        // Answer container (tagged so we can find it)
        LinearLayout answerContainer = new LinearLayout(this);
        answerContainer.setOrientation(LinearLayout.VERTICAL);
        answerContainer.setTag("answer_container");
        inner.addView(answerContainer);

        card.addView(inner);
        return card;
    }

    private boolean allAnswered() {
        for (QuestionView qv : questionViews) {
            if (qv.editText != null) {
                if (qv.editText.getText().toString().trim().isEmpty()) return false;
            } else if (qv.radioGroup != null) {
                if (qv.radioGroup.getCheckedRadioButtonId() == -1) return false;
            }
        }
        return true;
    }

    private String collectAnswers() {
        StringBuilder sb = new StringBuilder();
        for (QuestionView qv : questionViews) {
            sb.append("• ").append(qv.question).append("\n  → ");
            if (qv.editText != null) {
                sb.append(qv.editText.getText().toString().trim());
            } else if (qv.radioGroup != null) {
                int id = qv.radioGroup.getCheckedRadioButtonId();
                RadioButton rb = qv.radioGroup.findViewById(id);
                sb.append(rb != null ? rb.getText().toString() : "—");
            }
            sb.append("\n\n");
        }
        return sb.toString().trim();
    }

    private void confirmBooking() {
        btnConfirmBooking.setText("Booking...");
        btnConfirmBooking.setEnabled(false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in to book.", Toast.LENGTH_SHORT).show();
            resetButton();
            return;
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                if (userName == null) userName = "User";
                String answers = collectAnswers();
                String bookingId = UUID.randomUUID().toString();

                TestBooking booking = new TestBooking();
                booking.setBooking_id(bookingId);
                booking.setUser_id(user.getUid());
                booking.setUser_name(userName);
                booking.setTest_id(testId);
                booking.setTest_name(testName);
                booking.setBooking_date(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
                booking.setStatus("confirmed");
                booking.setTotal_amount(testPrice);
                booking.setPayment_status("pending");
                booking.setCreated_at(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date()));
                booking.setPreparation_instructions(prepInstructions);
                booking.setUser_answers(answers);
                booking.setAi_result_ready(false);
                booking.setAi_result("");

                db.child("test_bookings").child(bookingId).setValue(booking)
                    .addOnSuccessListener(unused -> {
                        // Booking saved — now show payment sheet
                        PaymentBottomSheet paymentSheet = new PaymentBottomSheet(booking, paidId -> {
                            // Payment successful — now call Gemini in background
                            runOnUiThread(() -> {
                                progressDialog = new android.app.ProgressDialog(AiLabQuestionsActivity.this);
                                progressDialog.setMessage("AI is analyzing your health context...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                            });
                            callGeminiApi(paidId, answers);
                        });
                        paymentSheet.show(getSupportFragmentManager(), "payment");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AiLabQuestionsActivity.this,
                                "Failed to save booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        resetButton();
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AiLabQuestionsActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                resetButton();
            }
        });
    }

    private void callGeminiApi(String bookingId, String userAnswers) {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = new android.app.ProgressDialog(AiLabQuestionsActivity.this);
            progressDialog.setMessage("AI is analyzing your health context...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        });

        String prompt = "You are a Senior Medical Consultant with 20+ years of experience. A patient named " + userFullName + " has booked a lab test: " + testName + ".\n\n"
                + "**PATIENT PROFILE:**\n"
                + "- Gender: " + userGender + "\n"
                + "- Age: " + (userAge > 0 ? userAge : "Not provided") + " years\n"
                + "- Test Category: " + testCategory + "\n"
                + "- Markers being tested: " + (testMarkers != null ? testMarkers : "N/A") + "\n\n"
                + "**HEALTH CONTEXT:**\n" + userAnswers + "\n\n"
                + "**INSTRUCTIONS:**\n"
                + "Generate a professional, empathetic Lab Test Analysis Report. Use ONLY HTML (<b>, <ul>, <li>, <p>, <h4>, <table>). No Markdown.\n\n"
                + "**REQUIRED SECTIONS:**\n"
                + "1. <b>EXECUTIVE SUMMARY</b>: Brief health status overview and what the test reveals.\n"
                + "2. <b>CLINICAL CORRELATION</b>: Connect markers to symptoms/history in plain medical language.\n"
                + "3. <b>MARKER REFERENCE GUIDE</b>: HTML <table> with Marker | Range | Low suggests | High suggests.\n"
                + "4. <b>PERSONALIZED RISK ASSESSMENT</b>: Identify 2-4 potential risks based on profile (Low/Mod/High suspicion).\n"
                + "5. <b>CLINICAL RECOMMENDATIONS</b>: 4-6 actionable items (fasting, questions for doctor, lifestyle).\n"
                + "6. <b>PROFESSIONAL DISCLAIMER</b>: Boldly state this is educational AI analysis, not diagnosis.\n\n"
                + "**TONE:** Professional, authoritative, and medically accurate. Return ONLY the HTML report no extra text before and after.";

        try {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(message);

            JSONObject body = new JSONObject();
            body.put("model", "google/gemini-2.0-flash-lite-001");
            body.put("messages", messages);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(OPENROUTER_URL)
                    .addHeader("Authorization", "Bearer " + OPENROUTER_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://heal-app.com")
                    .addHeader("X-Title", "Heal Healthcare App")
                    .post(RequestBody.create(body.toString(),
                            MediaType.parse("application/json; charset=utf-8")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    android.util.Log.e("AiLabQuestions", "API call failed", e);
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    });
                    saveAiResult(bookingId, "Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String aiText;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String raw = response.body().string();
                            JSONObject json = new JSONObject(raw);
                            
                            aiText = json.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                        } catch (Exception e) {
                            android.util.Log.e("AiLabQuestions", "Parse error", e);
                            aiText = "Error parsing AI response. The service might be temporarily unavailable.";
                        }
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        android.util.Log.e("AiLabQuestions", "API Error: " + response.code() + " - " + errorBody);
                        aiText = "AI analysis failed (Error " + response.code() + ").";
                    }
                    
                    runOnUiThread(() -> {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    });
                    saveAiResult(bookingId, aiText);
                }
            });
        } catch (Exception e) {
            saveAiResult(bookingId, "Error building AI request: " + e.getMessage());
        }
    }

    private void saveAiResult(String bookingId, String aiText) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        
        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("ai_result", aiText);
        updates.put("ai_result_ready", true);

        db.child("test_bookings").child(bookingId).updateChildren(updates)
            .addOnCompleteListener(task -> runOnUiThread(() -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this,
                            "Booking confirmed! Opening analysis report...",
                            Toast.LENGTH_LONG).show();
                            
                    // Immediately show the result detail
                    Intent intent = new Intent(this, ResultDetailActivity.class);
                    intent.putExtra(ResultDetailActivity.EXTRA_BOOKING_ID, bookingId);
                    intent.putExtra(ResultDetailActivity.EXTRA_TEST_NAME, testName);
                    intent.putExtra(ResultDetailActivity.EXTRA_BOOKING_DATE, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    intent.putExtra(ResultDetailActivity.EXTRA_AI_RESULT, aiText);
                    startActivity(intent);
                } else {
                    Toast.makeText(this,
                            "Booking confirmed, but AI result update failed.",
                            Toast.LENGTH_LONG).show();
                }
                setResult(RESULT_OK);
                finish();
            }));
    }

    private void resetButton() {
        btnConfirmBooking.setText("✦  Confirm Booking & Get AI Analysis");
        btnConfirmBooking.setEnabled(true);
    }

    private int dp(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }



    private static class QuestionView {
        String question;
        EditText editText;
        RadioGroup radioGroup;

        QuestionView(String question, EditText et, RadioGroup rg) {
            this.question = question;
            this.editText = et;
            this.radioGroup = rg;
        }
    }
}
