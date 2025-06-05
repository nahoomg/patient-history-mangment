package com.hospital.controllers;

import com.hospital.models.Admin;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AdminFormController implements Initializable {
    @FXML private TextField fullNameField;
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
        saveButton.setOnAction(e -> handleSaveAdmin());
        cancelButton.setOnAction(e -> handleCancel());
    }
    
    public void setParentController(AdminDashboardController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void handleSaveAdmin() {
        // Validate input fields
        if (fullNameField.getText().isEmpty() || 
            usernameField.getText().isEmpty() || 
            passwordField.getText().isEmpty()) {
            
            showMessage("Please fill all required fields.", true);
            return;
        }
        
        try {
            // Create admin object
            Admin admin = new Admin();
            admin.setFullName(fullNameField.getText());
            admin.setEmail(emailField.getText());
            admin.setUsername(usernameField.getText());
            admin.setPassword(passwordField.getText());
            
            // Save to database
            dbManager.addAdmin(admin);
            
            showMessage("Admin registered successfully!", false);
            
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