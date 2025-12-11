package com.example.bookcar.model;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class Trips {
    private static final String TAG = "Trips";

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

    /**
     * Constructor for creating a trip with date, time, quantity, driver ID, and trip ID
     * @param dateTrips The date of the trip
     * @param timeTrips The time of the trip
     * @param quantity The number of clients/seats
     * @param driversId The driver's user ID (must not be null or empty)
     * @param tripsId The trip ID (can be null for new trips)
     */
    public Trips(String dateTrips, String timeTrips, int quantity, String driversId, String tripsId) {
        if (TextUtils.isEmpty(driversId)) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty");
        }
        if (TextUtils.isEmpty(dateTrips)) {
            throw new IllegalArgumentException("Trip date cannot be null or empty");
        }
        if (TextUtils.isEmpty(timeTrips)) {
            throw new IllegalArgumentException("Trip time cannot be null or empty");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        this.dateTrips = dateTrips;
        this.timeTrips = timeTrips;
        this.quantity = quantity;
        this.driversId = driversId;
        this.tripsId = tripsId;
        this.status = STATUS_PENDING;
        this.createdAt = Timestamp.now();
    }

    /**
     * Constructor for creating a trip with description, day of week, time, and date
     * @param description Description of the trip
     * @param dayOfWeek Day of the week (1-7, where 1 is Monday)
     * @param timeTrips The time of the trip
     * @param dateTrips The date of the trip
     */
    public Trips(String description, int dayOfWeek, String timeTrips, String dateTrips) {
        if (TextUtils.isEmpty(dateTrips)) {
            throw new IllegalArgumentException("Trip date cannot be null or empty");
        }
        if (TextUtils.isEmpty(timeTrips)) {
            throw new IllegalArgumentException("Trip time cannot be null or empty");
        }
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("Day of week must be between 1 and 7");
        }

        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.timeTrips = timeTrips;
        this.dateTrips = dateTrips;
        this.status = STATUS_PENDING;
        this.createdAt = Timestamp.now();
    }

    /**
     * Convert Firestore document to Trips object
     * @param snapshot The Firestore DocumentSnapshot
     * @return Trips object or null if document doesn't exist
     */
    public static Trips fromFirestore(DocumentSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) {
            Log.w(TAG, "Cannot convert null or non-existent snapshot to Trips");
            return null;
        }

        try {
            Trips trip = new Trips();
            trip.tripsId = snapshot.getId();
            trip.driversId = snapshot.getString("driver_id");
            trip.departure = snapshot.getString("departure");
            trip.destination = snapshot.getString("destination");
            trip.dateDeparture = snapshot.getString("dateDeparture");
            trip.dateDestination = snapshot.getString("dateDestination");
            trip.dateTrips = snapshot.getString("dateTrips");

            // Handle both "dateTrip" (web) and "dateTrips" (mobile) for backward compatibility
            if (TextUtils.isEmpty(trip.dateTrips)) {
                trip.dateTrips = snapshot.getString("dateTrip");
            }

            trip.timeTrips = snapshot.getString("timeTrips");
            // Handle both "startTime" (web) and "timeTrips" (mobile) for backward compatibility
            if (TextUtils.isEmpty(trip.timeTrips)) {
                trip.timeTrips = snapshot.getString("startTime");
            }

            trip.description = snapshot.getString("description");
            trip.status = snapshot.getString("status");

            // Set default status if null
            if (TextUtils.isEmpty(trip.status)) {
                trip.status = STATUS_PENDING;
            }

            trip.createdAt = snapshot.getTimestamp("created_at");

            Long quantityLong = snapshot.getLong("quantity");
            trip.quantity = quantityLong != null ? quantityLong.intValue() : 0;

            Long dayOfWeekLong = snapshot.getLong("dayOfWeek");
            trip.dayOfWeek = dayOfWeekLong != null ? dayOfWeekLong.intValue() : 0;

            trip.departureCoordinates = snapshot.getGeoPoint("pickup_coordinates");
            trip.destinationCoordinates = snapshot.getGeoPoint("destination_coordinates");

            return trip;
        } catch (Exception e) {
            Log.e(TAG, "Error converting Firestore document to Trips", e);
            return null;
        }
    }
    /**
     * Convert Trips object to Map for Firestore
     * Includes validation for required fields
     * @return Map containing trip data
     * @throws IllegalStateException if required fields are missing
     */
    public Map<String, Object> toFirestore() {
        Map<String, Object> data = new HashMap<>();

        // Required field: driver_id
        if (TextUtils.isEmpty(driversId)) {
            Log.w(TAG, "Warning: driver_id is null or empty when converting to Firestore");
        }
        data.put("driver_id", driversId);

        // Optional fields
        if (departure != null) data.put("departure", departure);
        if (destination != null) data.put("destination", destination);
        if (dateDeparture != null) data.put("dateDeparture", dateDeparture);
        if (dateDestination != null) data.put("dateDestination", dateDestination);
        if (departureCoordinates != null) data.put("pickup_coordinates", departureCoordinates);
        if (destinationCoordinates != null) data.put("destination_coordinates", destinationCoordinates);

        data.put("quantity", quantity);

        // Store both field names for backward compatibility with web interface
        if (dateTrips != null) {
            data.put("dateTrip", dateTrips);  // For web consistency
            data.put("dateTrips", dateTrips); // For mobile consistency
        }

        if (timeTrips != null) {
            data.put("startTime", timeTrips);  // For web consistency
            data.put("timeTrips", timeTrips);  // For mobile consistency
        }

        if (description != null) data.put("description", description);
        data.put("dayOfWeek", dayOfWeek);

        // Status with validation
        String validStatus = status;
        if (!STATUS_PENDING.equals(status) && !STATUS_ONGOING.equals(status) && !STATUS_COMPLETED.equals(status)) {
            validStatus = STATUS_PENDING;
            Log.w(TAG, "Invalid status '" + status + "', defaulting to '" + STATUS_PENDING + "'");
        }
        data.put("status", validStatus);

        // Timestamp
        data.put("created_at", createdAt != null ? createdAt : Timestamp.now());

        return data;
    }

    /**
     * Validates if the trip has all required fields for creation
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (TextUtils.isEmpty(driversId)) {
            Log.e(TAG, "Validation failed: driver_id is required");
            return false;
        }
        if (TextUtils.isEmpty(dateTrips)) {
            Log.e(TAG, "Validation failed: dateTrips is required");
            return false;
        }
        if (TextUtils.isEmpty(timeTrips)) {
            Log.e(TAG, "Validation failed: timeTrips is required");
            return false;
        }
        return true;
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
        if (!STATUS_PENDING.equals(status) && !STATUS_ONGOING.equals(status) && !STATUS_COMPLETED.equals(status)) {
            Log.w(TAG, "Invalid status value: " + status + ", using PENDING");
            this.status = STATUS_PENDING;
        } else {
            this.status = status;
        }
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setDriversId(String driversId) {
        if (TextUtils.isEmpty(driversId)) {
            throw new IllegalArgumentException("Driver ID cannot be null or empty");
        }
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
