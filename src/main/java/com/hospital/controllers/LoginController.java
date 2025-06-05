package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Admin;
import com.hospital.models.Doctor;
import com.hospital.models.Hospital;
import com.hospital.models.Patient;
import com.hospital.utils.DatabaseManager;
import com.hospital.utils.Session;
import javafx.event.ActionEvent;
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
    @FXML private ComboBox<String> userTypeComboBox;
    @FXML private Label messageLabel;

    private final DatabaseManager dbManager = new DatabaseManager();

    @FXML
    private void initialize() {
        userTypeComboBox.getItems().addAll("Patient", "Doctor", "Admin", "Hospital");
        userTypeComboBox.setValue("Patient");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String userType = userTypeComboBox.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }

        try {
            switch (userType) {
                case "Patient":
                    loginAsPatient(username, password);
                    break;
                case "Doctor":
                    loginAsDoctor(username, password);
                    break;
                case "Admin":
                    loginAsAdmin(username, password);
                    break;
                case "Hospital":
                    loginAsHospital(username, password);
                    break;
            }
        } catch (SQLException e) {
            messageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loginAsPatient(String username, String password) throws SQLException {
        Patient patient = dbManager.authenticatePatient(username, password);
        if (patient != null) {
            Session.getInstance().setCurrentUser(patient);
            Session.getInstance().setUserType("patient");
            navigateToDashboard("/com/hospital/patient-dashboard.fxml", "Patient Dashboard");
        } else {
            messageLabel.setText("Invalid patient credentials");
        }
    }

    private void loginAsDoctor(String username, String password) throws SQLException {
        Doctor doctor = dbManager.authenticateDoctor(username, password);
        if (doctor != null) {
            Session.getInstance().setCurrentUser(doctor);
            Session.getInstance().setUserType("doctor");
            navigateToDashboard("/com/hospital/doctor-dashboard.fxml", "Doctor Dashboard");
        } else {
            messageLabel.setText("Invalid doctor credentials");
        }
    }

    private void loginAsAdmin(String username, String password) throws SQLException {
        Admin admin = dbManager.authenticateAdmin(username, password);
        if (admin != null) {
            Session.getInstance().setCurrentUser(admin);
            Session.getInstance().setUserType("admin");
            navigateToDashboard("/com/hospital/admin-dashboard.fxml", "Admin Dashboard");
        } else {
            messageLabel.setText("Invalid admin credentials");
        }
    }

    private void loginAsHospital(String username, String password) throws SQLException {
        Hospital hospital = dbManager.authenticateHospital(username, password);
        if (hospital != null) {
            Session.getInstance().setCurrentUser(hospital);
            Session.getInstance().setUserType("hospital");
            navigateToDashboard("/com/hospital/hospital-dashboard.fxml", "Hospital Dashboard");
        } else {
            messageLabel.setText("Invalid hospital credentials");
        }
    }

    private void navigateToDashboard(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMaximized(Main.USE_MAXIMIZED);
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/registration-choice.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, Main.LOGIN_WIDTH, Main.LOGIN_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Registration Options");
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            messageLabel.setText("Error loading registration page: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 