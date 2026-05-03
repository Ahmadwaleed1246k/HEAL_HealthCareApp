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

    private static final String GEMINI_API_KEY = "AIzaSyD1vais9mfNi0_P4SCEKDWkNLWeaoat9Og";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;

    private LinearLayout llQuestionsContainer;
    private TextView tvTestName, tvCategory, tvPrice, btnConfirmBooking;
    private android.app.ProgressDialog progressDialog;

    // Each question: label → chosen answer (radio) or typed text (edit)
    private final List<QuestionView> questionViews = new ArrayList<>();

    private String testId, testName, testCategory, testMarkers, prepInstructions;
    private double testPrice;

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

        buildQuestionsFor(testId);

        btnConfirmBooking.setOnClickListener(v -> {
            if (!allAnswered()) {
                Toast.makeText(this, "Please answer all questions before continuing.", Toast.LENGTH_SHORT).show();
                return;
            }
            confirmBooking();
        });
    }

    // ─────────────────────────────────────────────
    //  Per-test question definitions
    // ─────────────────────────────────────────────

    private void buildQuestionsFor(String id) {
        if (id == null) return;
        switch (id) {
            case "comprehensive_blood_work":
                addRadio("What is your age range?", "Under 18", "18–35", "36–50", "51–65", "65+");
                addRadio("What is your gender?", "Male", "Female", "Prefer not to say");
                addRadio("Current symptoms?", "Fatigue", "Dizziness", "Weakness", "None");
                addRadio("When did you last eat?", "Less than 2 hours ago", "2–6 hours ago", "More than 10 hours ago");
                addRadio("Known medical conditions?", "Diabetes", "Anaemia", "Hypertension", "None");
                addText("Current medications (if any)");
                break;

            case "lipid_profile_basic":
                addRadio("What is your age range?", "Under 18", "18–35", "36–50", "51–65", "65+");
                addRadio("What is your gender?", "Male", "Female", "Prefer not to say");
                addRadio("Hours fasted before this test?", "Less than 6 hours", "6–9 hours", "9–12 hours", "More than 12 hours");
                addRadio("Family history of heart disease?", "Yes", "No", "Not sure");
                addRadio("Do you smoke?", "Yes", "No", "Occasionally");
                addRadio("Exercise frequency?", "Daily", "3–5 times/week", "1–2 times/week", "Rarely/Never");
                break;

            case "thyroid_profile":
                addRadio("What is your age range?", "Under 18", "18–35", "36–50", "51–65", "65+");
                addRadio("What is your gender?", "Male", "Female", "Prefer not to say");
                addRadio("Symptoms you are experiencing?", "Unexplained weight gain", "Unexplained weight loss", "Fatigue & lethargy", "Hair loss", "None");
                addRadio("Currently on thyroid medication?", "Yes", "No");
                addRadio("Pregnancy status (if applicable)?", "Currently pregnant", "Postpartum (within 6 months)", "Not applicable");
                addRadio("Previous thyroid condition?", "Hypothyroidism", "Hyperthyroidism", "No history");
                break;

            case "liver_function_test":
                addRadio("What is your age range?", "Under 18", "18–35", "36–50", "51–65", "65+");
                addRadio("Alcohol consumption?", "Never", "Occasionally", "Regularly (weekly)", "Daily");
                addText("Recent medications or supplements taken");
                addRadio("Symptoms?", "Jaundice (yellow skin/eyes)", "Fatigue", "Nausea/vomiting", "Abdominal pain", "None");
                addRadio("Known liver or hepatitis condition?", "Hepatitis B", "Hepatitis C", "Fatty liver", "None");
                addRadio("Last 48h — consumed alcohol?", "Yes", "No");
                break;

            case "vitamin_d_b12_panel":
                addRadio("What is your age range?", "Under 18", "18–35", "36–50", "51–65", "65+");
                addRadio("What is your gender?", "Male", "Female", "Prefer not to say");
                addRadio("Dietary type?", "Vegetarian", "Vegan", "Non-vegetarian / Omnivore");
                addRadio("Average daily sun exposure?", "Less than 15 minutes", "15–30 minutes", "More than 30 minutes");
                addRadio("Symptoms?", "Persistent fatigue", "Bone or joint pain", "Numbness/tingling", "Memory issues", "None");
                addRadio("Currently taking Vitamin D/B12 supplements?", "Yes", "No");
                break;

            case "cardiac_wellness":
                addRadio("What is your age range?", "Under 30", "30–45", "46–60", "60+");
                addRadio("What is your gender?", "Male", "Female", "Prefer not to say");
                addRadio("Do you experience chest pain or pressure?", "Yes, frequently", "Yes, occasionally", "No");
                addRadio("Family history of heart attack or cardiac disease?", "Yes", "No", "Not sure");
                addRadio("Blood pressure status?", "Normal", "High (hypertension)", "Low", "Not aware");
                addRadio("Exercise tolerance?", "Can exercise without any discomfort", "Get breathless easily", "Unable to exercise due to symptoms");
                addRadio("Do you smoke or use tobacco?", "Yes", "No", "Ex-smoker");
                break;

            case "executive_health_panel":
                addRadio("What is your age range?", "Under 18", "18–35", "36–50", "51–65", "65+");
                addRadio("What is your gender?", "Male", "Female", "Prefer not to say");
                addRadio("Your approximate BMI category?", "Underweight", "Normal (18.5–24.9)", "Overweight (25–29.9)", "Obese (30+)", "Not sure");
                addRadio("Known health conditions?", "Diabetes", "Hypertension", "High cholesterol", "Thyroid disorder", "None");
                addRadio("When was your last full health check-up?", "Within 6 months", "6–12 months ago", "1–2 years ago", "More than 2 years ago", "Never");
                addRadio("Primary reason for this panel?", "Routine check-up", "Feeling unwell", "Pre-employment requirement", "Doctor's recommendation");
                break;

            case "dna_genetic_screening":
                addRadio("What is your age range?", "Under 18", "18–35", "36–50", "51–65", "65+");
                addRadio("What is your gender?", "Male", "Female", "Prefer not to say");
                addText("Any known family history of genetic or hereditary diseases? (describe or write 'None')");
                addRadio("Primary purpose of this test?", "Ancestry and heritage", "Understanding disease risk", "Both ancestry and health risk", "Medication response (pharmacogenomics)");
                addRadio("Have you done genetic testing before?", "Yes", "No");
                break;

            case "imaging_radiology":
                addText("Which body part or region needs to be imaged?");
                addText("Describe your symptoms or reason for imaging");
                addRadio("Have you had previous scans or imaging for this issue?", "Yes", "No");
                addRadio("Referred by a doctor?", "Yes", "No, self-referred");
                addRadio("Relevant conditions?", "Pregnancy", "Metal implants", "Claustrophobia", "None of the above");
                addRadio("Urgency?", "Routine", "Urgent (doctor advised)");
                break;

            default:
                // Generic fallback for unknown test types
                addRadio("What is your age range?", "Under 18", "18–35", "36–50", "51–65", "65+");
                addRadio("What is your gender?", "Male", "Female", "Prefer not to say");
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

    // ─────────────────────────────────────────────
    //  Validation & booking
    // ─────────────────────────────────────────────

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
        runOnUiThread(() -> btnConfirmBooking.setText("✦ Getting AI Analysis..."));

        String prompt = "You are a medical lab assistant AI. A patient has booked the following lab test:\n\n"
                + "Test: " + testName + "\n"
                + "Category: " + testCategory + "\n"
                + "Markers being tested: " + (testMarkers != null ? testMarkers : "N/A") + "\n\n"
                + "Patient provided the following health context:\n" + userAnswers + "\n\n"
                + "Based on this information, provide:\n"
                + "1. What these tests may indicate given the patient's context\n"
                + "2. Normal reference ranges for each marker\n"
                + "3. Key health insights and what to watch for\n"
                + "4. Recommended follow-up actions\n\n"
                + "Keep the response clear, structured, and in plain language a patient can understand.";

        try {
            // Gemini Request Format
            JSONObject part = new JSONObject();
            part.put("text", prompt);

            JSONArray parts = new JSONArray();
            parts.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject body = new JSONObject();
            body.put("contents", contents);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();

            Request request = new Request.Builder()
                    .url(GEMINI_URL)
                    .post(RequestBody.create(body.toString(),
                            MediaType.parse("application/json; charset=utf-8")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    android.util.Log.e("AiLabQuestions", "API call failed", e);
                    saveAiResult(bookingId, "Network error: " + e.getMessage() + ". Please check your internet connection.");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String aiText;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String raw = response.body().string();
                            JSONObject json = new JSONObject(raw);
                            
                            // Gemini Response Parsing
                            aiText = json.getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                        } catch (Exception e) {
                            android.util.Log.e("AiLabQuestions", "Parse error", e);
                            aiText = "Error parsing AI response. The service might be temporarily unavailable.";
                        }
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "No error body";
                        android.util.Log.e("AiLabQuestions", "API Error: " + response.code() + " - " + errorBody);
                        
                        if (response.code() == 404) {
                            aiText = "Model Not Found (Error 404). Please ensure the Gemini 1.5 Flash model is available in your region.";
                        } else if (response.code() == 400) {
                            aiText = "Invalid Request: " + errorBody;
                        } else if (response.code() == 403 || response.code() == 401) {
                            aiText = "API Key Error: Unauthorized. Please check your Google Studio API key.";
                        } else if (response.code() == 429) {
                            aiText = "AI service is busy (Rate limit exceeded). Please try again in a few minutes.";
                        } else {
                            aiText = "AI analysis failed (Error " + response.code() + ").";
                        }
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

    // ─────────────────────────────────────────────
    //  Question view holder
    // ─────────────────────────────────────────────

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
