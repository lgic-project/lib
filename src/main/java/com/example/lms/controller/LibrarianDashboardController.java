package com.example.lms.controller;

import com.example.lms.Main;
import com.example.lms.model.*;
import com.example.lms.model.BookCopy;
import com.example.lms.model.BookCopyDAO;
import com.example.lms.model.BookDAO;
import com.example.lms.model.Borrowing;
import com.example.lms.model.BorrowingDAO;
import com.example.lms.model.User;
import com.example.lms.model.UserDAO;
import com.example.lms.util.Database;
import com.example.lms.util.AppSettings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    private Button manageBooksBtn;
    
    @FXML
    private Button issueBooksSidebarBtn;
    
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
    private TableColumn<Book, Integer> copiesColumn;
    
    @FXML
    private TableColumn<Book, Integer> availableColumn;
    
    @FXML
    private TableColumn<Book, String> actionsColumn;
    
    @FXML
    private TabPane tabPane;
    
    @FXML
    private TextField searchBookField;
    
    @FXML
    private Button addBookButton;
    
    // Main container for dynamic content loading
    @FXML
    private BorderPane mainContainer;
    
    private User currentUser;
    private BookDAO bookDAO;
    private BookCopyDAO bookCopyDAO;
    private UserDAO userDAO;
    private BorrowingDAO borrowingDAO;
    
    // Current child controller for view switching
    private ChildController currentChildController;
    private ObservableList<Book> bookList;
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        try {
            // Initialize DAOs
            Connection conn = Database.getConnection();
            bookDAO = new BookDAO();
            bookCopyDAO = new BookCopyDAO();
            userDAO = new UserDAO();
            borrowingDAO = new BorrowingDAO();
            
            // Setup book table columns
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            authorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));
            isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
            categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
            publisherColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPublisherName()));
            
            // Add available copies column using BookCopyDAO
            copiesColumn.setCellValueFactory(cellData -> {
                int count = bookCopyDAO.getAvailableCopiesCount(cellData.getValue().getId());
                return new SimpleObjectProperty<>(count);
            });
            
            setupActionColumn();
            
            // Load data
            loadBooks();
            
            // Set up search functionality
            searchBookField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    searchBooks(newValue);
                } catch (SQLException e) {
                    showErrorAlert("Error searching books", e.getMessage());
                }
            });
            
            // Setup add book button
            addBookButton.setOnAction(this::handleAddBook);
            
            // Set up Issue Books sidebar button
            // Button is now wired via FXML onAction attribute
            
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
            System.err.println("Error loading books: " + e.getMessage());
            e.printStackTrace();
            // showErrorAlert("Error Loading Books", "Could not load books from database: " + e.getMessage());
        }
    }
    
    /**
     * Sets up the action column with view, edit, and delete buttons.
     */
    private void setupActionColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button copiesBtn = new Button("Copies");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, copiesBtn, deleteBtn);
            
            {
                viewBtn.getStyleClass().add("button-info");
                editBtn.getStyleClass().add("button-primary");
                copiesBtn.getStyleClass().add("button-secondary");
                deleteBtn.getStyleClass().add("button-danger");
                
                viewBtn.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    viewBookDetails(book);
                });
                
                editBtn.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    editBook(book);
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
     * Sets the active button in the sidebar
     * 
     * @param activeButton The button to set as active
     */
    private void setActiveButton(Button activeButton) {
        // Remove active class from all buttons
        manageBooksBtn.getStyleClass().remove("sidebar-button-active");
        issueBooksSidebarBtn.getStyleClass().remove("sidebar-button-active");
        
        // Add basic style to all buttons if they don't have it
        if (!manageBooksBtn.getStyleClass().contains("sidebar-button")) {
            manageBooksBtn.getStyleClass().add("sidebar-button");
        }
        if (!issueBooksSidebarBtn.getStyleClass().contains("sidebar-button")) {
            issueBooksSidebarBtn.getStyleClass().add("sidebar-button");
        }

        // Add active class to selected button
        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }
    
    /**
     * Opens the issue book dialog
     */
    @FXML
    private void openIssueBookDialog() throws IOException {
        // Load the dialog
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/issue-book-dialog.fxml"));
        DialogPane dialogPane = loader.load();
        
        // Create the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setDialogPane(dialogPane);
        dialog.setTitle("Issue Book");
        
        // Get the controller
        IssueBookDialogController controller = loader.getController();
        controller.setCurrentUser(currentUser);
        
        // Show the dialog and process result
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Refresh books view
            loadBooks();
        }
    }
     /**
     * Shows the Issue Books view by loading the librarian-issue-books.fxml into the main content area
     */
    @FXML
    private void showIssueBooksView() {
        try {
            // First close the current view/controller if applicable
            closeCurrentChildController();
            
            // Load the Issue Books view
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/librarian-issue-books.fxml"));
            Node issueBooks = loader.load();
            
            // Set the current child controller
            currentChildController = loader.getController();
            
            // Set the current user in the controller
            LibrarianIssueBooksController controller = loader.getController();
            controller.initData(currentUser);
            
            // Set the issue books view in the center of the main container
            mainContainer.setCenter(issueBooks);
            
            // Update active sidebar button
            setActiveButton(issueBooksSidebarBtn);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load Issue Books view", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Closes the current child controller if it exists
     */
    private void closeCurrentChildController() {
        if (currentChildController != null) {
            try {
                currentChildController.close();
                currentChildController = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Shows an alert dialog
     *
     * @param alertType The type of alert
     * @param title The alert title
     * @param header The alert header
     * @param content The alert content
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * This would generate overdue book reports.
     * For now, this is just a placeholder.
     */
    private void generateOverdueReport() {
        // To be implemented
    }
    
    /**
     * Shows the Manage Books view in the main content area
     */
    @FXML
    private void showManageBooksView() {
        try {
            // First close the current child controller if applicable
            closeCurrentChildController();
            
            // Reset the center of the main container to the default TabPane
            // This will show the Manage Books tab again
            mainContainer.setCenter(tabPane);
            
            // Refresh the books data
            loadBooks();
            
            // Update active sidebar button
            setActiveButton(manageBooksBtn);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load Manage Books view", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Closes resources used by this controller.
     */
    /**
     * Open the book copy management dialog for a book
     * 
     * @param book The book to manage copies for
     */
    private void manageBookCopies(Book book) {
        try {
            // Load the dialog
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/book-copy-dialog.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane((DialogPane) loader.load());
            dialog.setTitle("Manage Book Copies");
            
            // Get the controller and set the book and DAO
            BookCopyDialogController controller = loader.getController();
            controller.setBookCopyDAO(bookCopyDAO);
            controller.setBook(book);
            
            // Show the dialog
            dialog.showAndWait();
            
            // Refresh the book list to show updated copy counts
            loadBooks();
            
        } catch (IOException e) {
            showErrorAlert("Error", "Could not open book copies dialog: " + e.getMessage());
        }
    }
    
    @Override
    public void close() throws Exception {
        if (bookDAO != null) {
            try {
                bookDAO.close();
            } catch (SQLException e) {
                System.err.println("Error closing BookDAO: " + e.getMessage());
            }
        }
        
        if (bookCopyDAO != null) {
            try {
                bookCopyDAO.close();
            } catch (SQLException e) {
                System.err.println("Error closing BookCopyDAO: " + e.getMessage());
            }
        }
        
        if (userDAO != null) {
            try {
                userDAO.close();
            } catch (SQLException e) {
                System.err.println("Error closing UserDAO: " + e.getMessage());
            }
        }
        
        if (borrowingDAO != null) {
            try {
                borrowingDAO.close();
            } catch (SQLException e) {
                System.err.println("Error closing BorrowingDAO: " + e.getMessage());
            }
        }
    }
}
