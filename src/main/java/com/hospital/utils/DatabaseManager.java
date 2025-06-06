package com.hospital.utils;

import com.hospital.models.Doctor;
import com.hospital.models.Patient;
import com.hospital.models.Admin;
import com.hospital.models.TreatmentRequest;
import com.hospital.models.Hospital;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:hospital.db";
    private static DatabaseManager instance;

    public DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        File dbFile = new File("hospital.db");
        boolean isNewDatabase = !dbFile.exists();
        
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                if (isNewDatabase) {
                    System.out.println("A new database has been created.");
                }
                createTables(conn, isNewDatabase);
                updateExistingTables(conn);
            }
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables(Connection conn, boolean isNewDatabase) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            if (isNewDatabase) {
                // Create admins table
                String createAdminsTable = """
                    CREATE TABLE IF NOT EXISTS admins (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL
                    )
                """;
                stmt.execute(createAdminsTable);
                
                // Create patients table
                String createPatientsTable = """
                    CREATE TABLE IF NOT EXISTS patients (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        dateOfBirth TEXT NOT NULL,
                        gender TEXT NOT NULL,
                        address TEXT NOT NULL,
                        phoneNumber TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        bloodType TEXT,
                        allergies TEXT,
                        medicalHistory TEXT
                    )
                """;
                stmt.execute(createPatientsTable);
                
                // Create hospitals table
                String createHospitalsTable = """
                    CREATE TABLE IF NOT EXISTS hospitals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        address TEXT NOT NULL,
                        contactNumber TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        website TEXT,
                        username TEXT UNIQUE NOT NULL,
                        description TEXT,
                        rating REAL DEFAULT 0,
                        numRatings INTEGER DEFAULT 0
                    )
                """;
                stmt.execute(createHospitalsTable);
                
                // Create doctors table
                String createDoctorsTable = """
                    CREATE TABLE IF NOT EXISTS doctors (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        specialization TEXT NOT NULL,
                        contactNumber TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        hospital_id INTEGER,
                        FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
                    )
                """;
                stmt.execute(createDoctorsTable);
                
                // Create treatment_requests table
                String createTreatmentRequestsTable = """
                    CREATE TABLE IF NOT EXISTS treatment_requests (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        patient_id INTEGER NOT NULL,
                        date_requested TEXT NOT NULL,
                        preferred_date TEXT NOT NULL,
                        urgency TEXT NOT NULL,
                        symptoms TEXT NOT NULL,
                        status TEXT NOT NULL,
                        assigned_doctor_id INTEGER,
                        hospital_id INTEGER,
                        FOREIGN KEY (patient_id) REFERENCES patients(id),
                        FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(id),
                        FOREIGN KEY (hospital_id) REFERENCES hospitals(id)
                    )
                """;
                stmt.execute(createTreatmentRequestsTable);
                
                // Create activity_logs table
                String createActivityLogsTable = """
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_type TEXT NOT NULL,
                        user_id INTEGER NOT NULL,
                        action TEXT NOT NULL,
                        timestamp TEXT NOT NULL
                    )
                """;
                stmt.execute(createActivityLogsTable);
                
                // Insert default admin
                String insertAdmin = "INSERT INTO admins (firstName, lastName, email, username, password) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertAdmin)) {
                    pstmt.setString(1, "Admin");
                    pstmt.setString(2, "User");
                    pstmt.setString(3, "admin@hospital.com");
                    pstmt.setString(4, "admin");
                    pstmt.setString(5, "admin123");
                    pstmt.executeUpdate();
                }
            }
        }
    }

    private void updateExistingTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Check if hospital_id column exists in treatment_requests table
            ResultSet rs = conn.getMetaData().getColumns(null, null, "treatment_requests", "hospital_id");
            if (!rs.next()) {
                // Add hospital_id to treatment_requests
                String alterTreatmentRequestsTable = "ALTER TABLE treatment_requests ADD COLUMN hospital_id INTEGER REFERENCES hospitals(id)";
                stmt.execute(alterTreatmentRequestsTable);
                System.out.println("Added hospital_id column to treatment_requests table");
            }

            // Check if hospital_id column exists in doctors table
            rs = conn.getMetaData().getColumns(null, null, "doctors", "hospital_id");
            if (!rs.next()) {
                // Add hospital_id to doctors
                String alterDoctorsTable = "ALTER TABLE doctors ADD COLUMN hospital_id INTEGER REFERENCES hospitals(id)";
                stmt.execute(alterDoctorsTable);
                System.out.println("Added hospital_id column to doctors table");
            }
            
            // Check if hospital_id column exists in patients table
            rs = conn.getMetaData().getColumns(null, null, "patients", "hospital_id");
            if (!rs.next()) {
                // Add hospital_id to patients
                String alterPatientsTable = "ALTER TABLE patients ADD COLUMN hospital_id INTEGER REFERENCES hospitals(id)";
                stmt.execute(alterPatientsTable);
                System.out.println("Added hospital_id column to patients table");
            }
        } catch (SQLException e) {
            System.out.println("Warning: Could not update existing tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Admin operations
    public Admin authenticateAdmin(String username, String password) throws SQLException {
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Admin(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("fullName"),
                    rs.getString("email")
                );
            }
        }
        return null;
    }
    
    public void addAdmin(Admin admin) throws SQLException {
        String sql = "INSERT INTO admins (username, password, fullName, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, admin.getUsername());
            pstmt.setString(2, admin.getPassword());
            pstmt.setString(3, admin.getFullName());
            pstmt.setString(4, admin.getEmail());
            pstmt.executeUpdate();
        }
    }
    
    public List<Admin> getAllAdmins() throws SQLException {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admins";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Admin admin = new Admin(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("fullName"),
                    rs.getString("email")
                );
                admins.add(admin);
            }
        }
        return admins;
    }

    // Doctor operations
    public void addDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (firstName, lastName, specialization, contactNumber, email, username, password, hospital_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        System.out.println("Adding doctor with hospital ID: " + doctor.getHospitalId());
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getLastName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getContactNumber());
            pstmt.setString(5, doctor.getEmail());
            pstmt.setString(6, doctor.getUsername());
            pstmt.setString(7, doctor.getPassword());
            
            if (doctor.getHospitalId() > 0) {
                pstmt.setInt(8, doctor.getHospitalId());
            } else {
                throw new SQLException("Hospital ID is required for doctors");
            }
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating doctor failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    doctor.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating doctor failed, no ID obtained.");
                }
            }
        }
    }

    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Doctor doctor = new Doctor(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("specialization"),
                    rs.getString("contactNumber"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getObject("hospital_id") != null ? rs.getInt("hospital_id") : 0
                );
                doctors.add(doctor);
            }
        }
        return doctors;
    }
    
    public List<Doctor> searchDoctors(String searchTerm) throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE firstName LIKE ? OR lastName LIKE ? OR specialization LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String term = "%" + searchTerm + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Doctor doctor = new Doctor(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("specialization"),
                    rs.getString("contactNumber"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getObject("hospital_id") != null ? rs.getInt("hospital_id") : 0
                );
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    public Doctor authenticateDoctor(String username, String password) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Doctor(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("specialization"),
                    rs.getString("contactNumber"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getObject("hospital_id") != null ? rs.getInt("hospital_id") : 0
                );
            }
        }
        return null;
    }
    
    public Doctor getDoctorById(int id) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Doctor(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("specialization"),
                    rs.getString("contactNumber"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getObject("hospital_id") != null ? rs.getInt("hospital_id") : 0
                );
            }
        }
        return null;
    }

    public void updateDoctor(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctors SET firstName = ?, lastName = ?, specialization = ?, contactNumber = ?, email = ?, username = ?, password = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getLastName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getContactNumber());
            pstmt.setString(5, doctor.getEmail());
            pstmt.setString(6, doctor.getUsername());
            pstmt.setString(7, doctor.getPassword());
            pstmt.setInt(8, doctor.getId());
            pstmt.executeUpdate();
        }
    }

    public void deleteDoctor(int doctorId) throws SQLException {
        // First check if the doctor is assigned to any treatment requests
        String checkSql = "SELECT COUNT(*) FROM treatment_requests WHERE assigned_doctor_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, doctorId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Cannot delete doctor: Doctor is assigned to treatment requests. Please reassign the requests first.");
            }
            
            // If no assignments, proceed with deletion
            String deleteSql = "DELETE FROM doctors WHERE id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, doctorId);
                deleteStmt.executeUpdate();
            }
        }
    }

    // Patient operations
    public void addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (firstName, lastName, dateOfBirth, gender, contactNumber, address, medicalHistory, username, password, hospital_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setString(3, patient.getDateOfBirth().toString());
            pstmt.setString(4, patient.getGender());
            pstmt.setString(5, patient.getContactNumber());
            pstmt.setString(6, patient.getAddress());
            pstmt.setString(7, patient.getMedicalHistory());
            pstmt.setString(8, patient.getUsername());
            pstmt.setString(9, patient.getPassword());
            pstmt.setInt(10, patient.getHospitalId());
            pstmt.executeUpdate();
        }
    }

    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    LocalDate.parse(rs.getString("dateOfBirth")),
                    rs.getString("gender"),
                    rs.getString("contactNumber"),
                    rs.getString("address"),
                    rs.getString("medicalHistory"),
                    rs.getString("username"),
                    rs.getString("password")
                );
                patients.add(patient);
            }
        }
        return patients;
    }
    
    public List<Patient> searchPatients(String searchTerm) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE firstName LIKE ? OR lastName LIKE ? OR contactNumber LIKE ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String term = "%" + searchTerm + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    LocalDate.parse(rs.getString("dateOfBirth")),
                    rs.getString("gender"),
                    rs.getString("contactNumber"),
                    rs.getString("address"),
                    rs.getString("medicalHistory"),
                    rs.getString("username"),
                    rs.getString("password")
                );
                patients.add(patient);
            }
        }
        return patients;
    }

    public void updatePatientHistory(int patientId, String medicalHistory) throws SQLException {
        String sql = "UPDATE patients SET medicalHistory = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medicalHistory);
            pstmt.setInt(2, patientId);
            pstmt.executeUpdate();
        }
    }
    
    public String getPatientMedicalHistory(int patientId) throws SQLException {
        String sql = "SELECT medicalHistory FROM patients WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("medicalHistory");
            }
            return "";
        }
    }
    
    public void updatePatientMedicalHistory(int patientId, String medicalHistory) throws SQLException {
        updatePatientHistory(patientId, medicalHistory);
    }

    public void updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET firstName = ?, lastName = ?, contactNumber = ?, address = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setString(3, patient.getContactNumber());
            pstmt.setString(4, patient.getAddress());
            pstmt.setInt(5, patient.getId());
            pstmt.executeUpdate();
        }
    }

    public Patient getPatientById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Patient(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    LocalDate.parse(rs.getString("dateOfBirth")),
                    rs.getString("gender"),
                    rs.getString("contactNumber"),
                    rs.getString("address"),
                    rs.getString("medicalHistory"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        }
        return null;
    }

    public Patient authenticatePatient(String username, String password) throws SQLException {
        String sql = "SELECT * FROM patients WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Patient(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    LocalDate.parse(rs.getString("dateOfBirth")),
                    rs.getString("gender"),
                    rs.getString("contactNumber"),
                    rs.getString("address"),
                    rs.getString("medicalHistory"),
                    rs.getString("username"),
                    rs.getString("password")
                );
            }
        }
        return null;
    }
    
    // Treatment Request operations
    public void addTreatmentRequest(TreatmentRequest request) throws SQLException {
        String sql = "INSERT INTO treatment_requests (patient_id, date_requested, preferred_date, urgency, symptoms, status, assigned_doctor_id, hospital_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        System.out.println("Adding treatment request with hospital ID: " + request.getHospitalId());
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, request.getPatientId());
            pstmt.setString(2, request.getDateRequested().toString());
            pstmt.setString(3, request.getPreferredDate().toString());
            pstmt.setString(4, request.getUrgency());
            pstmt.setString(5, request.getSymptoms());
            pstmt.setString(6, request.getStatus());
            
            if (request.getAssignedDoctorId() > 0) {
                pstmt.setInt(7, request.getAssignedDoctorId());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            if (request.getHospitalId() > 0) {
                pstmt.setInt(8, request.getHospitalId());
            } else {
                throw new SQLException("Hospital ID is required for treatment requests");
            }
            
            pstmt.executeUpdate();
        }
    }
    
    public List<TreatmentRequest> getAllTreatmentRequests() throws SQLException {
        List<TreatmentRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.firstName || ' ' || p.lastName AS patient_name,
                   d.firstName || ' ' || d.lastName AS doctor_name,
                   h.name AS hospital_name
            FROM treatment_requests tr
            JOIN patients p ON tr.patient_id = p.id
            LEFT JOIN doctors d ON tr.assigned_doctor_id = d.id
            LEFT JOIN hospitals h ON tr.hospital_id = h.id
            ORDER BY 
                CASE 
                    WHEN tr.urgency = 'Emergency' THEN 1
                    WHEN tr.urgency = 'High' THEN 2
                    WHEN tr.urgency = 'Medium' THEN 3
                    WHEN tr.urgency = 'Low' THEN 4
                    ELSE 5
                END,
                tr.date_requested DESC
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                TreatmentRequest request = new TreatmentRequest(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    rs.getString("patient_name"),
                    LocalDate.parse(rs.getString("date_requested")),
                    LocalDate.parse(rs.getString("preferred_date")),
                    rs.getString("urgency"),
                    rs.getString("symptoms"),
                    rs.getString("status"),
                    rs.getObject("assigned_doctor_id") != null ? rs.getInt("assigned_doctor_id") : 0,
                    rs.getString("doctor_name"),
                    rs.getObject("hospital_id") != null ? rs.getInt("hospital_id") : 0,
                    rs.getString("hospital_name")
                );
                requests.add(request);
            }
        }
        return requests;
    }
    
    public List<TreatmentRequest> getTreatmentRequestsByUrgency(String urgency) throws SQLException {
        List<TreatmentRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.firstName || ' ' || p.lastName AS patient_name,
                   d.firstName || ' ' || d.lastName AS doctor_name,
                   h.name AS hospital_name
            FROM treatment_requests tr
            JOIN patients p ON tr.patient_id = p.id
            LEFT JOIN doctors d ON tr.assigned_doctor_id = d.id
            LEFT JOIN hospitals h ON tr.hospital_id = h.id
            WHERE tr.urgency = ?
            ORDER BY tr.date_requested DESC
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, urgency);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                TreatmentRequest request = new TreatmentRequest(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    rs.getString("patient_name"),
                    LocalDate.parse(rs.getString("date_requested")),
                    LocalDate.parse(rs.getString("preferred_date")),
                    rs.getString("urgency"),
                    rs.getString("symptoms"),
                    rs.getString("status"),
                    rs.getObject("assigned_doctor_id") != null ? rs.getInt("assigned_doctor_id") : 0,
                    rs.getString("doctor_name"),
                    rs.getObject("hospital_id") != null ? rs.getInt("hospital_id") : 0,
                    rs.getString("hospital_name")
                );
                requests.add(request);
            }
        }
        return requests;
    }
    
    public TreatmentRequest getTreatmentRequestById(int id) throws SQLException {
        String sql = """
            SELECT tr.*, 
                   p.firstName || ' ' || p.lastName AS patient_name,
                   d.firstName || ' ' || d.lastName AS doctor_name,
                   h.name AS hospital_name
            FROM treatment_requests tr
            JOIN patients p ON tr.patient_id = p.id
            LEFT JOIN doctors d ON tr.assigned_doctor_id = d.id
            LEFT JOIN hospitals h ON tr.hospital_id = h.id
            WHERE tr.id = ?
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new TreatmentRequest(
                    rs.getInt("id"),
                    rs.getInt("patient_id"),
                    rs.getString("patient_name"),
                    LocalDate.parse(rs.getString("date_requested")),
                    LocalDate.parse(rs.getString("preferred_date")),
                    rs.getString("urgency"),
                    rs.getString("symptoms"),
                    rs.getString("status"),
                    rs.getObject("assigned_doctor_id") != null ? rs.getInt("assigned_doctor_id") : 0,
                    rs.getString("doctor_name"),
                    rs.getObject("hospital_id") != null ? rs.getInt("hospital_id") : 0,
                    rs.getString("hospital_name")
                );
            }
        }
        return null;
    }
    
    public void updateTreatmentRequest(TreatmentRequest request) throws SQLException {
        String sql = "UPDATE treatment_requests SET status = ?, assigned_doctor_id = ?, hospital_id = ? WHERE id = ?";
        
        System.out.println("Updating treatment request: " + request.getId() + " with hospital ID: " + request.getHospitalId());
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, request.getStatus());
            
            if (request.getAssignedDoctorId() > 0) {
                pstmt.setInt(2, request.getAssignedDoctorId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            if (request.getHospitalId() > 0) {
                pstmt.setInt(3, request.getHospitalId());
            } else {
                throw new SQLException("Hospital ID is required for treatment requests");
            }
            
            pstmt.setInt(4, request.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    // Statistics methods
    public int getTotalPatientsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getTotalPatientsCountByHospital(int hospitalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients WHERE hospital_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getTotalDoctorsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctors";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getTotalDoctorsCountByHospital(int hospitalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctors WHERE hospital_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getPendingTreatmentsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM treatment_requests WHERE status = 'Pending'";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getPendingTreatmentsCountByHospital(int hospitalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM treatment_requests WHERE status = 'Pending' AND hospital_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public Map<String, Integer> getPatientGenderDistribution() throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT gender, COUNT(*) as count FROM patients GROUP BY gender";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                distribution.put(rs.getString("gender"), rs.getInt("count"));
            }
        }
        return distribution;
    }
    
    public Map<String, Integer> getPatientGenderDistributionByHospital(int hospitalId) throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT gender, COUNT(*) as count FROM patients WHERE hospital_id = ? GROUP BY gender";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                distribution.put(rs.getString("gender"), rs.getInt("count"));
            }
        }
        return distribution;
    }
    
    public Map<String, Integer> getTreatmentUrgencyDistribution() throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT urgency, COUNT(*) as count FROM treatment_requests GROUP BY urgency";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                distribution.put(rs.getString("urgency"), rs.getInt("count"));
            }
        }
        
        // Ensure all urgency levels are represented
        if (!distribution.containsKey("Low")) distribution.put("Low", 0);
        if (!distribution.containsKey("Medium")) distribution.put("Medium", 0);
        if (!distribution.containsKey("High")) distribution.put("High", 0);
        if (!distribution.containsKey("Emergency")) distribution.put("Emergency", 0);
        
        return distribution;
    }
    
    public Map<String, Integer> getTreatmentUrgencyDistributionByHospital(int hospitalId) throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        String sql = "SELECT urgency, COUNT(*) as count FROM treatment_requests WHERE hospital_id = ? GROUP BY urgency";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                distribution.put(rs.getString("urgency"), rs.getInt("count"));
            }
        }
        
        // Ensure all urgency levels are represented
        if (!distribution.containsKey("Low")) distribution.put("Low", 0);
        if (!distribution.containsKey("Medium")) distribution.put("Medium", 0);
        if (!distribution.containsKey("High")) distribution.put("High", 0);
        if (!distribution.containsKey("Emergency")) distribution.put("Emergency", 0);
        
        return distribution;
    }

    // Hospital operations
    public void addHospital(Hospital hospital) throws SQLException {
        String sql = "INSERT INTO hospitals (name, address, contactNumber, email, website, username, password, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hospital.getName());
            pstmt.setString(2, hospital.getAddress());
            pstmt.setString(3, hospital.getContactNumber());
            pstmt.setString(4, hospital.getEmail());
            pstmt.setString(5, hospital.getWebsite());
            pstmt.setString(6, hospital.getUsername());
            pstmt.setString(7, hospital.getPassword());
            pstmt.setString(8, hospital.getDescription());
            pstmt.executeUpdate();
        }
    }

    public Hospital authenticateHospital(String username, String password) throws SQLException {
        String sql = "SELECT * FROM hospitals WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Hospital(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("contactNumber"),
                    rs.getString("email"),
                    rs.getString("website") != null ? rs.getString("website") : "",
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("description") != null ? rs.getString("description") : ""
                );
            }
        }
        return null;
    }

    public List<Hospital> getAllHospitals() throws SQLException {
        List<Hospital> hospitals = new ArrayList<>();
        String sql = "SELECT * FROM hospitals";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // Debug: Print column names
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            System.out.println("Hospitals table has " + columnCount + " columns:");
            for (int i = 1; i <= columnCount; i++) {
                System.out.println(i + ": " + metaData.getColumnName(i));
            }
            
            while (rs.next()) {
                try {
                    Hospital hospital = new Hospital(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("contactNumber"),
                        rs.getString("email"),
                        rs.getString("website") != null ? rs.getString("website") : "",
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("description") != null ? rs.getString("description") : ""
                    );
                    hospitals.add(hospital);
                    System.out.println("Loaded hospital: " + hospital.getId() + " - " + hospital.getName());
                } catch (SQLException e) {
                    System.err.println("Error loading hospital: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return hospitals;
    }

    public Hospital getHospitalById(int id) throws SQLException {
        String sql = "SELECT * FROM hospitals WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Hospital(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("contactNumber"),
                    rs.getString("email"),
                    rs.getString("website") != null ? rs.getString("website") : "",
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("description") != null ? rs.getString("description") : ""
                );
            }
        }
        return null;
    }

    public void updateHospital(Hospital hospital) throws SQLException {
        String sql = "UPDATE hospitals SET name = ?, address = ?, contactNumber = ?, email = ?, website = ?, description = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hospital.getName());
            pstmt.setString(2, hospital.getAddress());
            pstmt.setString(3, hospital.getContactNumber());
            pstmt.setString(4, hospital.getEmail());
            pstmt.setString(5, hospital.getWebsite());
            pstmt.setString(6, hospital.getDescription());
            pstmt.setInt(7, hospital.getId());
            pstmt.executeUpdate();
        }
    }

    public void updateHospitalPassword(int hospitalId, String newPassword) throws SQLException {
        String sql = "UPDATE hospitals SET password = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, hospitalId);
            pstmt.executeUpdate();
        }
    }

    public boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM (SELECT username FROM patients UNION SELECT username FROM doctors UNION SELECT username FROM admins UNION SELECT username FROM hospitals) WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public int getTotalHospitalsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM hospitals";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public Map<String, Integer> getHospitalRegionDistribution() throws SQLException {
        Map<String, Integer> regionDistribution = new HashMap<>();
        
        // This is a simplified version assuming region is extracted from address
        // In a real system, you might have a separate region field
        String sql = """
            SELECT 
                CASE 
                    WHEN address LIKE '%urban%' THEN 'Urban'
                    WHEN address LIKE '%suburban%' THEN 'Suburban'
                    WHEN address LIKE '%rural%' THEN 'Rural'
                    ELSE 'Other'
                END AS region,
                COUNT(*) as count
            FROM hospitals
            GROUP BY region
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String region = rs.getString("region");
                int count = rs.getInt("count");
                regionDistribution.put(region, count);
            }
        }
        
        // Ensure all categories exist even if they have 0 count
        if (!regionDistribution.containsKey("Urban")) regionDistribution.put("Urban", 0);
        if (!regionDistribution.containsKey("Suburban")) regionDistribution.put("Suburban", 0);
        if (!regionDistribution.containsKey("Rural")) regionDistribution.put("Rural", 0);
        if (!regionDistribution.containsKey("Other")) regionDistribution.put("Other", 0);
        
        return regionDistribution;
    }

    public List<Hospital> searchHospitals(String searchTerm) throws SQLException {
        List<Hospital> hospitals = new ArrayList<>();
        String sql = """
            SELECT * FROM hospitals 
            WHERE name LIKE ? 
            OR address LIKE ? 
            OR contactNumber LIKE ?
            OR email LIKE ?
            OR username LIKE ?
        """;
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Hospital hospital = new Hospital(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("contactNumber"),
                    rs.getString("email"),
                    rs.getString("website") != null ? rs.getString("website") : "",
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("description") != null ? rs.getString("description") : ""
                );
                hospitals.add(hospital);
            }
        }
        
        return hospitals;
    }

    public void deleteHospital(int hospitalId) throws SQLException {
        String sql = "DELETE FROM hospitals WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hospitalId);
            pstmt.executeUpdate();
        }
    }

    public void deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, patientId);
            pstmt.executeUpdate();
        }
    }

    public List<TreatmentRequest> getTreatmentRequestsByHospital(int hospitalId) throws SQLException {
        List<TreatmentRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.firstName || ' ' || p.lastName AS patient_name,
                   d.firstName || ' ' || d.lastName AS doctor_name,
                   h.name AS hospital_name
            FROM treatment_requests tr
            JOIN patients p ON tr.patient_id = p.id
            LEFT JOIN doctors d ON tr.assigned_doctor_id = d.id
            LEFT JOIN hospitals h ON tr.hospital_id = h.id
            WHERE tr.hospital_id = ?
            ORDER BY 
                CASE 
                    WHEN tr.urgency = 'Emergency' THEN 1
                    WHEN tr.urgency = 'High' THEN 2
                    WHEN tr.urgency = 'Medium' THEN 3
                    WHEN tr.urgency = 'Low' THEN 4
                    ELSE 5
                END,
                tr.date_requested DESC
        """;
        
        System.out.println("Fetching treatment requests for hospital ID: " + hospitalId);
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                try {
                    TreatmentRequest request = new TreatmentRequest(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        LocalDate.parse(rs.getString("date_requested")),
                        LocalDate.parse(rs.getString("preferred_date")),
                        rs.getString("urgency"),
                        rs.getString("symptoms"),
                        rs.getString("status"),
                        rs.getObject("assigned_doctor_id") != null ? rs.getInt("assigned_doctor_id") : 0,
                        rs.getString("doctor_name"),
                        hospitalId,
                        rs.getString("hospital_name")
                    );
                    requests.add(request);
                    System.out.println("Found request: " + request.getId() + " for hospital: " + hospitalId);
                } catch (Exception e) {
                    System.err.println("Error parsing treatment request: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Total requests found for hospital " + hospitalId + ": " + requests.size());
        return requests;
    }

    public List<Doctor> getDoctorsByHospital(int hospitalId) throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE hospital_id = ?";
        
        System.out.println("Fetching doctors for hospital ID: " + hospitalId);
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setId(rs.getInt("id"));
                doctor.setFirstName(rs.getString("firstName"));
                doctor.setLastName(rs.getString("lastName"));
                doctor.setEmail(rs.getString("email"));
                doctor.setPassword(rs.getString("password"));
                doctor.setContactNumber(rs.getString("contactNumber"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setUsername(rs.getString("username"));
                doctor.setHospitalId(rs.getInt("hospital_id"));
                doctors.add(doctor);
                System.out.println("Found doctor: " + doctor.getId() + " for hospital: " + hospitalId);
            }
        }
        
        System.out.println("Total doctors found for hospital " + hospitalId + ": " + doctors.size());
        return doctors;
    }
    
    public List<Patient> getPatientsByHospital(int hospitalId) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE hospital_id = ?";
        
        System.out.println("Fetching patients for hospital ID: " + hospitalId);
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Patient patient = new Patient(
                    rs.getInt("id"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    LocalDate.parse(rs.getString("dateOfBirth")),
                    rs.getString("gender"),
                    rs.getString("contactNumber"),
                    rs.getString("address"),
                    rs.getString("medicalHistory"),
                    rs.getString("username"),
                    rs.getString("password"),
                    hospitalId
                );
                patients.add(patient);
                System.out.println("Found patient: " + patient.getId() + " for hospital: " + hospitalId);
            }
        }
        
        System.out.println("Total patients found for hospital " + hospitalId + ": " + patients.size());
        return patients;
    }

    public List<TreatmentRequest> getTreatmentRequestsByDoctorAndHospital(int doctorId, int hospitalId) throws SQLException {
        List<TreatmentRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.firstName || ' ' || p.lastName AS patient_name,
                   d.firstName || ' ' || d.lastName AS doctor_name,
                   h.name AS hospital_name
            FROM treatment_requests tr
            JOIN patients p ON tr.patient_id = p.id
            LEFT JOIN doctors d ON tr.assigned_doctor_id = d.id
            LEFT JOIN hospitals h ON tr.hospital_id = h.id
            WHERE tr.assigned_doctor_id = ? AND tr.hospital_id = ?
            ORDER BY 
                CASE 
                    WHEN tr.urgency = 'Emergency' THEN 1
                    WHEN tr.urgency = 'High' THEN 2
                    WHEN tr.urgency = 'Medium' THEN 3
                    WHEN tr.urgency = 'Low' THEN 4
                    ELSE 5
                END,
                tr.date_requested DESC
        """;
        
        System.out.println("Fetching treatment requests for doctor ID: " + doctorId + " and hospital ID: " + hospitalId);
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            pstmt.setInt(2, hospitalId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                try {
                    TreatmentRequest request = new TreatmentRequest(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getString("patient_name"),
                        LocalDate.parse(rs.getString("date_requested")),
                        LocalDate.parse(rs.getString("preferred_date")),
                        rs.getString("urgency"),
                        rs.getString("symptoms"),
                        rs.getString("status"),
                        doctorId,
                        rs.getString("doctor_name"),
                        hospitalId,
                        rs.getString("hospital_name")
                    );
                    requests.add(request);
                    System.out.println("Found request: " + request.getId() + " for doctor: " + doctorId + " and hospital: " + hospitalId);
                } catch (Exception e) {
                    System.err.println("Error parsing treatment request: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("Total requests found for doctor " + doctorId + " and hospital " + hospitalId + ": " + requests.size());
        return requests;
    }
} 