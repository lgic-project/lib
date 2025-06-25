package com.example.lms.controller;

import com.example.lms.model.*;
import com.example.lms.util.AppSettings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the issue book dialog.
 * Allows librarian to search and select a member and book to issue.
 */
public class IssueBookDialogController {

    @FXML
    private TextField memberSearchField;
    
    @FXML
    private TableView<User> membersTableView;
    
    @FXML
    private TableColumn<User, Integer> memberIdColumn;
    
    @FXML
    private TableColumn<User, String> memberNameColumn;
    
    @FXML
    private TableColumn<User, String> memberEmailColumn;
    
    @FXML
    private TableColumn<User, String> memberPhoneColumn;
    
    @FXML
    private TableColumn<User, Button> memberSelectColumn;

    @FXML
    private TextField bookSearchField;
    
    @FXML
    private TableView<Book> booksTableView;
    
    @FXML
    private TableColumn<Book, Integer> bookIdColumn;
    
    @FXML
    private TableColumn<Book, String> bookTitleColumn;
    
    @FXML
    private TableColumn<Book, String> bookAuthorColumn;
    
    @FXML
    private TableColumn<Book, String> bookIsbnColumn;
    
    @FXML
    private TableColumn<Book, Integer> bookAvailableColumn;
    
    @FXML
    private TableColumn<Book, Button> bookSelectColumn;
    
    @FXML
    private Button issueBtn;

    private UserDAO userDAO;
    private BookDAO bookDAO;
    private BookCopyDAO bookCopyDAO;
    private BorrowingDAO borrowingDAO;
    
    private User selectedMember;
    private Book selectedBook;
    private BookCopy selectedBookCopy;
    private User currentUser; // The librarian issuing the book
    
    private ObservableList<User> membersData = FXCollections.observableArrayList();
    private ObservableList<Book> booksData = FXCollections.observableArrayList();

    /**
     * Initialize the controller.
     */
    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        bookDAO = new BookDAO();
        bookCopyDAO = new BookCopyDAO();
        borrowingDAO = new BorrowingDAO();
        
