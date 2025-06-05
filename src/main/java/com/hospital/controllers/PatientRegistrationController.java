package com.hospital.controllers;

import com.hospital.Main;
import com.hospital.models.Patient;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class PatientRegistrationController implements Initializable {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private DatePicker dateOfBirthPicker;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField contactNumberField;
    @FXML private TextArea addressField;
    @FXML private TextArea medicalHistoryField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private TabPane registrationTabPane;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button registerButton;
    @FXML private Text stepIndicator;

    private DatabaseManager dbManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        genderComboBox.getItems().addAll("Male", "Female", "Other");
        dateOfBirthPicker.setValue(LocalDate.now().minusYears(18));
        
        // Set up tab navigation
        updateNavigationButtons();
        
        // Disable tab selection by clicking (force use of navigation buttons)
        registrationTabPane.setTabDragPolicy(TabPane.TabDragPolicy.FIXED);
        
        // Add listener to update step indicator when tab changes
        registrationTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateStepIndicator(newVal.intValue());
            updateNavigationButtons();
        });
    }
    
    @FXML
    private void handlePrevious() {
        int currentIndex = registrationTabPane.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            registrationTabPane.getSelectionModel().select(currentIndex - 1);
        }
    }
    
    @FXML
    private void handleNext() {
        int currentIndex = registrationTabPane.getSelectionModel().getSelectedIndex();
        int tabCount = registrationTabPane.getTabs().size();
        
        if (validateCurrentTab(currentIndex)) {
            if (currentIndex < tabCount - 1) {
                registrationTabPane.getSelectionModel().select(currentIndex + 1);
            }
        }
    }
    
    private boolean validateCurrentTab(int tabIndex) {
        switch (tabIndex) {
            case 0: // Personal Information
                if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                    dateOfBirthPicker.getValue() == null || genderComboBox.getValue() == null) {
                    showMessage("Please fill in all personal information fields", true);
                    return false;
                }
                return true;
                
            case 1: // Contact Information
                if (contactNumberField.getText().isEmpty() || addressField.getText().isEmpty()) {
                    showMessage("Please fill in all contact information fields", true);
                    return false;
                }
                return true;
                
            case 2: // Medical Information
                // Medical history is optional
                return true;
                
            case 3: // Account Information
                if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty() || 
                    confirmPasswordField.getText().isEmpty()) {
                    showMessage("Please fill in all account information fields", true);
                    return false;
                }
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showMessage("Passwords do not match", true);
                    return false;
                }
                return true;
                
            default:
                return true;
        }
    }
    
    private void updateNavigationButtons() {
        int currentIndex = registrationTabPane.getSelectionModel().getSelectedIndex();
        int tabCount = registrationTabPane.getTabs().size();
        
        // Update Previous button
        prevButton.setDisable(currentIndex == 0);
        
        // Update Next/Register buttons
        if (currentIndex == tabCount - 1) {
            nextButton.setVisible(false);
            registerButton.setVisible(true);
        } else {
            nextButton.setVisible(true);
            registerButton.setVisible(false);
        }
    }
    
    private void updateStepIndicator(int tabIndex) {
        String[] steps = {"Personal Information", "Contact Information", "Medical Information", "Account Information"};
        if (tabIndex >= 0 && tabIndex < steps.length) {
            stepIndicator.setText("Step " + (tabIndex + 1) + " of " + steps.length + ": " + steps[tabIndex]);
        }
    }

    @FXML
    private void handleRegister() {
        if (validateAllInputs()) {
            try {
                Patient patient = new Patient(
                    0, // ID will be set by database
                    firstNameField.getText(),
                    lastNameField.getText(),
                    dateOfBirthPicker.getValue(),
                    genderComboBox.getValue(),
                    contactNumberField.getText(),
                    addressField.getText(),
                    medicalHistoryField.getText(),
                    usernameField.getText(),
                    passwordField.getText()
                );

                dbManager.addPatient(patient);
                showMessage("Registration successful! Please log in.", false);
                
                // Clear fields after successful registration
                clearFields();
                
                // Automatically go back to login after 2 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            try {
                                backToLogin();
                            } catch (Exception e) {
                                System.out.println("Error returning to login: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                
            } catch (SQLException e) {
                showMessage("Error registering patient: " + e.getMessage(), true);
            }
        }
    }

    @FXML
    private void backToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/login.fxml"));
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            if (stage != null) {
                Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
                stage.setScene(scene);
                stage.setTitle("City Hospital Management System");
                stage.setMaximized(Main.USE_MAXIMIZED);
                stage.show();
            } else {
                System.out.println("Error: Stage is null, manually navigating to login page");
                Stage newStage = new Stage();
                Scene scene = new Scene(root, Main.DEFAULT_WIDTH, Main.DEFAULT_HEIGHT);
                newStage.setScene(scene);
                newStage.setTitle("City Hospital Management System");
                newStage.setMaximized(Main.USE_MAXIMIZED);
                newStage.show();
            }
        } catch (IOException e) {
            System.out.println("Error going back to login: " + e.getMessage());
            e.printStackTrace();
            showMessage("Error going back to login: " + e.getMessage(), true);
        }
    }

    private boolean validateAllInputs() {
        // Validate all tabs
        for (int i = 0; i < registrationTabPane.getTabs().size(); i++) {
            if (!validateCurrentTab(i)) {
                registrationTabPane.getSelectionModel().select(i);
                return false;
            }
        }
        return true;
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        dateOfBirthPicker.setValue(LocalDate.now().minusYears(18));
        genderComboBox.setValue(null);
        contactNumberField.clear();
        addressField.clear();
        medicalHistoryField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        
        // Reset to first tab
        registrationTabPane.getSelectionModel().select(0);
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
} 