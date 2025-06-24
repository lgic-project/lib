package com.example.lms.model;

import com.example.lms.util.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for AppSetting entity.
 * Handles all database operations related to application settings.
 */
public class AppSettingDAO {
    
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
            conn = Database.getConnection();
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
    
    /**
     * Add a new setting.
     * 
     * @param setting AppSetting object to add
     * @return true if successful, false otherwise
     */
    public boolean addSetting(AppSetting setting) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;
        
        try {
            conn = Database.getConnection();
            String query = "INSERT INTO app_setting (setting_key, setting_value) VALUES (?, ?)";
            stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, setting.getSettingKey());
            stmt.setString(2, setting.getSettingValue());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        setting.setId(generatedKeys.getInt(1));
                        success = true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding setting: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, conn);
        }
        
        return success;
    }
    
    /**
     * Update an existing setting using AppSetting object.
     * 
     * @param setting AppSetting object to update
     * @return true if successful, false otherwise
     */
    public boolean updateSetting(AppSetting setting) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;
        
        try {
            conn = Database.getConnection();
            String query = "UPDATE app_setting SET setting_value = ? WHERE setting_key = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, setting.getSettingValue());
            stmt.setString(2, setting.getSettingKey());
            
            int rowsAffected = stmt.executeUpdate();
            
            // If the setting doesn't exist, create it
            if (rowsAffected == 0) {
                String insertQuery = "INSERT INTO app_setting (setting_key, setting_value) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, setting.getSettingKey());
                insertStmt.setString(2, setting.getSettingValue());
                
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
     * Delete a setting by key.
     * 
     * @param key Setting key to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteSetting(String key) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;
        
        try {
            conn = Database.getConnection();
            String query = "DELETE FROM app_setting WHERE setting_key = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, key);
            
            int rowsAffected = stmt.executeUpdate();
            success = (rowsAffected > 0);
            
        } catch (SQLException e) {
            System.err.println("Error deleting setting: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(null, stmt, conn);
        }
        
        return success;
    }
}
