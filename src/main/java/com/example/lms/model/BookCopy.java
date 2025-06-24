package com.example.lms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for individual copies of books.
 */
public class BookCopy {
    private int id;
    private int bookId;
    private Book book;
    private String copyNumber;
    private Status status;
    private LocalDate acquisitionDate;
    private BigDecimal price;
    private String shelfLocation;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Enum for book copy status
     */
    public enum Status {
        AVAILABLE, 
        BORROWED, 
        RESERVED, 
        LOST, 
        DAMAGED
    }

    // Default constructor
    public BookCopy() {
        this.status = Status.AVAILABLE;
    }

    // Constructor with essential fields
    public BookCopy(int bookId, String copyNumber) {
        this.bookId = bookId;
        this.copyNumber = copyNumber;
        this.status = Status.AVAILABLE;
    }

    // Constructor with more fields
    public BookCopy(int bookId, String copyNumber, Status status, 
                    LocalDate acquisitionDate, BigDecimal price, String shelfLocation) {
        this.bookId = bookId;
        this.copyNumber = copyNumber;
        this.status = status;
        this.acquisitionDate = acquisitionDate;
        this.price = price;
        this.shelfLocation = shelfLocation;
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

    public String getCopyNumber() {
        return copyNumber;
    }

    public void setCopyNumber(String copyNumber) {
        this.copyNumber = copyNumber;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    // Parse status from string
    public static Status parseStatus(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return Status.AVAILABLE; // Default status
        }
    }

    @Override
    public String toString() {
        return "BookCopy{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", copyNumber='" + copyNumber + '\'' +
                ", status=" + status +
                ", shelfLocation='" + shelfLocation + '\'' +
                '}';
    }
}
