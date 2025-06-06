package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Doctor;
import com.hospital.models.Admin;
import com.hospital.models.Patient;
import com.hospital.models.TreatmentRequest;
import com.hospital.models.Hospital;
import com.hospital.models.ActivityLog;
import com.hospital.utils.ChartUtils;
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
import javafx.application.Platform;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Optional;

public class AdminDashboardController implements Initializable {
    // Dashboard elements
    @FXML private Label welcomeLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label pendingTreatmentsLabel;
    @FXML private PieChart genderDistributionChart;
    @FXML private BarChart<String, Number> treatmentUrgencyChart;
    
    // Treatment request elements
    // @FXML private ComboBox<String> urgencyFilterComboBox;
    // @FXML private TableView<TreatmentRequest> treatmentRequestsTable;
    // @FXML private TableColumn<TreatmentRequest, String> treatmentIdColumn;
    // @FXML private TableColumn<TreatmentRequest, String> patientNameColumn;
    // @FXML private TableColumn<TreatmentRequest, String> dateRequestedColumn;
    // @FXML private TableColumn<TreatmentRequest, String> preferredDateColumn;
    // @FXML private TableColumn<TreatmentRequest, String> urgencyColumn;
    // @FXML private TableColumn<TreatmentRequest, String> statusColumn;
    // @FXML private TableColumn<TreatmentRequest, String> assignedDoctorColumn;
    // @FXML private VBox treatmentDetailsPane;
    // @FXML private Label selectedPatientLabel;
    // @FXML private TextArea symptomsTextArea;
    // @FXML private ComboBox<Doctor> doctorAssignmentComboBox;
    // @FXML private ComboBox<String> statusComboBox;
    
    // Doctor management elements
    // @FXML private TextField doctorSearchField;
    // @FXML private TableView<Doctor> doctorsTable;
    // @FXML private TableColumn<Doctor, String> doctorIdColumn;
    // @FXML private TableColumn<Doctor, String> doctorFirstNameColumn;
    // @FXML private TableColumn<Doctor, String> doctorLastNameColumn;
    // @FXML private TableColumn<Doctor, String> doctorSpecializationColumn;
    // @FXML private TableColumn<Doctor, String> doctorContactColumn;
    // @FXML private TableColumn<Doctor, String> doctorEmailColumn;
    // @FXML private TableColumn<Doctor, String> doctorUsernameColumn;
    // @FXML private TextField editDoctorNameField;
    // @FXML private TextField editDoctorSpecializationField;
    // @FXML private TextField editDoctorContactField;
    // @FXML private TextField editDoctorEmailField;
    
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
    @FXML private VBox patientDetailsPane;
    @FXML private Label patientFullNameLabel;
    @FXML private Label patientDobGenderLabel;
    @FXML private Label patientContactLabel;
    @FXML private Label patientStatusLabel;

    // Hospital management elements
    @FXML private Label totalHospitalsLabel;
    @FXML private PieChart hospitalRegionChart;
    @FXML private TableView<Hospital> hospitalsTable;
    @FXML private TableColumn<Hospital, String> hospitalIdColumn;
    @FXML private TableColumn<Hospital, String> hospitalNameColumn;
    @FXML private TableColumn<Hospital, String> hospitalAddressColumn;
    @FXML private TableColumn<Hospital, String> hospitalContactColumn;
    @FXML private TableColumn<Hospital, String> hospitalEmailColumn;
    @FXML private TableColumn<Hospital, String> hospitalUsernameColumn;
    @FXML private TextField hospitalSearchField;
    @FXML private VBox hospitalDetailsPane;
    @FXML private TextField editHospitalNameField;
    @FXML private TextArea editHospitalAddressField;
    @FXML private TextField editHospitalContactField;
    @FXML private TextField editHospitalEmailField;
    @FXML private TextField editHospitalWebsiteField;
    @FXML private TextField editHospitalUsernameField;
    @FXML private PasswordField editHospitalPasswordField;
    @FXML private Label hospitalStatusLabel;

