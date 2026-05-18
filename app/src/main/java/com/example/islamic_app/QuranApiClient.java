package com.example.islamic_app;

import android.util.Log;

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
import java.util.Random;

public class QuranApiClient {

    public interface SurahCallback {
        void onSuccess(SurahPayload payload);
        void onError(String errorMessage);
    }

    public interface RandomAyahCallback {
        void onSuccess(SurahPayload surah, AyahItem ayah);
        void onError(String errorMessage);
    }

    public static void fetchSurah(int surahNumber, SurahCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL("https://api.qurani.ai/gw/qh/v1/surah/"
                        + surahNumber + "/quran-uthmani");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();

                InputStream stream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                if (stream == null) {
                    callback.onError("Network error");
                    return;
                }

                String body = readStream(stream);
                SurahPayload payload = parseResponse(body);

                if (payload == null) {
                    callback.onError("Parse error");
                    return;
                }

                if (responseCode >= 200 && responseCode < 300
                        && payload.ayahs != null
                        && !payload.ayahs.isEmpty()) {
                    callback.onSuccess(payload);
                } else {
                    callback.onError(
                            payload.errorMessage != null
                                    ? payload.errorMessage
                                    : "Something went wrong"
                    );
                }

            } catch (IOException e) {
                Log.e("QuranApiClient", "Network error", e);
                callback.onError("Network error");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    public static void fetchRandomAyah(RandomAyahCallback callback) {
        Random random = new Random();

        int randomSurahNumber = random.nextInt(114) + 1;

        fetchSurah(randomSurahNumber, new SurahCallback() {
            @Override
            public void onSuccess(SurahPayload payload) {
                if (payload.ayahs == null || payload.ayahs.isEmpty()) {
                    callback.onError("No ayahs found");
                    return;
                }

                int randomAyahIndex = random.nextInt(payload.ayahs.size());
                AyahItem randomAyah = payload.ayahs.get(randomAyahIndex);

                callback.onSuccess(payload, randomAyah);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    private static SurahPayload parseResponse(String body) {
        try {
            JSONObject root = new JSONObject(body);
            SurahPayload payload = new SurahPayload();

            if (!root.has("data")) {
                payload.errorMessage = "Something went wrong";
                return payload;
            }

            JSONObject data = root.getJSONObject("data");

            payload.name = data.optString("name", "Surah");
            payload.revelationType = data.optString("revelationType", "");
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
            Log.e("QuranApiClient", "Parse error", e);
            return null;
        }
    }

    private static String readStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }

        return builder.toString();
    }

    public static class AyahItem {
        public int numberInSurah;
        public String text;
    }

    public static class SurahPayload {
        public String name;
        public String revelationType;
        public int numberOfAyahs;
        public List<AyahItem> ayahs;
        public String errorMessage;
    }
}