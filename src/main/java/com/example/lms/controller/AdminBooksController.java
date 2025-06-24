package com.example.lms.controller;

import com.example.lms.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the admin books management view
 */
public class AdminBooksController implements ChildController {

    @FXML
    private Button addBookBtn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> categoryFilter;
    
    @FXML
    private Button searchBtn;
    
    @FXML
    private TableView<Book> booksTable;
    
    @FXML
    private TableColumn<Book, Integer> idColumn;
    
    @FXML
    private TableColumn<Book, String> titleColumn;
    
    @FXML
    private TableColumn<Book, String> authorColumn;
    
    @FXML
    private TableColumn<Book, String> isbnColumn;
    
    @FXML
    private TableColumn<Book, String> publisherColumn;
    
    @FXML
    private TableColumn<Book, Integer> yearColumn;
    
    @FXML
    private TableColumn<Book, Integer> copiesColumn;
    
    @FXML
    private TableColumn<Book, Void> actionsColumn;
    
    private User currentUser;
    private BookDAO bookDAO;
    private CategoryDAO categoryDAO;
    private BookCopyDAO bookCopyDAO;
    private ObservableList<Book> books = FXCollections.observableArrayList();
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Initialize DAOs
        bookDAO = new BookDAO();
        categoryDAO = new CategoryDAO();
        bookCopyDAO = new BookCopyDAO();
        
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(cellData -> {
            List<Author> authors = cellData.getValue().getAuthors();
            String authorNames = authors.stream()
                    .map(Author::getName)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(authorNames);
        });
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publisherColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPublisher() != null ? 
                    cellData.getValue().getPublisher().getName() : ""));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
        copiesColumn.setCellValueFactory(cellData -> {
            int count = bookCopyDAO.getAvailableCopiesCount(cellData.getValue().getId());
            return new SimpleObjectProperty<>(count);
        });
        
        // Set up action buttons
        setupActionButtons();
        
        // Load categories for filter
        loadCategoryFilter();
        
        // Load initial book data
        loadBooks();
    }
    
    /**
     * Initialize the controller with user data
     * @param user The current user
     */
    @Override
    public void initData(User user) {
        this.currentUser = user;
    }
    
    /**
     * Set up action buttons in the table
     */
    private void setupActionButtons() {
        Callback<TableColumn<Book, Void>, TableCell<Book, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Book, Void> call(final TableColumn<Book, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button viewBtn = new Button("View");
                    
                    {
                        editBtn.getStyleClass().add("button-small");
                        viewBtn.getStyleClass().add("button-small");
                        
                        editBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            editBook(book);
                        });
                        
                        viewBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            viewBookDetails(book);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Create a container for the buttons
                            HBox hbox = new HBox(5);
                            hbox.getChildren().addAll(viewBtn, editBtn);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    /**
     * Event handler for add book button
     */
    @FXML
    private void onAddBookClick() {
        // In a real implementation, this would open a dialog to add a new book
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Book");
        alert.setHeaderText(null);
        alert.setContentText("Add Book functionality will be implemented here.");
        alert.showAndWait();
    }
    
    /**
     * Event handler for search button
     */
    @FXML
    private void onSearchClick() {
        String searchTerm = searchField.getText().trim();
        String category = categoryFilter.getSelectionModel().getSelectedItem();
        
        if (category != null && category.equals("All Categories")) {
            category = null;
        }
        
        loadBooks(searchTerm, category);
    }
    
    /**
     * Load categories for the filter dropdown
     */
    private void loadCategoryFilter() {
        try {
            List<Category> categories = categoryDAO.getAllCategories();
            List<String> categoryNames = categories.stream()
                    .map(Category::getName)
                    .collect(Collectors.toList());
            
            // Add "All Categories" option at the beginning
            categoryNames.add(0, "All Categories");
            
            categoryFilter.setItems(FXCollections.observableArrayList(categoryNames));
            categoryFilter.getSelectionModel().selectFirst();
            
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            
            // Add a default "All Categories" option
            categoryFilter.setItems(FXCollections.observableArrayList("All Categories"));
            categoryFilter.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Load all books
     */
    private void loadBooks() {
        loadBooks(null, null);
    }
    
    /**
     * Load books with filtering
     * 
     * @param searchTerm Term to search by title, author, or ISBN
     * @param categoryName Category to filter by
     */
    private void loadBooks(String searchTerm, String categoryName) {
        try {
            List<Book> bookList;
            
            if (searchTerm == null || searchTerm.isEmpty()) {
                if (categoryName == null) {
                    bookList = bookDAO.getAllBooks();
                } else {
                    bookList = bookDAO.getBooksByCategory(categoryName);
                }
            } else {
                bookList = bookDAO.searchBooks(searchTerm, categoryName);
            }
            
            books.setAll(bookList);
            booksTable.setItems(books);
            
        } catch (SQLException e) {
            System.err.println("Error loading books: " + e.getMessage());
            
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load books: " + e.getMessage());
            alert.showAndWait();
            
            // Clear the table
            books.clear();
        }
    }
    
    /**
     * Edit a book
     * 
     * @param book The book to edit
     */
    private void editBook(Book book) {
        // In a real implementation, this would open a dialog to edit the book
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Book");
        alert.setHeaderText(null);
        alert.setContentText("Edit Book functionality will be implemented here.\nBook: " + book.getTitle());
        alert.showAndWait();
    }
    
    /**
     * View book details
     * 
     * @param book The book to view
     */
    private void viewBookDetails(Book book) {
        // In a real implementation, this would open a dialog showing detailed information
        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(book.getTitle()).append("\n");
        details.append("ISBN: ").append(book.getIsbn()).append("\n");
        details.append("Publication Year: ").append(book.getPublicationYear()).append("\n");
        
        if (book.getPublisher() != null) {
            details.append("Publisher: ").append(book.getPublisher().getName()).append("\n");
        }
        
        if (!book.getAuthors().isEmpty()) {
            details.append("Authors: ").append(book.getAuthors().stream()
                    .map(Author::getName)
                    .collect(Collectors.joining(", ")))
                    .append("\n");
        }
        
        if (!book.getCategories().isEmpty()) {
            details.append("Categories: ").append(book.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", ")))
                    .append("\n");
        }
        
        int availableCopies = bookCopyDAO.getAvailableCopiesCount(book.getId());
        int totalCopies = bookCopyDAO.getTotalCopiesCount(book.getId());
        details.append("Available Copies: ").append(availableCopies).append(" / ").append(totalCopies);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Book Details");
        alert.setHeaderText(book.getTitle());
        alert.setContentText(details.toString());
        alert.showAndWait();
    }
}
