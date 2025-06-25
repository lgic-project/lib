package com.example.lms.model;

import com.example.lms.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for Book entities
 */
public class BookDAO {
    
    private Connection connection;
    private PublisherDAO publisherDAO;
    private CategoryDAO categoryDAO;
    
    /**
     * Constructor that initializes the database connection and related DAOs
     */
    public BookDAO() {
        try {
            connection = Database.getConnection();
            publisherDAO = new PublisherDAO();
            categoryDAO = new CategoryDAO();
        } catch (SQLException e) {
            System.err.println("Error initializing BookDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all books from the database
     * 
     * @return List of all books
     * @throws SQLException if database error occurs
     */
    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books ORDER BY title";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Book book = extractBookFromResultSet(rs);
                loadBookRelations(book);
                books.add(book);
            }
        }
        
        return books;
    }
    
    /**
     * Get a book by ID
     * 
     * @param id Book ID
     * @return Book object or null if not found
     * @throws SQLException if database error occurs
     */
    public Book getBookById(int id) throws SQLException {
        String query = "SELECT * FROM books WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Book book = extractBookFromResultSet(rs);
                    loadBookRelations(book);
                    return book;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get a book by ISBN
     * 
     * @param isbn Book ISBN
     * @return Book object or null if not found
     * @throws SQLException if database error occurs
     */
    public Book getBookByISBN(String isbn) throws SQLException {
        String query = "SELECT * FROM books WHERE isbn = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, isbn);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Book book = extractBookFromResultSet(rs);
                    loadBookRelations(book);
                    return book;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get books by category
     * 
     * @param categoryName Category name
     * @return List of books in the category
     * @throws SQLException if database error occurs
     */
    public List<Book> getBooksByCategory(String categoryName) throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT b.* FROM books b " +
                       "JOIN book_categories bc ON b.id = bc.book_id " +
                       "JOIN categories c ON bc.category_id = c.id " +
                       "WHERE c.name = ? " +
                       "ORDER BY b.title";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = extractBookFromResultSet(rs);
                    loadBookRelations(book);
                    books.add(book);
                }
            }
        }
        
        return books;
    }
    
    /**
     * Get books by author
     * 
     * @param authorName Author name
     * @return List of books by the author
     * @throws SQLException if database error occurs
     */
    public List<Book> getBooksByAuthor(String authorName) throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE author_name LIKE ? ORDER BY title";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + authorName + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = extractBookFromResultSet(rs);
                    loadBookRelations(book);
                    books.add(book);
                }
            }
        }
        
        return books;
    }
    
    /**
     * Get books by publisher
     * 
     * @param publisherId Publisher ID
     * @return List of books by the publisher
     * @throws SQLException if database error occurs
     */
    public List<Book> getBooksByPublisher(int publisherId) throws SQLException {
        List<Book> books = new ArrayList<>();
        String query = "SELECT * FROM books WHERE publisher_id = ? ORDER BY title";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, publisherId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = extractBookFromResultSet(rs);
                    loadBookRelations(book);
                    books.add(book);
                }
            }
        }
        
        return books;
    }
    
    /**
     * Get the total number of books in the library
     * 
     * @return Total number of books
     * @throws SQLException if database error occurs
     */
    public int getTotalBooks() throws SQLException {
        String query = "SELECT COUNT(*) AS total FROM books";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        
        return 0;
    }
    
    /**
     * Search books by title, author, or ISBN
     * 
     * @param searchTerm Search term
     * @param category Optional category filter
     * @return List of matching books
     * @throws SQLException if database error occurs
     */
    public List<Book> searchBooks(String searchTerm, String category) throws SQLException {
        List<Book> books = new ArrayList<>();
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT b.* FROM books b ");
        
        if (category != null && !category.isEmpty()) {
            queryBuilder.append("JOIN book_categories bc ON b.id = bc.book_id ");
            queryBuilder.append("JOIN categories c ON bc.category_id = c.id ");
        }
        
        queryBuilder.append("WHERE (b.title LIKE ? OR b.author_name LIKE ? OR b.isbn LIKE ?) ");
        
        if (category != null && !category.isEmpty()) {
            queryBuilder.append("AND c.name = ? ");
        }
        
        queryBuilder.append("ORDER BY b.title");
        
        String query = queryBuilder.toString();
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            if (category != null && !category.isEmpty()) {
                stmt.setString(4, category);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Book book = extractBookFromResultSet(rs);
                    loadBookRelations(book);
                    books.add(book);
                }
            }
        }
        
        return books;
    }
    
    /**
     * Add a new book
     * 
     * @param book Book to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addBook(Book book) throws SQLException {
        String query = "INSERT INTO books (title, author_name, isbn, publisher_id, publication_year, edition, language, pages, description, cover_image_url) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthorName());
            stmt.setString(3, book.getIsbn());
            
            if (book.getPublisher() != null) {
                stmt.setInt(4, book.getPublisher().getId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            
            stmt.setInt(5, book.getPublicationYear());
            stmt.setString(6, book.getEdition());
            stmt.setString(7, book.getLanguage());
            stmt.setInt(8, book.getPages());
            stmt.setString(9, book.getDescription());
            stmt.setString(10, book.getCoverImageUrl());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        book.setId(generatedKeys.getInt(1));
                        
                        // Add categories
                        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
                            for (Category category : book.getCategories()) {
                                if (category.getId() == 0) {
                                    // New category, save it first
                                    categoryDAO.addCategory(category);
                                }
                                
                                // Create the book-category relation
                                String categoryQuery = "INSERT INTO book_categories (book_id, category_id) VALUES (?, ?)";
                                try (PreparedStatement categoryStmt = connection.prepareStatement(categoryQuery)) {
                                    categoryStmt.setInt(1, book.getId());
                                    categoryStmt.setInt(2, category.getId());
                                    categoryStmt.executeUpdate();
                                }
                            }
                        }
                        
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Add a new book
     * 
     * Update an existing book
     * 
     * @param book Book to update
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateBook(Book book) throws SQLException {
        String query = "UPDATE books SET title = ?, author_name = ?, isbn = ?, publication_year = ?, " +
                       "publisher_id = ?, edition = ?, language = ?, pages = ?, description = ?, cover_image_url = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthorName());
            stmt.setString(3, book.getIsbn());
            stmt.setInt(4, book.getPublicationYear());
            
            if (book.getPublisher() != null) {
                stmt.setInt(5, book.getPublisher().getId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            
            stmt.setString(6, book.getEdition());
            stmt.setString(7, book.getLanguage());
            stmt.setInt(8, book.getPages());
            stmt.setString(9, book.getDescription());
            stmt.setString(10, book.getCoverImageUrl());
            stmt.setInt(11, book.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                // Update categories (remove all and re-add)
                clearBookCategories(book.getId());
                for (Category category : book.getCategories()) {
                    categoryDAO.addCategoryToBook(category.getId(), book.getId());
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    // clearBookAuthors method removed - no longer needed with author_name in books table
    
    /**
     * Clear all category associations for a book
     * 
     * @param bookId Book ID
     * @throws SQLException if database error occurs
     */
    private void clearBookCategories(int bookId) throws SQLException {
        String query = "DELETE FROM book_categories WHERE book_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Delete a book by ID
     * 
     * @param id Book ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteBook(int id) throws SQLException {
        // First check if any book copies exist
        String checkCopiesQuery = "SELECT COUNT(*) FROM book_copies WHERE book_id = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkCopiesQuery)) {
            checkStmt.setInt(1, id);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Book has copies, cannot delete
                    return false;
                }
            }
        }
        
        // Authors are now directly in books table, no associations to clear
        
        // Remove category associations
        clearBookCategories(id);
        
        // Delete the book
        String deleteQuery = "DELETE FROM books WHERE id = ?";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, id);
            
            int affectedRows = deleteStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Get count of books by category
     * 
     * @return Map of category name to book count
     * @throws SQLException if database error occurs
     */
    public Map<String, Integer> getBookCountByCategory() throws SQLException {
        return categoryDAO.getBookCountByCategory();
    }
    
    /**
     * Helper method to extract a Book object from a ResultSet
     * 
     * @param rs ResultSet containing book data
     * @return Book object
     * @throws SQLException if database error occurs
     */
    private Book extractBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthorName(rs.getString("author_name"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setDescription(rs.getString("description"));
        book.setCoverImage(rs.getString("cover_image_url"));
        
        int publisherId = rs.getInt("publisher_id");
        if (!rs.wasNull()) {
            Publisher publisher = publisherDAO.getPublisherById(publisherId);
            book.setPublisher(publisher);
        }
        
        return book;
    }
    
    /**
     * Load book relations (authors and categories)
     * 
     * @param book Book to load relations for
     * @throws SQLException if database error occurs
     */
    private void loadBookRelations(Book book) throws SQLException {
        // Load categories
        List<Category> categories = categoryDAO.getCategoriesByBookId(book.getId());
        book.setCategories(categories);
    }
    
    /**
     * Close resources and release the database connection
     * 
     * @throws SQLException if database error occurs
     */
    public void close() throws SQLException {
        // Release the connection back to the pool
        Database.releaseConnection();

        // Close related DAOs
        if (publisherDAO != null) {
            publisherDAO.close();
        }
//            if (authorDAO != null) {
//                authorDAO.close();
//            }
        if (categoryDAO != null) {
            categoryDAO.close();
        }
    }
}
