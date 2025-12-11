package com.example.bookcar.model;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private static final String TAG = "Order";

    private String documentId;
    private String departure;
    private String destination;
    private String departureDate;
    private String returnDate;

    // New fields for refactored structure
    private String tripId;
    private String clientId;
    private String state; // "Booked", "Arranged", "Picked Up", "Completed", "Cancelled"
    private GeoPoint pickupCoordinates;
    private GeoPoint destinationCoordinates;
    private Timestamp createdAt;

    // Customer info for display
    private String customerName;
    private String customerPhone;

    // Constants for state
    public static final String STATE_BOOKED = "Booked";
    public static final String STATE_ARRANGED = "Arranged";
    public static final String STATE_PICKED_UP = "Picked Up";
    public static final String STATE_COMPLETED = "Completed";
    public static final String STATE_CANCELLED = "Cancelled";

    // Original constructor for backward compatibility
    public Order(String documentId, String departure, String destination, String departureDate, String returnDate) {
        this.documentId = documentId;
        this.departure = departure;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.state = STATE_BOOKED;
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor for new structure
     * @param tripId The trip ID (can be null if not yet arranged)
     * @param clientId The client's user ID (must not be null or empty)
     * @param departure Departure location
     * @param destination Destination location
     */
    public Order(String tripId, String clientId, String departure, String destination) {
        if (TextUtils.isEmpty(clientId)) {
            throw new IllegalArgumentException("Client ID cannot be null or empty");
        }

        this.tripId = tripId;
        this.clientId = clientId;
        this.departure = departure;
        this.destination = destination;
        this.state = STATE_BOOKED;
        this.createdAt = Timestamp.now();
    }

    // Empty constructor for Firestore
    public Order() {
    }

    /**
     * Convert Firestore document to Order object
     * @param snapshot The Firestore DocumentSnapshot
     * @return Order object or null if document doesn't exist
     */
    public static Order fromFirestore(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            Log.w(TAG, "Cannot convert null or non-existent snapshot to Order");
            return null;
        }

        try {
            Order order = new Order();
            order.documentId = snapshot.getId();
            order.tripId = snapshot.getString("trip_id");
            order.clientId = snapshot.getString("client_id");
            order.departure = snapshot.getString("departure");
            order.destination = snapshot.getString("destination");
            order.departureDate = snapshot.getString("departureDate");
            order.returnDate = snapshot.getString("returnDate");
            order.state = snapshot.getString("state");

            // Set default state if null
            if (TextUtils.isEmpty(order.state)) {
                order.state = STATE_BOOKED;
            }

            order.pickupCoordinates = snapshot.getGeoPoint("pickup_coordinates");
            order.destinationCoordinates = snapshot.getGeoPoint("destination_coordinates");
            order.createdAt = snapshot.getTimestamp("created_at");
            order.customerName = snapshot.getString("customer_name");
            order.customerPhone = snapshot.getString("customer_phone");

            return order;
        } catch (Exception e) {
            Log.e(TAG, "Error converting Firestore document to Order", e);
            return null;
        }
    }

    /**
     * Convert Order object to Map for Firestore
     * @return Map containing order data
     */
    public Map<String, Object> toFirestore() {
        Map<String, Object> data = new HashMap<>();

        // Required field: client_id
        if (TextUtils.isEmpty(clientId)) {
            Log.w(TAG, "Warning: client_id is null or empty when converting to Firestore");
        }
        data.put("client_id", clientId);

        // Optional trip_id (null if not yet arranged)
        if (tripId != null) data.put("trip_id", tripId);

        // Location data
        if (departure != null) data.put("departure", departure);
        if (destination != null) data.put("destination", destination);
        if (departureDate != null) data.put("departureDate", departureDate);
        if (returnDate != null) data.put("returnDate", returnDate);
        if (pickupCoordinates != null) data.put("pickup_coordinates", pickupCoordinates);
        if (destinationCoordinates != null) data.put("destination_coordinates", destinationCoordinates);

        // Customer info
        if (customerName != null) data.put("customer_name", customerName);
        if (customerPhone != null) data.put("customer_phone", customerPhone);

        // State with validation
        String validState = state;
        if (!isValidState(state)) {
            validState = STATE_BOOKED;
            Log.w(TAG, "Invalid state '" + state + "', defaulting to '" + STATE_BOOKED + "'");
        }
        data.put("state", validState != null ? validState : STATE_BOOKED);

        // Timestamp
        data.put("created_at", createdAt != null ? createdAt : Timestamp.now());

        return data;
    }

    /**
     * Validates if the order has all required fields for creation
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (TextUtils.isEmpty(clientId)) {
            Log.e(TAG, "Validation failed: client_id is required");
            return false;
        }
        return true;
    }

    /**
     * Check if a state value is valid
     * @param state The state to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidState(String state) {
        return STATE_BOOKED.equals(state) ||
               STATE_ARRANGED.equals(state) ||
               STATE_PICKED_UP.equals(state) ||
               STATE_COMPLETED.equals(state) ||
               STATE_CANCELLED.equals(state);
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        // Trip ID can be null if order is not yet arranged
        this.tripId = tripId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        if (TextUtils.isEmpty(clientId)) {
            throw new IllegalArgumentException("Client ID cannot be null or empty");
        }
        this.clientId = clientId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (!isValidState(state)) {
            Log.w(TAG, "Invalid state value: " + state + ", using BOOKED");
            this.state = STATE_BOOKED;
        } else {
            this.state = state;
        }
    }

    public GeoPoint getPickupCoordinates() {
        return pickupCoordinates;
    }

    public void setPickupCoordinates(GeoPoint pickupCoordinates) {
        this.pickupCoordinates = pickupCoordinates;
    }

    public GeoPoint getDestinationCoordinates() {
        return destinationCoordinates;
    }

    public void setDestinationCoordinates(GeoPoint destinationCoordinates) {
        this.destinationCoordinates = destinationCoordinates;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
}
