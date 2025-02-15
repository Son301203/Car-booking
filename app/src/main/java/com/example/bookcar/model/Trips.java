package com.example.bookcar.model;

public class Trips {
    private String username;
    private String phone;
    private String departure;
    private String destination;
    private String dateDeparture;
    private String dateDestination;
    private Double departureCoordinates;
    private Double destinationCoordinates;
    private String dateTrips;


    public Trips(String username, String phone, String departure, String destination,
                 String dateDeparture, String dateDestination, Double departureCoordinates,
                 Double destinationCoordinates, String dateTrips) {
        this.username = username;
        this.phone = phone;
        this.departure = departure;
        this.destination = destination;
        this.dateDeparture = dateDeparture;
        this.dateDestination = dateDestination;
        this.departureCoordinates = departureCoordinates;
        this.destinationCoordinates = destinationCoordinates;
        this.dateTrips = dateTrips;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getDateTrips() {
        return dateTrips;
    }

    public void setDateTrips(String dateTrips) {
        this.dateTrips = dateTrips;
    }
}


