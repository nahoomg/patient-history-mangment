package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Hospital;
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
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

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
    @FXML private ComboBox<Hospital> hospitalComboBox;
    @FXML private Label treatmentMessageLabel;
    
    private Patient currentPatient;
    private DatabaseManager dbManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        preferredDatePicker.setValue(LocalDate.now().plusDays(1));
        urgencyComboBox.getItems().addAll("Low", "Medium", "High", "Emergency");
        urgencyComboBox.setValue("Medium");
        
        // Load hospitals into the combo box
        loadHospitals();
        
        // Load patient data from session
        try {
            currentPatient = (Patient) com.hospital.utils.Session.getInstance().getCurrentUser();
            if (currentPatient != null) {
                setPatient(currentPatient);
                System.out.println("Patient data loaded from session: " + currentPatient.getFirstName() + " " + currentPatient.getLastName());
            } else {
                System.err.println("No patient data found in session");
                showTreatmentMessage("Error: No patient data found", true);
            }
        } catch (ClassCastException e) {
            System.err.println("Error casting session data to Patient: " + e.getMessage());
            showTreatmentMessage("Error loading patient data", true);
        }
    }
    
    private void loadHospitals() {
        try {
            List<Hospital> hospitals = dbManager.getAllHospitals();
            hospitalComboBox.getItems().clear();
            hospitalComboBox.getItems().addAll(hospitals);
            
            // Set up a custom cell factory to display hospital names
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
            
            // Set up a custom button cell to display the selected hospital name
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
            
            System.out.println("Loaded " + hospitals.size() + " hospitals into combo box");
        } catch (SQLException e) {
            System.err.println("Error loading hospitals: " + e.getMessage());
            showTreatmentMessage("Error loading hospitals", true);
        }
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
        // Validate inputs
        if (symptomsArea.getText().trim().isEmpty()) {
            showTreatmentMessage("Please describe your symptoms", true);
            return;
        }
        
        if (hospitalComboBox.getValue() == null) {
            showTreatmentMessage("Please select a hospital", true);
            return;
        }
        
        if (preferredDatePicker.getValue() == null) {
            showTreatmentMessage("Please select a preferred date", true);
            return;
        }
        
        // Create treatment request
        try {
            TreatmentRequest request = new TreatmentRequest();
            request.setPatientId(currentPatient.getId());
            request.setPatientName(currentPatient.getFirstName() + " " + currentPatient.getLastName());
            request.setDateRequested(LocalDate.now());
            request.setPreferredDate(preferredDatePicker.getValue());
            request.setUrgency(urgencyComboBox.getValue());
            request.setSymptoms(symptomsArea.getText().trim());
            request.setStatus("Pending");
            
            // Set hospital information
            Hospital selectedHospital = hospitalComboBox.getValue();
            request.setHospitalId(selectedHospital.getId());
            request.setHospitalName(selectedHospital.getName());
            
            System.out.println("Creating treatment request for hospital: " + selectedHospital.getName() + " (ID: " + selectedHospital.getId() + ")");
            
            // Save to database
            dbManager.addTreatmentRequest(request);
            
            // Update patient's medical history
            String historyUpdate = "\n--- TREATMENT REQUEST SUBMITTED ---\n" +
                                  "Date: " + LocalDate.now() + "\n" +
                                  "Hospital: " + selectedHospital.getName() + "\n" +
                                  "Preferred Date: " + preferredDatePicker.getValue() + "\n" +
                                  "Urgency: " + urgencyComboBox.getValue() + "\n" +
                                  "Symptoms: " + symptomsArea.getText().trim() + "\n\n";
            
            String currentHistory = currentPatient.getMedicalHistory();
            if (currentHistory == null) currentHistory = "";
            dbManager.updatePatientMedicalHistory(currentPatient.getId(), currentHistory + historyUpdate);
            
            // Update the medical history in the UI
            currentPatient.setMedicalHistory(currentHistory + historyUpdate);
            medicalHistoryArea.setText(currentPatient.getMedicalHistory());
            
            // Clear form and show success message
            symptomsArea.clear();
            preferredDatePicker.setValue(LocalDate.now().plusDays(1));
            urgencyComboBox.setValue("Medium");
            hospitalComboBox.setValue(null);
            
            showTreatmentMessage("Treatment request submitted successfully", false);
            
        } catch (SQLException e) {
            showTreatmentMessage("Error submitting treatment request: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            Scene scene = new Scene(root, Main.LOGIN_WIDTH, Main.LOGIN_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("City Hospital Management System - Login");
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showTreatmentMessage("Error logging out: " + e.getMessage(), true);
        }
    }
    
    private void showTreatmentMessage(String message, boolean isError) {
        treatmentMessageLabel.setText(message);
        treatmentMessageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
} 