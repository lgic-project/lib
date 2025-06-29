package com.example.lms.model;

import com.example.lms.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Get a borrowing by its ID
     * 
     * @param id Borrowing ID
     * @return Borrowing object if found, null otherwise
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
     * Get borrowings for a user
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
     * Get active borrowings (not returned yet)
     * 
     * @return List of active borrowings
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getActiveBorrowings() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings WHERE return_date IS NULL ORDER BY due_date ASC";
        
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
     * Get active borrowings count
     * 
     * @return Count of active borrowings
     * @throws SQLException if database error occurs
     */
    public int getActiveBorrowingsCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM borrowings WHERE return_date IS NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Get overdue borrowings
     * 
     * @return List of overdue borrowings
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> getOverdueBorrowings() throws SQLException {
        List<Borrowing> borrowings = new ArrayList<>();
        String query = "SELECT * FROM borrowings WHERE return_date IS NULL AND due_date < CURDATE() ORDER BY due_date ASC";
        
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
     * Create a new borrowing record
     * 
     * @param borrowing Borrowing object
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean createBorrowing(Borrowing borrowing) throws SQLException {
        // Begin transaction
        connection.setAutoCommit(false);
        
        try {
            // Insert borrowing record
            String insertQuery = "INSERT INTO borrowings (user_id, book_copy_id, borrow_date, due_date, issued_by) " +
                               "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, borrowing.getUser().getId());
                stmt.setInt(2, borrowing.getBookCopy().getId());
                stmt.setDate(3, java.sql.Date.valueOf(borrowing.getBorrowDate()));
                stmt.setDate(4, java.sql.Date.valueOf(borrowing.getDueDate()));
                
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
                stmt.setDate(1, java.sql.Date.valueOf(returnDate));
                
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
        
        String updateQuery = "UPDATE borrowings SET due_date = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
            stmt.setDate(1, java.sql.Date.valueOf(newDueDate));
            stmt.setInt(2, borrowingId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Get monthly borrowing statistics by year
     * 
     * @param year Year to get statistics for
     * @return Map with month numbers (1-12) as keys and borrowing counts as values
     * @throws SQLException if database error occurs
     */
    public Map<Integer, Integer> getMonthlyBorrowingCounts(int year) throws SQLException {
        Map<Integer, Integer> monthlyCounts = new HashMap<>();
        
        // Initialize all months to 0
        for (int i = 1; i <= 12; i++) {
            monthlyCounts.put(i, 0);
        }
        
        String query = "SELECT MONTH(borrow_date) as month, COUNT(*) as count " +
                       "FROM borrowings " +
                       "WHERE YEAR(borrow_date) = ? " +
                       "GROUP BY MONTH(borrow_date)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, year);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("month");
                    int count = rs.getInt("count");
                    monthlyCounts.put(month, count);
                }
            }
        }
        
        return monthlyCounts;
    }
    
    /**
     * Search borrowings
     * 
     * @param searchTerm Search term to look for in user name, book title, etc.
     * @param status Status filter (null for all)
     * @return List of borrowings matching the search criteria
     * @throws SQLException if database error occurs
     */
    public List<Borrowing> searchBorrowings(String searchTerm, Borrowing.Status status) throws SQLException {
        List<Borrowing> results = new ArrayList<>();
        
        // Base query
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT b.* FROM borrowings b " +
            "JOIN users u ON b.user_id = u.id " +
            "JOIN book_copies bc ON b.book_copy_id = bc.id " +
            "JOIN books bk ON bc.book_id = bk.id " +
            "WHERE (u.name LIKE ? OR bk.title LIKE ?) "
        );
        
        // Add status filter if specified
        if (status != null) {
            switch (status) {
                case ACTIVE:
                    queryBuilder.append("AND b.return_date IS NULL ");
                    break;
                case OVERDUE:
                    queryBuilder.append("AND b.return_date IS NULL AND b.due_date < CURDATE() ");
                    break;
                case RETURNED:
                    queryBuilder.append("AND b.return_date IS NOT NULL ");
                    break;
            }
        }
        
        // Add order by
        queryBuilder.append("ORDER BY b.borrow_date DESC");
        
        String query = queryBuilder.toString();
        String likePattern = "%" + searchTerm + "%";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, likePattern);
            stmt.setString(2, likePattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borrowing borrowing = extractBorrowingFromResultSet(rs);
                    results.add(borrowing);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Extract a Borrowing object from a ResultSet
     * 
     * @param rs ResultSet to extract from
     * @return Borrowing object
     * @throws SQLException if database error occurs
     */
    private Borrowing extractBorrowingFromResultSet(ResultSet rs) throws SQLException {
        Borrowing borrowing = new Borrowing();
        
        borrowing.setId(rs.getInt("id"));
        
        // Get the book copy
        int copyId = rs.getInt("book_copy_id");
        BookCopy bookCopy = bookCopyDAO.getBookCopyById(copyId);
        borrowing.setBookCopy(bookCopy);
        
        // Get the user
        int userId = rs.getInt("user_id");
        User user = userDAO.getUserById(userId);
        borrowing.setUser(user);
        
        // Get the issued by staff if available - check if column exists first
        try {
            int issuedById = rs.getInt("issued_by");
            if (!rs.wasNull()) {
                User issuedBy = userDAO.getUserById(issuedById);
                borrowing.setIssuedBy(issuedBy);
            }
        } catch (SQLException e) {
            // Column doesn't exist in the schema, skip it
        }
        
        // Get the returned to staff if available - check if column exists first
        try {
            int returnedToId = rs.getInt("returned_to");
            if (!rs.wasNull()) {
                User returnedTo = userDAO.getUserById(returnedToId);
                borrowing.setReturnedTo(returnedTo);
            }
        } catch (SQLException e) {
            // Column doesn't exist in the schema, skip it
        }
        
        // Get the dates
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
        
        // Set timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            borrowing.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            borrowing.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // Update the status based on dates
    borrowing.updateStatus();
    
    return borrowing;
}

/**
 * Get recent borrowing activities for dashboard
 * 
 * @param limit Maximum number of activities to return
 * @return List of maps containing activity details
 * @throws SQLException if database error occurs
 */
public List<Map<String, String>> getRecentBorrowingActivities(int limit) throws SQLException {
    List<Map<String, String>> activities = new ArrayList<>();
    
    String query = "SELECT b.id, bk.title, u.name as user_name, b.borrow_date, b.due_date " +
                   "FROM borrowings b " +
                   "JOIN book_copies bc ON b.book_copy_id = bc.id " +
                   "JOIN books bk ON bc.book_id = bk.id " +
                   "JOIN users u ON b.user_id = u.id " +
                   "ORDER BY b.borrow_date DESC LIMIT ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, limit);
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, String> activity = new HashMap<>();
                activity.put("type", "borrow");
                activity.put("id", rs.getString("id"));
                activity.put("title", rs.getString("title"));
                activity.put("user", rs.getString("user_name"));
                activity.put("date", rs.getTimestamp("borrow_date").toString());
                activities.add(activity);
            }
        }
    }
    
    return activities;
}

