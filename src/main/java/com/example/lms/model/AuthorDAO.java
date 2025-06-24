package com.example.lms.model;

import com.example.lms.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Author entities
 */
public class AuthorDAO {
    
    private Connection connection;
    
    /**
     * Constructor that initializes the database connection
     */
    public AuthorDAO() {
        try {
            connection = Database.getConnection();
        } catch (SQLException e) {
            System.err.println("Error initializing AuthorDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all authors from the database
     * 
     * @return List of all authors
     * @throws SQLException if database error occurs
     */
    public List<Author> getAllAuthors() throws SQLException {
        List<Author> authors = new ArrayList<>();
        String query = "SELECT * FROM authors ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Author author = extractAuthorFromResultSet(rs);
                authors.add(author);
            }
        }
        
        return authors;
    }
    
    /**
     * Get an author by ID
     * 
     * @param id Author ID
     * @return Author object or null if not found
     * @throws SQLException if database error occurs
     */
    public Author getAuthorById(int id) throws SQLException {
        String query = "SELECT * FROM authors WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractAuthorFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get authors for a specific book
     * 
     * @param bookId Book ID
     * @return List of authors for the book
     * @throws SQLException if database error occurs
     */
    public List<Author> getAuthorsByBookId(int bookId) throws SQLException {
        List<Author> authors = new ArrayList<>();
        String query = "SELECT a.* FROM authors a " +
                       "JOIN book_authors ba ON a.id = ba.author_id " +
                       "WHERE ba.book_id = ? " +
                       "ORDER BY a.name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Author author = extractAuthorFromResultSet(rs);
                    authors.add(author);
                }
            }
        }
        
        return authors;
    }
    
    /**
     * Add a new author
     * 
     * @param author Author to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addAuthor(Author author) throws SQLException {
        String query = "INSERT INTO authors (name, biography, birth_date) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, author.getName());
            stmt.setString(2, author.getBiography());
            
            if (author.getBirthDate() != null) {
                stmt.setDate(3, Date.valueOf(author.getBirthDate()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        author.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Update an existing author
     * 
     * @param author Author to update
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateAuthor(Author author) throws SQLException {
        String query = "UPDATE authors SET name = ?, biography = ?, birth_date = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, author.getName());
            stmt.setString(2, author.getBiography());
            
            if (author.getBirthDate() != null) {
                stmt.setDate(3, Date.valueOf(author.getBirthDate()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            
            stmt.setInt(4, author.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Delete an author by ID
     * 
     * @param id Author ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteAuthor(int id) throws SQLException {
        // First check if any books are associated with this author
        String checkQuery = "SELECT COUNT(*) FROM book_authors WHERE author_id = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, id);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Author has associated books, cannot delete
                    return false;
                }
            }
        }
        
        // No associated books, proceed with deletion
        String deleteQuery = "DELETE FROM authors WHERE id = ?";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, id);
            
            int affectedRows = deleteStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Search authors by name
     * 
     * @param searchTerm Search term to look for in author names
     * @return List of matching authors
     * @throws SQLException if database error occurs
     */
    public List<Author> searchAuthors(String searchTerm) throws SQLException {
        List<Author> authors = new ArrayList<>();
        String query = "SELECT * FROM authors WHERE name LIKE ? ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + searchTerm + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Author author = extractAuthorFromResultSet(rs);
                    authors.add(author);
                }
            }
        }
        
        return authors;
    }
    
    /**
     * Add an author-book association
     * 
     * @param authorId Author ID
     * @param bookId Book ID
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addAuthorToBook(int authorId, int bookId) throws SQLException {
        // Check if the association already exists
        String checkQuery = "SELECT COUNT(*) FROM book_authors WHERE author_id = ? AND book_id = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, authorId);
            checkStmt.setInt(2, bookId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Association already exists
                    return true;
                }
            }
        }
        
        // Add the association
        String insertQuery = "INSERT INTO book_authors (author_id, book_id) VALUES (?, ?)";
        
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setInt(1, authorId);
            insertStmt.setInt(2, bookId);
            
            int affectedRows = insertStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Remove an author-book association
     * 
     * @param authorId Author ID
     * @param bookId Book ID
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean removeAuthorFromBook(int authorId, int bookId) throws SQLException {
        String deleteQuery = "DELETE FROM book_authors WHERE author_id = ? AND book_id = ?";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, authorId);
            deleteStmt.setInt(2, bookId);
            
            int affectedRows = deleteStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Helper method to extract an Author object from a ResultSet
     * 
     * @param rs ResultSet containing author data
     * @return Author object
     * @throws SQLException if database error occurs
     */
    private Author extractAuthorFromResultSet(ResultSet rs) throws SQLException {
        Author author = new Author();
        author.setId(rs.getInt("id"));
        author.setName(rs.getString("name"));
        author.setBiography(rs.getString("biography"));
        
        Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            author.setBirthDate(birthDate.toLocalDate());
        }
        
        return author;
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
            System.err.println("Error closing AuthorDAO: " + e.getMessage());
        }
    }
}
