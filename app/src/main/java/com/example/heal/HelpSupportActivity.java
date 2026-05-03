package com.example.heal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpSupportActivity extends AppCompatActivity {

    private EditText etSubject, etMessage;
    private Button btnSendMessage;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        etSubject = findViewById(R.id.etSubject);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnSendMessage.setOnClickListener(v -> {
            String subject = etSubject.getText().toString().trim();
            String message = etMessage.getText().toString().trim();

            if (subject.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            sendEmail(subject, message);
        });
    }

    private void sendEmail(String subject, String message) {
        String recipient = "aw0412626644@gmail.com";
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        intent.putExtra(Intent.EXTRA_SUBJECT, "HEAL App Support: " + subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(intent, "Send Email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
