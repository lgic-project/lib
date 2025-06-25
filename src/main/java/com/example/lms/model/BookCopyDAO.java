package com.example.lms.model;

import com.example.lms.util.Database;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for BookCopy entities
 */
public class BookCopyDAO {
    
    private Connection connection;
    private BookDAO bookDAO;
    
    /**
     * Constructor that initializes the database connection and book DAO
     */
    public BookCopyDAO() {
        try {
            connection = Database.getConnection();
            bookDAO = new BookDAO();
        } catch (SQLException e) {
            System.err.println("Error initializing BookCopyDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all copies of a specific book
     * 
     * @param bookId Book ID
     * @return List of all copies of the book
     * @throws SQLException if database error occurs
     */
    public List<BookCopy> getCopiesByBookId(int bookId) throws SQLException {
        List<BookCopy> copies = new ArrayList<>();
        String query = "SELECT * FROM book_copies WHERE book_id = ? ORDER BY copy_number";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookCopy copy = extractBookCopyFromResultSet(rs);
                    copies.add(copy);
                }
            }
        }
        
        return copies;
    }
    
    /**
     * Get a specific copy by ID
     * 
     * @param id Copy ID
     * @return BookCopy object or null if not found
     * @throws SQLException if database error occurs
     */
    public BookCopy getCopyById(int id) throws SQLException {
        String query = "SELECT * FROM book_copies WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookCopyFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Alias for getCopyById to maintain compatibility with BorrowingDAO
     * 
     * @param id Copy ID
     * @return BookCopy object or null if not found
     * @throws SQLException if database error occurs
     */
    public BookCopy getBookCopyById(int id) throws SQLException {
        return getCopyById(id);
    }
    
    /**
     * Get a specific copy by book ID and copy number
     * 
     * @param bookId Book ID
     * @param copyNumber Copy number
     * @return BookCopy object or null if not found
     * @throws SQLException if database error occurs
     */
    public BookCopy getCopyByBookIdAndNumber(int bookId, int copyNumber) throws SQLException {
        String query = "SELECT * FROM book_copies WHERE book_id = ? AND copy_number = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, copyNumber);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractBookCopyFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get available copies of a book
     * 
     * @param bookId Book ID
     * @return List of available copies
     * @throws SQLException if database error occurs
     */
    public List<BookCopy> getAvailableCopiesByBookId(int bookId) throws SQLException {
        List<BookCopy> copies = new ArrayList<>();
        String query = "SELECT * FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE' ORDER BY copy_number";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    BookCopy copy = extractBookCopyFromResultSet(rs);
                    copies.add(copy);
                }
            }
        }
        
