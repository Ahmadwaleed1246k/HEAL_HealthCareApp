package com.example.heal;

import java.io.Serializable;

public class RoomBooking implements Serializable {
    private String bookingId;
    private String hospitalId;
    private String hospitalName;
    private String patientId;
    private String patientName;
    private String roomNumber;
    private String roomType;
    private String date;
    private double totalAmount;
    private double advancePaid;
    private String status; // confirmed, cancelled

    public RoomBooking() {}

    public RoomBooking(String bookingId, String hospitalId, String hospitalName, String patientId, String patientName, 
                       String roomNumber, String roomType, String date, double totalAmount, double advancePaid, String status) {
        this.bookingId = bookingId;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.date = date;
        this.totalAmount = totalAmount;
        this.advancePaid = advancePaid;
        this.status = status;
    }

    // Getters and Setters
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getHospitalId() { return hospitalId; }
    public void setHospitalId(String hospitalId) { this.hospitalId = hospitalId; }
    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(double advancePaid) { this.advancePaid = advancePaid; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