    // Recent activity tracking
    @FXML private TableView<ActivityLog> recentActivityTable;
    @FXML private TableColumn<ActivityLog, String> activityTimeColumn;
    @FXML private TableColumn<ActivityLog, String> activityTypeColumn;
    @FXML private TableColumn<ActivityLog, String> activityDescriptionColumn;

    // Add with the other field declarations at the top of the class
    @FXML private TextArea medicalHistoryTextArea;

    private DatabaseManager dbManager;
    private Admin currentAdmin;
    // private ObservableList<Doctor> doctorsList;
    private ObservableList<Admin> adminsList;
    private ObservableList<Patient> patientsList;
    // private ObservableList<TreatmentRequest> treatmentRequestsList;
    private ObservableList<Hospital> hospitalsList;
    private ObservableList<ActivityLog> activityLogList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        // doctorsList = FXCollections.observableArrayList();
        adminsList = FXCollections.observableArrayList();
        patientsList = FXCollections.observableArrayList();
        // treatmentRequestsList = FXCollections.observableArrayList();
        hospitalsList = FXCollections.observableArrayList();
        activityLogList = FXCollections.observableArrayList();
        
        initializeDashboard();
        // initializeTreatmentRequests();
        // initializeDoctorManagement();
        initializeAdminManagement();
        initializePatientManagement();
        initializeHospitalManagement();
        initializeActivityLog();
        
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
        // This method is no longer needed
    }
    
    private void initializeDoctorManagement() {
        // This method is no longer needed
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
        patientDobColumn.setCellValueFactory(cellData -> {
            LocalDate dob = cellData.getValue().getDateOfBirth();
            return new SimpleStringProperty(dob != null ? dob.toString() : "N/A");
        });
        patientGenderColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getGender()));
        patientContactColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContactNumber()));
        
        // Set initial column constraints
        patientIdColumn.setMinWidth(40);
        patientFirstNameColumn.setMinWidth(80);
        patientLastNameColumn.setMinWidth(80);
        patientDobColumn.setMinWidth(100);
        patientGenderColumn.setMinWidth(60);
        patientContactColumn.setMinWidth(80);
        
        patientsTable.setItems(patientsList);
        
        // Set column resize policy for better fit
        patientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Add listener for when the table width changes (window resize)
        patientsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                adjustPatientTableColumns();
            }
        });
        
        // Add listener for when the scene is shown (to adjust columns after rendering)
        patientsTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(this::adjustPatientTableColumns);
                
                // Also listen for window resize events
                newScene.windowProperty().addListener((winObs, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.widthProperty().addListener((widthObs, oldWidth, newWidth) -> {
                            Platform.runLater(this::adjustPatientTableColumns);
                        });
                    }
                });
            }
        });
        
        // Add selection listener for patient details
        patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showPatientDetails(newSelection);
            } else {
                patientDetailsPane.setVisible(false);
            }
        });
        
        // Hide patient details pane initially
        if (patientDetailsPane != null) {
            patientDetailsPane.setVisible(false);
        }
    }
    
    private void initializeHospitalManagement() {
        // Initialize hospital table columns
        hospitalIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        hospitalNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        hospitalAddressColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        hospitalContactColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContactNumber()));
        hospitalEmailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        hospitalUsernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        
        // Set initial column constraints
        hospitalIdColumn.setMinWidth(40);
        hospitalNameColumn.setMinWidth(100);
        hospitalAddressColumn.setMinWidth(150);
        hospitalContactColumn.setMinWidth(80);
        hospitalEmailColumn.setMinWidth(100);
        hospitalUsernameColumn.setMinWidth(80);
        
        hospitalsTable.setItems(hospitalsList);
        
        // Add selection listener for hospital details
        hospitalsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showHospitalDetails(newSelection);
            } else {
                hospitalDetailsPane.setVisible(false);
            }
        });
        
        // Set column resize properties for better fit
        hospitalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Add listener for when the table width changes (window resize)
        hospitalsTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                adjustHospitalTableColumns();
            }
        });
        
        // Add listener for when the scene is shown (to adjust columns after rendering)
        hospitalsTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(this::adjustHospitalTableColumns);
                
                // Also listen for window resize events
                newScene.windowProperty().addListener((winObs, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.widthProperty().addListener((widthObs, oldWidth, newWidth) -> {
                            Platform.runLater(this::adjustHospitalTableColumns);
                        });
                    }
                });
            }
        });
        
        // Hide hospital details pane initially
        if (hospitalDetailsPane != null) hospitalDetailsPane.setVisible(false);
    }
    
    private void initializeActivityLog() {
        if (recentActivityTable != null) {
            activityTimeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimestamp()));
            activityTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActivityType()));
            activityDescriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
            recentActivityTable.setItems(activityLogList);
        }
    }
    
    private void loadData() {
        try {
            // Load statistics for dashboard
            totalPatientsLabel.setText(String.valueOf(dbManager.getTotalPatientsCount()));
            totalDoctorsLabel.setText(String.valueOf(dbManager.getTotalDoctorsCount()));
            // pendingTreatmentsLabel.setText(String.valueOf(dbManager.getPendingTreatmentsCount()));
            
            // Add hospital count
            if (totalHospitalsLabel != null) {
                totalHospitalsLabel.setText(String.valueOf(dbManager.getTotalHospitalsCount()));
            }
            
            // Load gender distribution chart
            genderDistributionChart.getData().clear();
            Map<String, Integer> genderDistribution = dbManager.getPatientGenderDistribution();
            for (Map.Entry<String, Integer> entry : genderDistribution.entrySet()) {
                genderDistributionChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            
            // Load hospital region chart if available
            if (hospitalRegionChart != null) {
                hospitalRegionChart.getData().clear();
                Map<String, Integer> regionDistribution = dbManager.getHospitalRegionDistribution();
                if (regionDistribution != null && !regionDistribution.isEmpty()) {
                    for (Map.Entry<String, Integer> entry : regionDistribution.entrySet()) {
                        hospitalRegionChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
                    }
                } else {
                    // Add placeholder data if no regions are defined
                    hospitalRegionChart.getData().add(new PieChart.Data("Urban", 3));
                    hospitalRegionChart.getData().add(new PieChart.Data("Suburban", 2));
                    hospitalRegionChart.getData().add(new PieChart.Data("Rural", 1));
                }
            }
            
            // Load treatment urgency chart
            treatmentUrgencyChart.getData().clear();
            XYChart.Series<String, Number> urgencySeries = new XYChart.Series<>();
            urgencySeries.setName("Treatment Requests");
            
            // Use placeholder data instead of actual treatment data
            urgencySeries.getData().add(new XYChart.Data<>("Low", 12));
            urgencySeries.getData().add(new XYChart.Data<>("Medium", 8));
            urgencySeries.getData().add(new XYChart.Data<>("High", 5));
            urgencySeries.getData().add(new XYChart.Data<>("Emergency", 2));
            
            treatmentUrgencyChart.getData().add(urgencySeries);
            
            // Apply consistent colors to the chart
            ChartUtils.styleUrgencyChart(urgencySeries);
            
            // Also apply CSS styling directly to the chart
            treatmentUrgencyChart.getStylesheets().add(getClass().getResource("/com/hospital/chart-styles.css").toExternalForm());
            
            // Add style classes and tooltips to the bars
            for (XYChart.Data<String, Number> data : urgencySeries.getData()) {
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
            
            // Load activity log with placeholder data
            if (activityLogList != null) {
                activityLogList.clear();
                activityLogList.add(new ActivityLog("Today 10:30 AM", "Login", "Admin user logged in"));
                activityLogList.add(new ActivityLog("Today 10:35 AM", "Add", "New hospital registered: City Hospital"));
                activityLogList.add(new ActivityLog("Today 11:15 AM", "Update", "System configuration updated"));
                activityLogList.add(new ActivityLog("Yesterday 3:45 PM", "Add", "New patient registered: John Smith"));
                activityLogList.add(new ActivityLog("Yesterday 2:20 PM", "Update", "Hospital contact information updated"));
            }
            
            // Load all data tables
            loadAdmins();
            loadPatients();
            loadHospitals();
            
        } catch (SQLException e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error loading data: " + e.getMessage());
        }
    }
    
    private void loadTreatmentRequests() throws SQLException {
        // This method is no longer needed
    }
    
    private void loadAdmins() throws SQLException {
        adminsList.clear();
        adminsList.addAll(dbManager.getAllAdmins());
    }
    
    private void loadPatients() throws SQLException {
        patientsList.clear();
        List<Patient> patients = dbManager.getAllPatients();
        patientsList.addAll(patients);
        
        // Debug output
        System.out.println("Loaded " + patients.size() + " patients");
        for (Patient p : patients) {
            System.out.println("Patient: " + p.getId() + " - " + p.getFirstName() + " " + p.getLastName());
        }
        
        // Ensure the table is properly set with the items
        patientsTable.setItems(null); // Clear the table first
        patientsTable.layout(); // Force layout refresh
        patientsTable.setItems(patientsList); // Set the items again
        
        // Update status label if it exists
        if (patientStatusLabel != null) {
            patientStatusLabel.setText("Loaded " + patients.size() + " patients");
        }
        
        // Adjust columns to fit screen
        Platform.runLater(() -> {
            adjustPatientTableColumns();
            
            // Force another adjustment after a short delay to ensure proper sizing
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                    Platform.runLater(this::adjustPatientTableColumns);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
    
    private void loadHospitals() throws SQLException {
        hospitalsList.clear();
        List<Hospital> hospitals = dbManager.getAllHospitals();
        hospitalsList.addAll(hospitals);
        
        // Debug output
        System.out.println("Loaded " + hospitals.size() + " hospitals");
        for (Hospital h : hospitals) {
            System.out.println("Hospital: " + h.getId() + " - " + h.getName());
        }
        
        // If no hospitals exist, create a sample one
        if (hospitals.isEmpty()) {
            createSampleHospital();
            // Reload hospitals
            hospitals = dbManager.getAllHospitals();
            hospitalsList.clear();
            hospitalsList.addAll(hospitals);
        }
        
        // Update status label
        if (hospitalStatusLabel != null) {
            hospitalStatusLabel.setText("Loaded " + hospitals.size() + " hospitals");
        }
        
        // Ensure the table is properly set with the items
        hospitalsTable.setItems(null); // Clear the table first
        hospitalsTable.layout(); // Force layout refresh
        hospitalsTable.setItems(hospitalsList); // Set the items again
        
        // Adjust columns to fit screen
        // We need to wait for JavaFX to render the table before adjusting columns
        Platform.runLater(() -> {
            adjustHospitalTableColumns();
            
            // Force another adjustment after a short delay to ensure proper sizing
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                    Platform.runLater(this::adjustHospitalTableColumns);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
    
    private void createSampleHospital() {
        try {
            Hospital sampleHospital = new Hospital();
            sampleHospital.setName("City General Hospital");
            sampleHospital.setAddress("123 Main Street, Urban Area");
            sampleHospital.setContactNumber("555-123-4567");
            sampleHospital.setEmail("info@citygeneral.com");
            sampleHospital.setWebsite("www.citygeneral.com");
            sampleHospital.setUsername("citygeneral");
            sampleHospital.setPassword("hospital123");
            sampleHospital.setDescription("A leading healthcare provider in the city");
            
            dbManager.addHospital(sampleHospital);
            System.out.println("Created sample hospital");
            
            // Add to activity log
            activityLogList.add(0, new ActivityLog(
                formatCurrentTime(),
                "Add",
                "Sample hospital created: City General Hospital"
            ));
        } catch (SQLException e) {
            System.err.println("Error creating sample hospital: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showTreatmentDetails(TreatmentRequest request) {
        // This method is no longer needed
    }
    
    private void showHospitalDetails(Hospital hospital) {
        if (hospitalDetailsPane == null) return;
        
        editHospitalNameField.setText(hospital.getName());
        editHospitalAddressField.setText(hospital.getAddress());
        editHospitalContactField.setText(hospital.getContactNumber());
        editHospitalEmailField.setText(hospital.getEmail());
        editHospitalWebsiteField.setText(hospital.getWebsite());
        editHospitalUsernameField.setText(hospital.getUsername());
        editHospitalPasswordField.setText(""); // Don't display actual password for security
        
        hospitalDetailsPane.setVisible(true);
    }
    
    private void filterTreatmentRequests() {
        // This method is no longer needed
    }
    
    @FXML
    private void handleRefreshTreatments() {
        // This method is no longer needed
    }
    
    @FXML
    private void handleSaveAssignment() {
        // This method is no longer needed
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
    
    @FXML
    private void handleShowAddHospitalForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/hospital-form.fxml"));
            Parent root = loader.load();
            
            HospitalFormController controller = loader.getController();
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Add New Hospital");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh hospitals list after form is closed
            loadHospitals();
        } catch (IOException | SQLException e) {
            showAlert("Error opening hospital form: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleHospitalSearch() {
        String searchTerm = hospitalSearchField.getText().trim().toLowerCase();
        
        if (searchTerm.isEmpty()) {
            try {
                loadHospitals();
            } catch (SQLException e) {
                showAlert("Error refreshing hospitals: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        
        try {
            List<Hospital> searchResults = dbManager.searchHospitals(searchTerm);
            hospitalsList.clear();
            hospitalsList.addAll(searchResults);
        } catch (SQLException e) {
            showAlert("Error searching hospitals: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSaveHospitalChanges() {
        Hospital selectedHospital = hospitalsTable.getSelectionModel().getSelectedItem();
        if (selectedHospital == null) {
            showAlert("Please select a hospital to update");
            return;
        }
        
        // Validate input
        String name = editHospitalNameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Hospital name cannot be empty");
            return;
        }
        
        // Update hospital object
        selectedHospital.setName(name);
        selectedHospital.setAddress(editHospitalAddressField.getText().trim());
        selectedHospital.setContactNumber(editHospitalContactField.getText().trim());
        selectedHospital.setEmail(editHospitalEmailField.getText().trim());
        selectedHospital.setWebsite(editHospitalWebsiteField.getText().trim());
        selectedHospital.setUsername(editHospitalUsernameField.getText().trim());
        
        // Update password only if provided
        String password = editHospitalPasswordField.getText().trim();
        if (!password.isEmpty()) {
            selectedHospital.setPassword(password);
        }
        
        try {
            dbManager.updateHospital(selectedHospital);
            
            // Add to activity log
            activityLogList.add(0, new ActivityLog(
                formatCurrentTime(),
                "Update",
                "Hospital information updated: " + selectedHospital.getName()
            ));
            
            showAlert("Hospital updated successfully");
            loadHospitals();
        } catch (SQLException e) {
            showAlert("Error updating hospital: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDeleteHospital() {
        Hospital selectedHospital = hospitalsTable.getSelectionModel().getSelectedItem();
        if (selectedHospital == null) {
            showAlert("Please select a hospital to delete");
            return;
        }
        
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Hospital");
        confirmAlert.setContentText("Are you sure you want to delete the hospital: " + selectedHospital.getName() + "?\n\nThis action cannot be undone.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dbManager.deleteHospital(selectedHospital.getId());
                
                // Add to activity log
                activityLogList.add(0, new ActivityLog(
                    formatCurrentTime(),
                    "Delete",
                    "Hospital deleted: " + selectedHospital.getName()
                ));
                
                showAlert("Hospital deleted successfully");
                loadHospitals();
                hospitalDetailsPane.setVisible(false);
            } catch (SQLException e) {
                showAlert("Error deleting hospital: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleShowAddPatientForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hospital/patient-form.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Add New Patient");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            // Refresh patient list
            loadPatients();
        } catch (Exception e) {
            showAlert("Error opening patient form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDeletePatient() {
        Patient selectedPatient = patientsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showAlert("Please select a patient to delete");
            return;
        }
        
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Patient");
        confirmAlert.setContentText("Are you sure you want to delete the patient: " + 
                                    selectedPatient.getFirstName() + " " + selectedPatient.getLastName() + 
                                    "?\n\nThis action cannot be undone.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dbManager.deletePatient(selectedPatient.getId());
                
                // Add to activity log
                activityLogList.add(0, new ActivityLog(
                    formatCurrentTime(),
                    "Delete",
                    "Patient deleted: " + selectedPatient.getFirstName() + " " + selectedPatient.getLastName()
                ));
                
                showAlert("Patient deleted successfully");
                loadPatients();
            } catch (SQLException e) {
                showAlert("Error deleting patient: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void handleSaveMedicalHistory() {
        Patient selectedPatient = patientsTable.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            showAlert("Please select a patient");
            return;
        }
        
        String medicalHistory = medicalHistoryTextArea.getText();
        
        try {
            dbManager.updatePatientMedicalHistory(selectedPatient.getId(), medicalHistory);
            
            // Add to activity log
            activityLogList.add(0, new ActivityLog(
                formatCurrentTime(),
                "Update",
                "Medical history updated for patient: " + selectedPatient.getFirstName() + " " + selectedPatient.getLastName()
            ));
            
            showAlert("Medical history updated successfully");
        } catch (SQLException e) {
            showAlert("Error updating medical history: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRefreshHospitals() {
        try {
            // Clear the table first
            hospitalsList.clear();
            hospitalsTable.setItems(null);
            hospitalsTable.layout();
            
            // Update status label
            if (hospitalStatusLabel != null) {
                hospitalStatusLabel.setText("Refreshing hospital list...");
            }
            
            // Load hospitals from database
            loadHospitals();
            
            // Adjust table columns to fit screen
            adjustHospitalTableColumns();
            
            // Force refresh of the UI
            hospitalsTable.refresh();
            
            // Update status label with timestamp
            if (hospitalStatusLabel != null) {
                hospitalStatusLabel.setText("Refreshed at " + formatCurrentTime() + " - " + hospitalsList.size() + " hospitals");
            }
            
            showAlert("Hospital list refreshed successfully");
        } catch (SQLException e) {
            // Update status label with error
            if (hospitalStatusLabel != null) {
                hospitalStatusLabel.setText("Error refreshing: " + e.getMessage());
            }
            
            showAlert("Error refreshing hospitals: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRefreshPatients() {
        try {
            // Clear the table first
            patientsList.clear();
            patientsTable.setItems(null);
            patientsTable.layout();
            
            // Update status label
            if (patientStatusLabel != null) {
                patientStatusLabel.setText("Refreshing patient list...");
            }
            
            // Load patients from database
            loadPatients();
            
            // Adjust table columns to fit screen
            adjustPatientTableColumns();
            
            // Force refresh of the UI
            patientsTable.refresh();
            
            // Update status label with timestamp
            if (patientStatusLabel != null) {
                patientStatusLabel.setText("Refreshed at " + formatCurrentTime() + " - " + patientsList.size() + " patients");
            }
            
            showAlert("Patient list refreshed successfully");
        } catch (SQLException e) {
            // Update status label with error
            if (patientStatusLabel != null) {
                patientStatusLabel.setText("Error refreshing: " + e.getMessage());
            }
            
            showAlert("Error refreshing patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Adjusts the hospital table columns to fit the screen properly
     */
    private void adjustHospitalTableColumns() {
        // Set column resize policy
        hospitalsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Force the table to layout and update its width
        hospitalsTable.applyCss();
        hospitalsTable.layout();
        
        // Get the actual width of the table's content area
        double tableWidth = hospitalsTable.getWidth();
        if (tableWidth <= 0) {
            // If width is not yet available, use the scene width as a fallback
            if (hospitalsTable.getScene() != null) {
                tableWidth = hospitalsTable.getScene().getWidth() - 60; // Subtract padding
            } else {
                tableWidth = 800; // Default fallback width
            }
        }
        
        // Clear any previous constraints
        hospitalIdColumn.setMinWidth(40);
        hospitalNameColumn.setMinWidth(100);
        hospitalAddressColumn.setMinWidth(150);
        hospitalContactColumn.setMinWidth(80);
        hospitalEmailColumn.setMinWidth(100);
        hospitalUsernameColumn.setMinWidth(80);
        
        // Set proportional widths
        double totalWidth = tableWidth - 20; // Account for scrollbar and padding
        
        // ID column should be small
        hospitalIdColumn.setPrefWidth(totalWidth * 0.06); // 6%
        
        // Name is important, give it more space
        hospitalNameColumn.setPrefWidth(totalWidth * 0.20); // 20%
        
        // Address needs the most space
        hospitalAddressColumn.setPrefWidth(totalWidth * 0.30); // 30%
        
        // Contact and email get moderate space
        hospitalContactColumn.setPrefWidth(totalWidth * 0.14); // 14%
        
        hospitalEmailColumn.setPrefWidth(totalWidth * 0.20); // 20%
        
        // Username gets the remaining space
        hospitalUsernameColumn.setPrefWidth(totalWidth * 0.10); // 10%
        
        // Force the table to refresh
        hospitalsTable.refresh();
    }
    
    /**
     * Adjusts the patient table columns to fit the screen properly
     */
    private void adjustPatientTableColumns() {
        // Set column resize policy
        patientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Force the table to layout and update its width
        patientsTable.applyCss();
        patientsTable.layout();
        
        // Get the actual width of the table's content area
        double tableWidth = patientsTable.getWidth();
        if (tableWidth <= 0) {
            // If width is not yet available, use the scene width as a fallback
            if (patientsTable.getScene() != null) {
                tableWidth = patientsTable.getScene().getWidth() - 60; // Subtract padding
            } else {
                tableWidth = 800; // Default fallback width
            }
        }
        
        // Clear any previous constraints
        patientIdColumn.setMinWidth(40);
        patientFirstNameColumn.setMinWidth(80);
        patientLastNameColumn.setMinWidth(80);
        patientDobColumn.setMinWidth(100);
        patientGenderColumn.setMinWidth(60);
        patientContactColumn.setMinWidth(80);
        
        // Set proportional widths
        double totalWidth = tableWidth - 20; // Account for scrollbar and padding
        
        // ID column should be small
        patientIdColumn.setPrefWidth(totalWidth * 0.07); // 7%
        
        // Name columns
        patientFirstNameColumn.setPrefWidth(totalWidth * 0.20); // 20%
        patientLastNameColumn.setPrefWidth(totalWidth * 0.20); // 20%
        
        // Date of birth needs more space
        patientDobColumn.setPrefWidth(totalWidth * 0.20); // 20%
        
        // Gender is short
        patientGenderColumn.setPrefWidth(totalWidth * 0.13); // 13%
        
        // Contact gets the remaining space
        patientContactColumn.setPrefWidth(totalWidth * 0.20); // 20%
        
        // Force the table to refresh
        patientsTable.refresh();
    }
    
    private String formatCurrentTime() {
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, h:mm a");
        return java.time.LocalDateTime.now().format(formatter);
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showPatientDetails(Patient patient) {
        if (patientDetailsPane == null) return;
        
        patientFullNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
        
        LocalDate dob = patient.getDateOfBirth();
        String dobText = dob != null ? dob.toString() : "N/A";
        patientDobGenderLabel.setText(dobText + " / " + patient.getGender());
        
        patientContactLabel.setText(patient.getContactNumber());
        medicalHistoryTextArea.setText(patient.getMedicalHistory() != null ? patient.getMedicalHistory() : "");
        
        patientDetailsPane.setVisible(true);
    }
} 