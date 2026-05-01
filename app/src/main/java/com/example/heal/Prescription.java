package com.example.heal;

public class Prescription {
    private String prescriptionId;
    private String appointmentId;
    private String doctorId;
    private String doctorName;
    private String patientId;
    private String patientName;
    private String medicineName;
    private String dosage;
    private String frequency;
    private String instructions;
    private String date;

    public Prescription() {}

    public Prescription(String prescriptionId, String appointmentId, String doctorId, String doctorName, String patientId, String patientName, String medicineName, String dosage, String frequency, String instructions, String date) {
        this.prescriptionId = prescriptionId;
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.instructions = instructions;
        this.date = date;
    }

    // Getters and Setters
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }
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
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
