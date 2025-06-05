package com.hospital.controllers;

import com.hospital.models.Doctor;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DoctorDashboardController {
    @FXML private Label welcomeLabel;
    private Doctor currentDoctor;

    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        welcomeLabel.setText("Welcome, Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
    }
} 