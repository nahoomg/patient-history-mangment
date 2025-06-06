package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Doctor;
import com.hospital.models.Hospital;
import com.hospital.models.Patient;
import com.hospital.models.TreatmentRequest;
import com.hospital.utils.ChartUtils;
import com.hospital.utils.DatabaseManager;
import com.hospital.utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class HospitalDashboardController implements Initializable {
    // Header elements
    @FXML private Label hospitalNameLabel;
    
    // Dashboard elements
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label pendingTreatmentsLabel;
    @FXML private PieChart genderDistributionChart;
    @FXML private BarChart<String, Number> treatmentUrgencyChart;
    
    // Doctor management elements
    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableColumn<Doctor, Integer> doctorIdColumn;
    @FXML private TableColumn<Doctor, String> doctorFirstNameColumn;
    @FXML private TableColumn<Doctor, String> doctorLastNameColumn;
    @FXML private TableColumn<Doctor, String> doctorSpecializationColumn;
    @FXML private TableColumn<Doctor, String> doctorContactColumn;
    @FXML private TableColumn<Doctor, String> doctorEmailColumn;
    @FXML private TextField doctorSearchField;
    
    // Patient management elements
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Integer> patientIdColumn;
    @FXML private TableColumn<Patient, String> patientFirstNameColumn;
    @FXML private TableColumn<Patient, String> patientLastNameColumn;
    @FXML private TableColumn<Patient, String> patientDobColumn;
    @FXML private TableColumn<Patient, String> patientGenderColumn;
    @FXML private TableColumn<Patient, String> patientContactColumn;
    @FXML private TextField patientSearchField;
    @FXML private VBox patientDetailsPane;
    @FXML private TextArea medicalHistoryTextArea;
    
    // Treatment requests elements
    @FXML private TableView<TreatmentRequest> treatmentRequestsTable;
    @FXML private TableColumn<TreatmentRequest, Integer> treatmentIdColumn;
    @FXML private TableColumn<TreatmentRequest, String> patientNameColumn;
    @FXML private TableColumn<TreatmentRequest, String> dateRequestedColumn;
    @FXML private TableColumn<TreatmentRequest, String> preferredDateColumn;
    @FXML private TableColumn<TreatmentRequest, String> urgencyColumn;
    @FXML private TableColumn<TreatmentRequest, String> statusColumn;
    @FXML private TableColumn<TreatmentRequest, String> assignedDoctorColumn;
    @FXML private ComboBox<String> urgencyFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private VBox treatmentDetailsPane;
    @FXML private Label selectedPatientLabel;
    @FXML private TextArea symptomsTextArea;
    @FXML private ComboBox<Doctor> doctorAssignmentComboBox;
    @FXML private ComboBox<String> statusComboBox;
    
    // Hospital profile elements
    @FXML private TextField nameField;
    @FXML private TextArea addressField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;
    @FXML private TextField websiteField;
    @FXML private TextArea descriptionField;
    
    private Hospital hospital;
    private DatabaseManager dbManager = new DatabaseManager();
    private ObservableList<Doctor> doctors = FXCollections.observableArrayList();
    private ObservableList<Patient> patients = FXCollections.observableArrayList();
    private ObservableList<TreatmentRequest> treatmentRequests = FXCollections.observableArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadHospitalData();
        // Refresh data after a short delay to ensure UI components are properly initialized
        javafx.application.Platform.runLater(this::refreshAllData);
    }
    
    private void refreshAllData() {
        try {
            // Reload all data
            loadStatistics();
            loadDoctorsData();
            loadPatientsData();
            loadTreatmentRequestsData();
        } catch (Exception e) {
            System.err.println("Error refreshing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadHospitalData() {
        try {
            // Get current hospital from session
            hospital = (Hospital) Session.getInstance().getCurrentUser();
            
            if (hospital == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Hospital data not found. Please login again.");
                handleLogout();
                return;
            }
            
            // Set hospital name in header
            hospitalNameLabel.setText(hospital.getName());
            
            // Set hospital information
            nameField.setText(hospital.getName());
            addressField.setText(hospital.getAddress());
            contactField.setText(hospital.getContactNumber());
            emailField.setText(hospital.getEmail());
            websiteField.setText(hospital.getWebsite() != null ? hospital.getWebsite() : "");
            descriptionField.setText(hospital.getDescription() != null ? hospital.getDescription() : "");
            
            // Initialize data tables
            initializeDoctorsTable();
            initializePatientsTable();
            initializeTreatmentRequestsTable();
            
            // Hide details panes until needed
            if (patientDetailsPane != null) patientDetailsPane.setVisible(false);
            if (treatmentDetailsPane != null) treatmentDetailsPane.setVisible(false);
            
            // Set up tab change listener to refresh data when tabs are selected
            javafx.application.Platform.runLater(() -> {
                try {
                    TabPane tabPane = (TabPane) ((BorderPane) hospitalNameLabel.getScene().getRoot()).getCenter();
                    if (tabPane != null) {
                        tabPane.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.intValue() == 2) { // Patients tab (index 2)
                                System.out.println("Tab changed to Patients - reloading patients data");
                                loadAllPatientsForCurrentHospital();
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error setting up tab listener: " + e.getMessage());
                }
            });
            
            // Load initial statistics
            loadStatistics();
            
        } catch (ClassCastException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid user data. Please login again.");
            handleLogout();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadStatistics() {
        try {
            System.out.println("Loading statistics for hospital ID: " + hospital.getId());
            
            // Get counts for this hospital only
            int doctorCount = dbManager.getTotalDoctorsCountByHospital(hospital.getId());
            int patientCount = dbManager.getTotalPatientsCountByHospital(hospital.getId());
            int pendingTreatmentCount = dbManager.getPendingTreatmentsCountByHospital(hospital.getId());
            
            System.out.println("Statistics loaded - Doctors: " + doctorCount + 
                             ", Patients: " + patientCount + 
                             ", Pending Treatments: " + pendingTreatmentCount);
            
            if (totalDoctorsLabel != null) {
                totalDoctorsLabel.setText(String.valueOf(doctorCount));
            } else {
                System.err.println("totalDoctorsLabel is null");
            }
            
            if (totalPatientsLabel != null) {
                totalPatientsLabel.setText(String.valueOf(patientCount));
            } else {
                System.err.println("totalPatientsLabel is null");
            }
            
            if (pendingTreatmentsLabel != null) {
                pendingTreatmentsLabel.setText(String.valueOf(pendingTreatmentCount));
            } else {
                System.err.println("pendingTreatmentsLabel is null");
            }
            
            // Load charts if they exist
            if (genderDistributionChart != null) {
                System.out.println("Loading gender distribution chart");
                loadGenderDistributionChart();
            } else {
                System.err.println("genderDistributionChart is null");
            }
            
            if (treatmentUrgencyChart != null) {
                System.out.println("Loading treatment urgency chart");
                loadTreatmentUrgencyChart();
            } else {
                System.err.println("treatmentUrgencyChart is null");
            }
        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in loadStatistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadGenderDistributionChart() {
        try {
            Map<String, Integer> genderDistribution = dbManager.getPatientGenderDistributionByHospital(hospital.getId());
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            // Ensure we have entries for common genders even if count is zero
            if (!genderDistribution.containsKey("Male")) genderDistribution.put("Male", 0);
            if (!genderDistribution.containsKey("Female")) genderDistribution.put("Female", 0);
            if (!genderDistribution.containsKey("Other")) genderDistribution.put("Other", 0);
            
            genderDistribution.forEach((gender, count) -> {
                pieChartData.add(new PieChart.Data(gender, count));
            });
            
            genderDistributionChart.setData(pieChartData);
            
            // Add visible labels to the chart
            pieChartData.forEach(data -> {
                data.nameProperty().bind(
                    javafx.beans.binding.Bindings.concat(
                        data.getName(), ": ", data.pieValueProperty().intValue()
                    )
                );
            });
        } catch (SQLException e) {
            System.err.println("Error loading gender distribution chart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadTreatmentUrgencyChart() {
        try {
            Map<String, Integer> urgencyDistribution = dbManager.getTreatmentUrgencyDistributionByHospital(hospital.getId());
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Treatment Requests");
            
            // Define a specific order for urgency levels
            String[] urgencyLevels = {"Low", "Medium", "High", "Emergency"};
            
            // Add data points in the specified order
            for (String urgency : urgencyLevels) {
                int count = urgencyDistribution.getOrDefault(urgency, 0);
                series.getData().add(new XYChart.Data<>(urgency, count));
            }
            
            treatmentUrgencyChart.getData().clear();
            treatmentUrgencyChart.getData().add(series);
            
            // Apply consistent colors to the chart using both methods for maximum compatibility
            ChartUtils.styleUrgencyChart(series);
            
            // Also apply CSS styling directly to the chart
            treatmentUrgencyChart.getStylesheets().add(getClass().getResource("/com/hospital/chart-styles.css").toExternalForm());
            
            // Add data labels to the bars
            for (XYChart.Data<String, Number> data : series.getData()) {
                // Apply specific style class based on urgency level
                String urgencyLevel = data.getXValue();
                
                // Set up listener for when the node is created
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        // Apply specific style class
                        switch (urgencyLevel) {
                            case "Emergency":
                                newNode.getStyleClass().add("emergency-bar");
                                break;
                            case "High":
                                newNode.getStyleClass().add("high-bar");
                                break;
                            case "Medium":
                                newNode.getStyleClass().add("medium-bar");
                                break;
                            case "Low":
                                newNode.getStyleClass().add("low-bar");
                                break;
                            default:
                                break;
                        }
                        
                        // Add tooltip
                        Tooltip.install(newNode, new Tooltip(urgencyLevel + ": " + data.getYValue()));
                    }
                });
            }
        } catch (SQLException e) {
            System.err.println("Error loading treatment urgency chart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeDoctorsTable() {
        // Initialize table columns
        if (doctorIdColumn != null) doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (doctorFirstNameColumn != null) doctorFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        if (doctorLastNameColumn != null) doctorLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        if (doctorSpecializationColumn != null) doctorSpecializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        if (doctorContactColumn != null) doctorContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        if (doctorEmailColumn != null) doctorEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        // Load doctors data
        if (doctorsTable != null) {
            loadDoctorsData();
        }
    }
    
    private void loadDoctorsData() {
        try {
            // Only load doctors for this hospital
            List<Doctor> doctorList = dbManager.getDoctorsByHospital(hospital.getId());
            doctors.clear();
            doctors.addAll(doctorList);
            
            if (doctorsTable != null) {
                doctorsTable.setItems(doctors);
                doctorsTable.refresh();
                
                // Log the number of doctors loaded for debugging
                System.out.println("Loaded " + doctorList.size() + " doctors for hospital ID: " + hospital.getId());
                for (Doctor doc : doctorList) {
                    System.out.println("Doctor: " + doc.getFirstName() + " " + doc.getLastName());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading doctors: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializePatientsTable() {
        // Initialize table columns
        if (patientIdColumn != null) patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (patientFirstNameColumn != null) patientFirstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        if (patientLastNameColumn != null) patientLastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        if (patientDobColumn != null) patientDobColumn.setCellValueFactory(cellData -> {
            LocalDate dob = cellData.getValue().getDateOfBirth();
            return new SimpleStringProperty(dob != null ? dob.toString() : "N/A");
        });
        if (patientGenderColumn != null) patientGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        if (patientContactColumn != null) patientContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        
        // Load patients data
        if (patientsTable != null) {
            loadPatientsData();
            
            // Add selection listener
            patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showPatientDetails(newSelection);
                } else if (patientDetailsPane != null) {
                    patientDetailsPane.setVisible(false);
                }
            });
        }
    }
    
    private void loadPatientsData() {
        try {
            // Only load patients for this hospital
            List<Patient> patientList = dbManager.getPatientsByHospital(hospital.getId());
            patients.clear();
            patients.addAll(patientList);
            
            if (patientsTable != null) {
                patientsTable.setItems(patients);
                patientsTable.refresh();
                
                // Log the number of patients loaded for debugging
                System.out.println("Loaded " + patientList.size() + " patients for hospital ID: " + hospital.getId());
                for (Patient patient : patientList) {
                    System.out.println("Patient: " + patient.getFirstName() + " " + patient.getLastName());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads all patients for the current hospital, including those that might not be 
     * properly associated with the hospital in the database.
     */
    private void loadAllPatientsForCurrentHospital() {
        try {
            System.out.println("Loading ALL patients for hospital ID: " + hospital.getId());
            
            // Get all patients from the database
            List<Patient> allPatients = dbManager.getAllPatients();
            System.out.println("Retrieved " + allPatients.size() + " total patients from database");
            
            // Filter patients by the current hospital ID
            List<Patient> hospitalPatients = allPatients.stream()
                .filter(patient -> patient.getHospitalId() == hospital.getId())
                .toList();
            
            System.out.println("Filtered to " + hospitalPatients.size() + " patients for hospital ID: " + hospital.getId());
            
            // Update the UI
            patients.clear();
            patients.addAll(hospitalPatients);
            
            if (patientsTable != null) {
                patientsTable.setItems(patients);
                patientsTable.refresh();
                
                // Log the patients for debugging
                for (Patient patient : hospitalPatients) {
                    System.out.println("Patient: " + patient.getFirstName() + " " + patient.getLastName() + 
                                     ", Hospital ID: " + patient.getHospitalId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading all patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showPatientDetails(Patient patient) {
        if (patientDetailsPane == null || medicalHistoryTextArea == null) return;
        
        try {
            String medicalHistory = dbManager.getPatientMedicalHistory(patient.getId());
            medicalHistoryTextArea.setText(medicalHistory != null ? medicalHistory : "No medical history available.");
            patientDetailsPane.setVisible(true);
        } catch (SQLException e) {
            System.err.println("Error showing patient details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initializeTreatmentRequestsTable() {
        if (treatmentRequestsTable != null) {
            // Initialize columns
            treatmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
            dateRequestedColumn.setCellValueFactory(new PropertyValueFactory<>("dateRequested"));
            preferredDateColumn.setCellValueFactory(new PropertyValueFactory<>("preferredDate"));
            urgencyColumn.setCellValueFactory(new PropertyValueFactory<>("urgency"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            assignedDoctorColumn.setCellValueFactory(new PropertyValueFactory<>("assignedDoctorName"));
            
            // Set up filters
            urgencyFilterComboBox.getItems().addAll("All", "Emergency", "High", "Medium", "Low");
            urgencyFilterComboBox.setValue("All");
            urgencyFilterComboBox.setOnAction(e -> filterTreatmentRequests());
            
            statusFilterComboBox.getItems().addAll("All", "Pending", "Assigned", "In Progress", "Completed", "Cancelled");
            statusFilterComboBox.setValue("All");
            statusFilterComboBox.setOnAction(e -> filterTreatmentRequests());
            
            // Initialize status combo box for treatment details
            statusComboBox.getItems().addAll("Pending", "Assigned", "In Progress", "Completed", "Cancelled");
            
            // Add selection listener
            treatmentRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showTreatmentDetails(newSelection);
                } else if (treatmentDetailsPane != null) {
                    treatmentDetailsPane.setVisible(false);
                }
            });
            
            // Load treatment data
            loadTreatmentRequestsData();
        }
    }
    
    private void filterTreatmentRequests() {
        try {
            // Get the selected filters
            String urgencyFilter = urgencyFilterComboBox.getValue();
            String statusFilter = statusFilterComboBox.getValue();
            
            // Load all treatment requests for this hospital
            List<TreatmentRequest> allRequests = dbManager.getTreatmentRequestsByHospital(hospital.getId());
            
            // Apply filters
            List<TreatmentRequest> filteredRequests = allRequests;
            
            // Filter by urgency if not "All"
            if (!"All".equals(urgencyFilter)) {
                filteredRequests = filteredRequests.stream()
                    .filter(request -> request.getUrgency().equals(urgencyFilter))
                    .toList();
            }
            
            // Filter by status if not "All"
            if (!"All".equals(statusFilter)) {
                filteredRequests = filteredRequests.stream()
                    .filter(request -> request.getStatus().equals(statusFilter))
                    .toList();
            }
            
            // Update table
            treatmentRequests.clear();
            treatmentRequests.addAll(filteredRequests);
            
        } catch (SQLException e) {
            System.err.println("Error filtering treatment requests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadTreatmentRequestsData() {
        try {
            if (treatmentRequestsTable == null) {
                System.out.println("Treatment requests table is null - skipping data load");
                return;
            }
            
            System.out.println("Loading treatment requests for hospital ID: " + hospital.getId());
            
            // Only load treatment requests for this hospital
            List<TreatmentRequest> requestList = dbManager.getTreatmentRequestsByHospital(hospital.getId());
            System.out.println("Loaded " + requestList.size() + " treatment requests");
            
            // Create a new observable list if needed
            if (treatmentRequests == null) {
                treatmentRequests = FXCollections.observableArrayList();
            } else {
                treatmentRequests.clear();
            }
            
            // Add all items and update the table
            treatmentRequests.addAll(requestList);
            treatmentRequestsTable.setItems(treatmentRequests);
            treatmentRequestsTable.refresh();
            
            // Log the data for debugging
            for (TreatmentRequest req : requestList) {
                System.out.println("Request ID: " + req.getId() + 
                                 ", Patient: " + req.getPatientName() + 
                                 ", Status: " + req.getStatus() +
                                 ", Doctor: " + req.getAssignedDoctorName());
            }
        } catch (SQLException e) {
            System.err.println("Error loading treatment requests: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in loadTreatmentRequestsData: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showTreatmentDetails(TreatmentRequest request) {
        if (treatmentDetailsPane == null) return;
        
        try {
            // Get patient name
            Patient patient = dbManager.getPatientById(request.getPatientId());
            selectedPatientLabel.setText(patient.getFirstName() + " " + patient.getLastName());
            
            // Set symptoms
            symptomsTextArea.setText(request.getSymptoms());
            
            // Load doctor assignment combo - only load doctors from this hospital
            List<Doctor> doctorList = dbManager.getDoctorsByHospital(hospital.getId());
            doctorAssignmentComboBox.getItems().clear();
            doctorAssignmentComboBox.getItems().addAll(doctorList);
            
            // Set currently assigned doctor if any
            Integer assignedDoctorId = request.getAssignedDoctorId();
            if (assignedDoctorId != null && assignedDoctorId > 0) {
                for (Doctor doctor : doctorList) {
                    if (doctor.getId() == assignedDoctorId) {
                        doctorAssignmentComboBox.setValue(doctor);
                        break;
                    }
                }
            }
            
            // Set status
            statusComboBox.setValue(request.getStatus());
            
            treatmentDetailsPane.setVisible(true);
        } catch (SQLException e) {
            System.err.println("Error showing treatment details: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAddDoctor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/doctor-form.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Doctor");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh the doctors list
            loadDoctorsData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open doctor form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleEditDoctor() {
        Doctor selectedDoctor = doctorsTable.getSelectionModel().getSelectedItem();
        if (selectedDoctor == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a doctor to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/doctor-form.fxml"));
            Parent root = loader.load();
            
            // We'll need to create a new doctor-form.fxml with edit capabilities
            // For now, just show a message since the controller doesn't support editing
            showAlert(Alert.AlertType.INFORMATION, "Edit Doctor", 
                     "This feature will be implemented in the next phase.");
            
            // Refresh the doctors list in case any changes were made
            loadDoctorsData();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open doctor form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDeleteDoctor() {
        Doctor selectedDoctor = doctorsTable.getSelectionModel().getSelectedItem();
        if (selectedDoctor == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a doctor to delete.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Doctor");
        confirmAlert.setContentText("Are you sure you want to delete doctor: " + 
                                  selectedDoctor.getFirstName() + " " + selectedDoctor.getLastName() + "?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the doctor using the database manager
                dbManager.deleteDoctor(selectedDoctor.getId());
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Doctor deleted successfully.");
                loadDoctorsData();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete doctor: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleDoctorSearch() {
        String searchTerm = doctorSearchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadDoctorsData();
            return;
        }
        
        try {
            List<Doctor> filteredDoctors = dbManager.searchDoctors(searchTerm);
            doctors.clear();
            doctors.addAll(filteredDoctors);
            doctorsTable.setItems(doctors);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to search doctors: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handlePatientSearch() {
        String searchTerm = patientSearchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadPatientsData();
            return;
        }
        
        try {
            // Search for patients within the current hospital only
            List<Patient> filteredPatients = dbManager.searchPatients(searchTerm, hospital.getId());
            System.out.println("Searched for patients with term '" + searchTerm + "' in hospital ID: " + hospital.getId());
            System.out.println("Found " + filteredPatients.size() + " matching patients");
            
            patients.clear();
            patients.addAll(filteredPatients);
            patientsTable.setItems(patients);
            patientsTable.refresh();
            
            // Log the patients found for debugging
            for (Patient patient : filteredPatients) {
                System.out.println("Found patient: " + patient.getFirstName() + " " + patient.getLastName() + 
                                 ", Hospital ID: " + patient.getHospitalId());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to search patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRefreshPatients() {
        try {
            System.out.println("Refreshing patients data for hospital ID: " + hospital.getId());
            
            // Clear search field
            if (patientSearchField != null) {
                patientSearchField.clear();
            }
            
            // Load ALL patients for this hospital
            loadAllPatientsForCurrentHospital();
            
            // Show appropriate message based on results
            String message;
            if (patients.isEmpty()) {
                message = "No patients found for this hospital.";
            } else {
                message = patients.size() + " patients loaded successfully.";
            }
            
            // Show a success message
            showAlert(Alert.AlertType.INFORMATION, "Success", message);
        } catch (Exception e) {
            System.err.println("Error refreshing patients data: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh patients data: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleUpdateMedicalHistory() {
        Patient selectedPatient = patientsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a patient.");
            return;
        }
        
        String medicalHistory = medicalHistoryTextArea.getText();
        try {
            dbManager.updatePatientMedicalHistory(selectedPatient.getId(), medicalHistory);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Medical history updated successfully.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update medical history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRefreshTreatments() {
        try {
            System.out.println("Refreshing treatment requests data...");
            
            // Reset filters first
            urgencyFilterComboBox.setValue("All");
            statusFilterComboBox.setValue("All");
            
            // Load fresh data
            loadTreatmentRequestsData();
            
            // Show a success message
            showAlert(Alert.AlertType.INFORMATION, "Success", "Treatment requests data refreshed successfully.");
        } catch (Exception e) {
            System.err.println("Error refreshing treatment requests: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to refresh treatment requests: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSaveAssignment() {
        TreatmentRequest selectedRequest = treatmentRequestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a treatment request.");
            return;
        }
        
        Doctor selectedDoctor = doctorAssignmentComboBox.getValue();
        String selectedStatus = statusComboBox.getValue();
        
        if (selectedStatus == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a status.");
            return;
        }
        
        try {
            // Update request with doctor and status
            selectedRequest.setStatus(selectedStatus);
            
            if (selectedDoctor != null) {
                selectedRequest.setAssignedDoctorId(selectedDoctor.getId());
                selectedRequest.setAssignedDoctorName(selectedDoctor.getFirstName() + " " + selectedDoctor.getLastName());
            } else {
                selectedRequest.setAssignedDoctorId(0);
                selectedRequest.setAssignedDoctorName(null);
            }
            
            // Make sure the hospital ID is set correctly
            selectedRequest.setHospitalId(hospital.getId());
            selectedRequest.setHospitalName(hospital.getName());
            
            // Update in database
            dbManager.updateTreatmentRequest(selectedRequest);
            
            // Update patient's medical history
            try {
                Patient patient = dbManager.getPatientById(selectedRequest.getPatientId());
                if (patient != null) {
                    StringBuilder historyUpdate = new StringBuilder();
                    historyUpdate.append("\n--- TREATMENT REQUEST UPDATED ---\n");
                    historyUpdate.append("Date: ").append(LocalDate.now()).append("\n");
                    historyUpdate.append("Hospital: ").append(hospital.getName()).append("\n");
                    historyUpdate.append("Status: ").append(selectedStatus).append("\n");
                    
                    if (selectedDoctor != null) {
                        historyUpdate.append("Assigned Doctor: ").append(selectedDoctor.getFirstName())
                                    .append(" ").append(selectedDoctor.getLastName())
                                    .append(" (").append(selectedDoctor.getSpecialization()).append(")\n");
                    }
                    
                    historyUpdate.append("\n");
                    
                    String currentHistory = patient.getMedicalHistory();
                    if (currentHistory == null) currentHistory = "";
                    
                    dbManager.updatePatientMedicalHistory(patient.getId(), currentHistory + historyUpdate.toString());
                }
            } catch (SQLException e) {
                System.err.println("Warning: Could not update patient medical history: " + e.getMessage());
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success", "Treatment assignment updated successfully.");
            loadTreatmentRequestsData();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update treatment assignment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleUpdateHospitalInfo() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Hospital name cannot be empty.");
            return;
        }
        
        // Update hospital object
        hospital.setName(name);
        hospital.setAddress(addressField.getText().trim());
        hospital.setContactNumber(contactField.getText().trim());
        hospital.setEmail(emailField.getText().trim());
        hospital.setWebsite(websiteField.getText().trim());
        hospital.setDescription(descriptionField.getText().trim());
        
        try {
            dbManager.updateHospital(hospital);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Hospital information updated successfully.");
            
            // Update hospital name in header
            hospitalNameLabel.setText(hospital.getName());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update hospital information: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleChangePassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter new password");
        dialog.setContentText("New password:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            try {
                dbManager.updateHospitalPassword(hospital.getId(), result.get());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to change password: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Clear session
            Session.getInstance().clearSession();
            
            // Navigate to login
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/login.fxml"));
            Stage stage = (Stage) hospitalNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, Main.LOGIN_WIDTH, Main.LOGIN_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("City Hospital Management System");
            stage.setMaximized(false);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error navigating to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showDashboard() {
        // Get the tab pane from the center of the BorderPane
        TabPane tabPane = (TabPane) ((BorderPane) hospitalNameLabel.getScene().getRoot()).getCenter();
        // Select the Dashboard tab (index 0)
        tabPane.getSelectionModel().select(0);
    }
    
    @FXML
    private void showDoctors() {
        // Get the tab pane from the center of the BorderPane
        TabPane tabPane = (TabPane) ((BorderPane) hospitalNameLabel.getScene().getRoot()).getCenter();
        // Select the Doctors tab (index 1)
        tabPane.getSelectionModel().select(1);
    }
    
    @FXML
    private void showPatients() {
        // Get the tab pane from the center of the BorderPane
        TabPane tabPane = (TabPane) ((BorderPane) hospitalNameLabel.getScene().getRoot()).getCenter();
        // Select the Patients tab (index 2)
        tabPane.getSelectionModel().select(2);
        
        // Clear search field if it exists
        if (patientSearchField != null) {
            patientSearchField.clear();
        }
        
        // Refresh patients data when tab is selected
        loadAllPatientsForCurrentHospital();
    }
    
    @FXML
    private void showFacilities() {
        // For now, just show a message since the Facilities tab is not yet implemented
        showAlert(Alert.AlertType.INFORMATION, "Facilities", "The Facilities feature will be implemented in the next phase.");
    }
    
    @FXML
    private void showServices() {
        // For now, just show a message since the Services tab is not yet implemented
        showAlert(Alert.AlertType.INFORMATION, "Services", "The Services feature will be implemented in the next phase.");
    }
    
    @FXML
    private void showProfile() {
        // Get the tab pane from the center of the BorderPane
        TabPane tabPane = (TabPane) ((BorderPane) hospitalNameLabel.getScene().getRoot()).getCenter();
        // Select the Hospital Profile tab (index 4)
        tabPane.getSelectionModel().select(4);
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 