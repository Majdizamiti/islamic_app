package com.example.islamic_app;

/**
 * Data model for a Surah item in the Quran list.
 */
public class Surah {
    private final String number;
    private final String nameArabic;
    private final String nameEnglish;
    private final String type; // e.g., "مكية" or "مدنية"

    public Surah(String number, String nameArabic, String nameEnglish, String type) {
        this.number = number;
        this.nameArabic = nameArabic;
        this.nameEnglish = nameEnglish;
        this.type = type;
    }

    public String getNumber() { return number; }
    public String getNameArabic() { return nameArabic; }
    public String getNameEnglish() { return nameEnglish; }
    public String getType() { return type; }
}