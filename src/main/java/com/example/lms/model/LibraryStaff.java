package com.example.lms.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for library staff members.
 */
public class LibraryStaff {
    private int id;
    private int userId;
    private User user;
    private String position;
    private String department;
    private LocalDate hireDate;
    private BigDecimal salary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public LibraryStaff() {
    }

    // Constructor with essential fields
    public LibraryStaff(int userId, String position, String department) {
        this.userId = userId;
        this.position = position;
        this.department = department;
        this.hireDate = LocalDate.now();
    }

    // Constructor with more fields
    public LibraryStaff(int userId, String position, String department, 
                      LocalDate hireDate, BigDecimal salary) {
        this.userId = userId;
        this.position = position;
        this.department = department;
        this.hireDate = hireDate;
        this.salary = salary;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
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

    @Override
    public String toString() {
        return "LibraryStaff{" +
                "id=" + id +
                ", userId=" + userId +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
