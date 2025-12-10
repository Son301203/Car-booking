package com.example.bookcar.model;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Role {
    private String roleId;
    private String name; // "driver" or "user"
    private List<String> permissions;

    // Constants for role names
    public static final String ROLE_DRIVER = "driver";
    public static final String ROLE_USER = "user";

    // Empty constructor for Firestore
    public Role() {
    }

    public Role(String roleId, String name, List<String> permissions) {
        this.roleId = roleId;
        this.name = name;
        this.permissions = permissions;
    }

    // Convert Firestore document to Role object
    public static Role fromFirestore(DocumentSnapshot snapshot) {
        Role role = new Role();
        role.roleId = snapshot.getId();
        role.name = snapshot.getString("name");
        role.permissions = (List<String>) snapshot.get("permissions");
        return role;
    }

    // Convert Role object to Map for Firestore
    public Map<String, Object> toFirestore() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("permissions", permissions);
        return data;
    }

    // Getters and Setters
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}