        // Set up member table columns
        memberIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        memberNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        memberEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        memberPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        memberSelectColumn.setCellFactory(param -> new TableCell<>() {
            private final Button selectButton = new Button("Select");
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                setGraphic(selectButton);
                selectButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    selectMember(user);
                });
            }
        });
        
        // Set up book table columns
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        bookAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("authorName"));
        bookIsbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        bookAvailableColumn.setCellValueFactory(cellData -> {
            try {
                int availableCopies = bookCopyDAO.getAvailableCopiesCount(cellData.getValue().getId());
                return new SimpleObjectProperty<>(availableCopies);
            } catch (Exception e) {
                return new SimpleObjectProperty<>(0);
            }
        });
        bookSelectColumn.setCellFactory(param -> new TableCell<>() {
            private final Button selectButton = new Button("Select");
            
            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                Book currentBook = getTableView().getItems().get(getIndex());
                try {
                    int availableCopies = bookCopyDAO.getAvailableCopiesCount(currentBook.getId());
                    selectButton.setDisable(availableCopies <= 0);
                } catch (Exception e) {
                    selectButton.setDisable(true);
                }
                
                setGraphic(selectButton);
                selectButton.setOnAction(event -> {
                    Book selectedBook = getTableView().getItems().get(getIndex());
                    selectBook(selectedBook);
                });
            }
        });
        
        // Bind data
        membersTableView.setItems(membersData);
        booksTableView.setItems(booksData);
        
        // Initial load
        loadMembers("");
        loadBooks("");
        
        // Set issue button initially disabled
        updateIssueButtonStatus();
    }

    /**
     * Set the current user (librarian).
     * 
     * @param currentUser The currently logged-in librarian
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    /**
     * Search for members based on the search field text.
     */
    @FXML
    private void searchMembers() {
        String searchText = memberSearchField.getText().trim();
        loadMembers(searchText);
    }
    
    /**
     * Search for books based on the search field text.
     */
    @FXML
    private void searchBooks() {
        String searchText = bookSearchField.getText().trim();
        loadBooks(searchText);
    }
    
    /**
     * Load members matching the search criteria.
     * 
     * @param searchText The search text to filter by
     */
    private void loadMembers(String searchText) {
        membersData.clear();
        try {
            List<User> members;
            if (searchText.isEmpty()) {
                // Only fetch users with role "user"
                members = userDAO.getUsersByRole("user");
            } else {
                // Search users with role "user"
                members = userDAO.searchUsers(searchText, "user");
            }
            membersData.addAll(members);
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Error loading members: " + e.getMessage());
        }
    }
    
    /**
     * Load books matching the search criteria.
     * 
     * @param searchText The search text to filter by
     */
    private void loadBooks(String searchText) {
        booksData.clear();
        try {
            List<Book> books;
            if (searchText.isEmpty()) {
                books = bookDAO.getAllBooks();
            } else {
                books = bookDAO.searchBooks(searchText, ""); // Empty string as the second parameter for category
            }
            
            // Only add books with available copies
            for (Book book : books) {
                try {
                    int availableCopies = bookCopyDAO.getAvailableCopiesCount(book.getId());
                    if (availableCopies > 0) {
                        booksData.add(book);
                    }
                } catch (Exception e) {
                    System.err.println("Error checking available copies: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Error loading books: " + e.getMessage());
        }
    }
    
    /**
     * Select a member and highlight the row.
     * 
     * @param member The member to select
     */
    private void selectMember(User member) {
        this.selectedMember = member;
        
        // Highlight the selected row
        membersTableView.getSelectionModel().select(member);
        
        // Show selection in an alert
        showInfoAlert("Member Selected", "Selected member: " + member.getName());
        
        // Enable/disable issue button
        updateIssueButtonStatus();
    }
    
    /**
     * Select a book and highlight the row.
     * 
     * @param book The book to select
     */
    private void selectBook(Book book) {
        this.selectedBook = book;
        
        // Get the first available copy of this book
        try {
            List<BookCopy> availableCopies = bookCopyDAO.getAvailableCopiesByBookId(book.getId());
            if (!availableCopies.isEmpty()) {
                this.selectedBookCopy = availableCopies.get(0);
                
                // Highlight the selected row
                booksTableView.getSelectionModel().select(book);
                
                // Show selection in an alert
                showInfoAlert("Book Selected", "Selected book: " + book.getTitle() + 
                        "\nAvailable Copy ID: " + selectedBookCopy.getId() + 
                        "\nShelf Location: " + selectedBookCopy.getShelfLocation());
                
                // Enable/disable issue button
                updateIssueButtonStatus();
            } else {
                showErrorAlert("No Copies Available", "There are no available copies of this book.");
                this.selectedBook = null;
                this.selectedBookCopy = null;
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Error getting available copies: " + e.getMessage());
            this.selectedBook = null;
            this.selectedBookCopy = null;
        }
    }
    
    /**
     * Update the issue button status based on selection.
     */
    private void updateIssueButtonStatus() {
        issueBtn.setDisable(selectedMember == null || selectedBookCopy == null);
    }
    
    /**
     * Issue the selected book to the selected member.
     */
    @FXML
    private void issueBook() {
        // Check if both member and book copy are selected
        if (selectedMember == null) {
            showErrorAlert("Error", "Please select a valid member first");
            return;
        }
        
        if (selectedBookCopy == null) {
            showErrorAlert("Error", "Please select a valid book first");
            return;
        }
        
        try {
            // Get the default borrowing period from settings
            int borrowingPeriod = AppSettings.getDefaultBorrowingPeriod();
            
            // Create a new borrowing record
            Borrowing borrowing = new Borrowing();
            borrowing.setBookCopyId(selectedBookCopy.getId());
            borrowing.setUserId(selectedMember.getId());
            borrowing.setBorrowDate(LocalDate.now());
            borrowing.setDueDate(LocalDate.now().plusDays(borrowingPeriod));
            borrowing.setIssuedBy(currentUser); // Current librarian as issuer
            
            // Update the book copy status to BORROWED
            selectedBookCopy.setStatus(BookCopy.Status.BORROWED);
            boolean copyStatusUpdated = bookCopyDAO.updateCopyStatus(selectedBookCopy.getId(), BookCopy.Status.BORROWED);
            
            if (!copyStatusUpdated) {
                showErrorAlert("Error", "Failed to update book copy status.");
                return;
            }
            
            // Save the borrowing record
            boolean success = borrowingDAO.createBorrowing(borrowing);
            
            if (success) {
                showInfoAlert("Success", "Book issued successfully!\n" +
                        "Title: " + selectedBook.getTitle() + "\n" +
                        "Member: " + selectedMember.getName() + "\n" +
                        "Due Date: " + borrowing.getDueDate());
                
                // Clear the selection and refresh data
                selectedMember = null;
                selectedBook = null;
                selectedBookCopy = null;
                memberSearchField.clear();
                bookSearchField.clear();
                
                // Refresh tables
                loadMembers("");
                loadBooks("");
                
                // Update button status
                updateIssueButtonStatus();
            } else {
                showErrorAlert("Error", "Failed to issue the book. Please try again.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Error issuing book: " + e.getMessage());
        } catch (NumberFormatException e) {
            showErrorAlert("Error", "Invalid default borrowing period in settings.");
        }
    }
    
    /**
     * Close all database connections.
     */
    public void close() {
        try {
            if (userDAO != null) userDAO.close();
            if (bookDAO != null) bookDAO.close();
            if (bookCopyDAO != null) bookCopyDAO.close();
            if (borrowingDAO != null) borrowingDAO.close();
        } catch (SQLException e) {
            System.err.println("Error closing DAO resources: " + e.getMessage());
        }
    }
    
    /**
     * Show an error alert.
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show an information alert.
     */
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
