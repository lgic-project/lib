package com.example.lms.controller;

import com.example.lms.Main;
import com.example.lms.model.Book;
import com.example.lms.model.BookDAO;
import com.example.lms.model.User;
import com.example.lms.util.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the librarian dashboard.
 * Provides functionality to manage books, issue and return books, and manage members.
 */
public class LibrarianDashboardController implements DashboardController, AutoCloseable {

    @FXML
    private Label userNameLabel;
    
    @FXML
    private Button logoutBtn;
    
    @FXML
    private TableView<Book> bookTableView;
    
    @FXML
    private TableColumn<Book, String> titleColumn;
    
    @FXML
    private TableColumn<Book, String> authorColumn;
    
    @FXML
    private TableColumn<Book, String> isbnColumn;
    
    @FXML
    private TableColumn<Book, String> categoryColumn;
    
    @FXML
    private TableColumn<Book, String> publisherColumn;
    
    @FXML
    private TableColumn<Book, Integer> availableColumn;
    
    @FXML
    private TableColumn<Book, String> actionsColumn;
    
    @FXML
    private TextField searchBookField;
    
    @FXML
    private Button addBookButton;
    
    private User currentUser;
    private BookDAO bookDAO;
    private ObservableList<Book> bookList;
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        try {
            // Initialize BookDAO
            Connection conn = Database.getConnection();
            bookDAO = new BookDAO();
            
            // Setup table columns
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));
            isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
            publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisherName"));
            availableColumn.setCellValueFactory(new PropertyValueFactory<>("availableCopies"));
            
            // Setup action buttons
            setupActionColumn();
            
            // Load books
            loadBooks();
            
            // Setup search functionality
            searchBookField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    searchBooks(newValue);
                } catch (SQLException e) {
                    showErrorAlert("Error searching books", e.getMessage());
                }
            });
            
            // Setup add book button
            addBookButton.setOnAction(this::handleAddBook);
            
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Could not connect to the database: " + e.getMessage());
        }
    }
    
    /**
     * Sets the user data in the dashboard.
     * 
     * @param user The authenticated librarian user
     */
    @Override
    public void initData(User user) {
        this.currentUser = user;
        userNameLabel.setText("Welcome, " + user.getName());
    }
    
    /**
     * Handles the logout button click.
     * Returns to the login screen.
     */
    @FXML
    private void onLogoutClick() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            
            // Get current stage and set the new scene
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setTitle("Library Management System - Login");
            stage.setScene(scene);
            
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * This would contain methods to handle book management,
     * such as adding, editing, and removing books from the inventory.
     * For now, this is just a placeholder.
     */
    /**
     * Loads books from the database and displays them in the table.
     */
    private void loadBooks() {
        try {
            List<Book> books = bookDAO.getAllBooks();
            bookList = FXCollections.observableArrayList(books);
            bookTableView.setItems(bookList);
        } catch (SQLException e) {
            showErrorAlert("Error Loading Books", "Could not load books from database: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the action column with view, edit, and delete buttons.
     */
    private void setupActionColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, deleteBtn);
            
            {
                viewBtn.getStyleClass().add("button-info");
                editBtn.getStyleClass().add("button-primary");
                deleteBtn.getStyleClass().add("button-danger");
                
                viewBtn.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    viewBookDetails(book);
                });
                
                editBtn.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    editBook(book);
                });
                
                deleteBtn.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    deleteBook(book);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }
    
    /**
     * Handles the add book button click.
     */
    private void handleAddBook(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/book-dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane((DialogPane) loader.load());
            dialog.setTitle("Add New Book");
            
            BookDialogController controller = loader.getController();
            controller.setBookDAO(bookDAO); // Set the BookDAO
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return buttonType;
                }
                return null;
            });
            
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    loadBooks(); // Refresh books list
                    showInfoAlert("Success", "Book added successfully!");
                }
            });
            
        } catch (IOException e) {
            showErrorAlert("Error", "Could not open book dialog: " + e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/book-details-dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane((DialogPane) loader.load());
            dialog.setTitle("Book Details");
            
            // Get the controller and set the book
            BookDetailsController controller = loader.getController();
            controller.setBook(book);
            
            // Show the dialog
            dialog.showAndWait();
            
        } catch (IOException e) {
            showErrorAlert("Error", "Could not open book details dialog: " + e.getMessage());
        }
    }
    
    /**
     * Edit book details
     */
    private void editBook(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/book-dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane((DialogPane) loader.load());
            dialog.setTitle("Edit Book");
            
            BookDialogController controller = loader.getController();
            controller.setBookDAO(bookDAO);
            controller.setBook(book); // Set existing book
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return buttonType;
                }
                return null;
            });
            
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    loadBooks(); // Refresh books list
                    showInfoAlert("Success", "Book updated successfully!");
                }
            });
            
        } catch (IOException e) {
            showErrorAlert("Error", "Could not open book dialog: " + e.getMessage());
        }
    }
    
    /**
     * Delete a book
     */
    private void deleteBook(Book book) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, 
                "Are you sure you want to delete the book '" + book.getTitle() + "'?", 
                ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Delete");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    bookDAO.deleteBook(book.getId());
                    loadBooks(); // Refresh books list
                    showInfoAlert("Success", "Book deleted successfully!");
                } catch (SQLException e) {
                    showErrorAlert("Error Deleting Book", e.getMessage());
                }
            }
        });
    }
    
    /**
     * Search for books by title, author, or ISBN
     */
    private void searchBooks(String searchTerm) throws SQLException {
        if (searchTerm == null || searchTerm.isBlank()) {
            loadBooks(); // If search is empty, load all books
            return;
        }
        
        List<Book> matchingBooks = bookDAO.searchBooks(searchTerm, ""); // Using empty string as second parameter
        bookList.clear();
        bookList.addAll(matchingBooks);
    }
    
    /**
     * Helper method to show error alerts
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Helper method to show information alerts
     */
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * This would contain methods to handle book issuance to members.
     * For now, this is just a placeholder.
     */
    private void issueBook() {
        // To be implemented
    }
    
    /**
     * This would contain methods to handle book returns from members.
     * For now, this is just a placeholder.
     */
    private void returnBook() {
        // To be implemented
    }
    
    /**
     * This would generate overdue book reports.
     * For now, this is just a placeholder.
     */
    private void generateOverdueReport() {
        // To be implemented
    }
    
    /**
     * Closes resources used by this controller.
     */
    @Override
    public void close() throws Exception {
        if (bookDAO != null) {
            try {
                bookDAO.close();
            } catch (SQLException e) {
                System.err.println("Error closing BookDAO: " + e.getMessage());
            }
        }
    }
}
