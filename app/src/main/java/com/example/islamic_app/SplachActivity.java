package com.example.islamic_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic_app.databinding.ActivitySplachBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplachActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplachBinding binding = ActivitySplachBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Show Splash for 3 seconds then check auth state
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                // User is signed in, go to MainActivity
                intent = new Intent(SplachActivity.this, MainActivity.class);
            } else {
                // No user is signed in, go to LoginActivity
                intent = new Intent(SplachActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 3000);
    }
}