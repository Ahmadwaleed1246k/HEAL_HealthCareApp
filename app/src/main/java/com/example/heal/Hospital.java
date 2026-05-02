package com.example.heal;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Hospital implements Serializable {
    private String hospitalId;
    private String name;
    private String address;
    private String description;
    private String imageUrl;
    private float rating;
    private Map<String, Room> rooms;

    private double latitude;
    private double longitude;
    private double distanceMiles;
    private List<String> specialties;
    private int waitTime;

    public Hospital() {}

    public Hospital(String hospitalId, String name, String address, String description, String imageUrl, float rating) {
        this.hospitalId = hospitalId;
        this.name = name;
        this.address = address;
        this.description = description;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    public Hospital(String name, double latitude, double longitude, String address, double distanceMiles, List<String> specialties, int waitTime, double rating) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.distanceMiles = distanceMiles;
        this.specialties = specialties;
        this.waitTime = waitTime;
        this.rating = (float) rating;
    }

    // Getters and Setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getDistanceMiles() { return distanceMiles; }
    public void setDistanceMiles(double distanceMiles) { this.distanceMiles = distanceMiles; }
    public List<String> getSpecialties() { return specialties; }
    public void setSpecialties(List<String> specialties) { this.specialties = specialties; }
    public int getWaitTime() { return waitTime; }
    public void setWaitTime(int waitTime) { this.waitTime = waitTime; }

    // Getters and Setters
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public Map<String, Room> getRooms() { return rooms; }
    public void setRooms(Map<String, Room> rooms) { this.rooms = rooms; }
}
