package com.hospital.controllers;

import com.hospital.models.Patient;
import com.hospital.models.TreatmentRequest;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;

public class PatientDashboardController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label dobLabel;
    @FXML private Label genderLabel;
    @FXML private Label contactLabel;
    @FXML private Label addressLabel;
    @FXML private TextArea medicalHistoryArea;
    @FXML private TextArea symptomsArea;
    @FXML private DatePicker preferredDatePicker;
    @FXML private ComboBox<String> urgencyComboBox;
    @FXML private Label treatmentMessageLabel;
    
    private Patient currentPatient;
    private DatabaseManager dbManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        preferredDatePicker.setValue(LocalDate.now().plusDays(1));
        urgencyComboBox.getItems().addAll("Low", "Medium", "High", "Emergency");
        urgencyComboBox.setValue("Medium");
    }

    public void setPatient(Patient patient) {
        this.currentPatient = patient;
        
        // Set welcome message
        welcomeLabel.setText("Welcome, " + patient.getFirstName() + " " + patient.getLastName());
        
        // Display patient information
        updatePatientInfoDisplay();
        
        // Display medical history
        medicalHistoryArea.setText(patient.getMedicalHistory());
    }
    
    private void updatePatientInfoDisplay() {
        firstNameLabel.setText(currentPatient.getFirstName());
        lastNameLabel.setText(currentPatient.getLastName());
        dobLabel.setText(currentPatient.getDateOfBirth().toString());
        genderLabel.setText(currentPatient.getGender());
        contactLabel.setText(currentPatient.getContactNumber());
        addressLabel.setText(currentPatient.getAddress());
    }

    @FXML
    private void handleEditPersonalInfo() {
        // Create a dialog for editing patient information
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Edit Personal Information");
        dialog.setHeaderText("Update your personal information");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create fields with current values
        TextField firstNameField = new TextField(currentPatient.getFirstName());
        TextField lastNameField = new TextField(currentPatient.getLastName());
        TextField contactField = new TextField(currentPatient.getContactNumber());
        TextField addressField = new TextField(currentPatient.getAddress());
        
        // Add labels and fields to grid
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Contact Number:"), 0, 2);
        grid.add(contactField, 1, 2);
        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressField, 1, 3);
        
        // Set the dialog content
        dialog.getDialogPane().setContent(grid);
        
        // Style the dialog
        dialog.getDialogPane().setStyle("-fx-background-color: white;");
        dialog.getDialogPane().getStyleClass().add("modern-dialog");
        
        // Request focus on the first field
        firstNameField.requestFocus();
        
        // Convert the result to a patient when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Update patient object with new values
                currentPatient.setFirstName(firstNameField.getText());
                currentPatient.setLastName(lastNameField.getText());
                currentPatient.setContactNumber(contactField.getText());
                currentPatient.setAddress(addressField.getText());
                return currentPatient;
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            try {
                // Update the patient in the database
                dbManager.updatePatient(patient);
                
                // Update the UI
                updatePatientInfoDisplay();
                welcomeLabel.setText("Welcome, " + patient.getFirstName() + " " + patient.getLastName());
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Your personal information has been updated successfully.");
                alert.showAndWait();
                
            } catch (SQLException e) {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to update personal information: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void handleTreatmentRequest() {
        if (symptomsArea.getText().isEmpty()) {
            showTreatmentMessage("Please describe your symptoms", true);
            return;
        }

        try {
            // Create a treatment request entry
            TreatmentRequest request = new TreatmentRequest();
            request.setPatientId(currentPatient.getId());
            request.setPatientName(currentPatient.getFirstName() + " " + currentPatient.getLastName());
            request.setDateRequested(LocalDate.now());
            request.setPreferredDate(preferredDatePicker.getValue());
            request.setUrgency(urgencyComboBox.getValue());
            request.setSymptoms(symptomsArea.getText());
            request.setStatus("Pending");
            
            // Save to database
            dbManager.addTreatmentRequest(request);
            
            // Also update medical history for patient's records
            String treatmentRequest = "Date: " + LocalDate.now() + "\n" +
                                      "Preferred Date: " + preferredDatePicker.getValue() + "\n" +
                                      "Urgency: " + urgencyComboBox.getValue() + "\n" +
                                      "Symptoms: " + symptomsArea.getText() + "\n\n";
            
            // Append to medical history
            String updatedHistory = currentPatient.getMedicalHistory() + "\n--- NEW TREATMENT REQUEST ---\n" + treatmentRequest;
            currentPatient.setMedicalHistory(updatedHistory);
            
            // Update in database
            dbManager.updatePatientHistory(currentPatient.getId(), updatedHistory);
            
            // Update the medical history display
            medicalHistoryArea.setText(updatedHistory);
            
            // Clear the form
            symptomsArea.clear();
            preferredDatePicker.setValue(LocalDate.now().plusDays(1));
            urgencyComboBox.setValue("Medium");
            
            showTreatmentMessage("Treatment request submitted successfully", false);
        } catch (SQLException e) {
            showTreatmentMessage("Error submitting treatment request: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("City Hospital Management System");
            stage.show();
        } catch (IOException e) {
            System.out.println("Error logging out: " + e.getMessage());
        }
    }
    
    private void showTreatmentMessage(String message, boolean isError) {
        treatmentMessageLabel.setText(message);
        treatmentMessageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
} 