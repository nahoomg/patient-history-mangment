package com.hospital.controllers;

import com.hospital.models.Patient;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MedicalHistoryEditorController implements Initializable {
    @FXML private Label patientNameLabel;
    @FXML private TextArea medicalHistoryTextArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Patient patient;
    private DatabaseManager dbManager;
    private boolean saveClicked = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        
        saveButton.setOnAction(e -> handleSave());
        cancelButton.setOnAction(e -> handleCancel());
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
        patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
        
        try {
            String medicalHistory = dbManager.getPatientMedicalHistory(patient.getId());
            medicalHistoryTextArea.setText(medicalHistory);
        } catch (SQLException e) {
            e.printStackTrace();
            medicalHistoryTextArea.setText("Error loading medical history: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            String updatedHistory = medicalHistoryTextArea.getText();
            dbManager.updatePatientMedicalHistory(patient.getId(), updatedHistory);
            saveClicked = true;
            closeStage();
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error message
        }
    }
    
    @FXML
    private void handleCancel() {
        saveClicked = false;
        closeStage();
    }
    
    private void closeStage() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    
    public boolean isSaveClicked() {
        return saveClicked;
    }
} 