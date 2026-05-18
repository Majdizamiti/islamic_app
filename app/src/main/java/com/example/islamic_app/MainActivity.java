package com.example.islamic_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic_app.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        checkUserRole();

        // Redirect to Quranpage
        binding.Quranicon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Quranpage.class);
            startActivity(intent);
        });

        // Redirect to TasbihActivity
        binding.ivTasbih.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TasbihActivity.class);
            startActivity(intent);
        });
        binding.ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // FAB Click logic
        binding.fabAdd.setOnClickListener(v -> {
            if (binding.tvFabMessage.getVisibility() == View.VISIBLE) {
                binding.tvFabMessage.setVisibility(View.GONE);
            } else {
                binding.tvFabMessage.setVisibility(View.VISIBLE);
            }
        });

        // Navigate to TilawaActivity when clicking the message
        binding.tvFabMessage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TilawaActivity.class);
            startActivity(intent);
        });
    }

    private void checkUserRole() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            mDatabase.child("users").child(userId).child("role")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String role = snapshot.getValue(String.class);
                            if ("Creator".equalsIgnoreCase(role)) {
                                binding.fabAdd.setVisibility(View.VISIBLE);
                            } else {
                                binding.fabAdd.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            binding.fabAdd.setVisibility(View.GONE);
                        }
                    });
        } else {
            binding.fabAdd.setVisibility(View.GONE);
        }
    }


}