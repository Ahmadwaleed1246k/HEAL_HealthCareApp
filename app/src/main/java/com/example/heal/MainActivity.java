package com.example.heal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Get Started clicked!", Toast.LENGTH_SHORT).show();
                Intent I = new Intent(MainActivity.this, Signup.class);
                startActivity(I);
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Login clicked!", Toast.LENGTH_SHORT).show();
                Intent I = new Intent(MainActivity.this, Login.class);
                startActivity(I);
            }
        });
    }
}