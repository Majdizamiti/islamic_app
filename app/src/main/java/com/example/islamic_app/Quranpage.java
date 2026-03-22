package com.example.islamic_app;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.islamic_app.databinding.PageQuranBinding;
import java.util.ArrayList;
import java.util.List;

public class Quranpage extends AppCompatActivity implements SurahAdapter.OnSurahClickListener {

    private PageQuranBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PageQuranBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup RecyclerView
        binding.rvSurahs.setLayoutManager(new LinearLayoutManager(this));

        List<Surah> surahList = new ArrayList<>();
        surahList.add(new Surah("1", "الفاتحة", "Al-Fatihah • 1 آية", "مكية"));
        surahList.add(new Surah("2", "البقرة", "Al-Baqarah • 1 آية", "مدنية"));
        surahList.add(new Surah("3", "آل عمران", "Ali 'Imran • 1 آية", "مدنية"));
        surahList.add(new Surah("4", "النساء", "An-Nisa • 1 آية", "مدنية"));
        surahList.add(new Surah("4", "النساء", "An-Nisa • 1 آية", "مدنية"));

        SurahAdapter adapter = new SurahAdapter(surahList, this);
        binding.rvSurahs.setAdapter(adapter);

        // Back button
        binding.ivBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onSurahClick(Surah surah) {
        Toast.makeText(this, "Opening " + surah.getNameArabic(), Toast.LENGTH_SHORT).show();
    }
}