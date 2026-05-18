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

    /*
     * Callback used when we fetch a full surah.
     *
     * Because the API request runs in a background thread,
     * we cannot directly return the result like a normal method.
     *
     * So instead, we use a callback:
     * - onSuccess() when the API works
     * - onError() when something fails
     */
    public interface SurahCallback {
        void onSuccess(SurahPayload payload);
        void onError(String errorMessage);
    }

    /*
     * Callback used when we fetch one random ayah.
     *
     * It returns:
     * - the surah information
     * - the selected random ayah
     */
    public interface RandomAyahCallback {
        void onSuccess(SurahPayload surah, AyahItem ayah);
        void onError(String errorMessage);
    }

    /*
     * Fetches a surah from the Qurani API.
     *
     * Example:
     * fetchSurah(1, callback)
     *
     * This will call:
     * https://api.qurani.ai/gw/qh/v1/surah/1/quran-uthmani
     */
    public static void fetchSurah(int surahNumber, SurahCallback callback) {

        /*
         * Network requests cannot run on the main UI thread in Android.
         * So we create a new background thread.
         */
        new Thread(() -> {
            HttpURLConnection connection = null;

            try {
                /*
                 * Build the API URL.
                 *
                 * surahNumber changes depending on which surah we want.
                 * quran-uthmani means we request the Uthmani script version.
                 */
                URL url = new URL("https://api.qurani.ai/gw/qh/v1/surah/"
                        + surahNumber + "/quran-uthmani");

                /*
                 * Open HTTP connection using Java's built-in HttpURLConnection.
                 */
                connection = (HttpURLConnection) url.openConnection();

                /*
                 * We are making a GET request.
                 * GET means we only want to read/fetch data, not send or update data.
                 */
                connection.setRequestMethod("GET");

                /*
                 * Timeout settings:
                 * - connect timeout: max time to establish connection
                 * - read timeout: max time to wait for response data
                 */
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                /*
                 * Get HTTP response code.
                 *
                 * 200-299 usually means success.
                 * Example:
                 * 200 = OK
                 * 404 = Not Found
                 * 500 = Server Error
                 */
                int responseCode = connection.getResponseCode();

                /*
                 * If request is successful, read from input stream.
                 * If request failed, read from error stream.
                 */
                InputStream stream = responseCode >= 200 && responseCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                /*
                 * If both streams are null, we cannot read any response.
                 */
                if (stream == null) {
                    callback.onError("Network error");
                    return;
                }

                /*
                 * Convert the InputStream response into a String.
                 * The API returns JSON text.
                 */
                String body = readStream(stream);

                /*
                 * Parse JSON body into our custom Java object SurahPayload.
                 */
                SurahPayload payload = parseResponse(body);

                /*
                 * If parsing failed, return parse error.
                 */
                if (payload == null) {
                    callback.onError("Parse error");
                    return;
                }

                /*
                 * If HTTP response is successful and ayahs exist,
                 * send the result to the caller using onSuccess().
                 */
                if (responseCode >= 200 && responseCode < 300
                        && payload.ayahs != null
                        && !payload.ayahs.isEmpty()) {

                    callback.onSuccess(payload);

                } else {
                    /*
                     * If something went wrong, send the error message.
                     */
                    callback.onError(
                            payload.errorMessage != null
                                    ? payload.errorMessage
                                    : "Something went wrong"
                    );
                }

            } catch (IOException e) {
                /*
                 * IOException can happen because of:
                 * - no internet
                 * - API server down
                 * - timeout
                 * - invalid URL
                 */
                Log.e("QuranApiClient", "Network error", e);
                callback.onError("Network error");

            } finally {
                /*
                 * Always close/disconnect the connection to avoid leaks.
                 */
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    /*
     * Fetches a random ayah.
     *
     * Logic:
     * 1. Choose random surah number from 1 to 114.
     * 2. Fetch that full surah using fetchSurah().
     * 3. Choose random ayah from the returned ayah list.
     * 4. Return surah + ayah using callback.
     */
    public static void fetchRandomAyah(RandomAyahCallback callback) {
        Random random = new Random();

        /*
         * random.nextInt(114) gives numbers from 0 to 113.
         * We add +1 to make it from 1 to 114.
         */
        int randomSurahNumber = random.nextInt(114) + 1;

        /*
         * Reuse fetchSurah() instead of repeating API logic.
         * This follows DRY principle: Don't Repeat Yourself.
         */
        fetchSurah(randomSurahNumber, new SurahCallback() {
            @Override
            public void onSuccess(SurahPayload payload) {

                /*
                 * Safety check: make sure the surah has ayahs.
                 */
                if (payload.ayahs == null || payload.ayahs.isEmpty()) {
                    callback.onError("No ayahs found");
                    return;
                }

                /*
                 * Choose random ayah from this surah.
                 *
                 * If the surah has 7 ayahs:
                 * random.nextInt(7) gives index from 0 to 6.
                 */
                int randomAyahIndex = random.nextInt(payload.ayahs.size());

                /*
                 * Get the random ayah object from the list.
                 */
                AyahItem randomAyah = payload.ayahs.get(randomAyahIndex);

                /*
                 * Return both:
                 * - full surah data
                 * - selected random ayah
                 */
                callback.onSuccess(payload, randomAyah);
            }

            @Override
            public void onError(String errorMessage) {
                /*
                 * If fetchSurah failed, forward the error.
                 */
                callback.onError(errorMessage);
            }
        });
    }

    /*
     * Parses the JSON response from the API.
     *
     * The API response contains a "data" object.
     * Inside "data", we extract:
     * - surah name
     * - revelation type
     * - number of ayahs
     * - ayahs array
     */
    private static SurahPayload parseResponse(String body) {
        try {
            /*
             * Convert raw JSON string into JSONObject.
             */
            JSONObject root = new JSONObject(body);

            /*
             * Create our custom payload object.
             */
            SurahPayload payload = new SurahPayload();

            /*
             * If response does not have "data",
             * we consider it an invalid response.
             */
            if (!root.has("data")) {
                payload.errorMessage = "Something went wrong";
                return payload;
            }

            /*
             * Get the main data object.
             */
            JSONObject data = root.getJSONObject("data");

            /*
             * Extract surah information.
             *
             * optString and optInt are safer than getString/getInt
             * because they do not crash if the field is missing.
             */
            payload.name = data.optString("name", "Surah");
            payload.revelationType = data.optString("revelationType", "");
            payload.numberOfAyahs = data.optInt("numberOfAyahs", 0);

            /*
             * Extract the ayahs array.
             */
            JSONArray ayahsJson = data.optJSONArray("ayahs");

            /*
             * Initialize the ayahs list.
             */
            payload.ayahs = new ArrayList<>();

            /*
             * Convert each JSON ayah into an AyahItem Java object.
             */
            if (ayahsJson != null) {
                for (int i = 0; i < ayahsJson.length(); i++) {
                    JSONObject ayahJson = ayahsJson.getJSONObject(i);

                    AyahItem ayah = new AyahItem();

                    /*
                     * numberInSurah = ayah number inside the surah.
                     * text = actual ayah text.
                     */
                    ayah.numberInSurah = ayahJson.optInt("numberInSurah", i + 1);
                    ayah.text = ayahJson.optString("text", "").trim();

                    /*
                     * Add ayah to the list.
                     */
                    payload.ayahs.add(ayah);
                }
            }

            /*
             * Return the parsed surah payload.
             */
            return payload;

        } catch (JSONException e) {
            /*
             * JSONException happens if the response is not valid JSON
             * or the structure is different from what we expect.
             */
            Log.e("QuranApiClient", "Parse error", e);
            return null;
        }
    }

    /*
     * Converts InputStream into String.
     *
     * The API response comes as a stream of bytes.
     * We read it line by line and build one String.
     */
    private static String readStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();

        /*
         * Use UTF-8 because Quran Arabic text needs correct encoding.
         *
         * try-with-resources automatically closes the reader after finishing.
         */
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;

            /*
             * Read each line until there are no more lines.
             */
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }

        /*
         * Return the full API response as a string.
         */
        return builder.toString();
    }

    /*
     * Model class representing one ayah.
     *
     * Example:
     * numberInSurah = 1
     * text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
     */
    public static class AyahItem {
        public int numberInSurah;
        public String text;
    }

    /*
     * Model class representing one surah response.
     *
     * It contains:
     * - name of the surah
     * - revelation type
     * - number of ayahs
     * - list of ayahs
     * - error message if something went wrong
     */
    public static class SurahPayload {
        public String name;
        public String revelationType;
        public int numberOfAyahs;
        public List<AyahItem> ayahs;
        public String errorMessage;
    }
}