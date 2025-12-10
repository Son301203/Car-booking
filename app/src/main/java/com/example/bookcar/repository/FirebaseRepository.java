package com.example.bookcar.repository;

import android.util.Log;

import com.example.bookcar.model.Order;
import com.example.bookcar.model.Role;
import com.example.bookcar.model.Trips;
import com.example.bookcar.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Repository class to centralize Firebase Firestore operations
 * New collection structure:
 * - users/ (all users including drivers and clients)
 * - roles/ (role definitions)
 * - trips/ (all trips)
 * - orders/ (all orders)
 */
public class FirebaseRepository {
    private static final String TAG = "FirebaseRepository";

    // Collection names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ROLES = "roles";
    public static final String COLLECTION_TRIPS = "trips";
    public static final String COLLECTION_ORDERS = "orders";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";

    private final FirebaseFirestore db;

    public FirebaseRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    // ========== USER OPERATIONS ==========

    public CollectionReference getUsersCollection() {
        return db.collection(COLLECTION_USERS);
    }

    public DocumentReference getUserDocument(String userId) {
        return db.collection(COLLECTION_USERS).document(userId);
    }

    public Task<DocumentSnapshot> getUser(String userId) {
        return getUserDocument(userId).get();
    }

    public Task<Void> createUser(String userId, User user) {
        return getUserDocument(userId).set(user.toFirestore());
    }

    public Task<Void> updateUser(String userId, User user) {
        return getUserDocument(userId).update(user.toFirestore());
    }

    public Task<Void> deleteUser(String userId) {
        return getUserDocument(userId).delete();
    }

    public Query getUsersByRole(String roleId) {
        return getUsersCollection().whereEqualTo("role_id", roleId);
    }

    // ========== ROLE OPERATIONS ==========

    public CollectionReference getRolesCollection() {
        return db.collection(COLLECTION_ROLES);
    }

    public DocumentReference getRoleDocument(String roleId) {
        return db.collection(COLLECTION_ROLES).document(roleId);
    }

    public Task<DocumentSnapshot> getRole(String roleId) {
        return getRoleDocument(roleId).get();
    }

    public Task<Void> createRole(String roleId, Role role) {
        return getRoleDocument(roleId).set(role.toFirestore());
    }

    // ========== TRIP OPERATIONS ==========

    public CollectionReference getTripsCollection() {
        return db.collection(COLLECTION_TRIPS);
    }

    public DocumentReference getTripDocument(String tripId) {
        return db.collection(COLLECTION_TRIPS).document(tripId);
    }

    public Task<DocumentSnapshot> getTrip(String tripId) {
        return getTripDocument(tripId).get();
    }

    public Task<DocumentReference> createTrip(Trips trip) {
        return getTripsCollection().add(trip.toFirestore());
    }

    public Task<Void> updateTrip(String tripId, Trips trip) {
        return getTripDocument(tripId).update(trip.toFirestore());
    }

    public Task<Void> deleteTrip(String tripId) {
        return getTripDocument(tripId).delete();
    }

    public Query getTripsByDriver(String driverId) {
        return getTripsCollection().whereEqualTo("driver_id", driverId);
    }

    public Query getTripsByStatus(String status) {
        return getTripsCollection().whereEqualTo("status", status);
    }

    public Query getTripsByDriverAndStatus(String driverId, String status) {
        return getTripsCollection()
                .whereEqualTo("driver_id", driverId)
                .whereEqualTo("status", status);
    }

    // ========== ORDER OPERATIONS ==========

    public CollectionReference getOrdersCollection() {
        return db.collection(COLLECTION_ORDERS);
    }

    public DocumentReference getOrderDocument(String orderId) {
        return db.collection(COLLECTION_ORDERS).document(orderId);
    }

    public Task<DocumentSnapshot> getOrder(String orderId) {
        return getOrderDocument(orderId).get();
    }

    public Task<DocumentReference> createOrder(Order order) {
        return getOrdersCollection().add(order.toFirestore());
    }

    public Task<Void> updateOrder(String orderId, Order order) {
        return getOrderDocument(orderId).update(order.toFirestore());
    }

    public Task<Void> deleteOrder(String orderId) {
        return getOrderDocument(orderId).delete();
    }

    public Query getOrdersByClient(String clientId) {
        return getOrdersCollection().whereEqualTo("client_id", clientId);
    }

    public Query getOrdersByTrip(String tripId) {
        return getOrdersCollection().whereEqualTo("trip_id", tripId);
    }

    // ========== NOTIFICATION OPERATIONS ==========

    public CollectionReference getNotificationsCollection(String userId) {
        return db.collection(COLLECTION_NOTIFICATIONS)
                .document(COLLECTION_USERS)
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection("messages");
    }

    // ========== HELPER METHODS ==========

    /**
     * Check if user is a driver by checking their role_id
     */
    public void isUserDriver(String userId, OnRoleCheckListener listener) {
        getUser(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String roleId = task.getResult().getString("role_id");
                if (roleId != null) {
                    getRole(roleId).addOnCompleteListener(roleTask -> {
                        if (roleTask.isSuccessful() && roleTask.getResult().exists()) {
                            String roleName = roleTask.getResult().getString("name");
                            listener.onRoleChecked(Role.ROLE_DRIVER.equals(roleName));
                        } else {
                            listener.onRoleChecked(false);
                        }
                    });
                } else {
                    listener.onRoleChecked(false);
                }
            } else {
                listener.onRoleChecked(false);
            }
        });
    }

    public interface OnRoleCheckListener {
        void onRoleChecked(boolean isDriver);
    }
}

