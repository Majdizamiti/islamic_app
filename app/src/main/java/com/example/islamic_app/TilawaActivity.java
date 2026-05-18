package com.example.islamic_app;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic_app.databinding.ActivityTilawaBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TilawaActivity extends AppCompatActivity {

    private ActivityTilawaBinding binding;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<String> pickAudio = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    binding.etUrl.setText(uri.toString());
                    Toast.makeText(this, "File Selected", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTilawaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("tilawat");

        binding.ivBack.setOnClickListener(v -> finish());
        binding.btnPickFile.setOnClickListener(v -> pickAudio.launch("audio/*"));
        binding.btnSubmit.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        String title = binding.etTitle.getText().toString().trim();
        String reciter = binding.etReciter.getText().toString().trim();
        String url = binding.etUrl.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (title.isEmpty() || reciter.isEmpty() || url.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        String uid = (user != null) ? user.getUid() : "unknown";
        String email = (user != null) ? user.getEmail() : "unknown";

        String id = mDatabase.push().getKey();
        Tilawa tilawa = new Tilawa(id, title, reciter, url, description, uid, email, System.currentTimeMillis());

        if (id != null) {
            mDatabase.child(id).setValue(tilawa).addOnCompleteListener(task -> {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnSubmit.setEnabled(true);

                if (task.isSuccessful()) {
                    Toast.makeText(this, "Success! Tilawa Saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
