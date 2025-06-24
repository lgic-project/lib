package com.example.lms.model;

import com.example.lms.util.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Reservation entities
 */
public class ReservationDAO {
    
    private Connection connection;
    private BookDAO bookDAO;
    private UserDAO userDAO;
    
    /**
     * Constructor that initializes the database connection and related DAOs
     */
    public ReservationDAO() {
        try {
            connection = Database.getConnection();
            bookDAO = new BookDAO();
            userDAO = new UserDAO();
        } catch (SQLException e) {
            System.err.println("Error initializing ReservationDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all reservations from the database
     * 
     * @return List of all reservations
     * @throws SQLException if database error occurs
     */
    public List<Reservation> getAllReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations ORDER BY reservation_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Reservation reservation = extractReservationFromResultSet(rs);
                reservations.add(reservation);
            }
        }
        
        return reservations;
    }
    
    /**
     * Get active reservations (not fulfilled or canceled)
     * 
     * @return List of active reservations
     * @throws SQLException if database error occurs
     */
    public List<Reservation> getActiveReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations WHERE status = 'ACTIVE' ORDER BY reservation_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Reservation reservation = extractReservationFromResultSet(rs);
                reservations.add(reservation);
            }
        }
        
        return reservations;
    }
    
    /**
     * Get active reservations for a specific book
     * 
     * @param bookId Book ID
     * @return List of active reservations for the book
     * @throws SQLException if database error occurs
     */
    public List<Reservation> getActiveReservationsForBook(int bookId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations WHERE book_id = ? AND status = 'ACTIVE' ORDER BY reservation_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = extractReservationFromResultSet(rs);
                    reservations.add(reservation);
                }
            }
        }
        
        return reservations;
    }
    
    /**
     * Get reservations for a specific user
     * 
     * @param userId User ID
     * @return List of reservations for the user
     * @throws SQLException if database error occurs
     */
    public List<Reservation> getReservationsByUser(int userId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations WHERE user_id = ? ORDER BY reservation_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = extractReservationFromResultSet(rs);
                    reservations.add(reservation);
                }
            }
        }
        
        return reservations;
    }
    
    /**
     * Get active reservations for a specific user
     * 
     * @param userId User ID
     * @return List of active reservations for the user
     * @throws SQLException if database error occurs
     */
    public List<Reservation> getActiveReservationsByUser(int userId) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations WHERE user_id = ? AND status = 'ACTIVE' ORDER BY reservation_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = extractReservationFromResultSet(rs);
                    reservations.add(reservation);
                }
            }
        }
        
        return reservations;
    }
    
    /**
     * Get reservation by ID
     * 
     * @param id Reservation ID
     * @return Reservation object or null if not found
     * @throws SQLException if database error occurs
     */
    public Reservation getReservationById(int id) throws SQLException {
        String query = "SELECT * FROM reservations WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractReservationFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Check if a user has already reserved a book
     * 
     * @param userId User ID
     * @param bookId Book ID
     * @return true if user has active reservation for the book, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean hasUserReservedBook(int userId, int bookId) throws SQLException {
        String query = "SELECT COUNT(*) FROM reservations WHERE user_id = ? AND book_id = ? AND status = 'ACTIVE'";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Add a new reservation
     * 
     * @param reservation Reservation to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addReservation(Reservation reservation) throws SQLException {
        // Check if user already has an active reservation for this book
        if (hasUserReservedBook(reservation.getUser().getId(), reservation.getBook().getId())) {
            return false;
        }
        
        String query = "INSERT INTO reservations (user_id, book_id, reservation_date, expiry_date, status, notes) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reservation.getUser().getId());
            stmt.setInt(2, reservation.getBook().getId());
            stmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            
            if (reservation.getExpiryDate() != null) {
                stmt.setDate(4, Date.valueOf(reservation.getExpiryDate()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            
            stmt.setString(5, reservation.getStatus().toString());
            stmt.setString(6, reservation.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservation.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Update reservation status
     * 
     * @param reservationId Reservation ID
     * @param status New status
     * @param notificationDate Date when notification was sent (for NOTIFIED status)
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateReservationStatus(int reservationId, Reservation.Status status, LocalDate notificationDate) throws SQLException {
        String query = "UPDATE reservations SET status = ?, notification_date = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status.toString());
            
            if (notificationDate != null && status == Reservation.Status.NOTIFIED) {
                stmt.setDate(2, Date.valueOf(notificationDate));
            } else {
                stmt.setNull(2, Types.DATE);
            }
            
            stmt.setInt(3, reservationId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Cancel a reservation
     * 
     * @param reservationId Reservation ID
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean cancelReservation(int reservationId) throws SQLException {
        return updateReservationStatus(reservationId, Reservation.Status.CANCELLED, null);
    }
    
    /**
     * Mark a reservation as fulfilled
     * 
     * @param reservationId Reservation ID
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean fulfillReservation(int reservationId) throws SQLException {
        return updateReservationStatus(reservationId, Reservation.Status.FULFILLED, null);
    }
    
    /**
     * Mark a reservation as notified
     * 
     * @param reservationId Reservation ID
     * @param notificationDate Date when notification was sent
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean markAsNotified(int reservationId, LocalDate notificationDate) throws SQLException {
        return updateReservationStatus(reservationId, Reservation.Status.NOTIFIED, notificationDate);
    }
    
    /**
     * Get expired reservations (active but past expiry date)
     * 
     * @return List of expired reservations
     * @throws SQLException if database error occurs
     */
    public List<Reservation> getExpiredReservations() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations WHERE status = 'ACTIVE' AND expiry_date < ? ORDER BY expiry_date";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = extractReservationFromResultSet(rs);
                    reservations.add(reservation);
                }
            }
        }
        
        return reservations;
    }
    
    /**
     * Delete a reservation (admin function)
     * 
     * @param id Reservation ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteReservation(int id) throws SQLException {
        String query = "DELETE FROM reservations WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Get reservation statistics
     * 
     * @return Map with reservation statistics
     * @throws SQLException if database error occurs
     */
    public Map<String, Integer> getReservationStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        
        // Total reservations
        String totalQuery = "SELECT COUNT(*) AS total FROM reservations";
        try (PreparedStatement stmt = connection.prepareStatement(totalQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
            }
        }
        
        // Active reservations
        String activeQuery = "SELECT COUNT(*) AS active FROM reservations WHERE status = 'ACTIVE'";
        try (PreparedStatement stmt = connection.prepareStatement(activeQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("active", rs.getInt("active"));
            }
        }
        
        // Fulfilled reservations
        String fulfilledQuery = "SELECT COUNT(*) AS fulfilled FROM reservations WHERE status = 'FULFILLED'";
        try (PreparedStatement stmt = connection.prepareStatement(fulfilledQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("fulfilled", rs.getInt("fulfilled"));
            }
        }
        
        // Cancelled reservations
        String cancelledQuery = "SELECT COUNT(*) AS cancelled FROM reservations WHERE status = 'CANCELLED'";
        try (PreparedStatement stmt = connection.prepareStatement(cancelledQuery);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("cancelled", rs.getInt("cancelled"));
            }
        }
        
        return stats;
    }
    
    /**
     * Helper method to extract a Reservation object from a ResultSet
     * 
     * @param rs ResultSet containing reservation data
     * @return Reservation object
     * @throws SQLException if database error occurs
     */
    private Reservation extractReservationFromResultSet(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        
        Date reservationDate = rs.getDate("reservation_date");
        if (reservationDate != null) {
            reservation.setReservationDate(reservationDate.toLocalDate().atStartOfDay());
        }
        
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            reservation.setExpiryDate(expiryDate.toLocalDate());
        }
        
        Date notificationDate = rs.getDate("notification_date");
        if (notificationDate != null) {
            reservation.setNotificationDate(notificationDate.toLocalDate());
        }
        
        reservation.setStatus(Reservation.Status.valueOf(rs.getString("status")));
        reservation.setNotes(rs.getString("notes"));
        
        // Load related entities
        int userId = rs.getInt("user_id");
        User user = userDAO.getUserById(userId);
        reservation.setUser(user);
        
        int bookId = rs.getInt("book_id");
        Book book = bookDAO.getBookById(bookId);
        reservation.setBook(book);
        
        return reservation;
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
            if (bookDAO != null) {
                bookDAO.close();
            }
            if (userDAO != null) {
                userDAO.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing ReservationDAO: " + e.getMessage());
        }
    }
}
