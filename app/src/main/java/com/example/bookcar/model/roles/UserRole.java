package com.example.bookcar.model.roles;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

/**
 * Interface for user roles - now all users are in the "users" collection
 * Role is determined by role_id field
 */
public interface UserRole {
    String getCollectionName(); // Always returns "users"
    String getRoleId(); // Returns the role ID (driver or user)
    Map<String, Object> getDataFromSnapshot(DocumentSnapshot snapshot);
    Map<String, Object> getUpdates(String name, String phone, String dateOfBirth, String gender);
}