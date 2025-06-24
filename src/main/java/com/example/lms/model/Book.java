package com.example.lms.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class for book entities.
 */
public class Book {
    private int id;
    private String title;
    private String isbn;
    private int publisherId;
    private Publisher publisher;
    private int publicationYear;
    private String edition;
    private String language;
    private int pages;
    private String description;
    private String coverImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Author> authors;
    private List<Category> categories;

    // Default constructor
    public Book() {
        this.authors = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // Constructor with essential fields
    public Book(String title, String isbn, int publisherId, int publicationYear) {
        this.title = title;
        this.isbn = isbn;
        this.publisherId = publisherId;
        this.publicationYear = publicationYear;
        this.authors = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // Constructor with more fields
    public Book(String title, String isbn, int publisherId, int publicationYear, 
                String edition, String language, int pages, String description) {
        this.title = title;
        this.isbn = isbn;
        this.publisherId = publisherId;
        this.publicationYear = publicationYear;
        this.edition = edition;
        this.language = language;
        this.pages = pages;
        this.description = description;
        this.authors = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(int publisherId) {
        this.publisherId = publisherId;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
        if (publisher != null) {
            this.publisherId = publisher.getId();
        }
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
    
    // Alias methods for coverImage (for compatibility with BookDAO)
    public String getCoverImage() {
        return getCoverImageUrl();
    }
    
    public void setCoverImage(String coverImage) {
        setCoverImageUrl(coverImage);
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

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public void addAuthor(Author author) {
        if (this.authors == null) {
            this.authors = new ArrayList<>();
        }
        this.authors.add(author);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category) {
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }
        this.categories.add(category);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publicationYear=" + publicationYear +
                '}';
    }
}
