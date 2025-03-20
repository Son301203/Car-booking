package com.example.bookcar.model;

import com.example.bookcar.R;

public class Notification {
    private String title;
    private String message;
    private String image;
    private boolean isRead;

    public Notification(String title, String message, String image, boolean isRead) {
        this.title = title;
        this.message = message;
        this.image = image;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getImageResource() {
        switch (image) {
            case "trip.png":
                return R.drawable.trip;
            case "completed_trip.png":
                return R.drawable.completed_trip;
            default:
                return R.drawable.baseline_notifications_none_24;
        }
    }

    public boolean isRead() {
        return isRead;
    }
}
