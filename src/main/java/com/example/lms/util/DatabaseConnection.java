package com.example.lms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class to manage database connections.
 * Provides methods to get and close database connections.
 */
public class DatabaseConnection {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/lms";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Empty password as specified

    /**
     * Get a connection to the database.
     * @return Connection object for database operations.
     * @throws SQLException If connection fails.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Return a connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found.", e);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to database.", e);
        }
    }

    /**
     * Close a database connection safely.
     * @param connection The connection to close.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}
