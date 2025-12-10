package com.example.bookcar.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class Trips {
    private String departure;
    private String destination;
    private String dateDeparture;
    private String dateDestination;
    private GeoPoint departureCoordinates; // Changed from Double to GeoPoint
    private GeoPoint destinationCoordinates; // Changed from Double to GeoPoint
    private int quantity;
    private String dateTrips;
    private String timeTrips;
    private String description;
    private int dayOfWeek;
    private String driversId;
    private String tripsId;
    private String status; // "pending", "ongoing", "completed"
    private Timestamp createdAt;

    // Constants for status
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ONGOING = "ongoing";
    public static final String STATUS_COMPLETED = "completed";

    // Empty constructor for Firestore
    public Trips() {
    }

    public Trips(String dateTrips, String timeTrips, int quantity, String driversId, String tripsId) {
        this.dateTrips = dateTrips;
        this.timeTrips = timeTrips;
        this.quantity = quantity;
        this.driversId = driversId;
        this.tripsId = tripsId;
        this.status = STATUS_PENDING;
        this.createdAt = Timestamp.now();
    }

    public Trips(String description, int dayOfWeek, String timeTrips, String dateTrips) {
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.timeTrips = timeTrips;
        this.dateTrips = dateTrips;
        this.status = STATUS_PENDING;
        this.createdAt = Timestamp.now();
    }

    // Convert Firestore document to Trips object
    public static Trips fromFirestore(DocumentSnapshot snapshot) {
        Trips trip = new Trips();
        trip.tripsId = snapshot.getId();
        trip.driversId = snapshot.getString("driver_id");
        trip.departure = snapshot.getString("departure");
        trip.destination = snapshot.getString("destination");
        trip.dateDeparture = snapshot.getString("dateDeparture");
        trip.dateDestination = snapshot.getString("dateDestination");
        trip.dateTrips = snapshot.getString("dateTrips");
        trip.timeTrips = snapshot.getString("timeTrips");
        trip.description = snapshot.getString("description");
        trip.status = snapshot.getString("status");
        trip.createdAt = snapshot.getTimestamp("created_at");

        Long quantityLong = snapshot.getLong("quantity");
        trip.quantity = quantityLong != null ? quantityLong.intValue() : 0;

        Long dayOfWeekLong = snapshot.getLong("dayOfWeek");
        trip.dayOfWeek = dayOfWeekLong != null ? dayOfWeekLong.intValue() : 0;

        trip.departureCoordinates = snapshot.getGeoPoint("pickup_coordinates");
        trip.destinationCoordinates = snapshot.getGeoPoint("destination_coordinates");

        return trip;
    }

    // Convert Trips object to Map for Firestore
    public Map<String, Object> toFirestore() {
        Map<String, Object> data = new HashMap<>();
        data.put("driver_id", driversId);
        data.put("departure", departure);
        data.put("destination", destination);
        data.put("dateDeparture", dateDeparture);
        data.put("dateDestination", dateDestination);
        data.put("pickup_coordinates", departureCoordinates);
        data.put("destination_coordinates", destinationCoordinates);
        data.put("quantity", quantity);
        data.put("dateTrips", dateTrips);
        data.put("timeTrips", timeTrips);
        data.put("description", description);
        data.put("dayOfWeek", dayOfWeek);
        data.put("status", status != null ? status : STATUS_PENDING);
        data.put("created_at", createdAt != null ? createdAt : Timestamp.now());
        return data;
    }

    public String getDateTrips() {
        return dateTrips;
    }

    public void setDateTrips(String dateTrips) {
        this.dateTrips = dateTrips;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTimeTrips() {
        return timeTrips;
    }

    public String getDescription() {
        return description;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setTimeTrips(String timeTrips) {
        this.timeTrips = timeTrips;
    }

    public String getDriversId() {
        return driversId;
    }

    public String getTripsId() {
        return tripsId;
    }

    public String getDeparture() {
        return departure;
    }

    public String getDestination() {
        return destination;
    }

    public String getDateDeparture() {
        return dateDeparture;
    }

    public String getDateDestination() {
        return dateDestination;
    }

    public GeoPoint getDepartureCoordinates() {
        return departureCoordinates;
    }

    public GeoPoint getDestinationCoordinates() {
        return destinationCoordinates;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDateDeparture(String dateDeparture) {
        this.dateDeparture = dateDeparture;
    }

    public void setDateDestination(String dateDestination) {
        this.dateDestination = dateDestination;
    }

    public void setDepartureCoordinates(GeoPoint departureCoordinates) {
        this.departureCoordinates = departureCoordinates;
    }

    public void setDestinationCoordinates(GeoPoint destinationCoordinates) {
        this.destinationCoordinates = destinationCoordinates;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setDriversId(String driversId) {
        this.driversId = driversId;
    }

    public void setTripsId(String tripsId) {
        this.tripsId = tripsId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
