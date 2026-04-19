package com.example.islamic_app;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySurahDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Read the selected surah number passed from Quranpage.
        surahNumber = getIntent().getIntExtra(EXTRA_SURAH_NUMBER, -1);

        binding.ivBack.setOnClickListener(v -> finish());
        binding.btnRetry.setOnClickListener(v -> fetchSurah());

        if (surahNumber < 1 || surahNumber > 114) {
            showError(getString(R.string.surah_error_invalid_number));
            return;
        }

        fetchSurah();
    }

    private void fetchSurah() {
        // Every new request starts by switching the screen to loading state.
        showLoading();

        // Network call is done off the main thread to keep UI responsive.
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("https://api.qurani.ai/gw/qh/v1/surah/" + surahNumber + "?limit=2000&offset=0");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                // For non-2xx responses, read from error stream to extract API message.
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

                if (responseCode >= 200 && responseCode < 300 && payload.ayahs != null && !payload.ayahs.isEmpty()) {
                    // UI updates must run on the main thread.
                    runOnUiThread(() -> showContent(payload));
                } else {
                    String message = payload.errorMessage != null ? payload.errorMessage : getString(R.string.surah_error_generic);
                    runOnUiThread(() -> showError(message));
                }
            } catch (IOException e) {
                runOnUiThread(() -> showError(getString(R.string.surah_error_network)));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void showLoading() {
        // State 1/3: Loading
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.tvSurahName.setText(R.string.surah_loading);
        binding.tvSurahMeta.setText(R.string.surah_meta_placeholder);
    }

    private void showError(String message) {
        // State 2/3: Error
        binding.progressBar.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message);
    }

    private void showContent(SurahPayload payload) {
        // State 3/3: Content
        binding.progressBar.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);

        binding.tvSurahName.setText(payload.name);
        String meta = payload.revelationTypeArabic + " - " + payload.numberOfAyahs + " " + getString(R.string.ayah_word);
        binding.tvSurahMeta.setText(meta);

        binding.llAyahContainer.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        for (AyahItem ayah : payload.ayahs) {
            // Inflate one RTL ayah card per verse and append to the container.
            ItemAyahBinding ayahBinding = ItemAyahBinding.inflate(inflater, binding.llAyahContainer, false);
            ayahBinding.tvAyahText.setText(ayah.text);
            ayahBinding.tvAyahNumber.setText(String.valueOf(ayah.numberInSurah));
            binding.llAyahContainer.addView(ayahBinding.getRoot());
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

            Object dataObject = root.get("data");
            if (dataObject instanceof String) {
                // API can return an error text in data for invalid requests.
                payload.errorMessage = root.optString("data", getString(R.string.surah_error_generic));
                return payload;
            }

            JSONObject data = (JSONObject) dataObject;
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
                    // Remove BOM marker if present in first ayah text.
                    ayah.text = ayahJson.optString("text", "").replace("\uFEFF", "").trim();
                    payload.ayahs.add(ayah);
                }
            }

            return payload;
        } catch (JSONException e) {
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
