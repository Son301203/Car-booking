package com.example.bookcar.model.roles;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public interface UserRole {
    String getCollectionName();
    Map<String, Object> getDataFromSnapshot(DocumentSnapshot snapshot);
    Map<String, Object> getUpdates(String name, String phone, String dateOfBirth, String gender);
}