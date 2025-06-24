package com.example.lms.model;

import com.example.lms.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Data Access Object for Borrowing entities
 */
public class BorrowingDAO {
    
    private Connection connection;
    private BookCopyDAO bookCopyDAO;
    private UserDAO userDAO;
    
    /**
     * Constructor that initializes the database connection and related DAOs
     */
    public BorrowingDAO() {
        try {
            connection = Database.getConnection();
            bookCopyDAO = new BookCopyDAO();
            userDAO = new UserDAO();
        } catch (SQLException e) {
            System.err.println("Error initializing BorrowingDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all borrowings from the database
     * 
     * @return List of all borrowings
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getAllBorrowings() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings ORDER BY borrow_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Borrowing borrowing = extractBorrowingFromResultSet(rs);
                borrowings.add(borrowing);
            }
        }
        
        return borrowings;
    }
    
    /**
     * Get current active borrowings (not returned yet)
     * 
     * @return List of active borrowings
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getActiveBorrowings() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings WHERE return_date IS NULL ORDER BY due_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Borrowing borrowing = extractBorrowingFromResultSet(rs);
                borrowings.add(borrowing);
            }
        }
        
        return borrowings;
    }
    
    /**
     * Get borrowings for a specific user
     * 
     * @param userId User ID
     * @return List of borrowings for the user
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getBorrowingsByUser(int userId) throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings WHERE user_id = ? ORDER BY borrow_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borrowing borrowing = extractBorrowingFromResultSet(rs);
                    borrowings.add(borrowing);
                }
            }
        }
        
        return borrowings;
    }
    
    /**
     * Get active borrowings for a specific user
     * 
     * @param userId User ID
     * @return List of active borrowings for the user
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getActiveBorrowingsByUser(int userId) throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings WHERE user_id = ? AND return_date IS NULL ORDER BY due_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borrowing borrowing = extractBorrowingFromResultSet(rs);
                    borrowings.add(borrowing);
                }
            }
        }
        
        return borrowings;
    }
    
    /**
     * Get borrowings for a specific book copy
     * 
     * @param copyId Book Copy ID
     * @return List of borrowings for the copy
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getBorrowingsByCopy(int copyId) throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings WHERE copy_id = ? ORDER BY borrow_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, copyId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borrowing borrowing = extractBorrowingFromResultSet(rs);
                    borrowings.add(borrowing);
                }
            }
        }
        
        return borrowings;
    }
    
    /**
     * Get borrowing by ID
     * 
     * @param id Borrowing ID
     * @return Borrowing object or null if not found
     * @throws SQLException if database error occurs
     */
    public Borrowing getBorrowingById(int id) throws SQLException {
        String query = "SELECT * FROM borrowings WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractBorrowingFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get current borrowing for a specific book copy
     * 
     * @param copyId Book Copy ID
     * @return Current borrowing or null if not borrowed
     * @throws SQLException if database error occurs
     */
    public Borrowing getCurrentBorrowingByCopy(int copyId) throws SQLException {
        String query = "SELECT * FROM borrowings WHERE copy_id = ? AND return_date IS NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, copyId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractBorrowingFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Add a new borrowing record
     * 
     * @param borrowing Borrowing to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addBorrowing(Borrowing borrowing) throws SQLException {
        // Check if book copy is available
        BookCopy copy = bookCopyDAO.getCopyById(borrowing.getBookCopy().getId());
        if (copy == null || copy.getStatus() != BookCopy.Status.AVAILABLE) {
            return false;
        }
        
        // Begin transaction
        connection.setAutoCommit(false);
        
        try {
            // Insert borrowing record
            String insertQuery = "INSERT INTO borrowings (user_id, copy_id, borrow_date, due_date, issued_by) " +
                               "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, borrowing.getUser().getId());
                stmt.setInt(2, borrowing.getBookCopy().getId());
                stmt.setDate(3, Date.valueOf(borrowing.getBorrowDate()));
                stmt.setDate(4, Date.valueOf(borrowing.getDueDate()));
                
                if (borrowing.getIssuedBy() != null) {
                    stmt.setInt(5, borrowing.getIssuedBy().getId());
                } else {
                    stmt.setNull(5, Types.INTEGER);
                }
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            borrowing.setId(generatedKeys.getInt(1));
                            
                            // Update book copy status to BORROWED
                            boolean statusUpdated = bookCopyDAO.updateCopyStatus(
                                    borrowing.getBookCopy().getId(), 
                                    BookCopy.Status.BORROWED
                            );
                            
                            if (statusUpdated) {
                                connection.commit();
                                return true;
                            }
                        }
                    }
                }
            }
            