        return copies;
    }
    
    /**
     * Add a new book copy
     * 
     * @param copy BookCopy to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addBookCopy(BookCopy copy) throws SQLException {
        // Make sure we have a book
        if (copy.getBook() == null || copy.getBook().getId() == 0) {
            return false;
        }
        
        // Assign next copy number if not set
        if (copy.getCopyNumber() == null || copy.getCopyNumber().isEmpty()) {
            String nextCopyNumber = getNextCopyNumber(copy.getBook().getId());
            copy.setCopyNumber(nextCopyNumber);
        }
        
        // Set default status if not set
        if (copy.getStatus() == null) {
            copy.setStatus(BookCopy.Status.AVAILABLE);
        }
        
        // Set acquisition date if not set
        if (copy.getAcquisitionDate() == null) {
            copy.setAcquisitionDate(LocalDate.now());
        }
        
        String query = "INSERT INTO book_copies (book_id, copy_number, acquisition_date, status, shelf_location, notes) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, copy.getBook().getId());
            stmt.setString(2, copy.getCopyNumber());
            stmt.setDate(3, java.sql.Date.valueOf(copy.getAcquisitionDate()));
            stmt.setString(4, copy.getStatus().toString());
            stmt.setString(5, copy.getLocation());
            stmt.setString(6, copy.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        copy.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get the next available copy number for a book
     * 
     * @param bookId Book ID
     * @return Next available copy number
     * @throws SQLException if database error occurs
     */
    public String getNextCopyNumber(int bookId) throws SQLException {
        String query = "SELECT MAX(CAST(copy_number AS UNSIGNED)) AS max_num FROM book_copies WHERE book_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getObject("max_num") != null) {
                    int maxNum = rs.getInt("max_num");
                    return String.valueOf(maxNum + 1);
                } else {
                    return "1"; // First copy
                }
            }
        }
    }
    
    /**
     * Update an existing book copy
     * 
     * @param copy BookCopy to update
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateBookCopy(BookCopy copy) throws SQLException {
        String query = "UPDATE book_copies SET book_id = ?, copy_number = ?, acquisition_date = ?, status = ?, " +
                       "shelf_location = ?, notes = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, copy.getBook().getId());
            stmt.setString(2, copy.getCopyNumber());
            stmt.setDate(3, java.sql.Date.valueOf(copy.getAcquisitionDate()));
            stmt.setString(4, copy.getStatus().toString());
            stmt.setString(5, copy.getLocation());
            stmt.setString(6, copy.getNotes());
            stmt.setInt(7, copy.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Update the status of a book copy
     * 
     * @param copyId Copy ID
     * @param status New status
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateCopyStatus(int copyId, BookCopy.Status status) throws SQLException {
        String query = "UPDATE book_copies SET status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.toString());
            stmt.setInt(2, copyId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Delete a book copy
     * 
     * @param id Copy ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteBookCopy(int id) throws SQLException {
        // Check if the copy is currently borrowed
        String checkQuery = "SELECT COUNT(*) FROM borrowings WHERE book_copy_id = ? AND return_date IS NULL";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, id);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Copy is currently borrowed, cannot delete
                    return false;
                }
            }
        }
        
        // Delete the copy
        String deleteQuery = "DELETE FROM book_copies WHERE id = ?";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, id);
            
            int affectedRows = deleteStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Get inventory statistics
     * 
     * @return Map with inventory statistics
     * @throws SQLException if database error occurs
     */
    public Map<String, Integer> getInventoryStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        
        // Total copies
        String totalQuery = "SELECT COUNT(*) AS total FROM book_copies";
        try (PreparedStatement stmt = connection.prepareStatement(totalQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
            }
        }
        
        // Copies by status
        String statusQuery = "SELECT status, COUNT(*) AS count FROM book_copies GROUP BY status";
        try (PreparedStatement stmt = connection.prepareStatement(statusQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                stats.put(status.toLowerCase(), count);
            }
        }
        
        return stats;
    }
    
    /**
     * Check if a book has any available copies
     * 
     * @param bookId Book ID
     * @return true if book has available copies, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean hasAvailableCopies(int bookId) throws SQLException {
        String query = "SELECT COUNT(*) FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Get the count of available copies for a book
     * 
     * @param bookId Book ID
     * @return Count of available copies
     */
    public int getAvailableCopiesCount(int bookId) {
        String query = "SELECT COUNT(*) FROM book_copies WHERE book_id = ? AND status = 'AVAILABLE'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting available copies count: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Get the total count of copies for a book
     * 
     * @param bookId Book ID
     * @return Total count of copies
     */
    public int getTotalCopiesCount(int bookId) {
        String query = "SELECT COUNT(*) FROM book_copies WHERE book_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting total copies count: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * Helper method to extract a BookCopy object from a ResultSet
     * 
     * @param rs ResultSet containing book copy data
     * @return BookCopy object
     * @throws SQLException if database error occurs
     */
    private BookCopy extractBookCopyFromResultSet(ResultSet rs) throws SQLException {
        BookCopy copy = new BookCopy();
        copy.setId(rs.getInt("id"));
        copy.setCopyNumber(rs.getString("copy_number"));
        
        Date acquisitionDate = rs.getDate("acquisition_date");
        if (acquisitionDate != null) {
            copy.setAcquisitionDate(acquisitionDate.toLocalDate());
        }
        
        copy.setStatus(BookCopy.Status.valueOf(rs.getString("status")));
        copy.setLocation(rs.getString("shelf_location"));
        copy.setNotes(rs.getString("notes"));
        
        // Load the related book
        int bookId = rs.getInt("book_id");
        Book book = bookDAO.getBookById(bookId);
        copy.setBook(book);
        
        return copy;
    }
    
    /**
     * Close the database connection
     */
    public void close() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            // Close related DAOs
            if (bookDAO != null) {
                bookDAO.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing BookCopyDAO: " + e.getMessage());
            throw e;
        }
    }
}
