package com.example.dailycalendar;

public class Event {
    private int id;
    private String title;
    private String description;
    private String date;
    private String filePath; // New field to store the file path

    // Constructor updated to accept filePath as a parameter
    public Event(int id, String title, String description, String date, String filePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.filePath = filePath; // Initialize the file path
    }

    // Getter and setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
