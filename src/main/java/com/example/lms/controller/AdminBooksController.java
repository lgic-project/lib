package com.example.lms.controller;

import com.example.lms.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;

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
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));
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
                    private final Button copiesBtn = new Button("Copies");
                    private final Button deleteBtn = new Button("Delete");
                    
                    {
                        editBtn.getStyleClass().add("button-small");
                        viewBtn.getStyleClass().add("button-small");
                        copiesBtn.getStyleClass().add("button-small");
                        copiesBtn.getStyleClass().add("button-secondary");
                        deleteBtn.getStyleClass().add("button-small");
                        deleteBtn.getStyleClass().add("button-danger");
                        
                        editBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            editBook(book);
                        });
                        
                        viewBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            viewBookDetails(book);
                        });
                        
                        copiesBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            manageBookCopies(book);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            Book book = getTableView().getItems().get(getIndex());
                            deleteBook(book);
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
                            hbox.getChildren().addAll(viewBtn, editBtn, copiesBtn, deleteBtn);
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
        try {
            // Load the book dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lms/views/book-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add Book");
            
            // Get the controller
            BookDialogController controller = loader.getController();
            controller.setBook(null); // Create new book
            
            // Show the dialog and process the result
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Book newBook = controller.getBook();
                    saveBook(newBook, true);
                }
                controller.close(); // Clean up resources
            });
            
        } catch (IOException e) {
            System.err.println("Error loading book dialog: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load book dialog: " + e.getMessage());
            alert.showAndWait();
        }
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
        try {
            // Load the book dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lms/views/book-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit Book");
            
            // Get the controller and set the book to edit
            BookDialogController controller = loader.getController();
            controller.setBook(book);
            
            // Show the dialog and process the result
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Book updatedBook = controller.getBook();
                    saveBook(updatedBook, false);
                }
                controller.close(); // Clean up resources
            });
            
        } catch (IOException e) {
            System.err.println("Error loading book dialog: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load book dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * View book details
     * 
     * @param book The book to view
     */
    private void viewBookDetails(Book book) {
        try {
            // Load the book details dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lms/views/book-details-dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane((DialogPane) loader.load());
            dialog.setTitle("Book Details");
            
            // Get the controller and set the book
            BookDetailsController controller = loader.getController();
            controller.setBook(book);
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (IOException e) {
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open book details dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Save a book (add new or update existing)
     * 
     * @param book The book to save
     * @param isNewBook Whether this is a new book or an existing one being updated
     */
    /**
     * Open book copy management dialog
     * 
     * @param book The book to manage copies for
     */
    private void manageBookCopies(Book book) {
        try {
            // Load the dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lms/views/book-copy-dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane((DialogPane) loader.load());
            dialog.setTitle("Manage Book Copies");
            
            // Get the controller and set the book and DAO
            BookCopyDialogController controller = loader.getController();
            controller.setBookCopyDAO(bookCopyDAO);
            controller.setBook(book);
            
            // Show the dialog
            dialog.showAndWait();
            
            // Refresh the book list to update copy counts
            loadBooks();
            
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not open book copies dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void saveBook(Book book, boolean isNewBook) {
        try {
            boolean success;
            String successMessage;
            
            if (isNewBook) {
                // Add new book
                success = bookDAO.addBook(book);
                successMessage = "Book added successfully!";
            } else {
                // Update existing book
                success = bookDAO.updateBook(book);
                successMessage = "Book updated successfully!";
            }
            
            if (success) {
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText(successMessage);
                alert.showAndWait();
                
                // Refresh the book list
                loadBooks();
                
            } else {
                // Show error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to save book. Please try again.");
                alert.showAndWait();
            }
            
        } catch (SQLException e) {
            System.err.println("Error saving book: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message with details
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to save book");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Delete a book
     * 
     * @param book The book to delete
     */
    private void deleteBook(Book book) {
        // Show confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
                "Are you sure you want to delete the book '" + book.getTitle() + "'?", 
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Delete");
        
        // Handle user response
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    // Check if book has copies that are borrowed
                    int copiesCount = bookCopyDAO.getTotalCopiesCount(book.getId());
                    int availableCopiesCount = bookCopyDAO.getAvailableCopiesCount(book.getId());
                    
                    if (copiesCount > availableCopiesCount) {
                        // Show error - cannot delete book with borrowed copies
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Cannot Delete");
                        alert.setHeaderText(null);
                        alert.setContentText("This book has copies that are currently borrowed. " +
                                "All copies must be returned before deletion.");
                        alert.showAndWait();
                    } else {
                        // Delete the book
                        boolean success = bookDAO.deleteBook(book.getId());
                        
                        if (success) {
                            // Show success message
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Book deleted successfully!");
                            alert.showAndWait();
                            
                            // Refresh the book list
                            loadBooks();
                        } else {
                            // Show error message
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to delete book. Please try again.");
                            alert.showAndWait();
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error deleting book: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Show error message with details
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Error");
                    alert.setHeaderText("Failed to delete book");
                    alert.setContentText("Error: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }
}
