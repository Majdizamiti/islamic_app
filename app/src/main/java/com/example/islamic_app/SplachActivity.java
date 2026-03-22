package com.example.islamic_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic_app.databinding.ActivitySplachBinding;

public class SplachActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySplachBinding binding = ActivitySplachBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Show Splash for 4 seconds then move to MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplachActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close SplashActivity so user can't go back to it
        }, 4000);
    }
}