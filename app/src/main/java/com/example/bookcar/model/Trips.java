package com.example.bookcar.model;

public class Trips {
    private String dateTrips;
    private String timeTrips;
    private int quantity;


    public Trips(String dateTrips, String timeTrips, int quantity) {
        this.dateTrips = dateTrips;
        this.timeTrips = timeTrips;
        this.quantity = quantity;
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

    public void setTimeTrips(String timeTrips) {
        this.timeTrips = timeTrips;
    }
}


