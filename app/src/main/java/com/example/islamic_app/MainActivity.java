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

import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView tvRandomAyahText;
    private TextView tvRandomAyahInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        checkUserRole();
        loadData();

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

        tvRandomAyahText = findViewById(R.id.tvRandomAyahText);
        tvRandomAyahInfo = findViewById(R.id.tvRandomAyahInfo);

        loadRandomAyahForHome();
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

    private void loadData() {
        fetchPrayerTimes();
    }
    private void updatePrayerUI(String fajr, String dhuhr, String asr,
                                String maghrib, String isha) {

        binding.tvFajr.setText(fajr);
        binding.tvDhuhr.setText(dhuhr);
        binding.tvAsr.setText(asr);
        binding.tvMaghrib.setText(maghrib);
        binding.tvIsha.setText(isha);
    }

    private void fetchPrayerTimes() {
        new Thread(() -> {
            try {
                String city = "Tunis";
                String country = "Tunisia";

                String urlString = "https://api.aladhan.com/v1/timingsByCity?city="
                        + city + "&country=" + country + "&method=2";

                java.net.URL url = new java.net.URL(urlString);
                java.net.HttpURLConnection connection =
                        (java.net.HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");

                java.io.InputStream stream = connection.getInputStream();
                java.io.BufferedReader reader =
                        new java.io.BufferedReader(new java.io.InputStreamReader(stream));

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                org.json.JSONObject json = new org.json.JSONObject(result.toString());
                org.json.JSONObject timings =
                        json.getJSONObject("data").getJSONObject("timings");

                String fajr = timings.getString("Fajr");
                String dhuhr = timings.getString("Dhuhr");
                String asr = timings.getString("Asr");
                String maghrib = timings.getString("Maghrib");
                String isha = timings.getString("Isha");

                runOnUiThread(() -> updatePrayerUI(fajr, dhuhr, asr, maghrib, isha));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadRandomAyahForHome() {
        QuranApiClient.fetchRandomAyah(new QuranApiClient.RandomAyahCallback() {
            @Override
            public void onSuccess(QuranApiClient.SurahPayload surah, QuranApiClient.AyahItem ayah) {
                runOnUiThread(() -> {
                    tvRandomAyahText.setText(ayah.text);

                    String info = surah.name + " - الآية " + ayah.numberInSurah;
                    tvRandomAyahInfo.setText(info);
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    tvRandomAyahText.setText("تعذر تحميل الآية");
                    tvRandomAyahInfo.setText("");
                });
            }
        });
    }



}