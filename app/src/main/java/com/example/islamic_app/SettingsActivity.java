package com.example.islamic_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic_app.databinding.SettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private SettingsBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // 1. Load User Info
        if (user != null) {
            // Set email directly from login info for speed
            binding.tvUserEmail.setText(user.getEmail());

            // Also double-check the database if needed
            fetchEmailFromDatabase(user.getUid());
        }

        // 2. Back Button
        binding.ivBack.setOnClickListener(v -> finish());

        // 3. Logout Button
        binding.rlLogout.setOnClickListener(v -> performLogout());
    }

    private void fetchEmailFromDatabase(String uid) {
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("email")
                .get().addOnSuccessListener(snapshot -> {
                    String email = snapshot.getValue(String.class);
                    if (email != null) {
                        binding.tvUserEmail.setText(email);
                    }
                });
    }

    private void performLogout() {
        mAuth.signOut();
        Toast.makeText(this, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show();

        // Go to Login and clear history
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}