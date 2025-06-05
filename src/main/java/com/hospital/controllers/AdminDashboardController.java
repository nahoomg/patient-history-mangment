package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Doctor;
import com.hospital.models.Admin;
import com.hospital.models.Patient;
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
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {
    // Dashboard elements
    @FXML private Label welcomeLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label pendingTreatmentsLabel;
    @FXML private PieChart genderDistributionChart;
    @FXML private BarChart<String, Number> treatmentUrgencyChart;
    
    // Treatment request elements
    @FXML private ComboBox<String> urgencyFilterComboBox;
    @FXML private TableView<TreatmentRequest> treatmentRequestsTable;
    @FXML private TableColumn<TreatmentRequest, String> treatmentIdColumn;
    @FXML private TableColumn<TreatmentRequest, String> patientNameColumn;
    @FXML private TableColumn<TreatmentRequest, String> dateRequestedColumn;
    @FXML private TableColumn<TreatmentRequest, String> preferredDateColumn;
    @FXML private TableColumn<TreatmentRequest, String> urgencyColumn;
    @FXML private TableColumn<TreatmentRequest, String> statusColumn;
    @FXML private TableColumn<TreatmentRequest, String> assignedDoctorColumn;
    @FXML private VBox treatmentDetailsPane;
    @FXML private Label selectedPatientLabel;
    @FXML private TextArea symptomsTextArea;
    @FXML private ComboBox<Doctor> doctorAssignmentComboBox;
    @FXML private ComboBox<String> statusComboBox;
    
    // Doctor management elements
    @FXML private TextField doctorSearchField;
    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TableColumn<Doctor, String> doctorIdColumn;
    @FXML private TableColumn<Doctor, String> doctorFirstNameColumn;
    @FXML private TableColumn<Doctor, String> doctorLastNameColumn;
    @FXML private TableColumn<Doctor, String> doctorSpecializationColumn;
    @FXML private TableColumn<Doctor, String> doctorContactColumn;
    @FXML private TableColumn<Doctor, String> doctorEmailColumn;
    @FXML private TableColumn<Doctor, String> doctorUsernameColumn;
    
    // Admin management elements
    @FXML private TableView<Admin> adminsTable;
    @FXML private TableColumn<Admin, String> adminIdColumn;
    @FXML private TableColumn<Admin, String> adminFullNameColumn;
    @FXML private TableColumn<Admin, String> adminEmailColumn;
    @FXML private TableColumn<Admin, String> adminUsernameColumn;
    
    // Patient management elements
    @FXML private TextField patientSearchField;
    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> patientIdColumn;
    @FXML private TableColumn<Patient, String> patientFirstNameColumn;
    @FXML private TableColumn<Patient, String> patientLastNameColumn;
    @FXML private TableColumn<Patient, String> patientDobColumn;
    @FXML private TableColumn<Patient, String> patientGenderColumn;
    @FXML private TableColumn<Patient, String> patientContactColumn;

    private DatabaseManager dbManager;
    private Admin currentAdmin;
    private ObservableList<Doctor> doctorsList;
    private ObservableList<Admin> adminsList;
    private ObservableList<Patient> patientsList;
    private ObservableList<TreatmentRequest> treatmentRequestsList;
    private TreatmentRequest selectedTreatmentRequest;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        doctorsList = FXCollections.observableArrayList();
        adminsList = FXCollections.observableArrayList();
        patientsList = FXCollections.observableArrayList();
        treatmentRequestsList = FXCollections.observableArrayList();
        
        initializeDashboard();
        initializeTreatmentRequests();
        initializeDoctorManagement();
        initializeAdminManagement();
        initializePatientManagement();
        
        loadData();
    }

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        welcomeLabel.setText("Welcome, " + admin.getFullName());
    }
    
    private void initializeDashboard() {
        // Dashboard will be populated in loadData()
    }
    
    private void initializeTreatmentRequests() {
        // Initialize urgency filter
        urgencyFilterComboBox.getItems().addAll("All", "Emergency", "High", "Medium", "Low");
        urgencyFilterComboBox.setValue("All");
        urgencyFilterComboBox.setOnAction(e -> filterTreatmentRequests());
        
        // Initialize treatment request table columns
        treatmentIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        patientNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPatientName()));
        dateRequestedColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateRequested().toString()));
        preferredDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPreferredDate().toString()));
        urgencyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrgency()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        assignedDoctorColumn.setCellValueFactory(cellData -> {
            String doctorName = cellData.getValue().getAssignedDoctorName();
            return new SimpleStringProperty(doctorName != null && !doctorName.isEmpty() ? doctorName : "Not Assigned");
        });
        
        // Set up treatment request selection handler
        treatmentRequestsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedTreatmentRequest = newSelection;
                    showTreatmentDetails(newSelection);
                }
            }
        );
        
        // Initialize status combo box
        statusComboBox.getItems().addAll("Pending", "Assigned", "In Progress", "Completed", "Cancelled");
        
        // Hide treatment details pane initially
        treatmentDetailsPane.setVisible(false);
    }
    
    private void initializeDoctorManagement() {
        // Initialize doctor table columns
        doctorIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        doctorFirstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        doctorLastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        doctorSpecializationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSpecialization()));
        doctorContactColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContactNumber()));
        doctorEmailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        doctorUsernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        
        doctorsTable.setItems(doctorsList);
    }
    
    private void initializeAdminManagement() {
        // Initialize admin table columns
        adminIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        adminFullNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        adminEmailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        adminUsernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        
        adminsTable.setItems(adminsList);
    }
    
    private void initializePatientManagement() {
        // Initialize patient table columns
        patientIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        patientFirstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        patientLastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        patientDobColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDateOfBirth().toString()));
        patientGenderColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGender()));
        patientContactColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContactNumber()));
        
        patientsTable.setItems(patientsList);
    }
    
    private void loadData() {
        try {
            // Load statistics for dashboard
            totalPatientsLabel.setText(String.valueOf(dbManager.getTotalPatientsCount()));
            totalDoctorsLabel.setText(String.valueOf(dbManager.getTotalDoctorsCount()));
            pendingTreatmentsLabel.setText(String.valueOf(dbManager.getPendingTreatmentsCount()));
            
            // Load gender distribution chart
            genderDistributionChart.getData().clear();
            Map<String, Integer> genderDistribution = dbManager.getPatientGenderDistribution();
            for (Map.Entry<String, Integer> entry : genderDistribution.entrySet()) {
                genderDistributionChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            
            // Load treatment urgency chart
            treatmentUrgencyChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Treatment Requests");
            
            Map<String, Integer> urgencyDistribution = dbManager.getTreatmentUrgencyDistribution();
            series.getData().add(new XYChart.Data<>("Low", urgencyDistribution.get("Low")));
            series.getData().add(new XYChart.Data<>("Medium", urgencyDistribution.get("Medium")));
            series.getData().add(new XYChart.Data<>("High", urgencyDistribution.get("High")));
            series.getData().add(new XYChart.Data<>("Emergency", urgencyDistribution.get("Emergency")));
            
            treatmentUrgencyChart.getData().add(series);
            
            // Load treatment requests
            loadTreatmentRequests();
            
            // Load doctors, admins, and patients
            loadDoctors();
            loadAdmins();
            loadPatients();
            
        } catch (SQLException e) {
            showAlert("Error loading data: " + e.getMessage());
        }
    }
    
    private void loadTreatmentRequests() throws SQLException {
        // Clear and reload the treatment requests list
        treatmentRequestsList.clear();
        treatmentRequestsList.addAll(dbManager.getAllTreatmentRequests());
        
        // Ensure the table is properly set with the items
        treatmentRequestsTable.setItems(treatmentRequestsList);
        
        // Debug output to check if we're getting treatment requests
        System.out.println("Loaded " + treatmentRequestsList.size() + " treatment requests");
        
        // If no treatment requests exist, create a sample one for testing
        if (treatmentRequestsList.isEmpty() && dbManager.getTotalPatientsCount() > 0) {
            try {
                // Get the first patient for the sample request
                Patient patient = dbManager.getAllPatients().get(0);
                
                // Create a sample treatment request
                TreatmentRequest sampleRequest = new TreatmentRequest();
                sampleRequest.setPatientId(patient.getId());
                sampleRequest.setPatientName(patient.getFirstName() + " " + patient.getLastName());
                sampleRequest.setDateRequested(LocalDate.now());
                sampleRequest.setPreferredDate(LocalDate.now().plusDays(3));
                sampleRequest.setUrgency("Medium");
                sampleRequest.setSymptoms("Sample symptoms for testing the treatment request system.");
                sampleRequest.setStatus("Pending");
                
                // Add to database
                dbManager.addTreatmentRequest(sampleRequest);
                
                // Reload the list
                treatmentRequestsList.clear();
                treatmentRequestsList.addAll(dbManager.getAllTreatmentRequests());
                treatmentRequestsTable.setItems(treatmentRequestsList);
                
                System.out.println("Created a sample treatment request for testing");
            } catch (Exception e) {
                System.out.println("Could not create sample treatment request: " + e.getMessage());
            }
        }
        
        // Populate doctor assignment combo box
        ObservableList<Doctor> doctorsForAssignment = FXCollections.observableArrayList();
        doctorsForAssignment.addAll(dbManager.getAllDoctors());
        doctorAssignmentComboBox.setItems(doctorsForAssignment);
        
        // Add a custom toString method for the Doctor objects in the ComboBox
        doctorAssignmentComboBox.setCellFactory(param -> new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) {
                    setText(null);
                } else {
                    setText(doctor.getFirstName() + " " + doctor.getLastName() + " (" + doctor.getSpecialization() + ")");
                }
            }
        });
        
        doctorAssignmentComboBox.setButtonCell(new ListCell<Doctor>() {
            @Override
            protected void updateItem(Doctor doctor, boolean empty) {
                super.updateItem(doctor, empty);
                if (empty || doctor == null) {
                    setText(null);
                } else {
                    setText(doctor.getFirstName() + " " + doctor.getLastName() + " (" + doctor.getSpecialization() + ")");
                }
            }
        });
    }
    
    private void loadDoctors() throws SQLException {
        doctorsList.clear();
        doctorsList.addAll(dbManager.getAllDoctors());
    }
    
    private void loadAdmins() throws SQLException {
        adminsList.clear();
        adminsList.addAll(dbManager.getAllAdmins());
    }
    
    private void loadPatients() throws SQLException {
        patientsList.clear();
        patientsList.addAll(dbManager.getAllPatients());
    }
    
    private void showTreatmentDetails(TreatmentRequest request) {
        selectedPatientLabel.setText(request.getPatientName());
        symptomsTextArea.setText(request.getSymptoms());
        statusComboBox.setValue(request.getStatus());
        
        // Set selected doctor if assigned
        if (request.getAssignedDoctorId() > 0) {
            try {
                Doctor assignedDoctor = dbManager.getDoctorById(request.getAssignedDoctorId());
                if (assignedDoctor != null) {
                    doctorAssignmentComboBox.setValue(assignedDoctor);
                }
            } catch (SQLException e) {
                System.out.println("Error loading assigned doctor: " + e.getMessage());
            }
        } else {
            doctorAssignmentComboBox.setValue(null);
        }
        
        treatmentDetailsPane.setVisible(true);
    }
    
    private void filterTreatmentRequests() {
        try {
            String selectedUrgency = urgencyFilterComboBox.getValue();
            treatmentRequestsList.clear();
            
            if ("All".equals(selectedUrgency)) {
                treatmentRequestsList.addAll(dbManager.getAllTreatmentRequests());
            } else {
                treatmentRequestsList.addAll(dbManager.getTreatmentRequestsByUrgency(selectedUrgency));
            }
        } catch (SQLException e) {
            showAlert("Error filtering treatment requests: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefreshTreatments() {
        try {
            loadTreatmentRequests();
        } catch (SQLException e) {
            showAlert("Error refreshing treatment requests: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSaveAssignment() {
        if (selectedTreatmentRequest == null) {
            showAlert("Please select a treatment request first.");
            return;
        }
        
        Doctor selectedDoctor = doctorAssignmentComboBox.getValue();
        String selectedStatus = statusComboBox.getValue();
        
        if (selectedStatus == null) {
            showAlert("Please select a status.");
            return;
        }
        
        try {
            selectedTreatmentRequest.setStatus(selectedStatus);
            
            if (selectedDoctor != null) {
                selectedTreatmentRequest.setAssignedDoctorId(selectedDoctor.getId());
                selectedTreatmentRequest.setAssignedDoctorName(selectedDoctor.getFirstName() + " " + selectedDoctor.getLastName());
                
                // If status is just "Pending" and we're assigning a doctor, update to "Assigned"
                if ("Pending".equals(selectedStatus)) {
                    selectedTreatmentRequest.setStatus("Assigned");
                    statusComboBox.setValue("Assigned");
                }
                
                // Update the patient's medical history to reflect the doctor assignment
                try {
                    Patient patient = dbManager.getPatientById(selectedTreatmentRequest.getPatientId());
                    if (patient != null) {
                        String assignmentNote = "\n--- DOCTOR ASSIGNED ---\n" +
                                              "Date: " + LocalDate.now() + "\n" +
                                              "Doctor: " + selectedDoctor.getFirstName() + " " + selectedDoctor.getLastName() + "\n" +
                                              "Specialization: " + selectedDoctor.getSpecialization() + "\n" +
                                              "Status: " + selectedTreatmentRequest.getStatus() + "\n\n";
                        
                        String updatedHistory = patient.getMedicalHistory() + assignmentNote;
                        dbManager.updatePatientHistory(patient.getId(), updatedHistory);
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Could not update patient medical history: " + e.getMessage());
                }
            } else {
                selectedTreatmentRequest.setAssignedDoctorId(0);
                selectedTreatmentRequest.setAssignedDoctorName(null);
            }
            
            dbManager.updateTreatmentRequest(selectedTreatmentRequest);
            loadTreatmentRequests();
            loadData(); // Refresh dashboard statistics
            
            showAlert("Treatment assignment saved successfully!");
        } catch (SQLException e) {
            showAlert("Error saving assignment: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleShowAddDoctorForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/doctor-form.fxml"));
            Parent root = loader.load();
            
            DoctorFormController controller = loader.getController();
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Add New Doctor");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh doctors list after form is closed
            loadDoctors();
        } catch (IOException | SQLException e) {
            showAlert("Error opening doctor form: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleShowAddAdminForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/admin-form.fxml"));
            Parent root = loader.load();
            
            AdminFormController controller = loader.getController();
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Add New Admin");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh admins list after form is closed
            loadAdmins();
        } catch (IOException | SQLException e) {
            showAlert("Error opening admin form: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDoctorSearch() {
        try {
            String searchTerm = doctorSearchField.getText().trim();
            if (searchTerm.isEmpty()) {
                loadDoctors();
            } else {
                doctorsList.clear();
                doctorsList.addAll(dbManager.searchDoctors(searchTerm));
            }
        } catch (SQLException e) {
            showAlert("Error searching doctors: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePatientSearch() {
        try {
            String searchTerm = patientSearchField.getText().trim();
            if (searchTerm.isEmpty()) {
                loadPatients();
            } else {
                patientsList.clear();
                patientsList.addAll(dbManager.searchPatients(searchTerm));
            }
        } catch (SQLException e) {
            showAlert("Error searching patients: " + e.getMessage());
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