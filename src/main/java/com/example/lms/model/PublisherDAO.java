package com.example.lms.model;

import com.example.lms.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Publisher entities
 */
public class PublisherDAO {
    
    private Connection connection;
    
    /**
     * Constructor that initializes the database connection
     */
    public PublisherDAO() {
        try {
            connection = Database.getConnection();
        } catch (SQLException e) {
            System.err.println("Error initializing PublisherDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all publishers from the database
     * 
     * @return List of all publishers
     * @throws SQLException if database error occurs
     */
    public List<Publisher> getAllPublishers() throws SQLException {
        List<Publisher> publishers = new ArrayList<>();
        String query = "SELECT * FROM publishers ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Publisher publisher = extractPublisherFromResultSet(rs);
                publishers.add(publisher);
            }
        }
        
        return publishers;
    }
    
    /**
     * Get a publisher by ID
     * 
     * @param id Publisher ID
     * @return Publisher object or null if not found
     * @throws SQLException if database error occurs
     */
    public Publisher getPublisherById(int id) throws SQLException {
        String query = "SELECT * FROM publishers WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPublisherFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get a publisher by name
     * 
     * @param name Publisher name
     * @return Publisher object or null if not found
     * @throws SQLException if database error occurs
     */
    public Publisher getPublisherByName(String name) throws SQLException {
        String query = "SELECT * FROM publishers WHERE name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractPublisherFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Add a new publisher
     * 
     * @param publisher Publisher to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addPublisher(Publisher publisher) throws SQLException {
        String query = "INSERT INTO publishers (name, address, contact_email, contact_phone, website) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, publisher.getName());
            stmt.setString(2, publisher.getAddress());
            stmt.setString(3, publisher.getContactEmail());
            stmt.setString(4, publisher.getContactPhone());
            stmt.setString(5, publisher.getWebsite());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        publisher.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Update an existing publisher
     * 
     * @param publisher Publisher to update
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updatePublisher(Publisher publisher) throws SQLException {
        String query = "UPDATE publishers SET name = ?, address = ?, contact_email = ?, contact_phone = ?, website = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, publisher.getName());
            stmt.setString(2, publisher.getAddress());
            stmt.setString(3, publisher.getContactEmail());
            stmt.setString(4, publisher.getContactPhone());
            stmt.setString(5, publisher.getWebsite());
            stmt.setInt(6, publisher.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Delete a publisher by ID
     * 
     * @param id Publisher ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deletePublisher(int id) throws SQLException {
        // First check if any books are associated with this publisher
        String checkQuery = "SELECT COUNT(*) FROM books WHERE publisher_id = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, id);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Publisher has associated books, cannot delete
                    return false;
                }
            }
        }
        
        // No associated books, proceed with deletion
        String deleteQuery = "DELETE FROM publishers WHERE id = ?";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, id);
            
            int affectedRows = deleteStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Search publishers by name
     * 
     * @param searchTerm Search term to look for in publisher names
     * @return List of matching publishers
     * @throws SQLException if database error occurs
     */
    public List<Publisher> searchPublishers(String searchTerm) throws SQLException {
        List<Publisher> publishers = new ArrayList<>();
        String query = "SELECT * FROM publishers WHERE name LIKE ? ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Publisher publisher = extractPublisherFromResultSet(rs);
                    publishers.add(publisher);
                }
            }
        }
        
        return publishers;
    }
    
    /**
     * Helper method to extract a Publisher object from a ResultSet
     * 
     * @param rs ResultSet containing publisher data
     * @return Publisher object
     * @throws SQLException if database error occurs
     */
    private Publisher extractPublisherFromResultSet(ResultSet rs) throws SQLException {
        Publisher publisher = new Publisher();
        publisher.setId(rs.getInt("id"));
        publisher.setName(rs.getString("name"));
        publisher.setAddress(rs.getString("address"));
        publisher.setContactEmail(rs.getString("contact_email"));
        publisher.setContactPhone(rs.getString("contact_phone"));
        publisher.setWebsite(rs.getString("website"));
        return publisher;
    }
    
    /**
     * Close the database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing PublisherDAO: " + e.getMessage());
        }
    }
}
