package com.example.heal;

public class Appointment {
    private String appointmentId;
    private String doctorId;
    private String doctorName;
    private String patientId;
    private String patientName;
    private String date;
    private String time;
    private String status; // pending, accepted, rejected, rescheduled
    private String notes;
    private String timestamp;

    public Appointment() {
        // Required for Firebase
    }

    public Appointment(String appointmentId, String doctorId, String doctorName, String patientId, String patientName, String date, String time, String status) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
