package com.example.lms.model;

import com.example.lms.util.DatabaseConnection;
import com.example.lms.util.SecurityUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entity.
 * Handles all database operations related to users.
 */
public class UserDAO {

    /**
     * Authenticate a user using email and password.
     * 
     * @param email User email
     * @param password Plain text password
     * @return User object if authenticated, null otherwise
     */
    public User authenticate(String email, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM users WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                
                // Verify the password
                if (SecurityUtil.verifyPassword(password, hashedPassword)) {
                    user = mapResultSetToUser(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return user;
    }
    
    /**
     * Register a new user.
     * 
     * @param user User object with name, email, password, and role
     * @return true if registration successful, false otherwise
     */
    public boolean registerUser(User user) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // Check if email already exists
            if (emailExists(conn, user.getEmail())) {
                return false;
            }
            
            String query = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, SecurityUtil.hashPassword(user.getPassword()));
            stmt.setString(4, user.getRole().toString());
            
            int rowsAffected = stmt.executeUpdate();
            success = (rowsAffected > 0);
            
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, conn);
        }
        
        return success;
    }
    
    /**
     * Public method to check if an email already exists in the database.
     * 
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        Connection conn = null;
        boolean exists = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            exists = emailExists(conn, email);
        } catch (SQLException e) {
            System.err.println("Error checking if email exists: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, null, conn);
        }
        
        return exists;
    }
    
    /**
     * Check if an email already exists in the database.
     * 
     * @param conn Database connection
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    private boolean emailExists(Connection conn, String email) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;
        
        try {
            String query = "SELECT COUNT(*) FROM users WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                exists = (rs.getInt(1) > 0);
            }
            
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
        
        return exists;
    }
    
    /**
     * Generate and save a password reset token for a user.
     * 
     * @param email User's email
     * @return The token if successful, null otherwise
     */
    public String generatePasswordResetToken(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        String token = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // Check if email exists
            if (!emailExists(conn, email)) {
                return null;
            }
            
            // Generate token
            token = SecurityUtil.generateResetToken();
            
            // Set token expiry to 1 hour from now
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);
            
            // Update user with token and expiry
            String query = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, token);
            stmt.setTimestamp(2, Timestamp.valueOf(expiry));
            stmt.setString(3, email);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected <= 0) {
                token = null;
            }
            
        } catch (SQLException e) {
            System.err.println("Error generating reset token: " + e.getMessage());
            e.printStackTrace();
            token = null;
        } finally {
            closeResources(null, stmt, conn);
        }
        
        return token;
    }
    
    /**
     * Reset password using email and token.
     *
     * @param email       User's email
     * @param token       Reset token
     * @param newPassword New password
     * @return true if reset successful, false otherwise
     */
    public boolean resetPassword(String email, String token, String newPassword) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            System.out.println("Attempting to reset password for email: " + email);
            System.out.println("Token: " + token);
            
            // Find user with the given email, token and check expiry
            String query = "SELECT * FROM users WHERE email = ? AND reset_token = ? AND reset_token_expiry > ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, token);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            System.out.println("Executing query: " + query);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Found user with ID: " + rs.getInt("id"));
                // Update password
                String updateQuery = "UPDATE users SET password = ?, reset_token = NULL, reset_token_expiry = NULL WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, SecurityUtil.hashPassword(newPassword));
                updateStmt.setInt(2, rs.getInt("id"));
                
                int rowsAffected = updateStmt.executeUpdate();
                success = (rowsAffected > 0);
                System.out.println("Password update success: " + success);
                
                updateStmt.close();
            } else {
                System.out.println("No matching user found with the provided email and token");
            }
            
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return success;
    }
    
    /**
     * Get user by email.
     * 
     * @param email User's email
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM users WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = mapResultSetToUser(rs);
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return user;
    }
    
    /**
     * Get all users.
     * 
     * @return List of all users
     */
    public List<User> getAllUsers() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<User> users = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM users ORDER BY id";
            stmt = conn.prepareStatement(query);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return users;
    }
    
    /**
     * Map a ResultSet row to a User object.
     * 
     * @param rs ResultSet containing user data
     * @return User object
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        user.setResetToken(rs.getString("reset_token"));
        
        Timestamp resetTokenExpiry = rs.getTimestamp("reset_token_expiry");
        if (resetTokenExpiry != null) {
            user.setResetTokenExpiry(resetTokenExpiry.toLocalDateTime());
        }
        
        return user;
    }
    
    /**
     * Close database resources.
     */
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}
