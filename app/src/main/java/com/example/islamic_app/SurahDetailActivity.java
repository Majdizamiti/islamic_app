package com.example.islamic_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.islamic_app.databinding.ActivitySurahDetailBinding;
import com.example.islamic_app.databinding.ItemAyahBinding;

public class SurahDetailActivity extends AppCompatActivity {

    // Key used when opening this Activity from another Activity.
    // Example:
    // intent.putExtra(SurahDetailActivity.EXTRA_SURAH_NUMBER, 2);
    public static final String EXTRA_SURAH_NUMBER = "extra_surah_number";

    // ViewBinding object for activity_surah_detail.xml
    // It allows us to access views without findViewById.
    private ActivitySurahDetailBinding binding;

    // The current surah number being displayed.
    // Example: 1 = Al-Fatiha, 2 = Al-Baqarah, etc.
    private int surahNumber;

    // Current font size for ayah text.
    // User can increase/decrease it using zoom buttons.
    private float currentFontSize = 22f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the XML layout using ViewBinding
        binding = ActivitySurahDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the selected surah number sent from the previous screen.
        // If no surah number is sent, default value will be -1.
        surahNumber = getIntent().getIntExtra(EXTRA_SURAH_NUMBER, -1);

        // Back button: closes this screen and returns to previous screen.
        binding.ivBack.setOnClickListener(v -> finish());

        // Retry button: used when API loading fails.
        binding.btnRetry.setOnClickListener(v -> fetchSurah());

        // Next surah button.
        // It only works if we are not already at the last surah, which is 114.
        binding.btnNextSurah.setOnClickListener(v -> {
            if (surahNumber < 114) {
                surahNumber++;
                fetchSurah();
            }
        });

        // Previous surah button.
        // It only works if we are not already at the first surah.
        binding.btnPrevSurah.setOnClickListener(v -> {
            if (surahNumber > 1) {
                surahNumber--;
                fetchSurah();
            }
        });

        // Increase ayah font size.
        // Max size is 40 to avoid making the text too huge.
        binding.ivZoomIn.setOnClickListener(v -> {
            if (currentFontSize < 40) {
                currentFontSize += 2;
                updateFontSize();
            }
        });

        // Decrease ayah font size.
        // Min size is 14 to avoid making the text too small.
        binding.ivZoomOut.setOnClickListener(v -> {
            if (currentFontSize > 14) {
                currentFontSize -= 2;
                updateFontSize();
            }
        });

        // Validate surah number before calling the API.
        // Quran has only 114 surahs.
        if (surahNumber < 1 || surahNumber > 114) {
            showError(getString(R.string.surah_error_invalid_number));
            return;
        }

