package com.example.islamic_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.islamic_app.databinding.ItemSurahBinding;
import com.example.islamic_app.databinding.PageQuranBinding;
import java.util.ArrayList;
import java.util.List;

public class Quranpage extends AppCompatActivity {

    private PageQuranBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PageQuranBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- POPULATE SCROLLVIEW MANUALLY ---
        List<Surah> surahList = getFullSurahList();
        LayoutInflater inflater = getLayoutInflater();

        for (Surah surah : surahList) {
            // Inflate each surah item layout
            ItemSurahBinding itemBinding = ItemSurahBinding.inflate(inflater, binding.llSurahContainer, false);
            
            // Bind data
            itemBinding.tvSurahNumber.setText(surah.getNumber());
            itemBinding.tvSurahArabic.setText(surah.getNameArabic());
            itemBinding.tvSurahEnglish.setText(surah.getNameEnglish());
            itemBinding.tvSurahType.setText(surah.getType());

            // Apply dynamic styling based on Surah Type
            if ("مدنية".equals(surah.getType())) {
                itemBinding.ivSurahStar.setColorFilter(getColor(R.color.medinan_text));
                itemBinding.tvSurahType.setBackgroundTintList(getColorStateList(R.color.medinan_yellow));
                itemBinding.tvSurahType.setTextColor(getColor(R.color.medinan_text));
            } else {
                itemBinding.ivSurahStar.setColorFilter(getColor(R.color.meccan_text));
                itemBinding.tvSurahType.setBackgroundTintList(getColorStateList(R.color.meccan_green));
                itemBinding.tvSurahType.setTextColor(getColor(R.color.meccan_text));
            }

            // Set click listener
            itemBinding.getRoot().setOnClickListener(v -> onSurahClick(surah));
            
            // Add the item to the container
            binding.llSurahContainer.addView(itemBinding.getRoot());
        }

        // Back button
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void onSurahClick(Surah surah) {
        Toast.makeText(this, "Opening " + surah.getNameArabic(), Toast.LENGTH_SHORT).show();
    }

