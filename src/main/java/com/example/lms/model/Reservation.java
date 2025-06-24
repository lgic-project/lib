package com.example.lms.model;

import java.time.LocalDateTime;

/**
 * Model class for book reservations.
 */
public class Reservation {
    private int id;
    private int bookId;
    private Book book;
    private int userId;
    private User user;
    private LocalDateTime reservationDate;
    private LocalDateTime expiryDate;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;
    private LocalDateTime notificationDate;

    /**
     * Enum for reservation status
     */
    public enum Status {
        PENDING,
        FULFILLED,
        EXPIRED,
        NOTIFIED, CANCELLED
    }

    // Default constructor
    public Reservation() {
        this.status = Status.PENDING;
        this.reservationDate = LocalDateTime.now();
    }

    // Constructor with essential fields
    public Reservation(int bookId, int userId) {
        this.bookId = bookId;
        this.userId = userId;
        this.status = Status.PENDING;
        this.reservationDate = LocalDateTime.now();
    }

    // Constructor with more fields
    public Reservation(int bookId, int userId, LocalDateTime expiryDate) {
        this.bookId = bookId;
        this.userId = userId;
        this.reservationDate = LocalDateTime.now();
        this.expiryDate = expiryDate;
        this.status = Status.PENDING;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
        if (book != null) {
            this.bookId = book.getId();
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getId();
        }
    }

    public String getReservationDate() {
        return String.valueOf(reservationDate);
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getExpiryDate() {
        return String.valueOf(expiryDate);
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getNotificationDate() {
        return notificationDate;
    }
    
    public void setNotificationDate(LocalDateTime notificationDate) {
        this.notificationDate = notificationDate;
    }

    // Update status based on dates
    public void updateStatus() {
        if (status == Status.PENDING && expiryDate != null && 
            LocalDateTime.now().isAfter(expiryDate)) {
            this.status = Status.EXPIRED;
        }
    }

    // Mark as fulfilled
    public void fulfill() {
        this.status = Status.FULFILLED;
    }

    // Cancel reservation
    public void cancel() {
        this.status = Status.CANCELLED;
    }

    // Parse status from string
    public static Status parseStatus(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return Status.PENDING; // Default status
        }
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", userId=" + userId +
                ", reservationDate=" + reservationDate +
                ", status=" + status +
                '}';
    }
}
