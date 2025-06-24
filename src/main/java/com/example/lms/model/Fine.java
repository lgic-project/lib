package com.example.lms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for library fines.
 */
public class Fine {
    private int id;
    private int borrowingId;
    private Borrowing borrowing;
    private User user;                // User who owes the fine
    private BigDecimal amount;
    private Reason reason;
    private PaymentStatus paymentStatus;
    private LocalDate issueDate;      // Date when fine was issued
    private LocalDate paymentDate;    // Date when fine was paid
    private String paymentMethod;     // Payment method used
    private User issuedBy;           // Staff who issued the fine
    private User receivedBy;         // Staff who received the payment
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Enum for fine reasons
     */
    public enum Reason {
        LATE_RETURN,
        DAMAGED,
        LOST
    }

    /**
     * Enum for payment status
     */
    public enum PaymentStatus {
        UNPAID,
        PAID,
        WAIVED
    }

    // Default constructor
    public Fine() {
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    // Constructor with essential fields
    public Fine(int borrowingId, BigDecimal amount, Reason reason) {
        this.borrowingId = borrowingId;
        this.amount = amount;
        this.reason = reason;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBorrowingId() {
        return borrowingId;
    }

    public void setBorrowingId(int borrowingId) {
        this.borrowingId = borrowingId;
    }

    public Borrowing getBorrowing() {
        return borrowing;
    }

    public void setBorrowing(Borrowing borrowing) {
        this.borrowing = borrowing;
        if (borrowing != null) {
            this.borrowingId = borrowing.getId();
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
        
        // If marked as paid, set payment date to today
        if (paymentStatus == PaymentStatus.PAID && this.paymentDate == null) {
            this.paymentDate = LocalDate.now();
        }
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
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
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public User getIssuedBy() {
        return issuedBy;
    }
    
    public void setIssuedBy(User issuedBy) {
        this.issuedBy = issuedBy;
    }
    
    public User getReceivedBy() {
        return receivedBy;
    }
    
    public void setReceivedBy(User receivedBy) {
        this.receivedBy = receivedBy;
    }

    // Mark as paid
    public void pay() {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentDate = LocalDate.now();
    }

    // Waive the fine
    public void waive() {
        this.paymentStatus = PaymentStatus.WAIVED;
    }

    // Parse reason from string
    public static Reason parseReason(String reason) {
        try {
            return Reason.valueOf(reason.toUpperCase());
        } catch (Exception e) {
            return Reason.LATE_RETURN; // Default reason
        }
    }

    // Parse payment status from string
    public static PaymentStatus parsePaymentStatus(String status) {
        try {
            return PaymentStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return PaymentStatus.UNPAID; // Default status
        }
    }

    @Override
    public String toString() {
        return "Fine{" +
                "id=" + id +
                ", borrowingId=" + borrowingId +
                ", amount=" + amount +
                ", reason=" + reason +
                ", paymentStatus=" + paymentStatus +
                '}';
    }
}