    private List<Surah> getFullSurahList() {
        List<Surah> list = new ArrayList<>();
        list.add(new Surah("1", "الفاتحة", "Al-Fatihah • 1 آية", "مكية"));
        list.add(new Surah("2", "البقرة", "Al-Baqarah • 1 آية", "مدنية"));
        list.add(new Surah("3", "آل عمران", "Ali 'Imran • 1 آية", "مدنية"));
        list.add(new Surah("4", "النساء", "An-Nisa • 1 آية", "مدنية"));
        list.add(new Surah("5", "المائدة", "Al-Ma'idah • 1 آية", "مدنية"));
        list.add(new Surah("6", "الأنعام", "Al-An'am • 1 آية", "مكية"));
        list.add(new Surah("7", "الأعراف", "Al-A'raf • 1 آية", "مكية"));
        list.add(new Surah("8", "الأنفال", "Al-Anfal • 1 آية", "مدنية"));
        list.add(new Surah("9", "التوبة", "At-Tawbah • 1 آية", "مدنية"));
        list.add(new Surah("10", "يونس", "Yunus • 1 آية", "مكية"));
        list.add(new Surah("11", "هود", "Hud • 1 آية", "مكية"));
        list.add(new Surah("12", "يوسف", "Yusuf • 1 آية", "مكية"));
        list.add(new Surah("13", "الرعد", "Ar-Ra'd • 1 آية", "مدنية"));
        list.add(new Surah("14", "إبراهيم", "Ibrahim • 1 آية", "مكية"));
        list.add(new Surah("15", "الحجر", "Al-Hijr • 1 آية", "مكية"));
        list.add(new Surah("16", "النحل", "An-Nahl • 1 آية", "مكية"));
        list.add(new Surah("17", "الإسراء", "Al-Isra • 1 آية", "مكية"));
        list.add(new Surah("18", "الكهف", "Al-Kahf • 1 آية", "مكية"));
        list.add(new Surah("19", "مريم", "Maryam • 1 آية", "مكية"));
        list.add(new Surah("20", "طه", "Ta-Ha • 1 آية", "مكية"));
        list.add(new Surah("21", "الأنبياء", "Al-Anbiya • 1 آية", "مكية"));
        list.add(new Surah("22", "الحج", "Al-Hajj • 1 آية", "مدنية"));
        list.add(new Surah("23", "المؤمنون", "Al-Mu'minun • 1 آية", "مكية"));
        list.add(new Surah("24", "النور", "An-Nur • 1 آية", "مدنية"));
        list.add(new Surah("25", "الفرقان", "Al-Furqan • 1 آية", "مكية"));
        list.add(new Surah("26", "الشعراء", "Ash-Shu'ara • 1 آية", "مكية"));
        list.add(new Surah("27", "النمل", "An-Naml • 1 آية", "مكية"));
        list.add(new Surah("28", "القصص", "Al-Qasas • 1 آية", "مكية"));
        list.add(new Surah("29", "العنكبوت", "Al-Ankabut • 1 آية", "مكية"));
        list.add(new Surah("30", "الروم", "Ar-Rum • 1 آية", "مكية"));
        list.add(new Surah("31", "لقمان", "Luqman • 1 آية", "مكية"));
        list.add(new Surah("32", "السجدة", "As-Sajdah • 1 آية", "مكية"));
        list.add(new Surah("33", "الأحزاب", "Al-Ahzab • 1 آية", "مدنية"));
        list.add(new Surah("34", "سبأ", "Saba • 1 آية", "مكية"));
        list.add(new Surah("35", "فاطر", "Fatir • 1 آية", "مكية"));
        list.add(new Surah("36", "يس", "Ya-Sin • 1 آية", "مكية"));
        list.add(new Surah("37", "الصافات", "As-Saffat • 1 آية", "مكية"));
        list.add(new Surah("38", "ص", "Sad • 1 آية", "مكية"));
        list.add(new Surah("39", "الزمر", "Az-Zumar • 1 آية", "مكية"));
        list.add(new Surah("40", "غافر", "Ghafir • 1 آية", "مكية"));
        list.add(new Surah("41", "فصلت", "Fussilat • 1 آية", "مكية"));
        list.add(new Surah("42", "الشورى", "Ash-Shura • 1 آية", "مكية"));
        list.add(new Surah("43", "الزخرف", "Az-Zukhruf • 1 آية", "مكية"));
        list.add(new Surah("44", "الدخان", "Ad-Dukhan • 1 آية", "مكية"));
        list.add(new Surah("45", "الجاثية", "Al-Jathiyah • 1 آية", "مكية"));
        list.add(new Surah("46", "الأحقاف", "Al-Ahqaf • 1 آية", "مكية"));
        list.add(new Surah("47", "محمد", "Muhammad • 1 آية", "مدنية"));
        list.add(new Surah("48", "الفتح", "Al-Fath • 1 آية", "مدنية"));
        list.add(new Surah("49", "الحجرات", "Al-Hujurat • 1 آية", "مدنية"));
        list.add(new Surah("50", "ق", "Qaf • 1 آية", "مكية"));
        list.add(new Surah("51", "الذاريات", "Adh-Dhariyat • 1 آية", "مكية"));
        list.add(new Surah("52", "الطور", "At-Tur • 1 آية", "مكية"));
        list.add(new Surah("53", "النجم", "An-Najm • 1 آية", "مكية"));
        list.add(new Surah("54", "القمر", "Al-Qamar • 1 آية", "مكية"));
        list.add(new Surah("55", "الرحمن", "Ar-Rahman • 1 آية", "مدنية"));
        list.add(new Surah("56", "الواقعة", "Al-Waqi'ah • 1 آية", "مكية"));
        list.add(new Surah("57", "الحديد", "Al-Hadid • 1 آية", "مدنية"));
        list.add(new Surah("58", "المجادلة", "Al-Mujadilah • 1 آية", "مدنية"));
        list.add(new Surah("59", "الحشر", "Al-Hashr • 1 آية", "مدنية"));
        list.add(new Surah("60", "الممتحنة", "Al-Mumtahanah • 1 آية", "مدنية"));
        list.add(new Surah("61", "الصف", "As-Saff • 1 آية", "مدنية"));
        list.add(new Surah("62", "الجمعة", "Al-Jumu'ah • 1 آية", "مدنية"));
        list.add(new Surah("63", "المنافقون", "Al-Munafiqun • 1 آية", "مدنية"));
        list.add(new Surah("64", "التغابن", "At-Taghabun • 1 آية", "مدنية"));
        list.add(new Surah("65", "الطلاق", "At-Talaq • 1 آية", "مدنية"));
        list.add(new Surah("66", "التحريم", "At-Tahrim • 1 آية", "مدنية"));
        list.add(new Surah("67", "الملك", "Al-Mulk • 1 آية", "مكية"));
        list.add(new Surah("68", "القلم", "Al-Qalam • 1 آية", "مكية"));
        list.add(new Surah("69", "الحاقة", "Al-Haqqah • 1 آية", "مكية"));
        list.add(new Surah("70", "المعارج", "Al-Ma'arij • 1 آية", "مكية"));
        list.add(new Surah("71", "نوح", "Nuh • 1 آية", "مكية"));
        list.add(new Surah("72", "الجن", "Al-Jinn • 1 آية", "مكية"));
        list.add(new Surah("73", "المزمل", "Al-Muzzammil • 1 آية", "مكية"));
        list.add(new Surah("74", "المدثر", "Al-Muddaththir • 1 آية", "مكية"));
        list.add(new Surah("75", "القيامة", "Al-Qiyamah • 1 آية", "مكية"));
        list.add(new Surah("76", "الإنسان", "Al-Insan • 1 آية", "مدنية"));
        list.add(new Surah("77", "المرسلات", "Al-Mursalat • 1 آية", "مكية"));
        list.add(new Surah("78", "النبأ", "An-Naba • 1 آية", "مكية"));
        list.add(new Surah("79", "النازعات", "An-Nazi'at • 1 آية", "مكية"));
        list.add(new Surah("80", "عبس", "Abasa • 1 آية", "مكية"));
        list.add(new Surah("81", "التكوير", "At-Takwir • 1 آية", "مكية"));
        list.add(new Surah("82", "الانفطار", "Al-Infitar • 1 آية", "مكية"));
        list.add(new Surah("83", "المطففين", "Al-Mutaffifin • 1 آية", "مكية"));
        list.add(new Surah("84", "الانشقاق", "Al-Inshiqaq • 1 آية", "مكية"));
        list.add(new Surah("85", "البروج", "Al-Buruj • 1 آية", "مكية"));
        list.add(new Surah("86", "الطارق", "At-Tariq • 1 آية", "مكية"));
        list.add(new Surah("87", "الأعلى", "Al-A'la • 1 آية", "مكية"));
        list.add(new Surah("88", "الغاشية", "Al-Ghashiyah • 1 آية", "مكية"));
        list.add(new Surah("89", "الفجر", "Al-Fajr • 1 آية", "مكية"));
        list.add(new Surah("90", "البلد", "Al-Balad • 1 آية", "مكية"));
        list.add(new Surah("91", "الشمس", "Ash-Shams • 1 آية", "مكية"));
        list.add(new Surah("92", "الليل", "Al-Layl • 1 آية", "مكية"));
        list.add(new Surah("93", "الضحى", "Ad-Duha • 1 آية", "مكية"));
        list.add(new Surah("94", "الشرح", "Ash-Sharh • 1 آية", "مكية"));
        list.add(new Surah("95", "التين", "At-Tin • 1 آية", "مكية"));
        list.add(new Surah("96", "العلق", "Al-Alaq • 1 آية", "مكية"));
        list.add(new Surah("97", "القدر", "Al-Qadr • 1 آية", "مكية"));
        list.add(new Surah("98", "البينة", "Al-Bayyinah • 1 آية", "مدنية"));
        list.add(new Surah("99", "الزلزلة", "Az-Zalzalah • 1 آية", "مدنية"));
        list.add(new Surah("100", "العاديات", "Al-Adiyat • 1 آية", "مكية"));
        list.add(new Surah("101", "القارعة", "Al-Qari'ah • 1 آية", "مكية"));
        list.add(new Surah("102", "التكاثر", "At-Takathur • 1 آية", "مكية"));
        list.add(new Surah("103", "العصر", "Al-Asr • 1 آية", "مكية"));
        list.add(new Surah("104", "الهمزة", "Al-Humazah • 1 آية", "مكية"));
        list.add(new Surah("105", "الفيل", "Al-Fil • 1 آية", "مكية"));
        list.add(new Surah("106", "قريش", "Quraysh • 1 آية", "مكية"));
        list.add(new Surah("107", "الماعون", "Al-Ma'un • 1 آية", "مكية"));
        list.add(new Surah("108", "الكوثر", "Al-Kauthar • 1 آية", "مكية"));
        list.add(new Surah("109", "الكافرون", "Al-Kafirun • 1 آية", "مكية"));
        list.add(new Surah("110", "النصر", "An-Nasr • 1 آية", "مدنية"));
        list.add(new Surah("111", "المسد", "Al-Masad • 1 آية", "مكية"));
        list.add(new Surah("112", "الإخلاص", "Al-Ikhlas • 1 آية", "مكية"));
        list.add(new Surah("113", "الفلق", "Al-Falaq • 1 آية", "مكية"));
        list.add(new Surah("114", "الناس", "An-Nas • 1 آية", "مكية"));
        return list;
    }
}