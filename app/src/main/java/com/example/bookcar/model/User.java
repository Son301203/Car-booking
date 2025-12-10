package com.example.bookcar.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String roleId;
    private String dateOfBirth;
    private String gender;
    private Timestamp createdAt;

    // Empty constructor for Firestore
    public User() {
    }

    public User(String userId, String name, String email, String phone, String roleId) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.roleId = roleId;
        this.createdAt = Timestamp.now();
    }

    // Convert Firestore document to User object
    public static User fromFirestore(DocumentSnapshot snapshot) {
        User user = new User();
        user.userId = snapshot.getId();
        user.name = snapshot.getString("name");
        user.email = snapshot.getString("email");
        user.phone = snapshot.getString("phone");
        user.roleId = snapshot.getString("role_id");
        user.dateOfBirth = snapshot.getString("date_of_birth");
        user.gender = snapshot.getString("gender");
        user.createdAt = snapshot.getTimestamp("created_at");
        return user;
    }

    // Convert User object to Map for Firestore
    public Map<String, Object> toFirestore() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("phone", phone);
        data.put("role_id", roleId);
        data.put("date_of_birth", dateOfBirth);
        data.put("gender", gender);
        data.put("created_at", createdAt);
        return data;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