        // Load the selected surah from the API.
        fetchSurah();
    }

    /**
     * Fetches the current surah using QuranApiClient.
     *
     * Important:
     * QuranApiClient handles:
     * - API request
     * - HTTP connection
     * - JSON parsing
     * - Returning SurahPayload
     *
     * This Activity only handles UI display.
     */
    private void fetchSurah() {
        // Show loading UI while waiting for the API response.
        showLoading();

        QuranApiClient.fetchSurah(surahNumber, new QuranApiClient.SurahCallback() {
            @Override
            public void onSuccess(QuranApiClient.SurahPayload payload) {
                // API callback runs in a background thread.
                // UI must be updated on the main thread.
                runOnUiThread(() -> showContent(payload));
            }

            @Override
            public void onError(String errorMessage) {
                // Show the error message on the UI thread.
                runOnUiThread(() -> showError(errorMessage));
            }
        });
    }

    /**
     * Shows loading state while the surah is being fetched.
     */
    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);

        binding.tvSurahName.setText(R.string.surah_loading);
        binding.tvSurahMeta.setText(R.string.surah_meta_placeholder);
    }

    /**
     * Shows error state when something goes wrong.
     * Example:
     * - Network error
     * - API error
     * - Invalid surah number
     * - JSON parse error
     */
    private void showError(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);

        binding.tvErrorMessage.setText(message);
    }

    /**
     * Displays the surah content after it is successfully fetched.
     */
    private void showContent(QuranApiClient.SurahPayload payload) {
        binding.progressBar.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);
        binding.scrollContent.setVisibility(View.VISIBLE);

        // Display surah name.
        binding.tvSurahName.setText(payload.name);

        // Convert "Meccan" / "Medinan" to Arabic.
        String revelationTypeArabic = toArabicRevelationType(payload.revelationType);

        // Example:
        // مكية - 7 آيات
        String meta = revelationTypeArabic
                + " - "
                + payload.numberOfAyahs
                + " "
                + getString(R.string.ayah_word);

        binding.tvSurahMeta.setText(meta);

        // Clear old ayahs before adding the new surah ayahs.
        // This is important when using next/previous buttons.
        binding.llAyahContainer.removeAllViews();

        LayoutInflater inflater = getLayoutInflater();

        // Add Bismillah header for all surahs except Surah At-Tawbah.
        // Surah 9 does not start with Bismillah.
        if (surahNumber != 9) {
            View bismillahView = inflater.inflate(
                    R.layout.item_bismillah,
                    binding.llAyahContainer,
                    false
            );

            binding.llAyahContainer.addView(bismillahView);
        }

        // Safety check in case API returned no ayahs.
        if (payload.ayahs == null || payload.ayahs.isEmpty()) {
            showError(getString(R.string.surah_error_parse));
            return;
        }

        // Loop through every ayah and add it to the LinearLayout.
        for (QuranApiClient.AyahItem ayah : payload.ayahs) {
            String text = ayah.text;

            // The API may include Bismillah inside the first ayah.
            // Since we already display Bismillah as a separate header,
            // we remove it from the first ayah to avoid duplication.
            if (ayah.numberInSurah == 1 && surahNumber != 9) {
                String b1 = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ";
                String b2 = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ";

                if (text.startsWith(b1)) {
                    text = text.substring(b1.length()).trim();
                } else if (text.startsWith(b2)) {
                    text = text.substring(b2.length()).trim();
                }
            }

            // Only add the ayah if there is text left.
            // This avoids showing an empty card if the first ayah was only Bismillah.
            if (!text.isEmpty()) {
                ItemAyahBinding ayahBinding = ItemAyahBinding.inflate(
                        inflater,
                        binding.llAyahContainer,
                        false
                );

                // Set ayah text.
                ayahBinding.tvAyahText.setText(text);

                // Apply current font size.
                ayahBinding.tvAyahText.setTextSize(currentFontSize);

                // Set ayah number.
                ayahBinding.tvAyahNumber.setText(String.valueOf(ayah.numberInSurah));

                // Add the ayah card to the container.
                binding.llAyahContainer.addView(ayahBinding.getRoot());
            }
        }

        // Make sure font size is applied to all views,
        // including Bismillah and ayah cards.
        updateFontSize();
    }

    /**
     * Updates font size for:
     * - Bismillah text
     * - All ayah text views
     */
    private void updateFontSize() {
        // Display current font size in the UI.
        binding.tvFontSize.setText((int) currentFontSize + "px");

        // Loop through all children inside the ayah container.
        for (int i = 0; i < binding.llAyahContainer.getChildCount(); i++) {
            View child = binding.llAyahContainer.getChildAt(i);

            // If this child is the Bismillah TextView,
            // make it slightly larger than normal ayah text.
            if (child.getId() == R.id.tvBismillah) {
                ((android.widget.TextView) child).setTextSize(currentFontSize + 6);
            } else {
                try {
                    // Try binding this child as an ayah item layout.
                    ItemAyahBinding itemBinding = ItemAyahBinding.bind(child);

                    // Update ayah text size.
                    itemBinding.tvAyahText.setTextSize(currentFontSize);

                } catch (Exception ignored) {
                    // Ignore views that are not item_ayah layouts.
                }
            }
        }
    }

    // Meccan Or Medina
    private String toArabicRevelationType(String revelationType) {
        if ("Meccan".equalsIgnoreCase(revelationType)) {
            return getString(R.string.revelation_meccan_ar);
        }

        if ("Medinan".equalsIgnoreCase(revelationType)) {
            return getString(R.string.revelation_medinan_ar);
        }

        return getString(R.string.revelation_unknown_ar);
    }
}