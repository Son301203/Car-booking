package com.example.bookcar.model;

public class Trips {
    private String departure;
    private String destination;
    private String dateDeparture;
    private String dateDestination;
    private Double departureCoordinates;
    private Double destinationCoordinates;
    private int quantity;
    private String dateTrips;
    private String timeTrips;
    private String driversId;
    private String tripsId;

    public Trips(String dateTrips, String timeTrips, int quantity, String driversId, String tripsId) {
        this.dateTrips = dateTrips;
        this.timeTrips = timeTrips;
        this.quantity = quantity;
        this.driversId = driversId;
        this.tripsId = tripsId;
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

    public Double getDepartureCoordinates() {
        return departureCoordinates;
    }

    public Double getDestinationCoordinates() {
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

    public void setDepartureCoordinates(Double departureCoordinates) {
        this.departureCoordinates = departureCoordinates;
    }

    public void setDestinationCoordinates(Double destinationCoordinates) {
        this.destinationCoordinates = destinationCoordinates;
    }
}


