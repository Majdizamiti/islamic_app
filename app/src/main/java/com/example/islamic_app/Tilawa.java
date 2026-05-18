package com.example.islamic_app;

public class Tilawa {
    public String id;
    public String title;
    public String reciter;
    public String url;
    public String description;
    public long timestamp;

    public Tilawa() {
        // Default constructor required for calls to DataSnapshot.getValue(Tilawa.class)
    }

    public Tilawa(String id, String title, String reciter, String url, String description, long timestamp) {
        this.id = id;
        this.title = title;
        this.reciter = reciter;
        this.url = url;
        this.description = description;
        this.timestamp = timestamp;
    }
}
