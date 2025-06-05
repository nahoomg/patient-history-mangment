package com.hospital;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.hospital.models.Doctor;
import com.hospital.models.Patient;
import com.hospital.utils.DatabaseManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class Main extends Application {
    private DatabaseManager dbManager;
    private Doctor currentDoctor;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/hospital/login.fxml"));
            primaryStage.setTitle("City Hospital Management System");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLoginScreen(Stage stage) {
        VBox loginBox = new VBox(10);
        loginBox.setPadding(new Insets(20));
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setStyle("-fx-background-color: #f5f5f5;");

        // Hospital Logo/Title
        Label hospitalLabel = new Label("City Hospital");
        hospitalLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        Label titleLabel = new Label("Patient Management System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Doctor Login");
        subtitleLabel.setStyle("-fx-font-size: 16px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-font-size: 14px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-font-size: 14px;");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px;");
        loginButton.setMaxWidth(300);

        Label orLabel = new Label("OR");
        orLabel.setStyle("-fx-font-size: 14px;");

        Button registerPatientButton = new Button("Register New Patient");
        registerPatientButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        registerPatientButton.setMaxWidth(300);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        loginButton.setOnAction(e -> {
            try {
                Doctor doctor = dbManager.authenticateDoctor(
                    usernameField.getText(),
                    passwordField.getText()
                );
                
                if (doctor != null) {
                    currentDoctor = doctor;
                    showMainScreen(stage);
                } else {
                    messageLabel.setText("Invalid username or password");
                }
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        registerPatientButton.setOnAction(e -> showPatientRegistrationForm(stage));

        // Add some spacing between elements
        Region spacer1 = new Region();
        spacer1.setPrefHeight(20);
        Region spacer2 = new Region();
        spacer2.setPrefHeight(20);

        loginBox.getChildren().addAll(
            hospitalLabel,
            titleLabel,
            spacer1,
            subtitleLabel,
            new Label("Username:"),
            usernameField,
            new Label("Password:"),
            passwordField,
            loginButton,
            spacer2,
            new Separator(),
            orLabel,
            registerPatientButton,
            messageLabel
        );

        Scene scene = new Scene(loginBox, 400, 600);
        stage.setTitle("City Hospital - Patient Management System");
        stage.setScene(scene);
        stage.show();
    }

    private void showPatientRegistrationForm(Stage stage) {
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(20));
        formBox.setAlignment(Pos.CENTER);
        formBox.setStyle("-fx-background-color: #f5f5f5;");

        // Hospital Logo/Title
        Label hospitalLabel = new Label("City Hospital");
        hospitalLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        Label titleLabel = new Label("New Patient Registration");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Create input fields
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.setMaxWidth(300);

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.setMaxWidth(300);

        DatePicker dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setPromptText("Date of Birth");
        dateOfBirthPicker.setMaxWidth(300);

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        genderBox.setPromptText("Select Gender");
        genderBox.setMaxWidth(300);

        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");
        contactField.setMaxWidth(300);

        TextField addressField = new TextField();
        addressField.setPromptText("Address");
        addressField.setMaxWidth(300);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setMaxWidth(300);

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        registerButton.setMaxWidth(300);

        Button backButton = new Button("Back to Login");
        backButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-size: 14px;");
        backButton.setMaxWidth(300);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        registerButton.setOnAction(e -> {
            if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                dateOfBirthPicker.getValue() == null || genderBox.getValue() == null ||
                usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                messageLabel.setText("Please fill in all required fields!");
                return;
            }

            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                messageLabel.setText("Passwords do not match!");
                return;
            }

            try {
                Patient newPatient = new Patient(
                    0,
                    firstNameField.getText(),
                    lastNameField.getText(),
                    dateOfBirthPicker.getValue(),
                    genderBox.getValue(),
                    contactField.getText(),
                    addressField.getText(),
                    "", // Empty medical history - will be updated by doctors
                    usernameField.getText(),
                    passwordField.getText()
                );
                
                dbManager.addPatient(newPatient);
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Registration successful! You can now access your records through the hospital system.");
                
                // Clear fields and return to login after delay
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        javafx.application.Platform.runLater(() -> showLoginScreen(stage));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }).start();
                
            } catch (Exception ex) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        backButton.setOnAction(e -> showLoginScreen(stage));

        // Add some spacing between sections
        Region spacer1 = new Region();
        spacer1.setPrefHeight(20);
        Region spacer2 = new Region();
        spacer2.setPrefHeight(20);

        formBox.getChildren().addAll(
            hospitalLabel,
            titleLabel,
            spacer1,
            new Label("Personal Information"),
            new Label("First Name:*"),
            firstNameField,
            new Label("Last Name:*"),
            lastNameField,
            new Label("Date of Birth:*"),
            dateOfBirthPicker,
            new Label("Gender:*"),
            genderBox,
            spacer2,
            new Label("Contact Information"),
            new Label("Contact Number:"),
            contactField,
            new Label("Address:"),
            addressField,
            new Separator(),
            new Label("Account Information"),
            new Label("Username:*"),
            usernameField,
            new Label("Password:*"),
            passwordField,
            new Label("Confirm Password:*"),
            confirmPasswordField,
            registerButton,
            new Separator(),
            backButton,
            messageLabel
        );

        ScrollPane scrollPane = new ScrollPane(formBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        Scene scene = new Scene(scrollPane, 400, 700);
        stage.setTitle("City Hospital - Patient Registration");
        stage.setScene(scene);
    }

    private void showMainScreen(Stage stage) {
        TabPane tabPane = new TabPane();
        
        // Patients List Tab
        Tab patientsTab = new Tab("Patients");
        patientsTab.setClosable(false);
        VBox patientsBox = new VBox(10);
        patientsBox.setPadding(new Insets(10));
        
        TableView<Patient> patientTable = new TableView<>();
        
        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getFirstName() + " " + data.getValue().getLastName()
            )
        );
        
        TableColumn<Patient, String> dobCol = new TableColumn<>("Date of Birth");
        dobCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDateOfBirth().toString()
            )
        );
        
        patientTable.getColumns().addAll(nameCol, dobCol);
        
        Button viewDetailsButton = new Button("View Details");
        Button addPatientButton = new Button("Add New Patient");
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(viewDetailsButton, addPatientButton);
        
        patientsBox.getChildren().addAll(patientTable, buttonBox);
        patientsTab.setContent(patientsBox);
        
        // Profile Tab
        Tab profileTab = new Tab("Profile");
        profileTab.setClosable(false);
        VBox profileBox = new VBox(10);
        profileBox.setPadding(new Insets(10));
        
        Label nameLabel = new Label("Dr. " + currentDoctor.getFirstName() + 
                                  " " + currentDoctor.getLastName());
        Label specLabel = new Label("Specialization: " + 
                                  currentDoctor.getSpecialization());
        
        profileBox.getChildren().addAll(nameLabel, specLabel);
        profileTab.setContent(profileBox);
        
        tabPane.getTabs().addAll(patientsTab, profileTab);
        
        // Refresh patient list
        try {
            patientTable.getItems().setAll(dbManager.getAllPatients());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Handle view details button
        viewDetailsButton.setOnAction(e -> {
            Patient selectedPatient = 
                patientTable.getSelectionModel().getSelectedItem();
            if (selectedPatient != null) {
                showPatientDetails(selectedPatient);
            }
        });
        
        // Handle add patient button
        addPatientButton.setOnAction(e -> showAddPatientDialog());
        
        Scene scene = new Scene(tabPane, 800, 600);
        stage.setTitle("Hospital Management System - Dr. " + 
                      currentDoctor.getLastName());
        stage.setScene(scene);
    }

    private void showPatientDetails(Patient patient) {
        Stage detailsStage = new Stage();
        VBox detailsBox = new VBox(10);
        detailsBox.setPadding(new Insets(20));

        TextArea historyArea = new TextArea(patient.getMedicalHistory());
        historyArea.setWrapText(true);
        historyArea.setPrefRowCount(10);

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            try {
                dbManager.updatePatientHistory(patient.getId(), historyArea.getText());
                detailsStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        detailsBox.getChildren().addAll(
            new Label("Patient: " + patient.getFirstName() + " " + patient.getLastName()),
            new Label("Date of Birth: " + patient.getDateOfBirth()),
            new Label("Medical History:"),
            historyArea,
            saveButton
        );

        Scene scene = new Scene(detailsBox, 400, 500);
        detailsStage.setTitle("Patient Details");
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    private void showAddPatientDialog() {
        Stage addStage = new Stage();
        VBox addBox = new VBox(10);
        addBox.setPadding(new Insets(20));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        DatePicker dobPicker = new DatePicker();
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("Male", "Female", "Other");
        TextField contactField = new TextField();
        contactField.setPromptText("Contact Number");
        TextField addressField = new TextField();
        addressField.setPromptText("Address");
        TextArea historyArea = new TextArea();
        historyArea.setPromptText("Medical History");

        Button saveButton = new Button("Save Patient");
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        saveButton.setOnAction(e -> {
            try {
                Patient newPatient = new Patient(
                    0,
                    firstNameField.getText(),
                    lastNameField.getText(),
                    dobPicker.getValue(),
                    genderBox.getValue(),
                    contactField.getText(),
                    addressField.getText(),
                    historyArea.getText()
                );
                dbManager.addPatient(newPatient);
                addStage.close();
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        addBox.getChildren().addAll(
            new Label("First Name:"),
            firstNameField,
            new Label("Last Name:"),
            lastNameField,
            new Label("Date of Birth:"),
            dobPicker,
            new Label("Gender:"),
            genderBox,
            new Label("Contact Number:"),
            contactField,
            new Label("Address:"),
            addressField,
            new Label("Medical History:"),
            historyArea,
            saveButton,
            messageLabel
        );

        Scene scene = new Scene(addBox, 400, 600);
        addStage.setTitle("Add New Patient");
        addStage.setScene(scene);
        addStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 