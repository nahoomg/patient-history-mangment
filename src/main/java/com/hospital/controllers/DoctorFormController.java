package com.hospital.controllers;

import com.hospital.models.Doctor;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DoctorFormController implements Initializable {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField specializationField;
    @FXML private TextField contactNumberField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label messageLabel;

    private DatabaseManager dbManager;
    private AdminDashboardController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        
        // Set up button handlers
        saveButton.setOnAction(e -> handleSaveDoctor());
        cancelButton.setOnAction(e -> handleCancel());
    }
    
    public void setParentController(AdminDashboardController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void handleSaveDoctor() {
        // Validate input fields
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() || 
            specializationField.getText().isEmpty() || usernameField.getText().isEmpty() || 
            passwordField.getText().isEmpty()) {
            
            showMessage("Please fill all required fields.", true);
            return;
        }
        
        try {
            // Create doctor object
            Doctor doctor = new Doctor();
            doctor.setFirstName(firstNameField.getText());
            doctor.setLastName(lastNameField.getText());
            doctor.setSpecialization(specializationField.getText());
            doctor.setContactNumber(contactNumberField.getText());
            doctor.setEmail(emailField.getText());
            doctor.setUsername(usernameField.getText());
            doctor.setPassword(passwordField.getText());
            
            // Save to database
            dbManager.addDoctor(doctor);
            
            showMessage("Doctor registered successfully!", false);
            
            // Close the form after a brief delay
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) saveButton.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();
            
        } catch (SQLException e) {
            showMessage("Error: " + e.getMessage(), true);
        }
    }
    
    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        
        if (isError) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #2ecc71;");
        }
    }
} 