package com.hospital.controllers;

import com.hospital.models.Hospital;
import com.hospital.models.Patient;
import com.hospital.models.TreatmentRequest;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class TreatmentRequestFormController implements Initializable {
    @FXML private ComboBox<Hospital> hospitalComboBox;
    @FXML private DatePicker preferredDatePicker;
    @FXML private ComboBox<String> urgencyComboBox;
    @FXML private TextArea symptomsTextArea;
    
    private DatabaseManager dbManager;
    private Patient currentPatient;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        
        // Set up urgency combobox
        urgencyComboBox.getItems().addAll("Emergency", "High", "Medium", "Low");
        
        // Set preferred date to default to tomorrow
        preferredDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Load all hospitals
        loadHospitals();
    }
    
    public void setPatient(Patient patient) {
        this.currentPatient = patient;
    }
    
    private void loadHospitals() {
        try {
            List<Hospital> hospitals = dbManager.getAllHospitals();
            hospitalComboBox.getItems().addAll(hospitals);
            
            // Set up custom cell factory to display hospital names properly
            hospitalComboBox.setCellFactory(param -> new ListCell<Hospital>() {
                @Override
                protected void updateItem(Hospital hospital, boolean empty) {
                    super.updateItem(hospital, empty);
                    if (empty || hospital == null) {
                        setText(null);
                    } else {
                        setText(hospital.getName());
                    }
                }
            });
            
            hospitalComboBox.setButtonCell(new ListCell<Hospital>() {
                @Override
                protected void updateItem(Hospital hospital, boolean empty) {
                    super.updateItem(hospital, empty);
                    if (empty || hospital == null) {
                        setText(null);
                    } else {
                        setText(hospital.getName());
                    }
                }
            });
            
        } catch (SQLException e) {
            showAlert("Error loading hospitals: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleSubmit() {
        // Validate inputs
        if (hospitalComboBox.getValue() == null) {
            showAlert("Please select a hospital", Alert.AlertType.WARNING);
            return;
        }
        
        if (preferredDatePicker.getValue() == null) {
            showAlert("Please select a preferred date", Alert.AlertType.WARNING);
            return;
        }
        
        if (urgencyComboBox.getValue() == null) {
            showAlert("Please select an urgency level", Alert.AlertType.WARNING);
            return;
        }
        
        if (symptomsTextArea.getText().trim().isEmpty()) {
            showAlert("Please describe your symptoms", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            // Create treatment request
            TreatmentRequest request = new TreatmentRequest();
            request.setPatientId(currentPatient.getId());
            request.setPatientName(currentPatient.getFirstName() + " " + currentPatient.getLastName());
            request.setDateRequested(LocalDate.now());
            request.setPreferredDate(preferredDatePicker.getValue());
            request.setUrgency(urgencyComboBox.getValue());
            request.setSymptoms(symptomsTextArea.getText().trim());
            request.setStatus("Pending");
            
            // Set the selected hospital
            Hospital selectedHospital = hospitalComboBox.getValue();
            request.setHospitalId(selectedHospital.getId());
            request.setHospitalName(selectedHospital.getName());
            
            System.out.println("Creating request for hospital: " + selectedHospital.getName() + " (ID: " + selectedHospital.getId() + ")");
            
            try {
                // Save to database
                dbManager.addTreatmentRequest(request);
                
                // Update patient's medical history
                String historyUpdate = "\n--- TREATMENT REQUEST SUBMITTED ---\n" +
                                      "Date: " + LocalDate.now() + "\n" +
                                      "Hospital: " + selectedHospital.getName() + "\n" +
                                      "Preferred Date: " + preferredDatePicker.getValue() + "\n" +
                                      "Urgency: " + urgencyComboBox.getValue() + "\n" +
                                      "Symptoms: " + symptomsTextArea.getText().trim() + "\n\n";
                
                String currentHistory = currentPatient.getMedicalHistory();
                if (currentHistory == null) currentHistory = "";
                dbManager.updatePatientMedicalHistory(currentPatient.getId(), currentHistory + historyUpdate);
                
                showAlert("Treatment request submitted successfully", Alert.AlertType.INFORMATION);
                closeForm();
            } catch (SQLException e) {
                showAlert("Error submitting treatment request: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        } catch (Exception e) {
            showAlert("Error: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCancel() {
        closeForm();
    }
    
    private void closeForm() {
        Stage stage = (Stage) hospitalComboBox.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType == Alert.AlertType.ERROR ? "Error" : 
                      alertType == Alert.AlertType.WARNING ? "Warning" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 