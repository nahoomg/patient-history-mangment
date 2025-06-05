package com.hospital.models;

import java.time.LocalDate;

public class TreatmentRequest {
    private int id;
    private int patientId;
    private String patientName;
    private LocalDate dateRequested;
    private LocalDate preferredDate;
    private String urgency;
    private String symptoms;
    private String status;
    private int assignedDoctorId;
    private String assignedDoctorName;

    public TreatmentRequest() {
        this.dateRequested = LocalDate.now();
        this.status = "Pending";
    }

    public TreatmentRequest(int id, int patientId, String patientName, LocalDate dateRequested, 
                           LocalDate preferredDate, String urgency, String symptoms, 
                           String status, int assignedDoctorId, String assignedDoctorName) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.dateRequested = dateRequested;
        this.preferredDate = preferredDate;
        this.urgency = urgency;
        this.symptoms = symptoms;
        this.status = status;
        this.assignedDoctorId = assignedDoctorId;
        this.assignedDoctorName = assignedDoctorName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public LocalDate getDateRequested() { return dateRequested; }
    public void setDateRequested(LocalDate dateRequested) { this.dateRequested = dateRequested; }

    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAssignedDoctorId() { return assignedDoctorId; }
    public void setAssignedDoctorId(int assignedDoctorId) { this.assignedDoctorId = assignedDoctorId; }

    public String getAssignedDoctorName() { return assignedDoctorName; }
    public void setAssignedDoctorName(String assignedDoctorName) { this.assignedDoctorName = assignedDoctorName; }
} 