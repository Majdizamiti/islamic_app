package com.example.islamic_app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Redirect to Quranpage
        binding.Quranicon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Quranpage.class);
            startActivity(intent);
        });

        // Redirect to QiblaActivity
        binding.ivQibla.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QiblaActivity.class);
            startActivity(intent);
        });
    }
}