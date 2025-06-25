package com.example.lms.controller;

import com.example.lms.model.Book;
import com.example.lms.model.BookCopy;
import com.example.lms.model.BookCopyDAO;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Book Copy management dialog
 */
public class BookCopyDialogController {
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label authorLabel;
    
    @FXML
    private Label isbnLabel;
    
    @FXML
    private Label idLabel;
    
    @FXML
    private Label totalCopiesLabel;
    
    @FXML
    private Label availableCopiesLabel;
    
    @FXML
    private Label checkedOutCopiesLabel;
    
    @FXML
    private Label damagedCopiesLabel;
    
    @FXML
    private TableView<BookCopy> copiesTableView;
    
    @FXML
    private TableColumn<BookCopy, String> copyNumberColumn;
    
    @FXML
    private TableColumn<BookCopy, String> statusColumn;
    
    @FXML
    private TableColumn<BookCopy, String> locationColumn;
    
    @FXML
    private TableColumn<BookCopy, LocalDate> dateColumn;
    
    @FXML
    private TableColumn<BookCopy, Void> actionsColumn;
    
    @FXML
    private ComboBox<BookCopy.Status> statusComboBox;
    
    @FXML
    private TextField locationField;
    
    @FXML
    private DatePicker acquisitionDatePicker;
    
    @FXML
    private TextArea notesArea;
    
    @FXML
    private Button addCopyButton;
    
