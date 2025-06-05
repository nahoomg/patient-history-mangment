package com.hospital.controllers;

import com.hospital.models.Patient;
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

    private DatabaseManager dbManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dbManager = DatabaseManager.getInstance();
        genderComboBox.getItems().addAll("Male", "Female", "Other");
        dateOfBirthPicker.setValue(LocalDate.now().minusYears(18));
    }

    @FXML
    private void handleRegister() {
        if (validateInputs()) {
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
                stage.setScene(new Scene(root));
                stage.setTitle("City Hospital Management System");
                stage.show();
            } else {
                System.out.println("Error: Stage is null, manually navigating to login page");
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setTitle("City Hospital Management System");
                newStage.show();
            }
        } catch (IOException e) {
            System.out.println("Error going back to login: " + e.getMessage());
            e.printStackTrace();
            showMessage("Error going back to login: " + e.getMessage(), true);
        }
    }

    private boolean validateInputs() {
        // Basic validation
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
            dateOfBirthPicker.getValue() == null || genderComboBox.getValue() == null ||
            usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showMessage("Please fill in all required fields", true);
            return false;
        }

        // Password confirmation
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showMessage("Passwords do not match", true);
            return false;
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
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
    }
} 