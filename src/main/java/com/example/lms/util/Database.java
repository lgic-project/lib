package com.example.lms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Database utility class for managing database connections
 */
public class Database {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/lms";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // Connection parameters to enhance stability
    private static final String CONNECTION_PARAMS = "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";
    
    private static Connection connection;
    private static final AtomicInteger openConnections = new AtomicInteger(0);
    
    /**
     * Get a database connection
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                // Create a connection with connection pooling parameters
                connection = DriverManager.getConnection(
                    DB_URL + CONNECTION_PARAMS, 
                    DB_USER, 
                    DB_PASSWORD
                );
                
                // Add connection validation
                if (!connection.isValid(2)) { // 2 second timeout
                    connection = null;
                    throw new SQLException("Failed to validate connection.");
                }
                
                // Set auto-commit to true (default)
                connection.setAutoCommit(true);
                
                // Reset connection counter when creating a new connection
                openConnections.set(0);
                System.out.println("Database connection created successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }
        
        int currentCount = openConnections.incrementAndGet();
        System.out.println("Connection obtained. Active connections: " + currentCount);
        return connection;
    }
    
    /**
     * Release a connection (decrement usage counter)
     * Only close if no one is using it anymore
     * 
     * @throws SQLException if database error occurs
     */
    public static synchronized void releaseConnection() throws SQLException {
        int currentCount = openConnections.decrementAndGet();
        if (currentCount < 0) {
            // Reset to 0 if it goes negative somehow
            openConnections.set(0);
            currentCount = 0;
        }
        
        System.out.println("Connection released. Active connections: " + currentCount);
        
        if (currentCount == 0) {
            closeConnection();
        }
    }
    
    /**
     * Close the database connection
     * This should only be called when the application is shutting down
     * or when releaseConnection determines no one is using it anymore
     * 
     * @throws SQLException if database error occurs
     */
    private static synchronized void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
            System.out.println("Database connection closed.");
        }
    }
    
    /**
     * Reset the connection counter and close any existing connection
     * This is for emergency recovery only, should not be part of normal operation
     * 
     * @throws SQLException if database error occurs 
     */
    public static synchronized void resetConnection() throws SQLException {
        openConnections.set(0);
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
        System.out.println("Database connection has been reset.");
    }
}
