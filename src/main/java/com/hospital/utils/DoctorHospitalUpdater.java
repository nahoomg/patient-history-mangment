package com.hospital.utils;

import com.hospital.models.Doctor;
import com.hospital.models.Hospital;

import java.sql.SQLException;
import java.util.List;

/**
 * Utility class to update doctors with hospital IDs
 */
public class DoctorHospitalUpdater {
    
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        
        try {
            // Get all doctors
            List<Doctor> doctors = dbManager.getAllDoctors();
            System.out.println("Found " + doctors.size() + " doctors");
            
            // Get all hospitals
            List<Hospital> hospitals = dbManager.getAllHospitals();
            
            if (hospitals.isEmpty()) {
                System.out.println("No hospitals found. Please add hospitals first.");
                return;
            }
            
            // Get the first hospital as default
            int defaultHospitalId = hospitals.get(0).getId();
            System.out.println("Using hospital ID " + defaultHospitalId + " as default");
            
            // Update each doctor
            int updatedCount = 0;
            for (Doctor doctor : doctors) {
                if (doctor.getHospitalId() <= 0) {
                    dbManager.updateDoctorHospital(doctor.getId(), defaultHospitalId);
                    updatedCount++;
                }
            }
            
            System.out.println("Updated " + updatedCount + " doctors with hospital ID " + defaultHospitalId);
            
        } catch (SQLException e) {
            System.err.println("Error updating doctors: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 