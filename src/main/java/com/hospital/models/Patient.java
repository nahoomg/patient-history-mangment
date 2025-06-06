package com.hospital.models;

import java.time.LocalDate;

public class Patient {
    private int id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String contactNumber;
    private String address;
    private String medicalHistory;
    private String username;
    private String password;
    private int hospitalId;

    public Patient() {}

    public Patient(int id, String firstName, String lastName, LocalDate dateOfBirth, 
                  String gender, String contactNumber, String address, String medicalHistory) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.address = address;
        this.medicalHistory = medicalHistory;
    }

    // Constructor with username and password
    public Patient(int id, String firstName, String lastName, LocalDate dateOfBirth, 
                  String gender, String contactNumber, String address, String medicalHistory,
                  String username, String password) {
        this(id, firstName, lastName, dateOfBirth, gender, contactNumber, address, medicalHistory);
        this.username = username;
        this.password = password;
    }
    
    // Constructor with all fields including hospitalId
    public Patient(int id, String firstName, String lastName, LocalDate dateOfBirth, 
                  String gender, String contactNumber, String address, String medicalHistory,
                  String username, String password, int hospitalId) {
        this(id, firstName, lastName, dateOfBirth, gender, contactNumber, address, medicalHistory, username, password);
        this.hospitalId = hospitalId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getHospitalId() { return hospitalId; }
    public void setHospitalId(int hospitalId) { this.hospitalId = hospitalId; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
} 