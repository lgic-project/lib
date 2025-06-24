package com.example.lms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for book borrowing records.
 */
public class Borrowing {
    private int id;
    private int bookCopyId;
    private BookCopy bookCopy;
    private int userId;
    private User user;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Enum for borrowing status
     */
    public enum Status {
        ACTIVE,
        RETURNED,
        OVERDUE,
        LOST
    }

    // Default constructor
    public Borrowing() {
        this.status = Status.ACTIVE;
    }

    // Constructor with essential fields
    public Borrowing(int bookCopyId, int userId, LocalDate borrowDate, LocalDate dueDate) {
        this.bookCopyId = bookCopyId;
        this.userId = userId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.status = Status.ACTIVE;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookCopyId() {
        return bookCopyId;
    }

    public void setBookCopyId(int bookCopyId) {
        this.bookCopyId = bookCopyId;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
        if (bookCopy != null) {
            this.bookCopyId = bookCopy.getId();
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

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
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

    // Update status based on dates
    public void updateStatus() {
        if (returnDate != null) {
            this.status = Status.RETURNED;
        } else if (LocalDate.now().isAfter(dueDate)) {
            this.status = Status.OVERDUE;
        }
    }

    // Parse status from string
    public static Status parseStatus(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return Status.ACTIVE; // Default status
        }
    }

    @Override
    public String toString() {
        return "Borrowing{" +
                "id=" + id +
                ", bookCopyId=" + bookCopyId +
                ", userId=" + userId +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
