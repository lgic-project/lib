package com.example.lms.controller;

import com.example.lms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Controller for the book dialog form to add/edit books
 */
public class BookDialogController {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private ComboBox<Publisher> publisherComboBox;
    @FXML private TextField yearField;
    @FXML private TextField editionField;
    @FXML private TextField languageField;
    @FXML private TextField pagesField;
    @FXML private ListView<Category> categoriesListView;
    @FXML private Button addCategoryBtn;
    @FXML private Button removeCategoryBtn;
    @FXML private TextArea descriptionArea;
    @FXML private ImageView coverImageView;
    @FXML private Button uploadImageBtn;
    @FXML private Label imagePathLabel;

    private Book book;
    private PublisherDAO publisherDAO;
    private CategoryDAO categoryDAO;
    private ObservableList<Category> allCategories;
    private ObservableList<Category> selectedCategories;
    private String uploadedImagePath = null;
    private boolean isImageChanged = false;
    private BookDAO bookDAO;

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Initialize DAOs
        publisherDAO = new PublisherDAO();
        categoryDAO = new CategoryDAO();
        
        // Set up category list
        selectedCategories = FXCollections.observableArrayList();
        categoriesListView.setItems(selectedCategories);
        
        // Load publishers for dropdown
        loadPublishers();
        
        // Initialize the cover image view with a blank rectangular placeholder
        coverImageView.setStyle("-fx-background-color: lightgray;");
        
        // Add event handlers
        uploadImageBtn.setOnAction(event -> handleUploadImage());
        addCategoryBtn.setOnAction(event -> handleAddCategory());
        removeCategoryBtn.setOnAction(event -> handleRemoveCategory());
        
