package com.example.heal;

import java.io.Serializable;

public class Room implements Serializable {
    private String roomId;
    private String roomNumber;
    private String type; // General, Deluxe, ICU, etc.
    private double pricePerDay;
    private boolean isAvailable;

    public Room() {}

    public Room(String roomId, String roomNumber, String type, double pricePerDay, boolean isAvailable) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerDay = pricePerDay;
        this.isAvailable = isAvailable;
    }

    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(double pricePerDay) { this.pricePerDay = pricePerDay; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
