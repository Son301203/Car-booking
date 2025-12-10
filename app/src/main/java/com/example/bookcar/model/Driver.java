package com.example.bookcar.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Driver {
    private String documentId;
    private String name;
    private String phone;
    private String license;
    private String identification;
    private String dateOfBirth;
    private String email;
    private String gender;
    private String roleId;
    private Timestamp createdAt;

    // Empty constructor for Firestore
    public Driver() {
    }

    // Full constructor
    public Driver(String name, String phone, String license, String identification,
                  String dateOfBirth, String email, String gender, String roleId) {
        this.name = name;
        this.phone = phone;
        this.license = license;
        this.identification = identification;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.gender = gender;
        this.roleId = roleId;
        this.createdAt = Timestamp.now();
    }

    // Convert Firestore document to Driver object
    public static Driver fromFirestore(DocumentSnapshot snapshot) {
        Driver driver = new Driver();
        driver.documentId = snapshot.getId();
        driver.name = snapshot.getString("name");
        driver.phone = snapshot.getString("phone");
        driver.license = snapshot.getString("license");
        driver.identification = snapshot.getString("identification");
        driver.dateOfBirth = snapshot.getString("date_of_birth");
        driver.email = snapshot.getString("email");
        driver.gender = snapshot.getString("gender");
        driver.roleId = snapshot.getString("role_id");
        driver.createdAt = snapshot.getTimestamp("created_at");
        return driver;
    }

    // Convert Driver object to Map for Firestore
    public Map<String, Object> toFirestore() {
        Map<String, Object> data = new HashMap<>();
        if (name != null) data.put("name", name);
        if (phone != null) data.put("phone", phone);
        if (license != null) data.put("license", license);
        if (identification != null) data.put("identification", identification);
        if (dateOfBirth != null) data.put("date_of_birth", dateOfBirth);
        if (email != null) data.put("email", email);
        if (gender != null) data.put("gender", gender);
        if (roleId != null) data.put("role_id", roleId);
        data.put("created_at", createdAt != null ? createdAt : Timestamp.now());
        return data;
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getIdentification() {
        return identification;
    }

    public void setIdentification(String identification) {
        this.identification = identification;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

