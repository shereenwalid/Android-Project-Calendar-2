package com.example.dailycalendar;

public class CarouselItem {
    private String title;
    private int imageResId; // For drawable resources
    // Optionally: private String imageUrl; // For image URLs

    public CarouselItem(String title, int imageResId) {
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }
}