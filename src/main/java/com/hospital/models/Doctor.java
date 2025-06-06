package com.hospital.models;

public class Doctor {
    private int id;
    private String firstName;
    private String lastName;
    private String specialization;
    private String contactNumber;
    private String email;
    private String username;
    private String password;
    private int hospitalId;

    public Doctor() {}

    public Doctor(int id, String firstName, String lastName, String specialization,
                 String contactNumber, String email, String username, String password, int hospitalId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.email = email;
        this.username = username;
        this.password = password;
        this.hospitalId = hospitalId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getHospitalId() { return hospitalId; }
    public void setHospitalId(int hospitalId) { this.hospitalId = hospitalId; }

    @Override
    public String toString() {
        return "Dr. " + firstName + " " + lastName + " (" + specialization + ")";
    }
} 