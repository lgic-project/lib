package com.example.lms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class for author entities.
 */
public class Author {
    private int id;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private String nationality;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public Author() {
    }

    // Constructor with essential fields
    public Author(String name, String nationality) {
        this.name = name;
        this.nationality = nationality;
    }

    // Constructor with all fields
    public Author(String name, String biography, LocalDate birthDate, String nationality) {
        this.name = name;
        this.biography = biography;
        this.birthDate = birthDate;
        this.nationality = nationality;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
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
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nationality='" + nationality + '\'' +
                '}';
    }
}
