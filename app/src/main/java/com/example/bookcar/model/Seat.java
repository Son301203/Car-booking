package com.example.bookcar.model;

public class Seat {
    private String clientId;
    private String username;
    private String phone;
    private String driverId;
    private String tripId;

    public Seat(String clientId, String driverId, String tripId) {
        this.clientId = clientId;
        this.driverId = driverId;
        this.tripId = tripId;
    }


    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getClientId() {
        return clientId;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getTripId() {
        return tripId;
    }


    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
