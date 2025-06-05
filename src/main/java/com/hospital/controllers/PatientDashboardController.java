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
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

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
        firstNameLabel.setText(patient.getFirstName());
        lastNameLabel.setText(patient.getLastName());
        dobLabel.setText(patient.getDateOfBirth().toString());
        genderLabel.setText(patient.getGender());
        contactLabel.setText(patient.getContactNumber());
        addressLabel.setText(patient.getAddress());
        
        // Display medical history
        medicalHistoryArea.setText(patient.getMedicalHistory());
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