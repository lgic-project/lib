package com.example.lms.controller;

import com.example.lms.Main;
import com.example.lms.model.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the librarian issue books view.
 */
public class LibrarianIssueBooksController implements ChildController {
    
    @FXML
    private Button issueNewBookBtn;
    
    @FXML
    private TableView<Borrowing> issuedBooksTableView;
    
    @FXML
    private TableColumn<Borrowing, String> issuedBookTitleColumn;
    
    @FXML
    private TableColumn<Borrowing, String> issuedMemberNameColumn;
    
    @FXML
    private TableColumn<Borrowing, LocalDate> issuedDateColumn;
    
    @FXML
    private TableColumn<Borrowing, LocalDate> dueDateColumn;
    
    @FXML
    private TableColumn<Borrowing, String> issuedActionsColumn;
    
    private User currentUser;
    private BookDAO bookDAO;
    private BookCopyDAO bookCopyDAO;
    private UserDAO userDAO;
    private BorrowingDAO borrowingDAO;
    
    /**
     * Initializes the controller
     */
    public void initialize() {
        // Initialize DAOs
        bookDAO = new BookDAO();
        bookCopyDAO = new BookCopyDAO();
        userDAO = new UserDAO();
        borrowingDAO = new BorrowingDAO();

        // Setup issued books table columns
        issuedBookTitleColumn.setCellValueFactory(cellData -> {
            try {
                Book book = bookDAO.getBookById(bookCopyDAO.getBookCopyById(cellData.getValue().getBookCopy().getId()).getBookId());
                return new SimpleStringProperty(book != null ? book.getTitle() : "Unknown");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });

        issuedMemberNameColumn.setCellValueFactory(cellData -> {
            User user = cellData.getValue().getUser();
            return new SimpleStringProperty(user != null ? user.getName() : "Unknown");
        });

        issuedDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBorrowDate()));
        dueDateColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDueDate()));

        setupIssuedActionsColumn();

        // Load data
        loadIssuedBooks();

    }
    
    /**
     * Sets up the action column for the issued books table
     */
    private void setupIssuedActionsColumn() {
        issuedActionsColumn.setCellFactory(param -> new TableCell<Borrowing, String>() {
            private final Button returnButton = new Button("Return");
            
            {
                returnButton.getStyleClass().add("button-primary");
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                HBox hbox = new HBox(5);
                hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                hbox.getChildren().add(returnButton);
                
                Borrowing borrowing = getTableView().getItems().get(getIndex());
                returnButton.setOnAction(event -> handleReturnBook(borrowing));
                
                setGraphic(hbox);
            }
        });
    }
    
    /**
     * Handles returning a book
     * 
     * @param borrowing The borrowing record to process
     */
    private void handleReturnBook(Borrowing borrowing) {
        try {
            // Confirm return
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Return");
            confirmAlert.setHeaderText("Return Book");
            confirmAlert.setContentText("Are you sure you want to mark this book as returned?");
            
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Update the book copy status to AVAILABLE
                BookCopy bookCopy = borrowing.getBookCopy();
                if (bookCopy != null) {
                    bookCopy.setStatus(BookCopy.Status.AVAILABLE);
                    bookCopyDAO.updateCopyStatus(bookCopy.getId(), BookCopy.Status.AVAILABLE);
                }
                
                // Update the borrowing record (mark as returned)
                borrowing.setReturnDate(LocalDate.now());
                borrowing.setReturnedTo(currentUser); // Set the current librarian as who received the book
                borrowingDAO.updateBorrowing(borrowing);
                
                // Refresh data
                loadIssuedBooks();
                
                showInfoAlert("Success", "Book has been returned successfully.");
            }
        } catch (SQLException e) {
            showErrorAlert("Error", "Error returning book: " + e.getMessage());
        }
    }
    
    /**
     * Opens the issue book dialog
     */
    @FXML
    private void openIssueBookDialog() {
        try {
            // Load the dialog
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/issue-book-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            // Create the dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Issue New Book");
            
            // Get the controller and set the current user (librarian)
            IssueBookDialogController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            // Show the dialog
            dialog.showAndWait();
            
            // Refresh the issued books list
            loadIssuedBooks();
            
        } catch (IOException e) {
            showErrorAlert("Error", "Could not open issue book dialog: " + e.getMessage());
        }
    }
    
    /**
     * Loads issued books from the database
     */
    private void loadIssuedBooks() {
        try {
            // Get currently borrowed books (not yet returned)
            List<Borrowing> borrowings = borrowingDAO.getCurrentBorrowings();
            issuedBooksTableView.setItems(FXCollections.observableArrayList(borrowings));
        } catch (SQLException e) {
            showErrorAlert("Error", "Error loading issued books: " + e.getMessage());
        }
    }
    
    /**
     * Initialize the controller with user data (implementation of ChildController interface)
     * 
     * @param user The current authenticated user
     */
    @Override
    public void initData(User user) {
        this.currentUser = user;
    }
    
    /**
     * Set the current user (librarian)
     * 
     * @param user Current librarian user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    /**
     * Shows an error alert
     * 
     * @param title Alert title
     * @param message Alert message
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an info alert
     * 
     * @param title Alert title
     * @param message Alert message
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Close resources before switching views
     */
    @Override
    public void close() throws Exception {
        if (bookDAO != null) {
            bookDAO.close();
        }
        if (bookCopyDAO != null) {
            bookCopyDAO.close();
        }
        if (userDAO != null) {
            userDAO.close();
        }
        if (borrowingDAO != null) {
            borrowingDAO.close();
        }
    }
}
