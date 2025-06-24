package com.example.lms.model;

import com.example.lms.util.Database;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Fine entities
 */
public class FineDAO {
    
    private Connection connection;
    private UserDAO userDAO;
    private BorrowingDAO borrowingDAO;
    
    /**
     * Constructor that initializes the database connection and related DAOs
     */
    public FineDAO() {
        try {
            connection = Database.getConnection();
            userDAO = new UserDAO();
            borrowingDAO = new BorrowingDAO();
        } catch (SQLException e) {
            System.err.println("Error initializing FineDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all fines from the database
     * 
     * @return List of all fines
     * @throws SQLException if database error occurs
     */
    public List<Fine> getAllFines() throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT * FROM fines ORDER BY issue_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Fine fine = extractFineFromResultSet(rs);
                fines.add(fine);
            }
        }
        
        return fines;
    }
    
    /**
     * Get unpaid fines
     * 
     * @return List of unpaid fines
     * @throws SQLException if database error occurs
     */
    public List<Fine> getUnpaidFines() throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT * FROM fines WHERE payment_date IS NULL ORDER BY issue_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Fine fine = extractFineFromResultSet(rs);
                fines.add(fine);
            }
        }
        
        return fines;
    }
    
    /**
     * Get fines for a specific user
     * 
     * @param userId User ID
     * @return List of fines for the user
     * @throws SQLException if database error occurs
     */
    public List<Fine> getFinesByUser(int userId) throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT * FROM fines WHERE user_id = ? ORDER BY issue_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fine fine = extractFineFromResultSet(rs);
                    fines.add(fine);
                }
            }
        }
        
        return fines;
    }
    
    /**
     * Get unpaid fines for a specific user
     * 
     * @param userId User ID
     * @return List of unpaid fines for the user
     * @throws SQLException if database error occurs
     */
    public List<Fine> getUnpaidFinesByUser(int userId) throws SQLException {
        List<Fine> fines = new ArrayList<>();
        String query = "SELECT * FROM fines WHERE user_id = ? AND payment_date IS NULL ORDER BY issue_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fine fine = extractFineFromResultSet(rs);
                    fines.add(fine);
                }
            }
        }
        
        return fines;
    }
    
    /**
     * Get fine by ID
     * 
     * @param id Fine ID
     * @return Fine object or null if not found
     * @throws SQLException if database error occurs
     */
    public Fine getFineById(int id) throws SQLException {
        String query = "SELECT * FROM fines WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractFineFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Add a new fine
     * 
     * @param fine Fine to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addFine(Fine fine) throws SQLException {
        String query = "INSERT INTO fines (user_id, borrowing_id, amount, issue_date, reason, issued_by) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, fine.getUser().getId());
            
            if (fine.getBorrowing() != null) {
                stmt.setInt(2, fine.getBorrowing().getId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            
            stmt.setBigDecimal(3, fine.getAmount());
            stmt.setDate(4, Date.valueOf(fine.getIssueDate()));
            stmt.setString(5, fine.getReason().toString());
            
            if (fine.getIssuedBy() != null) {
                stmt.setInt(6, fine.getIssuedBy().getId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        fine.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Record fine payment
     * 
     * @param fineId Fine ID
     * @param paymentDate Payment date
     * @param paymentMethod Payment method
     * @param receivedBy User who received the payment
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean payFine(int fineId, LocalDate paymentDate, String paymentMethod, User receivedBy) throws SQLException {
        String query = "UPDATE fines SET payment_date = ?, payment_method = ?, received_by = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(paymentDate));
            stmt.setString(2, paymentMethod);
            
            if (receivedBy != null) {
                stmt.setInt(3, receivedBy.getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setInt(4, fineId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Calculate and add fines for overdue books
     * 
     * @param dailyRate Daily fine amount
     * @param issuedBy User who issued the fines
     * @return Number of fines added
     * @throws SQLException if database error occurs
     */
    public int calculateAndAddOverdueFines(double dailyRate, User issuedBy) throws SQLException {
        int finesAdded = 0;
        
        // Get overdue borrowings
        List<Borrowing> overdueBorrowings = borrowingDAO.getOverdueBorrowings();
        LocalDate today = LocalDate.now();
        
        for (Borrowing borrowing : overdueBorrowings) {
            // Check if fine already exists for this borrowing
            String checkQuery = "SELECT COUNT(*) FROM fines WHERE borrowing_id = ?";
            boolean fineExists = false;
            
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, borrowing.getId());
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        fineExists = true;
                    }
                }
            }
            
            if (!fineExists) {
                // Calculate days overdue
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                        borrowing.getDueDate(), today);
                
                // Calculate fine amount
                double amount = daysOverdue * dailyRate;
                
                // Create new fine
                Fine fine = new Fine();
                fine.setUser(borrowing.getUser());
                fine.setBorrowing(borrowing);
                fine.setAmount(new BigDecimal(amount));
                fine.setIssueDate(today);
                fine.setReason(Fine.Reason.LATE_RETURN);
                fine.setIssuedBy(issuedBy);
                
                if (addFine(fine)) {
                    finesAdded++;
                }
            }
        }
        
        return finesAdded;
    }
    
    /**
     * Get total amount of unpaid fines for a user
     * 
     * @param userId User ID
     * @return Total amount of unpaid fines
     * @throws SQLException if database error occurs
     */
    public double getUnpaidFinesAmountForUser(int userId) throws SQLException {
        String query = "SELECT SUM(amount) AS total FROM fines WHERE user_id = ? AND payment_date IS NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        
        return 0.0;
    }
    
    /**
     * Check if a user has unpaid fines
     * 
     * @param userId User ID
     * @return true if user has unpaid fines, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean hasUnpaidFines(int userId) throws SQLException {
        return getUnpaidFinesAmountForUser(userId) > 0;
    }
    
    /**
     * Get the total amount of all pending fines
     * 
     * @return Total amount of pending fines
     * @throws SQLException if database error occurs
     */
    public double getTotalPendingFines() throws SQLException {
        String query = "SELECT SUM(amount) AS total FROM fines WHERE payment_date IS NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                double total = rs.getDouble("total");
                if (!rs.wasNull()) {
                    return total;
                }
            }
        }
        
        return 0.0; // Return 0 if no pending fines or if result is null
    }
    
    /**
     * Delete a fine (admin function)
     * 
     * @param id Fine ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteFine(int id) throws SQLException {
        String query = "DELETE FROM fines WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Get fine statistics
     * 
     * @return Map with fine statistics
     * @throws SQLException if database error occurs
     */
    public Map<String, Object> getFineStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        // Total number of fines
        String countQuery = "SELECT COUNT(*) AS total_count FROM fines";
        try (PreparedStatement stmt = connection.prepareStatement(countQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("totalCount", rs.getInt("total_count"));
            }
        }
        
        // Total amount of fines
        String totalQuery = "SELECT SUM(amount) AS total_amount FROM fines";
        try (PreparedStatement stmt = connection.prepareStatement(totalQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("totalAmount", rs.getDouble("total_amount"));
            }
        }
        
        // Unpaid fines count
        String unpaidCountQuery = "SELECT COUNT(*) AS unpaid_count FROM fines WHERE payment_date IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(unpaidCountQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("unpaidCount", rs.getInt("unpaid_count"));
            }
        }
        
        // Unpaid fines amount
        String unpaidAmountQuery = "SELECT SUM(amount) AS unpaid_amount FROM fines WHERE payment_date IS NULL";
        try (PreparedStatement stmt = connection.prepareStatement(unpaidAmountQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("unpaidAmount", rs.getDouble("unpaid_amount"));
            }
        }
        
        return stats;
    }
    
    /**
     * Helper method to extract a Fine object from a ResultSet
     * 
     * @param rs ResultSet containing fine data
     * @return Fine object
     * @throws SQLException if database error occurs
     */
    private Fine extractFineFromResultSet(ResultSet rs) throws SQLException {
        Fine fine = new Fine();
        fine.setId(rs.getInt("id"));
        fine.setAmount(rs.getBigDecimal("amount"));
        
        Date issueDate = rs.getDate("issue_date");
        if (issueDate != null) {
            fine.setIssueDate(issueDate.toLocalDate());
        }
        
        Date paymentDate = rs.getDate("payment_date");
        if (paymentDate != null) {
            fine.setPaymentDate(paymentDate.toLocalDate());
        }
        
        fine.setReason(Fine.parseReason(rs.getString("reason")));
        fine.setPaymentMethod(rs.getString("payment_method"));
        
        // Load related entities
        int userId = rs.getInt("user_id");
        User user = userDAO.getUserById(userId);
        fine.setUser(user);
        
        int borrowingId = rs.getInt("borrowing_id");
        if (!rs.wasNull()) {
            Borrowing borrowing = borrowingDAO.getBorrowingById(borrowingId);
            fine.setBorrowing(borrowing);
        }
        
        int issuedById = rs.getInt("issued_by");
        if (!rs.wasNull()) {
            User issuedBy = userDAO.getUserById(issuedById);
            fine.setIssuedBy(issuedBy);
        }
        
        int receivedById = rs.getInt("received_by");
        if (!rs.wasNull()) {
            User receivedBy = userDAO.getUserById(receivedById);
            fine.setReceivedBy(receivedBy);
        }
        
        return fine;
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
            // UserDAO doesn't have close() method
            // No need to close the userDAO here
            if (borrowingDAO != null) {
                borrowingDAO.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing FineDAO: " + e.getMessage());
        }
    }
}
