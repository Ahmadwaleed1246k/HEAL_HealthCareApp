package com.example.heal;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DoctorReplyActivity extends AppCompatActivity {

    private TextView tvPatientName, tvSymptoms, tvDescription;
    private EditText etReply;
    private View btnSubmitReply;
    private ChatMessage chatMessage;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_reply);

        chatMessage = (ChatMessage) getIntent().getSerializableExtra("chatMessage");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("chats").child(chatMessage.getMessageId());

        tvPatientName = findViewById(R.id.tvPatientName);
        tvSymptoms = findViewById(R.id.tvSymptoms);
        tvDescription = findViewById(R.id.tvDescription);
        etReply = findViewById(R.id.etReply);
        btnSubmitReply = findViewById(R.id.btnSubmitReply);

        tvPatientName.setText("Patient: " + chatMessage.getPatientName());
        tvSymptoms.setText(chatMessage.getSymptoms());
        tvDescription.setText(chatMessage.getDescription());
        
        if (chatMessage.getReply() != null) {
            etReply.setText(chatMessage.getReply());
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnSubmitReply.setOnClickListener(v -> submitReply());
    }

    private void submitReply() {
        String reply = etReply.getText().toString().trim();

        if (reply.isEmpty()) {
            Toast.makeText(this, "Please enter a reply", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("reply", reply);
        updates.put("status", "replied");

        mDatabase.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DoctorReplyActivity.this, "Reply submitted successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(DoctorReplyActivity.this, "Failed to submit reply", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
