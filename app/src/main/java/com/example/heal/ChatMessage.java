package com.example.heal;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private String messageId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String symptoms;
    private String description;
    private String reply;
    private String status;
    private String timestamp;

    public ChatMessage() {
        // Required for Firebase
    }

    public ChatMessage(String messageId, String patientId, String patientName, String doctorId, String doctorName) {
        this.messageId = messageId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.status = "pending";
    }

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
