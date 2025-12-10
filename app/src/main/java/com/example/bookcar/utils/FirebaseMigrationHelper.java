package com.example.bookcar.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class to migrate data from old Firebase structure to new structure
 *
 * OLD STRUCTURE:
 * - drivers/{driverId}
 * - users/{userId}
 * - drivers/{driverId}/trips/{tripId}
 * - users/{userId}/orders/{orderId}
 *
 * NEW STRUCTURE:
 * - users/{userId} (unified - includes role_id field)
 * - roles/{roleId}
 * - trips/{tripId} (at root level with driver_id reference)
 * - orders/{orderId} (at root level with client_id and trip_id references)
 */
public class FirebaseMigrationHelper {
    private static final String TAG = "FirebaseMigration";
    private final FirebaseFirestore db;

    public FirebaseMigrationHelper() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Main migration method - call this to migrate all data
     */
    public void migrateAllData(MigrationCallback callback) {
        Log.d(TAG, "Starting migration...");

        // Step 1: Create role documents
        createRoles()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Roles created successfully");

                // Step 2: Migrate drivers to unified users collection
                migrateDriversToUsers()
                    .addOnSuccessListener(aVoid2 -> {
                        Log.d(TAG, "Drivers migrated successfully");

                        // Step 3: Update existing users with role_id
                        updateExistingUsersWithRoleId()
                            .addOnSuccessListener(aVoid3 -> {
                                Log.d(TAG, "Users updated successfully");

                                // Step 4: Migrate trips to root level
                                migrateTripsToRoot()
                                    .addOnSuccessListener(aVoid4 -> {
                                        Log.d(TAG, "Trips migrated successfully");

                                        // Step 5: Migrate orders to root level
                                        migrateOrdersToRoot()
                                            .addOnSuccessListener(aVoid5 -> {
                                                Log.d(TAG, "Migration completed successfully!");
                                                callback.onSuccess();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Failed to migrate orders", e);
                                                callback.onFailure(e);
                                            });
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to migrate trips", e);
                                        callback.onFailure(e);
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to update users", e);
                                callback.onFailure(e);
                            });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to migrate drivers", e);
                        callback.onFailure(e);
                    });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to create roles", e);
                callback.onFailure(e);
            });
    }

    /**
     * Step 1: Create role documents
     */
    private Task<Void> createRoles() {
        WriteBatch batch = db.batch();

        // Driver role
        Map<String, Object> driverRole = new HashMap<>();
        driverRole.put("name", "driver");
        driverRole.put("permissions", new ArrayList<String>());
        batch.set(db.collection("roles").document("driver"), driverRole);

        // User/Client role
        Map<String, Object> userRole = new HashMap<>();
        userRole.put("name", "user");
        userRole.put("permissions", new ArrayList<String>());
        batch.set(db.collection("roles").document("user"), userRole);

        return batch.commit();
    }

    /**
     * Step 2: Migrate drivers from drivers collection to users collection
     */
    private Task<Void> migrateDriversToUsers() {
        return db.collection("drivers").get()
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                WriteBatch batch = db.batch();
                int count = 0;

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String driverId = document.getId();
                    Map<String, Object> driverData = document.getData();

                    // Add role_id field
                    driverData.put("role_id", "driver");

                    // Ensure consistent field names
                    if (driverData.containsKey("date_of_birth")) {
                        // Already correct
                    } else if (driverData.containsKey("dateOfBirth")) {
                        driverData.put("date_of_birth", driverData.get("dateOfBirth"));
                        driverData.remove("dateOfBirth");
                    }

                    batch.set(db.collection("users").document(driverId), driverData);
                    count++;

                    // Firestore batch limit is 500 operations
                    if (count % 500 == 0) {
                        batch.commit();
                        batch = db.batch();
                    }
                }

                return batch.commit();
            });
    }

    /**
     * Step 3: Update existing users in users collection with role_id
     */
    private Task<Void> updateExistingUsersWithRoleId() {
        return db.collection("users").get()
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                WriteBatch batch = db.batch();
                int count = 0;

                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Only update if role_id doesn't exist
                    if (!document.contains("role_id")) {
                        String userId = document.getId();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("role_id", "user");

                        // Standardize field names
                        if (document.contains("username") && !document.contains("name")) {
                            updates.put("name", document.getString("username"));
                        }
                        if (document.contains("date of birth") && !document.contains("date_of_birth")) {
                            updates.put("date_of_birth", document.getString("date of birth"));
                        }

                        batch.update(db.collection("users").document(userId), updates);
                        count++;

                        if (count % 500 == 0) {
                            batch.commit();
                            batch = db.batch();
                        }
                    }
                }

                return batch.commit();
            });
    }

    /**
     * Step 4: Migrate trips from nested collection to root level
     */
    private Task<Void> migrateTripsToRoot() {
        return db.collection("users").whereEqualTo("role_id", "driver").get()
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                List<Task<Void>> allTasks = new ArrayList<>();

                for (QueryDocumentSnapshot driverDoc : task.getResult()) {
                    String driverId = driverDoc.getId();

                    // Also check old drivers collection
                    Task<Void> migrateTask = db.collection("drivers")
                        .document(driverId)
                        .collection("trips")
                        .get()
                        .continueWithTask(tripsTask -> {
                            if (!tripsTask.isSuccessful()) {
                                return Tasks.forResult(null);
                            }

                            WriteBatch batch = db.batch();
                            int count = 0;

                            for (QueryDocumentSnapshot tripDoc : tripsTask.getResult()) {
                                Map<String, Object> tripData = tripDoc.getData();
                                tripData.put("driver_id", driverId);

                                // Add status if not exists
                                if (!tripData.containsKey("status")) {
                                    tripData.put("status", "pending");
                                }

                                batch.set(db.collection("trips").document(tripDoc.getId()), tripData);
                                count++;

                                if (count % 500 == 0) {
                                    batch.commit();
                                    batch = db.batch();
                                }
                            }

                            return batch.commit();
                        });

                    allTasks.add(migrateTask);
                }

                return Tasks.whenAll(allTasks);
            });
    }

    /**
     * Step 5: Migrate orders from nested collection to root level
     */
    private Task<Void> migrateOrdersToRoot() {
        return db.collection("users").whereEqualTo("role_id", "user").get()
            .continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                List<Task<Void>> allTasks = new ArrayList<>();

                for (QueryDocumentSnapshot userDoc : task.getResult()) {
                    String userId = userDoc.getId();

                    Task<Void> migrateTask = db.collection("users")
                        .document(userId)
                        .collection("orders")
                        .get()
                        .continueWithTask(ordersTask -> {
                            if (!ordersTask.isSuccessful()) {
                                return Tasks.forResult(null);
                            }

                            WriteBatch batch = db.batch();
                            int count = 0;

                            for (QueryDocumentSnapshot orderDoc : ordersTask.getResult()) {
                                Map<String, Object> orderData = orderDoc.getData();
                                orderData.put("client_id", userId);

                                // Convert coordinate subcollections to GeoPoint if needed
                                // This would require additional subcollection queries

                                batch.set(db.collection("orders").document(orderDoc.getId()), orderData);
                                count++;

                                if (count % 500 == 0) {
                                    batch.commit();
                                    batch = db.batch();
                                }
                            }

                            return batch.commit();
                        });

                    allTasks.add(migrateTask);
                }

                return Tasks.whenAll(allTasks);
            });
    }

    public interface MigrationCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}

