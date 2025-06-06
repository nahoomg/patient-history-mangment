package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Doctor;
import com.hospital.models.TreatmentRequest;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DoctorDashboardController implements Initializable {
    // Dashboard elements
    @FXML private Label welcomeLabel;
    @FXML private Label assignedPatientsLabel;
    @FXML private Label pendingTreatmentsLabel;
    @FXML private Label completedTreatmentsLabel;
    @FXML private PieChart treatmentStatusChart;
    @FXML private BarChart<String, Number> treatmentUrgencyChart;
    
    // Profile elements
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label specializationLabel;
    @FXML private Label contactNumberLabel;
    @FXML private Label emailLabel;
    @FXML private Label usernameLabel;
    
    // Assigned patients elements
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private TableView<TreatmentRequest> assignedPatientsTable;
    @FXML private TableColumn<TreatmentRequest, String> treatmentIdColumn;
    @FXML private TableColumn<TreatmentRequest, String> patientNameColumn;
    @FXML private TableColumn<TreatmentRequest, String> dateRequestedColumn;
    @FXML private TableColumn<TreatmentRequest, String> preferredDateColumn;
    @FXML private TableColumn<TreatmentRequest, String> urgencyColumn;
    @FXML private TableColumn<TreatmentRequest, String> statusColumn;
    @FXML private VBox treatmentDetailsPane;
    @FXML private Label selectedPatientLabel;
    @FXML private TextArea symptomsTextArea;
    @FXML private ComboBox<String> treatmentStatusComboBox;
    @FXML private TextArea treatmentNotesArea;
    
    private Doctor currentDoctor;
    private DatabaseManager dbManager;
    private ObservableList<TreatmentRequest> treatmentRequestsList;
    private TreatmentRequest selectedTreatmentRequest;
    private Map<Integer, String> treatmentNotes = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        treatmentRequestsList = FXCollections.observableArrayList();
        
        initializeStatusFilter();
        initializeAssignedPatientsTable();
        initializeTreatmentStatusComboBox();
        
        // Hide treatment details pane initially
        treatmentDetailsPane.setVisible(false);
    }

    public void setDoctor(Doctor doctor) {
        this.currentDoctor = doctor;
        welcomeLabel.setText("Welcome, Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
        
        // Set profile information
        firstNameLabel.setText(doctor.getFirstName());
        lastNameLabel.setText(doctor.getLastName());
        specializationLabel.setText(doctor.getSpecialization());
        contactNumberLabel.setText(doctor.getContactNumber());
        emailLabel.setText(doctor.getEmail());
        usernameLabel.setText(doctor.getUsername());
        
        // Load data
        loadData();
    }
    
    private void initializeStatusFilter() {
        statusFilterComboBox.getItems().addAll("All", "Pending", "Assigned", "In Progress", "Completed", "Cancelled");
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.setOnAction(e -> filterTreatmentRequests());
    }
    
    private void initializeAssignedPatientsTable() {
        // Set up table columns
        treatmentIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        patientNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName()));
        dateRequestedColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateRequested().toString()));
        preferredDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPreferredDate().toString()));
        urgencyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrgency()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Set up selection handler
        assignedPatientsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedTreatmentRequest = newSelection;
                    showTreatmentDetails(newSelection);
                }
            }
        );
        
        assignedPatientsTable.setItems(treatmentRequestsList);
    }
    
    private void initializeTreatmentStatusComboBox() {
        treatmentStatusComboBox.getItems().addAll("Assigned", "In Progress", "Completed", "Cancelled");
    }
    
    private void loadData() {
        try {
            // Load assigned treatments
            loadAssignedTreatments();
            
            // Update dashboard statistics
            updateDashboardStatistics();
            
        } catch (SQLException e) {
            showAlert("Error loading data: " + e.getMessage());
        }
    }
    
    private void loadAssignedTreatments() throws SQLException {
        List<TreatmentRequest> doctorTreatments;
        
        if (currentDoctor.getHospitalId() > 0) {
            // Get treatments for the current doctor and hospital
            doctorTreatments = dbManager.getTreatmentRequestsByDoctorAndHospital(
                currentDoctor.getId(), currentDoctor.getHospitalId());
        } else {
            // Fallback to get all treatments assigned to this doctor
            List<TreatmentRequest> allTreatments = dbManager.getAllTreatmentRequests();
            doctorTreatments = allTreatments.stream()
                .filter(t -> t.getAssignedDoctorId() == currentDoctor.getId())
                .collect(Collectors.toList());
        }
        
        treatmentRequestsList.clear();
        treatmentRequestsList.addAll(doctorTreatments);
        
        System.out.println("Loaded " + treatmentRequestsList.size() + " treatments for Dr. " + 
                          currentDoctor.getFirstName() + " " + currentDoctor.getLastName() + 
                          " at hospital ID: " + currentDoctor.getHospitalId());
    }
    
    private void updateDashboardStatistics() {
        // Count treatments by status
        int totalAssigned = treatmentRequestsList.size();
        int pending = countTreatmentsByStatus("Pending");
        int inProgress = countTreatmentsByStatus("In Progress");
        int completed = countTreatmentsByStatus("Completed");
        
        // Update labels
        assignedPatientsLabel.setText(String.valueOf(totalAssigned));
        pendingTreatmentsLabel.setText(String.valueOf(pending + inProgress));
        completedTreatmentsLabel.setText(String.valueOf(completed));
        
        // Update treatment status chart
        treatmentStatusChart.getData().clear();
        if (pending > 0) treatmentStatusChart.getData().add(new PieChart.Data("Pending", pending));
        if (inProgress > 0) treatmentStatusChart.getData().add(new PieChart.Data("In Progress", inProgress));
        if (completed > 0) treatmentStatusChart.getData().add(new PieChart.Data("Completed", completed));
        
        // Update treatment urgency chart
        treatmentUrgencyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Treatment Requests");
        
        int lowUrgency = countTreatmentsByUrgency("Low");
        int mediumUrgency = countTreatmentsByUrgency("Medium");
        int highUrgency = countTreatmentsByUrgency("High");
        int emergencyUrgency = countTreatmentsByUrgency("Emergency");
        
        series.getData().add(new XYChart.Data<>("Low", lowUrgency));
        series.getData().add(new XYChart.Data<>("Medium", mediumUrgency));
        series.getData().add(new XYChart.Data<>("High", highUrgency));
        series.getData().add(new XYChart.Data<>("Emergency", emergencyUrgency));
        
        treatmentUrgencyChart.getData().add(series);
    }
    
    private int countTreatmentsByStatus(String status) {
        return (int) treatmentRequestsList.stream()
            .filter(t -> status.equals(t.getStatus()))
            .count();
    }
    
    private int countTreatmentsByUrgency(String urgency) {
        return (int) treatmentRequestsList.stream()
            .filter(t -> urgency.equals(t.getUrgency()))
            .count();
    }
    
    private void showTreatmentDetails(TreatmentRequest request) {
        selectedPatientLabel.setText(request.getPatientName());
        symptomsTextArea.setText(request.getSymptoms());
        treatmentStatusComboBox.setValue(request.getStatus());
        
        // Load treatment notes if available
        String notes = treatmentNotes.getOrDefault(request.getId(), "");
        treatmentNotesArea.setText(notes);
        
        treatmentDetailsPane.setVisible(true);
    }
    
    private void filterTreatmentRequests() {
        try {
            String selectedStatus = statusFilterComboBox.getValue();
            
            if ("All".equals(selectedStatus)) {
                loadAssignedTreatments();
            } else {
                List<TreatmentRequest> doctorTreatments;
                
                if (currentDoctor.getHospitalId() > 0) {
                    // Get all treatments for this doctor and hospital
                    doctorTreatments = dbManager.getTreatmentRequestsByDoctorAndHospital(
                        currentDoctor.getId(), currentDoctor.getHospitalId());
                } else {
                    // Fallback to get all treatments assigned to this doctor
                    List<TreatmentRequest> allTreatments = dbManager.getAllTreatmentRequests();
                    doctorTreatments = allTreatments.stream()
                        .filter(t -> t.getAssignedDoctorId() == currentDoctor.getId())
                        .collect(Collectors.toList());
                }
                
                // Filter by status
                List<TreatmentRequest> filteredTreatments = doctorTreatments.stream()
                    .filter(t -> selectedStatus.equals(t.getStatus()))
                    .collect(Collectors.toList());
                
                treatmentRequestsList.clear();
                treatmentRequestsList.addAll(filteredTreatments);
            }
        } catch (SQLException e) {
            showAlert("Error filtering treatment requests: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefreshAssignments() {
        try {
            loadAssignedTreatments();
            updateDashboardStatistics();
        } catch (SQLException e) {
            showAlert("Error refreshing assignments: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateTreatment() {
        if (selectedTreatmentRequest == null) {
            showAlert("No treatment selected");
            return;
        }
        
        String newStatus = treatmentStatusComboBox.getValue();
        String notes = treatmentNotesArea.getText();
        
        try {
            // Update treatment status in database
            selectedTreatmentRequest.setStatus(newStatus);
            dbManager.updateTreatmentRequest(selectedTreatmentRequest);
            
            // Save treatment notes
            treatmentNotes.put(selectedTreatmentRequest.getId(), notes);
            
            // Update patient medical history if treatment is completed
            if ("Completed".equals(newStatus)) {
                updatePatientMedicalHistory(selectedTreatmentRequest, notes, newStatus);
            }
            
            // Refresh data
            loadAssignedTreatments();
            updateDashboardStatistics();
            
            showAlert("Treatment updated successfully");
            
        } catch (SQLException e) {
            showAlert("Error updating treatment: " + e.getMessage());
        }
    }
    
    private void updatePatientMedicalHistory(TreatmentRequest request, String notes, String status) throws SQLException {
        // Get patient's current medical history
        String currentHistory = dbManager.getPatientMedicalHistory(request.getPatientId());
        
        // Add new treatment record
        LocalDate today = LocalDate.now();
        String treatmentRecord = "\n--- Treatment on " + today + " ---\n" +
                                "Doctor: Dr. " + currentDoctor.getFirstName() + " " + currentDoctor.getLastName() + "\n" +
                                "Symptoms: " + request.getSymptoms() + "\n" +
                                "Treatment: " + notes + "\n" +
                                "Status: " + status + "\n";
        
        // Update patient's medical history
        String updatedHistory = currentHistory + treatmentRecord;
        dbManager.updatePatientMedicalHistory(request.getPatientId(), updatedHistory);
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
            showAlert("Error logging out: " + e.getMessage());
        }
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 