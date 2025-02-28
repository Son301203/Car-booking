package com.example.bookcar.model.roles;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ClientRole implements UserRole {
    @Override
    public String getCollectionName() {
        return "users";
    }

    @Override
    public Map<String, Object> getDataFromSnapshot(DocumentSnapshot snapshot) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", snapshot.getString("username"));
        data.put("phone", snapshot.getString("phone"));
        data.put("dateOfBirth", snapshot.getString("date of birth"));
        data.put("gender", snapshot.getString("gender"));
        return data;
    }

    @Override
    public Map<String, Object> getUpdates(String name, String phone, String dateOfBirth, String gender) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", name);
        updates.put("phone", phone);
        updates.put("date of birth", dateOfBirth);
        updates.put("gender", gender);
        return updates;
    }
}