/**
 * Update a borrowing record
 * 
 * @param borrowing The borrowing record to update
 * @return true if successful, false otherwise
 * @throws SQLException if database error occurs
 */
public boolean updateBorrowing(Borrowing borrowing) throws SQLException {
    String query = "UPDATE borrowings SET return_date = ?, returned_to = ?, updated_at = NOW() WHERE id = ?";
    
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        if (borrowing.getReturnDate() != null) {
            stmt.setDate(1, Date.valueOf(borrowing.getReturnDate()));
        } else {
            stmt.setNull(1, Types.DATE);
        }
        
        if (borrowing.getReturnedTo() != null) {
            stmt.setInt(2, borrowing.getReturnedTo().getId());
        } else {
            stmt.setNull(2, Types.INTEGER);
        }
        
        stmt.setInt(3, borrowing.getId());
        
        int rowsUpdated = stmt.executeUpdate();
        return rowsUpdated > 0;
    }
}

/**
 * Get current borrowings (books that have been borrowed and not returned yet)
 * 
 * @return List of current borrowings
 * @throws SQLException if database error occurs
 */
public List<Borrowing> getCurrentBorrowings() throws SQLException {
    // This is functionally identical to getActiveBorrowings, but provided for semantic clarity
    return getActiveBorrowings();
}

/**
 * Close resources and release the database connection
 */
public void close() throws SQLException {
    // Release the connection back to the pool
    Database.releaseConnection();
    
    // Close child DAOs if necessary
    if (bookCopyDAO != null) {
        bookCopyDAO.close();
    }
}
}
