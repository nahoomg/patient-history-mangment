package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Hospital;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class HospitalRegistrationController implements Initializable {
    @FXML private TabPane registrationTabPane;
    @FXML private Text stepIndicator;
    
    // Hospital Information fields
    @FXML private TextField nameField;
    @FXML private TextArea addressField;
    @FXML private TextField contactNumberField;
    @FXML private TextField emailField;
    
    // Hospital Details fields
    @FXML private TextField websiteField;
    @FXML private TextArea descriptionField;
    
    // Account Information fields
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    
    // Navigation buttons
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;
    
    private final DatabaseManager dbManager = new DatabaseManager();
    private int currentTabIndex = 0;
    private final String[] stepTexts = {
        "Step 1 of 3: Hospital Information", 
        "Step 2 of 3: Hospital Details", 
        "Step 3 of 3: Account Information"
    };
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Disable tab selection by clicking
        registrationTabPane.setTabDragPolicy(TabPane.TabDragPolicy.FIXED);
        
        // Set up tab change listener to update buttons and step indicator
        registrationTabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            currentTabIndex = newValue.intValue();
            updateNavigationButtons();
            stepIndicator.setText(stepTexts[currentTabIndex]);
        });
        
        // Initial button state
        updateNavigationButtons();
    }
    
    private void updateNavigationButtons() {
        prevButton.setDisable(currentTabIndex == 0);
        nextButton.setVisible(currentTabIndex < registrationTabPane.getTabs().size() - 1);
        registerButton.setVisible(currentTabIndex == registrationTabPane.getTabs().size() - 1);
    }
    
    @FXML
    private void handlePrevious() {
        if (currentTabIndex > 0) {
            registrationTabPane.getSelectionModel().select(currentTabIndex - 1);
        }
    }
    
    @FXML
    private void handleNext() {
        // Validate current tab before proceeding
        if (validateCurrentTab()) {
            registrationTabPane.getSelectionModel().select(currentTabIndex + 1);
        }
    }
    
    private boolean validateCurrentTab() {
        messageLabel.setText("");
        
        switch (currentTabIndex) {
            case 0: // Hospital Information
                if (nameField.getText().trim().isEmpty()) {
                    messageLabel.setText("Hospital name is required.");
                    return false;
                }
                if (addressField.getText().trim().isEmpty()) {
                    messageLabel.setText("Address is required.");
                    return false;
                }
                if (contactNumberField.getText().trim().isEmpty()) {
                    messageLabel.setText("Contact number is required.");
                    return false;
                }
                if (emailField.getText().trim().isEmpty()) {
                    messageLabel.setText("Email is required.");
                    return false;
                }
                if (!emailField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    messageLabel.setText("Please enter a valid email address.");
                    return false;
                }
                break;
                
            case 1: // Hospital Details
                // These fields are optional, but we can add validation if needed
                break;
                
            case 2: // Account Information
                if (usernameField.getText().trim().isEmpty()) {
                    messageLabel.setText("Username is required.");
                    return false;
                }
                if (passwordField.getText().isEmpty()) {
                    messageLabel.setText("Password is required.");
                    return false;
                }
                if (passwordField.getText().length() < 6) {
                    messageLabel.setText("Password must be at least 6 characters long.");
                    return false;
                }
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    messageLabel.setText("Passwords do not match.");
                    return false;
                }
                break;
        }
        
        return true;
    }
    
    @FXML
    private void handleRegister() {
        if (!validateCurrentTab()) {
            return;
        }
        
        try {
            // Check if username already exists
            if (dbManager.isUsernameExists(usernameField.getText())) {
                messageLabel.setText("Username already exists. Please choose another one.");
                return;
            }
            
            // Create hospital object
            Hospital hospital = new Hospital(
                0, // ID will be assigned by the database
                nameField.getText().trim(),
                addressField.getText().trim(),
                contactNumberField.getText().trim(),
                emailField.getText().trim(),
                websiteField.getText().trim(),
                usernameField.getText().trim(),
                passwordField.getText(),
                descriptionField.getText().trim()
            );
            
            // Save to database
            dbManager.addHospital(hospital);
            
            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText(null);
            alert.setContentText("Hospital registration successful! You can now log in with your credentials.");
            alert.showAndWait();
            
            // Navigate to login screen
            navigateToLogin();
            
        } catch (SQLException e) {
            messageLabel.setText("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void backToRegistrationChoice() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/registration-choice.fxml"));
            Stage stage = (Stage) registrationTabPane.getScene().getWindow();
            Scene scene = new Scene(root, Main.LOGIN_WIDTH, Main.LOGIN_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Registration Choice - City Hospital");
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.out.println("Error going back to registration choice: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/login.fxml"));
            Stage stage = (Stage) registrationTabPane.getScene().getWindow();
            Scene scene = new Scene(root, Main.LOGIN_WIDTH, Main.LOGIN_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("City Hospital Management System");
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.out.println("Error navigating to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 