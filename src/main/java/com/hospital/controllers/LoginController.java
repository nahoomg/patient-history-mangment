package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Doctor;
import com.hospital.models.Admin;
import com.hospital.models.Patient;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private DatabaseManager dbManager;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter username and password", true);
            return;
        }

        try {
            // First try admin login
            Admin admin = dbManager.authenticateAdmin(username, password);
            if (admin != null) {
                openAdminDashboard(admin);
                return;
            }

            // Then try doctor login
            Doctor doctor = dbManager.authenticateDoctor(username, password);
            if (doctor != null) {
                openDoctorDashboard(doctor);
                return;
            }

            // Finally try patient login
            Patient patient = dbManager.authenticatePatient(username, password);
            if (patient != null) {
                openPatientDashboard(patient);
                return;
            }

            // If we get here, authentication failed
            showMessage("Invalid username or password", true);
            
        } catch (SQLException e) {
            showMessage("Database error: " + e.getMessage(), true);
        }
    }

    @FXML
    private void openRegistrationPage() {
        try {
            // Print debug information
            System.out.println("Attempting to load patient registration page");
            String resourcePath = "/com/hospital/patient-registration.fxml";
            System.out.println("Resource path: " + resourcePath);
            
            // Check if resource exists
            if (getClass().getResource(resourcePath) == null) {
                System.out.println("ERROR: Resource not found: " + resourcePath);
                showMessage("Resource not found: " + resourcePath, true);
                return;
            }
            
            Parent root = FXMLLoader.load(getClass().getResource(resourcePath));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Patient Registration - City Hospital");
            stage.setMaximized(Main.USE_MAXIMIZED);
            stage.show();
        } catch (IOException e) {
            System.out.println("ERROR loading registration page: " + e.getMessage());
            e.printStackTrace();
            showMessage("Error opening registration page: " + e.getMessage(), true);
        } catch (Exception e) {
            System.out.println("UNEXPECTED ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            showMessage("Unexpected error: " + e.getMessage(), true);
        }
    }

    private void openAdminDashboard(Admin admin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/admin-dashboard.fxml"));
            Parent root = loader.load();
            
            AdminDashboardController controller = loader.getController();
            controller.setAdmin(admin);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard - City Hospital");
            stage.setMaximized(Main.USE_MAXIMIZED);
            stage.show();
        } catch (IOException e) {
            showMessage("Error loading admin dashboard: " + e.getMessage(), true);
        }
    }

    private void openDoctorDashboard(Doctor doctor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/doctor-dashboard.fxml"));
            Parent root = loader.load();
            
            DoctorDashboardController controller = loader.getController();
            controller.setDoctor(doctor);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Doctor Dashboard - City Hospital");
            stage.setMaximized(Main.USE_MAXIMIZED);
            stage.show();
        } catch (IOException e) {
            showMessage("Error loading doctor dashboard: " + e.getMessage(), true);
        }
    }

    private void openPatientDashboard(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/patient-dashboard.fxml"));
            Parent root = loader.load();
            
            PatientDashboardController controller = loader.getController();
            controller.setPatient(patient);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Patient Dashboard - City Hospital");
            stage.setMaximized(Main.USE_MAXIMIZED);
            stage.show();
        } catch (IOException e) {
            showMessage("Error loading patient dashboard: " + e.getMessage(), true);
        }
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
} 