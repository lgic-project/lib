package com.example.lms.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class to manage application settings stored in the database
 */
public class AppSettings {
    
    /**
     * Get the default borrowing period in days from application settings
     * 
     * @return int Number of days for default borrowing period, defaults to 14 if not found
     */
    public static int getDefaultBorrowingPeriod() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = Database.getConnection();
            String query = "SELECT value FROM app_settings WHERE setting_key = 'default_borrow_days'";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Integer.parseInt(rs.getString("value"));
            }
        } catch (SQLException | NumberFormatException e) {
            System.err.println("Error retrieving default borrowing period: " + e.getMessage());
        } finally {
            // No need to close the connection here, just release it back to the pool
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                Database.releaseConnection();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        // Default to 14 days if not found in database
        return 14;
    }
}
