package com.hospital.controllers;

import com.hospital.models.Patient;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.time.LocalDate;

public class PatientFormController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField contactField;
    @FXML private TextArea addressField;
    @FXML private TextArea medicalHistoryField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorMessageLabel;

    private DatabaseManager dbManager;
    private Patient patientToEdit;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        dbManager = DatabaseManager.getInstance();
        
        // Initialize gender options
        genderComboBox.getItems().addAll("Male", "Female", "Other");
    }

    public void setPatientToEdit(Patient patient) {
        this.patientToEdit = patient;
        this.isEditMode = true;
        
        // Populate form fields with patient data
        firstNameField.setText(patient.getFirstName());
        lastNameField.setText(patient.getLastName());
        dobPicker.setValue(patient.getDateOfBirth());
        genderComboBox.setValue(patient.getGender());
        contactField.setText(patient.getContactNumber());
        addressField.setText(patient.getAddress());
        medicalHistoryField.setText(patient.getMedicalHistory());
        usernameField.setText(patient.getUsername());
        
        // Don't populate password fields for security
        passwordField.setPromptText("Enter new password (leave empty to keep current)");
        confirmPasswordField.setPromptText("Confirm new password (leave empty to keep current)");
        
        // Update window title
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.setTitle("Edit Patient");
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            if (isEditMode) {
                updatePatient();
            } else {
                addNewPatient();
            }
            
            // Close the form
            closeForm();
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addNewPatient() throws SQLException {
        // Create new patient object
        Patient newPatient = new Patient(
            0, // ID will be set by database
            firstNameField.getText().trim(),
            lastNameField.getText().trim(),
            dobPicker.getValue(),
            genderComboBox.getValue(),
            contactField.getText().trim(),
            addressField.getText().trim(),
            medicalHistoryField.getText().trim(),
            usernameField.getText().trim(),
            passwordField.getText()
        );
        
        // Check if username already exists
        if (dbManager.isUsernameExists(newPatient.getUsername())) {
            showError("Username already exists. Please choose another username.");
            usernameField.requestFocus();
            return;
        }
        
        // Add patient to database
        dbManager.addPatient(newPatient);
        showSuccessAlert("Patient added successfully");
    }
    
    private void updatePatient() throws SQLException {
        // Update patient object with new values
        patientToEdit.setFirstName(firstNameField.getText().trim());
        patientToEdit.setLastName(lastNameField.getText().trim());
        patientToEdit.setDateOfBirth(dobPicker.getValue());
        patientToEdit.setGender(genderComboBox.getValue());
        patientToEdit.setContactNumber(contactField.getText().trim());
        patientToEdit.setAddress(addressField.getText().trim());
        patientToEdit.setMedicalHistory(medicalHistoryField.getText().trim());
        
        // Check if username was changed and if it's already taken
        String newUsername = usernameField.getText().trim();
        if (!newUsername.equals(patientToEdit.getUsername())) {
            patientToEdit.setUsername(newUsername);
            if (dbManager.isUsernameExists(newUsername)) {
                showError("Username already exists. Please choose another username.");
                usernameField.requestFocus();
                return;
            }
        }
        
        // Update password if provided
        String newPassword = passwordField.getText();
        if (!newPassword.isEmpty()) {
            patientToEdit.setPassword(newPassword);
        }
        
        // Update patient in database
        dbManager.updatePatient(patientToEdit);
        showSuccessAlert("Patient updated successfully");
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }
    
    private void closeForm() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
    
    private boolean validateInputs() {
        // Reset error message
        hideError();
        
        // Validate required fields
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name is required");
            firstNameField.requestFocus();
            return false;
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name is required");
            lastNameField.requestFocus();
            return false;
        }
        
        if (dobPicker.getValue() == null) {
            showError("Date of birth is required");
            dobPicker.requestFocus();
            return false;
        }
        
        // Validate date of birth is not in the future
        if (dobPicker.getValue().isAfter(LocalDate.now())) {
            showError("Date of birth cannot be in the future");
            dobPicker.requestFocus();
            return false;
        }
        
        if (genderComboBox.getValue() == null) {
            showError("Gender is required");
            genderComboBox.requestFocus();
            return false;
        }
        
        if (contactField.getText().trim().isEmpty()) {
            showError("Contact number is required");
            contactField.requestFocus();
            return false;
        }
        
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required");
            usernameField.requestFocus();
            return false;
        }
        
        // Password validation only required for new patients
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