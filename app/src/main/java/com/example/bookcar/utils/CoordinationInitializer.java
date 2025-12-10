package com.example.bookcar.utils;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to initialize coordination role and default coordination account
 * Run this once to setup the coordination account
 */
public class CoordinationInitializer {
    private static final String TAG = "CoordinationInitializer";

    // Default coordination account credentials
    private static final String DEFAULT_COORDINATION_EMAIL = "coordination@bookcar.com";
    private static final String DEFAULT_COORDINATION_PASSWORD = "Coordination@2024";
    private static final String DEFAULT_COORDINATION_NAME = "Điều phối viên";
    private static final String DEFAULT_COORDINATION_PHONE = "0000000000";

    public interface InitializationCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    /**
     * Initialize coordination role and create default coordination account
     * Call this method from your app's initialization or setup screen
     */
    public static void initializeCoordination(InitializationCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Step 1: Check if coordination role exists
        db.collection("roles")
                .whereEqualTo("name", "coordination")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Role exists, get its ID
                        String roleId = querySnapshot.getDocuments().get(0).getId();
                        Log.d(TAG, "Coordination role already exists with ID: " + roleId);

                        // Check if coordination account exists
                        checkAndCreateCoordinationAccount(auth, db, roleId, callback);
                    } else {
                        // Create coordination role
                        createCoordinationRole(auth, db, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking coordination role", e);
                    callback.onFailure("Lỗi khi kiểm tra role: " + e.getMessage());
                });
    }

    private static void createCoordinationRole(FirebaseAuth auth, FirebaseFirestore db,
                                               InitializationCallback callback) {
        Map<String, Object> coordinationRole = new HashMap<>();
        coordinationRole.put("name", "coordination");
        coordinationRole.put("permissions", new HashMap<String, Object>() {{
            put("manage_drivers", true);
            put("view_trips", true);
            put("manage_system", true);
        }});

        db.collection("roles")
                .add(coordinationRole)
                .addOnSuccessListener(documentReference -> {
                    String roleId = documentReference.getId();
                    Log.d(TAG, "Coordination role created with ID: " + roleId);

                    // Now create coordination account
                    createCoordinationAccount(auth, db, roleId, callback);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating coordination role", e);
                    callback.onFailure("Lỗi khi tạo role: " + e.getMessage());
                });
    }

    private static void checkAndCreateCoordinationAccount(FirebaseAuth auth, FirebaseFirestore db,
                                                          String roleId, InitializationCallback callback) {
        // Check if any user has coordination role
        db.collection("users")
                .whereEqualTo("role_id", roleId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // No coordination account exists, create one
                        createCoordinationAccount(auth, db, roleId, callback);
                    } else {
                        Log.d(TAG, "Coordination account already exists");
                        callback.onSuccess("Tài khoản điều phối viên đã tồn tại");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking coordination account", e);
                    callback.onFailure("Lỗi khi kiểm tra tài khoản: " + e.getMessage());
                });
    }

    private static void createCoordinationAccount(FirebaseAuth auth, FirebaseFirestore db,
                                                  String roleId, InitializationCallback callback) {
        auth.createUserWithEmailAndPassword(DEFAULT_COORDINATION_EMAIL, DEFAULT_COORDINATION_PASSWORD)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        String userId = auth.getCurrentUser().getUid();

                        // Save coordination user data
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", DEFAULT_COORDINATION_NAME);
                        userData.put("email", DEFAULT_COORDINATION_EMAIL);
                        userData.put("password", DEFAULT_COORDINATION_PASSWORD);
                        userData.put("phone", DEFAULT_COORDINATION_PHONE);
                        userData.put("role_id", roleId);
                        userData.put("created_at", com.google.firebase.Timestamp.now());

                        db.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Coordination account created successfully");
                                    // Sign out after creating the account
                                    auth.signOut();
                                    callback.onSuccess("Tạo tài khoản điều phối viên thành công!\n" +
                                            "Email: " + DEFAULT_COORDINATION_EMAIL + "\n" +
                                            "Password: " + DEFAULT_COORDINATION_PASSWORD);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error saving coordination user", e);
                                    callback.onFailure("Lỗi khi lưu thông tin: " + e.getMessage());
                                });
                    } else {
                        String error = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Error creating coordination account", task.getException());
                        callback.onFailure("Lỗi khi tạo tài khoản: " + error);
                    }
                });
    }
}

