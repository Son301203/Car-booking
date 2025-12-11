package com.example.bookcar.utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {

    private static final String NOTIFICATION_IMAGE = "trip.png";

    /**
     * Send notification to a user about their trip arrangement
     */
    public static void sendUserNotification(String userId, String driverId, String driverName, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, Object> notification = new HashMap<>();
        notification.put("driverId", driverId);
        notification.put("title", "Về chuyến đi");
        notification.put("message", message != null ? message : driverName + " sẽ là tài xế của bạn");
        notification.put("image", NOTIFICATION_IMAGE);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications")
                .document("users")
                .collection("users")
                .document(userId)
                .collection("messages")
                .document(timestamp)
                .set(notification);
    }

    /**
     * Send notification to a driver about their new trip
     */
    public static void sendDriverNotification(String driverId, String tripDate, String tripTime, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, Object> notification = new HashMap<>();
        notification.put("driverId", driverId);
        notification.put("title", "Chuyến đi");
        notification.put("message", message != null ? message : "Bạn sẽ có chuyến đi lúc " + tripTime + ", " + tripDate);
        notification.put("image", NOTIFICATION_IMAGE);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications")
                .document("drivers")
                .collection("drivers")
                .document(driverId)
                .collection("messages")
                .document(timestamp)
                .set(notification);
    }

    /**
     * Send custom notification to a user
     */
    public static void sendCustomUserNotification(String userId, String title, String message, String image) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("image", image != null ? image : NOTIFICATION_IMAGE);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications")
                .document("users")
                .collection("users")
                .document(userId)
                .collection("messages")
                .document(timestamp)
                .set(notification);
    }

    /**
     * Send custom notification to a driver
     */
    public static void sendCustomDriverNotification(String driverId, String title, String message, String image) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String timestamp = String.valueOf(System.currentTimeMillis());

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("image", image != null ? image : NOTIFICATION_IMAGE);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications")
                .document("drivers")
                .collection("drivers")
                .document(driverId)
                .collection("messages")
                .document(timestamp)
                .set(notification);
    }
}

