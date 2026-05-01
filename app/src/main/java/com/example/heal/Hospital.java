package com.example.heal;

public class Hospital {
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private double distanceMiles;
    private String[] specialties;
    private int waitTimeMin;
    private double rating;

    public Hospital(String name, double latitude, double longitude, String address, double distanceMiles, String[] specialties, int waitTimeMin, double rating) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.distanceMiles = distanceMiles;
        this.specialties = specialties;
        this.waitTimeMin = waitTimeMin;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public double getDistanceMiles() {
        return distanceMiles;
    }

    public String[] getSpecialties() {
        return specialties;
    }

    public int getWaitTimeMin() {
        return waitTimeMin;
    }

    public double getRating() {
        return rating;
    }
}
