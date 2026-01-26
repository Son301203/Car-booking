package com.example.bookcar.model;

import java.io.Serializable;

/**
 * Simple wrapper class to pass customer info for cluster map visualization
 * This avoids issues with Firebase Timestamp not being Serializable
 */
public class ClusterCustomerInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String customerName;
    private String customerPhone;
    private String pickupAddress;
    private double latitude;
    private double longitude;

    public ClusterCustomerInfo() {
        // Default constructor
    }

    public ClusterCustomerInfo(String customerName, String customerPhone, String pickupAddress,
                               double latitude, double longitude) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.pickupAddress = pickupAddress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
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

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Create ClusterCustomerInfo from Order object
     */
    public static ClusterCustomerInfo fromOrder(Order order) {
        ClusterCustomerInfo info = new ClusterCustomerInfo();
        info.customerName = order.getCustomerName();
        info.customerPhone = order.getCustomerPhone();
        info.pickupAddress = order.getDeparture();

        if (order.getPickupCoordinates() != null) {
            info.latitude = order.getPickupCoordinates().getLatitude();
            info.longitude = order.getPickupCoordinates().getLongitude();
        }

        return info;
    }
}