        // Basic input validation for numeric fields
        yearField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        pagesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                pagesField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * Populate the dialog with book data for editing
     * 
     * @param book The book to edit
     */
    public void setBook(Book book) {
        this.book = book;
        
        if (book != null) {
            // Fill form with book data
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthorName());
            isbnField.setText(book.getIsbn());
            yearField.setText(String.valueOf(book.getPublicationYear()));
            
            if (book.getEdition() != null) {
                editionField.setText(book.getEdition());
            }
            
            if (book.getLanguage() != null) {
                languageField.setText(book.getLanguage());
            } else {
                languageField.setText("English"); // Default
            }
            
            if (book.getPages() > 0) {
                pagesField.setText(String.valueOf(book.getPages()));
            }
            
            if (book.getDescription() != null) {
                descriptionArea.setText(book.getDescription());
            }
            
            // Set publisher
            for (Publisher publisher : publisherComboBox.getItems()) {
                if (publisher.getId() == book.getPublisherId()) {
                    publisherComboBox.setValue(publisher);
                    break;
                }
            }
            
            // Load categories
            selectedCategories.setAll(book.getCategories());
            
            // Show cover image if exists
            if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
                try {
                    String imagePath = "src/main/resources" + book.getCoverImageUrl();
                    File imageFile = new File(imagePath);
                    
                    if (imageFile.exists()) {
                        Image image = new Image(imageFile.toURI().toString());
                        coverImageView.setImage(image);
                        imagePathLabel.setText(book.getCoverImageUrl());
                        uploadedImagePath = imagePath;
                    } else {
                        // Load default "no image" image
                        Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/lms/images/no-image.png"));
                        coverImageView.setImage(defaultImage);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading cover image: " + e.getMessage());
                }
            }
        } else {
            // Creating a new book
            this.book = new Book();
            languageField.setText("English"); // Default language
            
            // Load default "no image" image
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/lms/images/no-image.png"));
                coverImageView.setImage(defaultImage);
            } catch (Exception e) {
                System.err.println("Error loading default image: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get the book with updated values from the form
     * 
     * @return The updated book object
     */
    public Book getBook() {
        if (this.book == null) {
            this.book = new Book();
            book.setCreatedAt(LocalDateTime.now());
        }
        
        // Update book with form values
        book.setTitle(titleField.getText().trim());
        book.setAuthorName(authorField.getText().trim());
        book.setIsbn(isbnField.getText().trim());
        
        // Handle numeric fields
        try {
            book.setPublicationYear(Integer.parseInt(yearField.getText().trim()));
        } catch (NumberFormatException e) {
            book.setPublicationYear(0);
        }
        
        try {
            book.setPages(!pagesField.getText().trim().isEmpty() ? 
                Integer.parseInt(pagesField.getText().trim()) : 0);
        } catch (NumberFormatException e) {
            book.setPages(0);
        }
        
        book.setEdition(editionField.getText().trim());
        book.setLanguage(languageField.getText().trim());
        book.setDescription(descriptionArea.getText().trim());
        
        // Set publisher
        Publisher selectedPublisher = publisherComboBox.getValue();
        if (selectedPublisher != null) {
            book.setPublisher(selectedPublisher);
        }
        
        // Set categories
        book.setCategories(selectedCategories);
        
        // Set cover image URL if changed
        if (isImageChanged && uploadedImagePath != null) {
            // Convert absolute path to relative URL format
            String relativePath = uploadedImagePath.replace("src/main/resources", "");
            book.setCoverImageUrl(relativePath);
        }
        
        book.setUpdatedAt(LocalDateTime.now());
        
        return book;
    }
    
    /**
     * Handle image upload
     */
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Cover Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) uploadImageBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Create unique filename based on timestamp and original name
                String originalFileName = selectedFile.getName();
                String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
                String newFileName = UUID.randomUUID().toString() + extension;
                
                // Define the target directory and ensure it exists
                Path targetDir = Paths.get("src/main/resources/com/example/lms/images/covers");
                Files.createDirectories(targetDir);
                
                // Define the target path
                Path targetPath = targetDir.resolve(newFileName);
                
                // Copy the file to our application's directory
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update UI
                Image image = new Image(targetPath.toUri().toString());
                coverImageView.setImage(image);
                
                // Save the path for later use
                uploadedImagePath = targetPath.toString().replace("\\", "/");
                imagePathLabel.setText(uploadedImagePath);
                isImageChanged = true;
                
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Image Upload Failed");
                alert.setContentText("Failed to upload image: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    /**
     * Handle adding a category to the book
     */
    private void handleAddCategory() {
        try {
            // Load all categories if not already loaded
            if (allCategories == null) {
                List<Category> categories = categoryDAO.getAllCategories();
                allCategories = FXCollections.observableArrayList(categories);
            }
            
            // Filter out already selected categories
            ObservableList<Category> availableCategories = FXCollections.observableArrayList();
            for (Category category : allCategories) {
                boolean alreadySelected = false;
                for (Category selected : selectedCategories) {
                    if (selected.getId() == category.getId()) {
                        alreadySelected = true;
                        break;
                    }
                }
                if (!alreadySelected) {
                    availableCategories.add(category);
                }
            }
            
            // Create a dialog to select a category
            ChoiceDialog<Category> dialog = new ChoiceDialog<>();
            dialog.getItems().addAll(availableCategories);
            dialog.setTitle("Add Category");
            dialog.setHeaderText("Select a category to add");
            dialog.setContentText("Category:");
            
            dialog.showAndWait().ifPresent(category -> {
                if (!selectedCategories.contains(category)) {
                    selectedCategories.add(category);
                }
            });
            
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load categories: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Handle removing a category from the book
     */
    private void handleRemoveCategory() {
        Category selectedCategory = categoriesListView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            selectedCategories.remove(selectedCategory);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a category to remove.");
            alert.showAndWait();
        }
    }
    
    /**
     * Load publishers for the combobox
     */
    private void loadPublishers() {
        try {
            List<Publisher> publishers = publisherDAO.getAllPublishers();
            publisherComboBox.setItems(FXCollections.observableArrayList(publishers));
            
            // Set cell factory to display publisher name
            publisherComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Publisher publisher, boolean empty) {
                    super.updateItem(publisher, empty);
                    if (empty || publisher == null) {
                        setText(null);
                    } else {
                        setText(publisher.getName());
                    }
                }
            });
            
            // Same for the button cell
            publisherComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Publisher publisher, boolean empty) {
                    super.updateItem(publisher, empty);
                    if (empty || publisher == null) {
                        setText(null);
                    } else {
                        setText(publisher.getName());
                    }
                }
            });
            
        } catch (SQLException e) {
            System.err.println("Error loading publishers: " + e.getMessage());
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load publishers: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Set the BookDAO instance to be used by this controller
     * 
     * @param bookDAO the BookDAO instance to use
     */
    public void setBookDAO(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }
    
    /**
     * Clean up resources when dialog is closed
     * Note: We don't close DAOs here as they might be used by the parent controller
     */
    public void close() {
        // Only clean up resources that are specific to this dialog
        // No need to close DAOs as they're managed by the parent controller
        // or will be garbage collected when no longer referenced
    }
}
