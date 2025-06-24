package com.example.lms.model;

import com.example.lms.util.Database;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for LibraryStaff entities
 */
public class LibraryStaffDAO {
    
    private Connection connection;
    private UserDAO userDAO;
    
    /**
     * Constructor that initializes the database connection and user DAO
     */
    public LibraryStaffDAO() {
        try {
            connection = Database.getConnection();
            userDAO = new UserDAO();
        } catch (SQLException e) {
            System.err.println("Error initializing LibraryStaffDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all staff members from the database
     * 
     * @return List of all staff members
     * @throws SQLException if database error occurs
     */
    public List<LibraryStaff> getAllStaff() throws SQLException {
        List<LibraryStaff> staffList = new ArrayList<>();
        String query = "SELECT * FROM library_staff ORDER BY department, position";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                LibraryStaff staff = extractStaffFromResultSet(rs);
                staffList.add(staff);
            }
        }
        
        return staffList;
    }
    
    /**
     * Get staff members by department
     * 
     * @param department Department name
     * @return List of staff in the department
     * @throws SQLException if database error occurs
     */
    public List<LibraryStaff> getStaffByDepartment(String department) throws SQLException {
        List<LibraryStaff> staffList = new ArrayList<>();
        String query = "SELECT * FROM library_staff WHERE department = ? ORDER BY position";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, department);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LibraryStaff staff = extractStaffFromResultSet(rs);
                    staffList.add(staff);
                }
            }
        }
        
        return staffList;
    }
    
    /**
     * Get staff by ID
     * 
     * @param id Staff ID
     * @return LibraryStaff object or null if not found
     * @throws SQLException if database error occurs
     */
    public LibraryStaff getStaffById(int id) throws SQLException {
        String query = "SELECT * FROM library_staff WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractStaffFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get staff by user ID
     * 
     * @param userId User ID
     * @return LibraryStaff object or null if not found
     * @throws SQLException if database error occurs
     */
    public LibraryStaff getStaffByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM library_staff WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractStaffFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Add a new staff member
     * 
     * @param staff LibraryStaff to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addStaff(LibraryStaff staff) throws SQLException {
        String query = "INSERT INTO library_staff (user_id, department, position, hire_date, salary, " +
                      "office_number, extension, emergency_contact) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, staff.getUser().getId());
            stmt.setString(2, staff.getDepartment());
            stmt.setString(3, staff.getPosition());
            
            if (staff.getHireDate() != null) {
                stmt.setDate(4, Date.valueOf(staff.getHireDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            
            stmt.setBigDecimal(5, staff.getSalary());
            stmt.setString(6, staff.getOfficeNumber());
            stmt.setString(7, staff.getExtension());
            stmt.setString(8, staff.getEmergencyContact());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        staff.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Update an existing staff member
     * 
     * @param staff LibraryStaff to update
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateStaff(LibraryStaff staff) throws SQLException {
        String query = "UPDATE library_staff SET department = ?, position = ?, hire_date = ?, " +
                      "salary = ?, office_number = ?, extension = ?, emergency_contact = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, staff.getDepartment());
            stmt.setString(2, staff.getPosition());
            
            if (staff.getHireDate() != null) {
                stmt.setDate(3, Date.valueOf(staff.getHireDate()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            
            stmt.setBigDecimal(4, staff.getSalary());
            stmt.setString(5, staff.getOfficeNumber());
            stmt.setString(6, staff.getExtension());
            stmt.setString(7, staff.getEmergencyContact());
            stmt.setInt(8, staff.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Delete a staff member
     * 
     * @param id Staff ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteStaff(int id) throws SQLException {
        String query = "DELETE FROM library_staff WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Search staff by name
     * 
     * @param searchTerm Search term for staff name
     * @return List of matching staff members
     * @throws SQLException if database error occurs
     */
    public List<LibraryStaff> searchStaffByName(String searchTerm) throws SQLException {
        List<LibraryStaff> staffList = new ArrayList<>();
        String query = "SELECT ls.* FROM library_staff ls " +
                      "JOIN users u ON ls.user_id = u.id " +
                      "WHERE u.name LIKE ? " +
                      "ORDER BY u.name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LibraryStaff staff = extractStaffFromResultSet(rs);
                    staffList.add(staff);
                }
            }
        }
        
        return staffList;
    }
    
    /**
     * Search staff by name and department
     * 
     * @param name Staff name search term
     * @param department Department search term (can be empty for all departments)
     * @return List of matching staff members
     * @throws SQLException if database error occurs
     */
    public List<LibraryStaff> searchStaff(String name, String department) throws SQLException {
        List<LibraryStaff> staffList = new ArrayList<>();
        
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT ls.* FROM library_staff ls " +
            "JOIN users u ON ls.user_id = u.id " +
            "WHERE 1=1"
        );
        
        // Add search conditions based on provided parameters
        List<Object> params = new ArrayList<>();
        
        if (name != null && !name.trim().isEmpty()) {
            queryBuilder.append(" AND u.name LIKE ?");
            params.add("%" + name + "%");
        }
        
        if (department != null && !department.trim().isEmpty()) {
            queryBuilder.append(" AND ls.department LIKE ?");
            params.add("%" + department + "%");
        }
        
        queryBuilder.append(" ORDER BY u.name");
        String query = queryBuilder.toString();
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LibraryStaff staff = extractStaffFromResultSet(rs);
                    staffList.add(staff);
                }
            }
        }
        
        return staffList;
    }
    
    /**
     * Get a list of all departments
     * 
     * @return List of department names
     * @throws SQLException if database error occurs
     */
    public List<String> getAllDepartments() throws SQLException {
        List<String> departments = new ArrayList<>();
        String query = "SELECT DISTINCT department FROM library_staff ORDER BY department";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                departments.add(rs.getString("department"));
            }
        }
        
        return departments;
    }
    
    /**
     * Get staff statistics
     * 
     * @return Map with staff statistics
     * @throws SQLException if database error occurs
     */
    public Map<String, Object> getStaffStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Total staff count
        String totalQuery = "SELECT COUNT(*) AS total FROM library_staff";
        try (PreparedStatement stmt = connection.prepareStatement(totalQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("totalCount", rs.getInt("total"));
            }
        }
        
        // Staff count by department
        Map<String, Integer> departmentCounts = new HashMap<>();
        String deptQuery = "SELECT department, COUNT(*) AS count FROM library_staff GROUP BY department ORDER BY count DESC";
        try (PreparedStatement stmt = connection.prepareStatement(deptQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String department = rs.getString("department");
                int count = rs.getInt("count");
                departmentCounts.put(department, count);
            }
        }
        stats.put("departmentCounts", departmentCounts);
        
        // Average salary
        String salaryQuery = "SELECT AVG(salary) AS avg_salary FROM library_staff";
        try (PreparedStatement stmt = connection.prepareStatement(salaryQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("averageSalary", rs.getBigDecimal("avg_salary"));
            }
        }
        
        return stats;
    }
    
    /**
     * Helper method to extract a LibraryStaff object from a ResultSet
     * 
     * @param rs ResultSet containing staff data
     * @return LibraryStaff object
     * @throws SQLException if database error occurs
     */
    private LibraryStaff extractStaffFromResultSet(ResultSet rs) throws SQLException {
        LibraryStaff staff = new LibraryStaff();
        staff.setId(rs.getInt("id"));
        staff.setDepartment(rs.getString("department"));
        staff.setPosition(rs.getString("position"));
        
        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            staff.setHireDate(hireDate.toLocalDate());
        }
        
        // Get salary as BigDecimal
        staff.setSalary(rs.getBigDecimal("salary"));
        staff.setOfficeNumber(rs.getString("office_number"));
        staff.setExtension(rs.getString("extension"));
        staff.setEmergencyContact(rs.getString("emergency_contact"));
        
        // Load related user
        int userId = rs.getInt("user_id");
        User user = userDAO.getUserById(userId);
        staff.setUser(user);
        
        return staff;
    }
    
    /**
     * Close the database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            // UserDAO handles its own connection, no need to close it here
            
        } catch (SQLException e) {
            System.err.println("Error closing LibraryStaffDAO: " + e.getMessage());
        }
    }
}
