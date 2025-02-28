package com.example.bookcar.model.roles;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class DriverRole implements UserRole {
    @Override
    public String getCollectionName() {
        return "drivers";
    }

    @Override
    public Map<String, Object> getDataFromSnapshot(DocumentSnapshot snapshot) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", snapshot.getString("name"));
        data.put("phone", snapshot.getString("phone"));
        data.put("dateOfBirth", snapshot.getString("date_of_birth"));
        data.put("gender", snapshot.getString("gender"));
        return data;
    }

    @Override
    public Map<String, Object> getUpdates(String name, String phone, String dateOfBirth, String gender) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("date_of_birth", dateOfBirth);
        updates.put("gender", gender.toLowerCase());
        return updates;
    }
}