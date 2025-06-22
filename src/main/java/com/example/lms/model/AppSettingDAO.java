package com.example.lms.model;

import com.example.lms.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for AppSetting entity.
 * Handles all database operations related to application settings.
 */
public class AppSettingDAO {
    
    /**
     * Get a setting value by key.
     * 
     * @param key Setting key
     * @return Setting value or null if not found
     */
    public String getSettingValue(String key) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String value = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT setting_value FROM app_setting WHERE setting_key = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, key);
            
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                value = rs.getString("setting_value");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting setting value: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return value;
    }
    
    /**
     * Update a setting value.
     * 
     * @param key Setting key
     * @param value New setting value
     * @return true if successful, false otherwise
     */
    public boolean updateSetting(String key, String value) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "UPDATE app_setting SET setting_value = ? WHERE setting_key = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, value);
            stmt.setString(2, key);
            
            int rowsAffected = stmt.executeUpdate();
            
            // If the setting doesn't exist, create it
            if (rowsAffected == 0) {
                String insertQuery = "INSERT INTO app_setting (setting_key, setting_value) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, key);
                insertStmt.setString(2, value);
                
                rowsAffected = insertStmt.executeUpdate();
                insertStmt.close();
            }
            
            success = (rowsAffected > 0);
            
        } catch (SQLException e) {
            System.err.println("Error updating setting: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, conn);
        }
        
        return success;
    }
    
    /**
     * Get all settings as a map.
     * 
     * @return Map of setting key-value pairs
     */
    public Map<String, String> getAllSettings() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, String> settings = new HashMap<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT setting_key, setting_value FROM app_setting";
            stmt = conn.prepareStatement(query);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all settings: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return settings;
    }
    
    /**
     * Get all AppSetting objects.
     * 
     * @return List of all AppSetting objects
     */
    public List<AppSetting> getAllAppSettings() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<AppSetting> settings = new ArrayList<>();
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT * FROM app_setting ORDER BY id";
            stmt = conn.prepareStatement(query);
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                settings.add(mapResultSetToAppSetting(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting all app settings: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        return settings;
    }
    
    /**
     * Map a ResultSet row to an AppSetting object.
     * 
     * @param rs ResultSet containing app setting data
     * @return AppSetting object
     */
    private AppSetting mapResultSetToAppSetting(ResultSet rs) throws SQLException {
        AppSetting setting = new AppSetting();
        setting.setId(rs.getInt("id"));
        setting.setSettingKey(rs.getString("setting_key"));
        setting.setSettingValue(rs.getString("setting_value"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            setting.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            setting.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return setting;
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
