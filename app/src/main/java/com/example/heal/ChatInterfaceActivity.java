package com.example.heal;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatInterfaceActivity extends AppCompatActivity {

    private TextView tvDoctorName, tvDoctorReply;
    private EditText etSymptoms, etDescription;
    private View btnSendChat;
    private LinearLayout layoutReply;
    private Doctor doctor;
    private String patientId, patientName;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_interface);

        doctor = (Doctor) getIntent().getSerializableExtra("doctor");
        patientId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        patientName = new SessionManager(this).getName();

        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorReply = findViewById(R.id.tvDoctorReply);
        etSymptoms = findViewById(R.id.etSymptoms);
        etDescription = findViewById(R.id.etDescription);
        btnSendChat = findViewById(R.id.btnSendChat);
        layoutReply = findViewById(R.id.layoutReply);

        tvDoctorName.setText(doctor.getName());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        mDatabase = FirebaseDatabase.getInstance().getReference().child("chats")
                .child(patientId + "_" + doctor.getDoctor_id());

        btnSendChat.setOnClickListener(v -> sendConsultation());

        checkExistingChat();
    }

    private void checkExistingChat() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    ChatMessage message = snapshot.getValue(ChatMessage.class);
                    if (message != null && message.getSymptoms() != null && !message.getSymptoms().isEmpty()) {
                        etSymptoms.setText(message.getSymptoms());
                        etDescription.setText(message.getDescription());
                        if (!message.getSymptoms().isEmpty()) {
                            etSymptoms.setEnabled(false);
                            etDescription.setEnabled(false);
                        }
                        btnSendChat.setVisibility(View.GONE);
                        layoutReply.setVisibility(View.VISIBLE);

                        if (message.getReply() != null && !message.getReply().isEmpty()) {
                            tvDoctorReply.setText(message.getReply());
                        } else {
                            tvDoctorReply.setText("Waiting for doctor's response...");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendConsultation() {
        String symptoms = etSymptoms.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (symptoms.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = patientId + "_" + doctor.getDoctor_id();
        ChatMessage message = new ChatMessage(messageId, patientId, patientName, doctor.getDoctor_id(), doctor.getName());
        message.setSymptoms(symptoms);
        message.setDescription(description);
        message.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        mDatabase.setValue(message).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ChatInterfaceActivity.this, "Consultation sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ChatInterfaceActivity.this, "Failed to send consultation", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
