package com.hospital.controllers;

import com.hospital.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class RegistrationChoiceController {
    @FXML private Button patientButton;

    @FXML
    private void handlePatientRegistration() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/patient-registration.fxml"));
            Stage stage = (Stage) patientButton.getScene().getWindow();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Patient Registration - City Hospital");
            stage.setMaximized(Main.USE_MAXIMIZED);
            stage.show();
        } catch (IOException e) {
            System.out.println("Error loading patient registration page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void backToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/login.fxml"));
            Stage stage = (Stage) patientButton.getScene().getWindow();
            Scene scene = new Scene(root, Main.LOGIN_WIDTH, Main.LOGIN_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("City Hospital Management System");
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.out.println("Error going back to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 