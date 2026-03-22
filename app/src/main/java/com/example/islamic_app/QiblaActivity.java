package com.example.islamic_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic_app.databinding.QiblaBinding;

public class QiblaActivity extends AppCompatActivity {

    private QiblaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = QiblaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}