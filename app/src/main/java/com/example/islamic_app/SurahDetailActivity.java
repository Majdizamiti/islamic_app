package com.example.islamic_app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.islamic_app.databinding.ActivitySurahDetailBinding;
import com.example.islamic_app.databinding.ItemAyahBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SurahDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SURAH_NUMBER = "extra_surah_number";

    private ActivitySurahDetailBinding binding;
    private int surahNumber;
    private float currentFontSize = 22f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySurahDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Read the selected surah number passed from Quranpage.
        surahNumber = getIntent().getIntExtra(EXTRA_SURAH_NUMBER, -1);

        binding.ivBack.setOnClickListener(v -> finish());
        binding.btnRetry.setOnClickListener(v -> fetchSurah());

        binding.btnNextSurah.setOnClickListener(v -> {
            if (surahNumber < 114) {
                surahNumber++;
                fetchSurah();
            }
        });

        binding.btnPrevSurah.setOnClickListener(v -> {
            if (surahNumber > 1) {
                surahNumber--;
                fetchSurah();
            }
        });

        binding.ivZoomIn.setOnClickListener(v -> {
            if (currentFontSize < 40) {
                currentFontSize += 2;
                updateFontSize();
            }
        });

        binding.ivZoomOut.setOnClickListener(v -> {
            if (currentFontSize > 14) {
                currentFontSize -= 2;
                updateFontSize();
            }
        });

        if (surahNumber < 1 || surahNumber > 114) {
            showError(getString(R.string.surah_error_invalid_number));
            return;
        }

        fetchSurah();
    }

    private void fetchSurah() {
        showLoading();

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // Using the requested qurani.ai API
                // We use the surah endpoint with quran-uthmani edition to get all ayahs at once efficiently
                URL url = new URL("https://api.qurani.ai/gw/qh/v1/surah/" + surahNumber + "/quran-uthmani");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                InputStream stream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                if (stream == null) {
                    runOnUiThread(() -> showError(getString(R.string.surah_error_generic)));
                    return;
                }

                String body = readStream(stream);
                SurahPayload payload = parseResponse(body);

                if (payload == null) {
                    runOnUiThread(() -> showError(getString(R.string.surah_error_parse)));
                    return;
                }

                if (responseCode >= 200 && responseCode < 300 && payload.ayahs != null) {
                    runOnUiThread(() -> showContent(payload));
                } else {
                    String message = payload.errorMessage != null ? payload.errorMessage : getString(R.string.surah_error_generic);
                    runOnUiThread(() -> showError(message));
                }
            } catch (IOException e) {
                Log.e("SurahDetail", "Network error", e);
                runOnUiThread(() -> showError(getString(R.string.surah_error_network)));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.tvSurahName.setText(R.string.surah_loading);
        binding.tvSurahMeta.setText(R.string.surah_meta_placeholder);
    }

    private void showError(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message);
    }

    private void updateFontSize() {
        binding.tvFontSize.setText((int)currentFontSize + "px");
        for (int i = 0; i < binding.llAyahContainer.getChildCount(); i++) {
            View child = binding.llAyahContainer.getChildAt(i);
            if (child.getId() == R.id.tvBismillah) {
                ((android.widget.TextView) child).setTextSize(currentFontSize + 6); // Keep it slightly larger
            } else {
                try {
                    ItemAyahBinding itemBinding = ItemAyahBinding.bind(child);
                    itemBinding.tvAyahText.setTextSize(currentFontSize);
                } catch (Exception ignored) {}
            }
        }
    }

    private void showContent(SurahPayload payload) {
        binding.progressBar.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);

        binding.tvSurahName.setText(payload.name);
        String meta = payload.revelationTypeArabic + " - " + payload.numberOfAyahs + " " + getString(R.string.ayah_word);
        binding.tvSurahMeta.setText(meta);

        binding.llAyahContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();

        // Add Bismillah header for all surahs except Tawbah (9)
        if (surahNumber != 9) {
            View bismillahView = inflater.inflate(R.layout.item_bismillah, binding.llAyahContainer, false);
            binding.llAyahContainer.addView(bismillahView);
        }

        for (AyahItem ayah : payload.ayahs) {
            String text = ayah.text;
            
            // Remove Bismillah from the first ayah of every surah except Surah 9 (Tawbah)
            if (ayah.numberInSurah == 1 && surahNumber != 9) {
                // Calculate lengths of Bismillah variations to remove them precisely
                String b1 = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"; // Variation with Alif Wasla
                String b2 = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"; // Standard Alif variation
                
                if (text.startsWith(b1)) {
                    text = text.substring(b1.length()).trim();
                } else if (text.startsWith(b2)) {
                    text = text.substring(b2.length()).trim();
                }
            }

            // Only add a card if there is remaining text (handles cases like Surah 1 Ayah 1 becoming empty)
            if (!text.isEmpty()) {
                ItemAyahBinding ayahBinding = ItemAyahBinding.inflate(inflater, binding.llAyahContainer, false);
                ayahBinding.tvAyahText.setText(text);
                ayahBinding.tvAyahText.setTextSize(currentFontSize);
                ayahBinding.tvAyahNumber.setText(String.valueOf(ayah.numberInSurah));
                binding.llAyahContainer.addView(ayahBinding.getRoot());
            }
        }
    }

    private SurahPayload parseResponse(String body) {
        try {
            JSONObject root = new JSONObject(body);
            SurahPayload payload = new SurahPayload();

            if (!root.has("data")) {
                payload.errorMessage = getString(R.string.surah_error_generic);
                return payload;
            }

            JSONObject data = root.getJSONObject("data");
            payload.name = data.optString("name", getString(R.string.surah_title_default));
            String revelationType = data.optString("revelationType", "");
            payload.revelationTypeArabic = toArabicRevelationType(revelationType);
            payload.numberOfAyahs = data.optInt("numberOfAyahs", 0);

            JSONArray ayahsJson = data.optJSONArray("ayahs");
            payload.ayahs = new ArrayList<>();
            if (ayahsJson != null) {
                for (int i = 0; i < ayahsJson.length(); i++) {
                    JSONObject ayahJson = ayahsJson.getJSONObject(i);
                    AyahItem ayah = new AyahItem();
                    ayah.numberInSurah = ayahJson.optInt("numberInSurah", i + 1);
                    ayah.text = ayahJson.optString("text", "").trim();
                    payload.ayahs.add(ayah);
                }
            }

            return payload;
        } catch (JSONException e) {
            Log.e("SurahDetail", "Parse error", e);
            return null;
        }
    }

    private String toArabicRevelationType(String revelationType) {
        if ("Meccan".equalsIgnoreCase(revelationType)) {
            return getString(R.string.revelation_meccan_ar);
        }
        if ("Medinan".equalsIgnoreCase(revelationType)) {
            return getString(R.string.revelation_medinan_ar);
        }
        return getString(R.string.revelation_unknown_ar);
    }

    private String readStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private static class AyahItem {
        int numberInSurah;
        String text;
    }

    private static class SurahPayload {
        String name;
        String revelationTypeArabic;
        int numberOfAyahs;
        List<AyahItem> ayahs;
        String errorMessage;
    }
}
