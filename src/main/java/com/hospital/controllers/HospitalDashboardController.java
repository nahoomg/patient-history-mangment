package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Hospital;
import com.hospital.utils.DatabaseManager;
import com.hospital.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class HospitalDashboardController implements Initializable {
    @FXML private Label hospitalNameLabel;
    @FXML private Label totalDoctorsLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label servicesLabel;
    
    @FXML private Label nameValueLabel;
    @FXML private Label addressValueLabel;
    @FXML private Label contactValueLabel;
    @FXML private Label emailValueLabel;
    @FXML private Label websiteValueLabel;
    @FXML private TextArea descriptionTextArea;
    
    @FXML private VBox dashboardContent;
    @FXML private VBox doctorsContent;
    @FXML private VBox patientsContent;
    @FXML private VBox facilitiesContent;
    @FXML private VBox servicesContent;
    @FXML private VBox profileContent;
    
    private Hospital hospital;
    private DatabaseManager dbManager = new DatabaseManager();
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadHospitalData();
    }
    
    private void loadHospitalData() {
        try {
            // Get current hospital from session
            hospital = (Hospital) Session.getInstance().getCurrentUser();
            
            if (hospital == null) {
                showAlert("Error", "Hospital data not found. Please login again.");
                handleLogout();
                return;
            }
            
            // Set hospital name in header
            hospitalNameLabel.setText(hospital.getName());
            
            // Set hospital information
            nameValueLabel.setText(hospital.getName());
            addressValueLabel.setText(hospital.getAddress());
            contactValueLabel.setText(hospital.getContactNumber());
            emailValueLabel.setText(hospital.getEmail());
            websiteValueLabel.setText(hospital.getWebsite() != null ? hospital.getWebsite() : "Not provided");
            descriptionTextArea.setText(hospital.getDescription() != null ? hospital.getDescription() : "No description provided.");
            
            // Set placeholder statistics (would come from database in real implementation)
            totalDoctorsLabel.setText("0");
            totalPatientsLabel.setText("0");
            servicesLabel.setText("0");
            
        } catch (ClassCastException e) {
            showAlert("Error", "Invalid user data. Please login again.");
            handleLogout();
        } catch (Exception e) {
            showAlert("Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
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
            showAlert("Error", "Error navigating to login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showDashboard() {
        hideAllContent();
        dashboardContent.setVisible(true);
    }
    
    @FXML
    private void showDoctors() {
        hideAllContent();
        doctorsContent.setVisible(true);
    }
    
    @FXML
    private void showPatients() {
        hideAllContent();
        patientsContent.setVisible(true);
    }
    
    @FXML
    private void showFacilities() {
        hideAllContent();
        facilitiesContent.setVisible(true);
    }
    
    @FXML
    private void showServices() {
        hideAllContent();
        servicesContent.setVisible(true);
    }
    
    @FXML
    private void showProfile() {
        hideAllContent();
        profileContent.setVisible(true);
    }
    
    private void hideAllContent() {
        dashboardContent.setVisible(false);
        doctorsContent.setVisible(false);
        patientsContent.setVisible(false);
        facilitiesContent.setVisible(false);
        servicesContent.setVisible(false);
        profileContent.setVisible(false);
    }
    
    @FXML
    private void handleEditInfo() {
        showAlert("Information", "Edit hospital information feature will be implemented in the next phase.");
    }
    
    @FXML
    private void handleAddDoctor() {
        showAlert("Information", "Add doctor feature will be implemented in the next phase.");
    }
    
    @FXML
    private void handleAddFacility() {
        showAlert("Information", "Add facility feature will be implemented in the next phase.");
    }
    
    @FXML
    private void handleAddService() {
        showAlert("Information", "Add service feature will be implemented in the next phase.");
    }
    
    @FXML
    private void handleChangePassword() {
        showAlert("Information", "Change password feature will be implemented in the next phase.");
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 