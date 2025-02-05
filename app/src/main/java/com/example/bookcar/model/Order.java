package com.example.bookcar.model;

public class Order {
    private String departure;
    private String destination;
    private String departureDate;
    private String returnDate;

    public Order(String departure, String destination, String departureDate, String returnDate) {
        this.departure = departure;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
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
}