    private Book book;
    private BookCopyDAO bookCopyDAO;
    private ObservableList<BookCopy> copiesList;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Initialize the status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(BookCopy.Status.values()));
        statusComboBox.getSelectionModel().select(BookCopy.Status.AVAILABLE);
        
        // Set up the date picker with current date by default
        acquisitionDatePicker.setValue(LocalDate.now());
        
        // Set up table columns
        copyNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCopyNumber()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().toString()));
        locationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));
        dateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAcquisitionDate()));
        
        // Set up the actions column
        setupActionsColumn();
    }
    
    /**
     * Set the book for this dialog and load its copies
     * 
     * @param book The book whose copies to manage
     */
    public void setBook(Book book) {
        this.book = book;
        
        // Update book information labels
        titleLabel.setText(book.getTitle());
        authorLabel.setText(book.getAuthorName());
        isbnLabel.setText(book.getIsbn());
        idLabel.setText(String.valueOf(book.getId()));
        
        // Load copies from the database
        loadBookCopies();
    }
    
    /**
     * Set the book copy DAO
     * 
     * @param bookCopyDAO The BookCopyDAO to use
     */
    public void setBookCopyDAO(BookCopyDAO bookCopyDAO) {
        this.bookCopyDAO = bookCopyDAO;
    }
    
    /**
     * Load book copies from database
     */
    private void loadBookCopies() {
        try {
            if (bookCopyDAO != null && book != null) {
                // Load copies
                List<BookCopy> copies = bookCopyDAO.getCopiesByBookId(book.getId());
                copiesList = FXCollections.observableArrayList(copies);
                copiesTableView.setItems(copiesList);
                
                // Update summary labels
                updateSummaryLabels();
            }
        } catch (SQLException e) {
            showErrorAlert("Error Loading Copies", "Could not load book copies: " + e.getMessage());
        }
    }
    
    /**
     * Update summary labels with current copy statistics
     */
    private void updateSummaryLabels() {
        try {
            if (bookCopyDAO != null && book != null) {
                int totalCopies = bookCopyDAO.getTotalCopiesCount(book.getId());
                int availableCopies = bookCopyDAO.getAvailableCopiesCount(book.getId());
                
                // Count copies by status
                Map<BookCopy.Status, Integer> statusCounts = new HashMap<>();
                for (BookCopy copy : copiesList) {
                    BookCopy.Status status = copy.getStatus();
                    statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
                }
                
                // Update labels
                totalCopiesLabel.setText(String.valueOf(totalCopies));
                availableCopiesLabel.setText(String.valueOf(availableCopies));
                checkedOutCopiesLabel.setText(String.valueOf(statusCounts.getOrDefault(BookCopy.Status.DAMAGED, 0)));
                damagedCopiesLabel.setText(String.valueOf(
                        statusCounts.getOrDefault(BookCopy.Status.DAMAGED, 0) + 
                        statusCounts.getOrDefault(BookCopy.Status.LOST, 0)));
            }
        } catch (Exception e) {
            System.err.println("Error updating summary labels: " + e.getMessage());
        }
    }
    
    /**
     * Set up the actions column with edit and delete buttons
     */
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);
            
            {
                editBtn.getStyleClass().add("button-primary");
                editBtn.setOnAction(event -> {
                    BookCopy copy = getTableView().getItems().get(getIndex());
                    editBookCopy(copy);
                });
                
                deleteBtn.getStyleClass().add("button-danger");
                deleteBtn.setOnAction(event -> {
                    BookCopy copy = getTableView().getItems().get(getIndex());
                    deleteBookCopy(copy);
                });
                
                // Disable buttons based on copy status
                editBtn.disableProperty().bind(new SimpleObjectProperty<Boolean>() {
                    @Override
                    public Boolean get() {
                        if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            BookCopy copy = getTableView().getItems().get(getIndex());
                            return copy.getStatus() == BookCopy.Status.DAMAGED;
                        }
                        return true;
                    }
                });
                
                deleteBtn.disableProperty().bind(new SimpleObjectProperty<Boolean>() {
                    @Override
                    public Boolean get() {
                        if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            BookCopy copy = getTableView().getItems().get(getIndex());
                            return copy.getStatus() == BookCopy.Status.DAMAGED;
                        }
                        return true;
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
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
     * Handle adding a new book copy
     */
    @FXML
    private void handleAddCopy(ActionEvent event) {
        try {
            // Create new book copy
            BookCopy newCopy = new BookCopy();
            newCopy.setBook(book);
            newCopy.setStatus(statusComboBox.getValue());
            newCopy.setLocation(locationField.getText());
            newCopy.setAcquisitionDate(acquisitionDatePicker.getValue());
            newCopy.setNotes(notesArea.getText());
            
            // Add to database
            boolean success = bookCopyDAO.addBookCopy(newCopy);
            
            if (success) {
                // Clear form fields
                locationField.clear();
                acquisitionDatePicker.setValue(LocalDate.now());
                notesArea.clear();
                
                // Reload copies
                loadBookCopies();
                
                showInfoAlert("Success", "Book copy added successfully.");
            } else {
                showErrorAlert("Error", "Failed to add book copy.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Could not add book copy: " + e.getMessage());
        }
    }
    
    /**
     * Edit an existing book copy
     */
    private void editBookCopy(BookCopy copy) {
        try {
            // Create a dialog for editing
            Dialog<BookCopy> dialog = new Dialog<>();
            dialog.setTitle("Edit Book Copy");
            dialog.setHeaderText("Edit Copy #" + copy.getCopyNumber());
            
            // Set the button types
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Create the form fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
            
            ComboBox<BookCopy.Status> statusField = new ComboBox<>();
            statusField.setItems(FXCollections.observableArrayList(BookCopy.Status.values()));
            statusField.setValue(copy.getStatus());
            
            TextField locationField = new TextField(copy.getLocation());
            TextArea notesField = new TextArea(copy.getNotes());
            
            grid.add(new Label("Status:"), 0, 0);
            grid.add(statusField, 1, 0);
            grid.add(new Label("Location:"), 0, 1);
            grid.add(locationField, 1, 1);
            grid.add(new Label("Notes:"), 0, 2);
            grid.add(notesField, 1, 2);
            
            dialog.getDialogPane().setContent(grid);
            
            // Convert the result when the save button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    copy.setStatus(statusField.getValue());
                    copy.setLocation(locationField.getText());
                    copy.setNotes(notesField.getText());
                    return copy;
                }
                return null;
            });
            
            // Show the dialog and process the result
            dialog.showAndWait().ifPresent(result -> {
                try {
                    boolean success = bookCopyDAO.updateBookCopy(result);
                    if (success) {
                        loadBookCopies();
                        showInfoAlert("Success", "Book copy updated successfully.");
                    } else {
                        showErrorAlert("Error", "Failed to update book copy.");
                    }
                } catch (SQLException e) {
                    showErrorAlert("Database Error", "Could not update book copy: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            showErrorAlert("Error", "Could not edit book copy: " + e.getMessage());
        }
    }
    
    /**
     * Delete a book copy
     */
    private void deleteBookCopy(BookCopy copy) {
        // Confirm delete
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Book Copy");
        confirm.setContentText("Are you sure you want to delete copy #" + copy.getCopyNumber() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean success = bookCopyDAO.deleteBookCopy(copy.getId());
                    
                    if (success) {
                        loadBookCopies();
                        showInfoAlert("Success", "Book copy deleted successfully.");
                    } else {
                        showErrorAlert("Error", "Failed to delete book copy. It may be currently checked out.");
                    }
                } catch (SQLException e) {
                    showErrorAlert("Database Error", "Could not delete book copy: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Show an information alert
     */
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Show an error alert
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
