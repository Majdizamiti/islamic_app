package com.example.islamic_app;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.islamic_app.databinding.ActivityTilawaBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TilawaActivity extends AppCompatActivity {

    private ActivityTilawaBinding binding;
    private DatabaseReference mDatabase;
    private Uri selectedFileUri;

    private final ActivityResultLauncher<String> pickAudioLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedFileUri = uri;
                    binding.etUrl.setText(uri.toString());
                    Toast.makeText(this, "تم اختيار الملف بنجاح", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTilawaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = FirebaseDatabase.getInstance().getReference("tilawat");

        binding.ivBack.setOnClickListener(v -> finish());
        binding.btnPickFile.setOnClickListener(v -> pickAudioLauncher.launch("audio/*"));
        binding.btnSubmit.setOnClickListener(v -> saveTilawa());
    }

    private void saveTilawa() {
        String title = binding.etTitle.getText().toString().trim();
        String reciter = binding.etReciter.getText().toString().trim();
        String url = binding.etUrl.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.etTitle.setError("Title is required");
            return;
        }

        if (TextUtils.isEmpty(reciter)) {
            binding.etReciter.setError("Reciter name is required");
            return;
        }

        if (TextUtils.isEmpty(url)) {
            binding.etUrl.setError("URL is required");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmit.setEnabled(false);

        String id = mDatabase.push().getKey();
        Tilawa tilawa = new Tilawa(id, title, reciter, url, description, System.currentTimeMillis());

        if (id != null) {
            mDatabase.child(id).setValue(tilawa)
                    .addOnCompleteListener(task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmit.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(TilawaActivity.this, "تم حفظ التلاوة بنجاح", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(TilawaActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
