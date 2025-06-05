package com.hospital.controllers;

import com.hospital.models.Hospital;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;

public class HospitalFormController {

    @FXML private TextField nameField;
    @FXML private TextArea addressField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;
    @FXML private TextField websiteField;
    @FXML private TextArea descriptionField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorMessageLabel;

    private DatabaseManager dbManager;
    private Hospital hospitalToEdit;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
    }

    public void setHospitalToEdit(Hospital hospital) {
        this.hospitalToEdit = hospital;
        this.isEditMode = true;
        
        // Populate form fields with hospital data
        nameField.setText(hospital.getName());
        addressField.setText(hospital.getAddress());
        contactField.setText(hospital.getContactNumber());
        emailField.setText(hospital.getEmail());
        websiteField.setText(hospital.getWebsite());
        descriptionField.setText(hospital.getDescription());
        usernameField.setText(hospital.getUsername());
        
        // Don't populate password fields for security
        passwordField.setPromptText("Enter new password (leave empty to keep current)");
        confirmPasswordField.setPromptText("Confirm new password (leave empty to keep current)");
        
        // Update window title
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.setTitle("Edit Hospital");
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            if (isEditMode) {
                updateHospital();
            } else {
                addNewHospital();
            }
            
            // Close the form
            closeForm();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addNewHospital() throws SQLException {
        // Create new hospital object
        Hospital newHospital = new Hospital(
            0, // ID will be set by database
            nameField.getText().trim(),
            addressField.getText().trim(),
            contactField.getText().trim(),
            emailField.getText().trim(),
            websiteField.getText().trim(),
            usernameField.getText().trim(),
            passwordField.getText(),
            descriptionField.getText().trim()
        );
        
        // Check if username already exists
        if (dbManager.isUsernameExists(newHospital.getUsername())) {
            showError("Username already exists. Please choose another username.");
            usernameField.requestFocus();
            return;
        }
        
        // Add hospital to database
        dbManager.addHospital(newHospital);
        showSuccessAlert("Hospital added successfully");
    }
    
    private void updateHospital() throws SQLException {
        // Update hospital object with new values
        hospitalToEdit.setName(nameField.getText().trim());
        hospitalToEdit.setAddress(addressField.getText().trim());
        hospitalToEdit.setContactNumber(contactField.getText().trim());
        hospitalToEdit.setEmail(emailField.getText().trim());
        hospitalToEdit.setWebsite(websiteField.getText().trim());
        hospitalToEdit.setDescription(descriptionField.getText().trim());
        
        // Check if username was changed and if it's already taken
        String newUsername = usernameField.getText().trim();
        if (!newUsername.equals(hospitalToEdit.getUsername())) {
            hospitalToEdit.setUsername(newUsername);
            if (dbManager.isUsernameExists(newUsername)) {
                showError("Username already exists. Please choose another username.");
                usernameField.requestFocus();
                return;
            }
        }
        
        // Update password if provided
        String newPassword = passwordField.getText();
        if (!newPassword.isEmpty()) {
            hospitalToEdit.setPassword(newPassword);
        }
        
        // Update hospital in database
        dbManager.updateHospital(hospitalToEdit);
        showSuccessAlert("Hospital updated successfully");
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }
    
    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
    
    private boolean validateInputs() {
        // Reset error message
        hideError();
        
        // Validate required fields
        if (nameField.getText().trim().isEmpty()) {
            showError("Hospital name is required");
            nameField.requestFocus();
            return false;
        }
        
        if (addressField.getText().trim().isEmpty()) {
            showError("Address is required");
            addressField.requestFocus();
            return false;
        }
        
        if (contactField.getText().trim().isEmpty()) {
            showError("Contact number is required");
            contactField.requestFocus();
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required");
            emailField.requestFocus();
            return false;
        }
        
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required");
            usernameField.requestFocus();
            return false;
        }
        
        // Password validation only required for new hospitals
        if (!isEditMode && passwordField.getText().isEmpty()) {
            showError("Password is required");
            passwordField.requestFocus();
            return false;
        }
        
        // Validate password match if provided
        if (!passwordField.getText().isEmpty() && 
            !passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        // Validate email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!emailField.getText().trim().matches(emailRegex)) {
            showError("Invalid email format");
            emailField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
    }
    
    private void hideError() {
        errorMessageLabel.setVisible(false);
    }
    
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 