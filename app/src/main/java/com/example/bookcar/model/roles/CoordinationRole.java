package com.example.bookcar.model.roles;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Coordination role implementation - uses unified "users" collection
 */
public class CoordinationRole implements UserRole {
    private static final String ROLE_ID = "coordination"; // Role ID for coordination

    @Override
    public String getCollectionName() {
        return "users"; // All users in same collection
    }

    @Override
    public String getRoleId() {
        return ROLE_ID;
    }

    @Override
    public Map<String, Object> getDataFromSnapshot(DocumentSnapshot snapshot) {
        Map<String, Object> data = new HashMap<>();
        // Support both old and new field names
        String name = snapshot.getString("name");
        if (name == null) {
            name = snapshot.getString("username");
        }
        data.put("name", name);
        data.put("phone", snapshot.getString("phone"));

        String dateOfBirth = snapshot.getString("date_of_birth");
        if (dateOfBirth == null) {
            dateOfBirth = snapshot.getString("date of birth");
        }
        data.put("dateOfBirth", dateOfBirth);
        data.put("gender", snapshot.getString("gender"));
        return data;
    }

    @Override
    public Map<String, Object> getUpdates(String name, String phone, String dateOfBirth, String gender) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("date_of_birth", dateOfBirth);
        updates.put("gender", gender);
        updates.put("role_id", ROLE_ID); // Ensure role_id is set
        return updates;
    }
}

