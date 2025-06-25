package com.example.lms.model;

import com.example.lms.util.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category entities
 */
public class CategoryDAO {
    
    private Connection connection;
    
    /**
     * Constructor that initializes the database connection
     */
    public CategoryDAO() {
        try {
            connection = Database.getConnection();
        } catch (SQLException e) {
            System.err.println("Error initializing CategoryDAO: " + e.getMessage());
        }
    }
    
    /**
     * Get all categories from the database
     * 
     * @return List of all categories
     * @throws SQLException if database error occurs
     */
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM categories ORDER BY name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Category category = extractCategoryFromResultSet(rs);
                categories.add(category);
            }
        }
        
        return categories;
    }
    
    /**
     * Get a category by ID
     * 
     * @param id Category ID
     * @return Category object or null if not found
     * @throws SQLException if database error occurs
     */
    public Category getCategoryById(int id) throws SQLException {
        String query = "SELECT * FROM categories WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCategoryFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get a category by name
     * 
     * @param name Category name
     * @return Category object or null if not found
     * @throws SQLException if database error occurs
     */
    public Category getCategoryByName(String name) throws SQLException {
        String query = "SELECT * FROM categories WHERE name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCategoryFromResultSet(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Get categories for a specific book
     * 
     * @param bookId Book ID
     * @return List of categories for the book
     * @throws SQLException if database error occurs
     */
    public List<Category> getCategoriesByBookId(int bookId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT c.* FROM categories c " +
                       "JOIN book_categories bc ON c.id = bc.category_id " +
                       "WHERE bc.book_id = ? " +
                       "ORDER BY c.name";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, bookId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Category category = extractCategoryFromResultSet(rs);
                    categories.add(category);
                }
            }
        }
        
        return categories;
    }
    
    /**
     * Add a new category
     * 
     * @param category Category to add
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addCategory(Category category) throws SQLException {
        String query = "INSERT INTO categories (name, description) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        category.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Update an existing category
     * 
     * @param category Category to update
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateCategory(Category category) throws SQLException {
        String query = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.getId());
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Delete a category by ID
     * 
     * @param id Category ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteCategory(int id) throws SQLException {
        // First check if any books are associated with this category
        String checkQuery = "SELECT COUNT(*) FROM book_categories WHERE category_id = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, id);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Category has associated books, cannot delete
                    return false;
                }
            }
        }
        
        // No associated books, proceed with deletion
        String deleteQuery = "DELETE FROM categories WHERE id = ?";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, id);
            
            int affectedRows = deleteStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Add a category-book association
     * 
     * @param categoryId Category ID
     * @param bookId Book ID
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addCategoryToBook(int categoryId, int bookId) throws SQLException {
        // Check if the association already exists
        String checkQuery = "SELECT COUNT(*) FROM book_categories WHERE category_id = ? AND book_id = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, categoryId);
            checkStmt.setInt(2, bookId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Association already exists
                    return true;
                }
            }
        }
        
        // Add the association
        String insertQuery = "INSERT INTO book_categories (category_id, book_id) VALUES (?, ?)";
        
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setInt(1, categoryId);
            insertStmt.setInt(2, bookId);
            
            int affectedRows = insertStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Remove a category-book association
     * 
     * @param categoryId Category ID
     * @param bookId Book ID
     * @return true if successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean removeCategoryFromBook(int categoryId, int bookId) throws SQLException {
        String deleteQuery = "DELETE FROM book_categories WHERE category_id = ? AND book_id = ?";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, categoryId);
            deleteStmt.setInt(2, bookId);
            
            int affectedRows = deleteStmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    /**
     * Get book count by category for statistics
     * 
     * @return Map of category name to book count
     * @throws SQLException if database error occurs
     */
    public java.util.Map<String, Integer> getBookCountByCategory() throws SQLException {
        java.util.Map<String, Integer> categoryCount = new java.util.HashMap<>();
        
        String query = "SELECT c.name, COUNT(bc.book_id) as book_count " +
                       "FROM categories c " +
                       "LEFT JOIN book_categories bc ON c.id = bc.category_id " +
                       "GROUP BY c.id, c.name " +
                       "ORDER BY book_count DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String categoryName = rs.getString("name");
                int count = rs.getInt("book_count");
                categoryCount.put(categoryName, count);
            }
        }
        
        return categoryCount;
    }
    
    /**
     * Helper method to extract a Category object from a ResultSet
     * 
     * @param rs ResultSet containing category data
     * @return Category object
     * @throws SQLException if database error occurs
     */
    private Category extractCategoryFromResultSet(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        return category;
    }
    
    /**
     * Close resources and release the database connection
     * 
     * @throws SQLException if database error occurs
     */
    public void close() throws SQLException {
        // Release the connection back to the pool
        Database.releaseConnection();
    }
}