            // If we got here, something failed
            connection.rollback();
            return false;
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Record a book return
     * 
     * @param borrowingId Borrowing ID
     * @param returnDate Return date
     * @param returnedTo Staff who processed the return
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean returnBook(int borrowingId, LocalDate returnDate, User returnedTo) throws SQLException {
        // Get the borrowing record
        Borrowing borrowing = getBorrowingById(borrowingId);
        if (borrowing == null || borrowing.getReturnDate() != null) {
            return false;
        }
        
        // Begin transaction
        connection.setAutoCommit(false);
        
        try {
            // Update borrowing record
            String updateQuery = "UPDATE borrowings SET return_date = ?, returned_to = ? WHERE id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setDate(1, Date.valueOf(returnDate));
                
                if (returnedTo != null) {
                    stmt.setInt(2, returnedTo.getId());
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                stmt.setInt(3, borrowingId);
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    // Update book copy status back to AVAILABLE
                    boolean statusUpdated = bookCopyDAO.updateCopyStatus(
                            borrowing.getBookCopy().getId(), 
                            BookCopy.Status.AVAILABLE
                    );
                    
                    if (statusUpdated) {
                        connection.commit();
                        return true;
                    }
                }
            }
            
            // If we got here, something failed
            connection.rollback();
            return false;
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    /**
     * Extend due date for a borrowing
     * 
     * @param borrowingId Borrowing ID
     * @param newDueDate New due date
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean extendDueDate(int borrowingId, LocalDate newDueDate) throws SQLException {
        // Get the borrowing record
        Borrowing borrowing = getBorrowingById(borrowingId);
        if (borrowing == null || borrowing.getReturnDate() != null) {
            return false;
        }
        
        // Update due date
        String updateQuery = "UPDATE borrowings SET due_date = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setDate(1, Date.valueOf(newDueDate));
            stmt.setInt(2, borrowingId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Get overdue borrowings
     * 
     * @return List of overdue borrowings
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getOverdueBorrowings() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings WHERE return_date IS NULL AND due_date < ? ORDER BY due_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borrowing borrowing = extractBorrowingFromResultSet(rs);
                    borrowings.add(borrowing);
                }
            }
        }
        
        return borrowings;
    }
    
    /**
     * Get overdue borrowings for a specific user
     * 
     * @param userId User ID
     * @return List of overdue borrowings for the user
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getOverdueBorrowingsByUser(int userId) throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings WHERE user_id = ? AND return_date IS NULL AND due_date < ? ORDER BY due_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borrowing borrowing = extractBorrowingFromResultSet(rs);
                    borrowings.add(borrowing);
                }
            }
        }
        
        return borrowings;
    }
    
    /**
     * Get the count of currently active borrowings
     * 
     * @return Count of active borrowings
     * @throws SQLException if database error occurs
     */
    public int getActiveBorrowingsCount() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM borrowings WHERE return_date IS NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        
        return 0;
    }
    
    /**
     * Get monthly borrowing counts for a specific year
     * 
     * @param year Year to get counts for
     * @return Map with month abbreviation as key and borrowing count as value
     * @throws SQLException if database error occurs
     */
    public Map<String, Integer> getMonthlyBorrowingCounts(int year) throws SQLException {
        Map<String, Integer> monthlyCounts = new HashMap<>();
        
        // Initialize all months with zero count
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String month : months) {
            monthlyCounts.put(month, 0);
        }
        
        // Query for monthly counts in the given year
        String query = "SELECT MONTH(borrow_date) AS month, COUNT(*) AS count " +
                      "FROM borrowings WHERE YEAR(borrow_date) = ? " +
                      "GROUP BY MONTH(borrow_date)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int monthNumber = rs.getInt("month");
                    int count = rs.getInt("count");
                    
                    // Month numbers are 1-based, array indices are 0-based
                    if (monthNumber >= 1 && monthNumber <= 12) {
                        monthlyCounts.put(months[monthNumber - 1], count);
                    }
                }
            }
        }
        
        return monthlyCounts;
    }
    
    /**
     * Get recent borrowings for dashboard
     * 
     * @param limit Number of borrowings to retrieve
     * @return List of recent borrowings
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getRecentBorrowings(int limit) throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings ORDER BY borrow_date DESC LIMIT ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borrowing borrowing = extractBorrowingFromResultSet(rs);
                    borrowings.add(borrowing);
                }
            }
        }
        
        return borrowings;
    }
    
    /**
     * Helper method to extract a Borrowing object from a ResultSet
     * 
     * @param rs ResultSet containing borrowing data
     * @return Borrowing object
     * @throws SQLException if database error occurs
     */
    private Borrowing extractBorrowingFromResultSet(ResultSet rs) throws SQLException {
        Borrowing borrowing = new Borrowing();
        borrowing.setId(rs.getInt("id"));
        
        Date borrowDate = rs.getDate("borrow_date");
        if (borrowDate != null) {
            borrowing.setBorrowDate(borrowDate.toLocalDate());
        }
        
        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            borrowing.setDueDate(dueDate.toLocalDate());
        }
        
        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            borrowing.setReturnDate(returnDate.toLocalDate());
        }
        
        // Load related entities
        int userId = rs.getInt("user_id");
        User user = userDAO.getUserById(userId);
        borrowing.setUser(user);
        
        int copyId = rs.getInt("copy_id");
        BookCopy copy = bookCopyDAO.getCopyById(copyId);
        borrowing.setBookCopy(copy);
        
        int issuedById = rs.getInt("issued_by");
        if (!rs.wasNull()) {
            User issuedBy = userDAO.getUserById(issuedById);
            borrowing.setIssuedBy(issuedBy);
        }
        
        int returnedToId = rs.getInt("returned_to");
        if (!rs.wasNull()) {
            User returnedTo = userDAO.getUserById(returnedToId);
            borrowing.setReturnedTo(returnedTo);
        }
        
        return borrowing;
    }
    
    /**
     * Close the database connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            // Close related DAOs
            if (bookCopyDAO != null) {
                bookCopyDAO.close();
            }
            if (userDAO != null) {
                userDAO.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing BorrowingDAO: " + e.getMessage());
        }
    }
}
