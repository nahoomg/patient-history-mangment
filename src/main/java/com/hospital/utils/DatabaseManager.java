package com.hospital.utils;

import com.hospital.models.Doctor;
import com.hospital.models.Patient;
import com.hospital.models.Admin;
import com.hospital.models.TreatmentRequest;
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

    private DatabaseManager() {
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
            }
        } catch (SQLException e) {
            System.out.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables(Connection conn, boolean isNewDatabase) throws SQLException {
        // Create admins table
        String createAdminsTable = """
            CREATE TABLE IF NOT EXISTS admins (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                fullName TEXT NOT NULL,
                email TEXT
            )
        """;

        // Create doctors table
        String createDoctorsTable = """
            CREATE TABLE IF NOT EXISTS doctors (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                specialization TEXT NOT NULL,
                contactNumber TEXT,
                email TEXT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            )
        """;

        // Create patients table
        String createPatientsTable = """
            CREATE TABLE IF NOT EXISTS patients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                dateOfBirth TEXT NOT NULL,
                gender TEXT NOT NULL,
                contactNumber TEXT,
                address TEXT,
                medicalHistory TEXT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            )
        """;
        
        // Create treatment_requests table
        String createTreatmentRequestsTable = """
            CREATE TABLE IF NOT EXISTS treatment_requests (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id INTEGER NOT NULL,
                date_requested TEXT NOT NULL,
                preferred_date TEXT NOT NULL,
                urgency TEXT NOT NULL,
                symptoms TEXT,
                status TEXT NOT NULL,
                assigned_doctor_id INTEGER,
                FOREIGN KEY (patient_id) REFERENCES patients(id),
                FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(id)
            )
        """;

        try (Statement stmt = conn.createStatement()) {
            // Create tables if they don't exist
            stmt.execute(createAdminsTable);
            stmt.execute(createDoctorsTable);
            stmt.execute(createPatientsTable);
            stmt.execute(createTreatmentRequestsTable);
            
            // Add default admin account if this is a new database
            if (isNewDatabase) {
                String checkAdmin = "SELECT COUNT(*) FROM admins";
                ResultSet rs = stmt.executeQuery(checkAdmin);
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertDefaultAdmin = """
                        INSERT INTO admins (username, password, fullName, email)
                        VALUES ('admin', 'admin123', 'System Administrator', 'admin@hospital.com')
                    """;
                    stmt.execute(insertDefaultAdmin);
                    System.out.println("Default admin account created.");
                }
            }
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
        String sql = "INSERT INTO doctors (firstName, lastName, specialization, contactNumber, email, username, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getLastName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getContactNumber());
            pstmt.setString(5, doctor.getEmail());
            pstmt.setString(6, doctor.getUsername());
            pstmt.setString(7, doctor.getPassword());
            pstmt.executeUpdate();
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
                    rs.getString("password")
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
                    rs.getString("password")
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
                    rs.getString("password")
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
                    rs.getString("password")
                );
            }
        }
        return null;
    }

    // Patient operations
    public void addPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (firstName, lastName, dateOfBirth, gender, contactNumber, address, medicalHistory, username, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        String sql = "INSERT INTO treatment_requests (patient_id, date_requested, preferred_date, urgency, symptoms, status, assigned_doctor_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
            
            pstmt.executeUpdate();
        }
    }
    
    public List<TreatmentRequest> getAllTreatmentRequests() throws SQLException {
        List<TreatmentRequest> requests = new ArrayList<>();
        String sql = """
            SELECT tr.*, 
                   p.firstName || ' ' || p.lastName AS patient_name,
                   d.firstName || ' ' || d.lastName AS doctor_name
            FROM treatment_requests tr
            JOIN patients p ON tr.patient_id = p.id
            LEFT JOIN doctors d ON tr.assigned_doctor_id = d.id
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
                    rs.getString("doctor_name")
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
                   d.firstName || ' ' || d.lastName AS doctor_name
            FROM treatment_requests tr
            JOIN patients p ON tr.patient_id = p.id
            LEFT JOIN doctors d ON tr.assigned_doctor_id = d.id
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
                    rs.getString("doctor_name")
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
                   d.firstName || ' ' || d.lastName AS doctor_name
            FROM treatment_requests tr
            JOIN patients p ON tr.patient_id = p.id
            LEFT JOIN doctors d ON tr.assigned_doctor_id = d.id
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
                    rs.getString("doctor_name")
                );
            }
        }
        return null;
    }
    
    public void updateTreatmentRequest(TreatmentRequest request) throws SQLException {
        String sql = "UPDATE treatment_requests SET status = ?, assigned_doctor_id = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, request.getStatus());
            
            if (request.getAssignedDoctorId() > 0) {
                pstmt.setInt(2, request.getAssignedDoctorId());
            } else {
                pstmt.setNull(2, java.sql.Types.INTEGER);
            }
            
            pstmt.setInt(3, request.getId());
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
} 