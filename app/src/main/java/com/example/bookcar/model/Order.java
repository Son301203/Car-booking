package com.example.bookcar.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private String documentId;
    private String departure;
    private String destination;
    private String departureDate;
    private String returnDate;

    // New fields for refactored structure
    private String tripId;
    private String clientId;
    private String state; // "Booked", "Arranged", "Completed", "Cancelled"
    private GeoPoint pickupCoordinates;
    private GeoPoint destinationCoordinates;
    private Timestamp createdAt;

    // Customer info for display
    private String customerName;
    private String customerPhone;

    // Original constructor for backward compatibility
    public Order(String documentId, String departure, String destination, String departureDate, String returnDate) {
        this.documentId = documentId;
        this.departure = departure;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
    }

    // New constructor for new structure
    public Order(String tripId, String clientId, String departure, String destination) {
        this.tripId = tripId;
        this.clientId = clientId;
        this.departure = departure;
        this.destination = destination;
        this.createdAt = Timestamp.now();
    }

    // Empty constructor for Firestore
    public Order() {
    }

    // Convert Firestore document to Order object
    public static Order fromFirestore(DocumentSnapshot snapshot) {
        Order order = new Order();
        order.documentId = snapshot.getId();
        order.tripId = snapshot.getString("trip_id");
        order.clientId = snapshot.getString("client_id");
        order.departure = snapshot.getString("departure");
        order.destination = snapshot.getString("destination");
        order.departureDate = snapshot.getString("departureDate");
        order.returnDate = snapshot.getString("returnDate");
        order.state = snapshot.getString("state");
        order.pickupCoordinates = snapshot.getGeoPoint("pickup_coordinates");
        order.destinationCoordinates = snapshot.getGeoPoint("destination_coordinates");
        order.createdAt = snapshot.getTimestamp("created_at");
        order.customerName = snapshot.getString("customer_name");
        order.customerPhone = snapshot.getString("customer_phone");
        return order;
    }

    // Convert Order object to Map for Firestore
    public Map<String, Object> toFirestore() {
        Map<String, Object> data = new HashMap<>();
        if (tripId != null) data.put("trip_id", tripId);
        if (clientId != null) data.put("client_id", clientId);
        if (departure != null) data.put("departure", departure);
        if (destination != null) data.put("destination", destination);
        if (departureDate != null) data.put("departureDate", departureDate);
        if (returnDate != null) data.put("returnDate", returnDate);
        if (state != null) data.put("state", state);
        if (pickupCoordinates != null) data.put("pickup_coordinates", pickupCoordinates);
        if (destinationCoordinates != null) data.put("destination_coordinates", destinationCoordinates);
        if (customerName != null) data.put("customer_name", customerName);
        if (customerPhone != null) data.put("customer_phone", customerPhone);
        data.put("created_at", createdAt != null ? createdAt : Timestamp.now());
        return data;
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
        this.tripId = tripId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
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
        this.state = state;
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
