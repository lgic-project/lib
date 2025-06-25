package com.example.lms.controller;

import com.example.lms.model.Book;
import com.example.lms.model.Category;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.File;

/**
 * Controller for the book details dialog.
 * Shared between admin and librarian dashboards for viewing book details.
 */
public class BookDetailsController {

    @FXML
    private Label titleLabel;
    
    @FXML
    private ImageView coverImageView;
    
    @FXML
    private TextArea detailsArea;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Default initialization
    }
    
    /**
     * Set the book to display details for
     * 
     * @param book The book to display details for
     */
    public void setBook(Book book) {
        if (book == null) {
            return;
        }
        
        // Set the title
        titleLabel.setText(book.getTitle());
        
        // Display book cover image if available
        if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
            try {
                // First try loading as a resource
                String coverPath = book.getCoverImageUrl();
                
                // Try to load image directly from the file path
                try {
                    File imageFile = new File(coverPath);
                    if (imageFile.exists()) {
                        Image coverImage = new Image(imageFile.toURI().toString());
                        coverImageView.setImage(coverImage);
                        System.out.println("Loaded image from file: " + coverPath);
                    } else {
                        // If not a direct file path, try as a resource path
                        Image coverImage = new Image(getClass().getResourceAsStream(coverPath));
                        if (coverImage != null && !coverImage.isError()) {
                            coverImageView.setImage(coverImage);
                            System.out.println("Loaded image from resource: " + coverPath);
                        } else {
                            // Try with leading slash if needed
                            String adjustedPath = coverPath.startsWith("/") ? coverPath : "/" + coverPath;
                            coverImage = new Image(getClass().getResourceAsStream(adjustedPath));
                            if (coverImage != null && !coverImage.isError()) {
                                coverImageView.setImage(coverImage);
                                System.out.println("Loaded image from resource with adjusted path: " + adjustedPath);
                            } else {
                                setImagePlaceholder();
                                System.out.println("Could not load image, using placeholder: " + coverPath);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("First loading attempt failed: " + e.getMessage());
                    
                    // If that fails, try as an absolute URL
                    try {
                        Image coverImage = new Image(coverPath);
                        if (!coverImage.isError()) {
                            coverImageView.setImage(coverImage);
                            System.out.println("Loaded image as URL: " + coverPath);
                        } else {
                            setImagePlaceholder();
                            System.out.println("Could not load image, using placeholder: " + coverPath);
                        }
                    } catch (Exception e2) {
                        setImagePlaceholder();
                        System.out.println("All image loading attempts failed: " + coverPath);
                    }
                }
            } catch (Exception e) {
                // Set placeholder if loading fails
                setImagePlaceholder();
                System.err.println("Error loading book cover image: " + e.getMessage());
            }
        } else {
            // Set placeholder if no image path
            setImagePlaceholder();
        }
        
        // Build the book details text
        StringBuilder details = new StringBuilder();
        details.append("Title: ").append(book.getTitle()).append("\n\n");
        details.append("ISBN: ").append(book.getIsbn()).append("\n\n");
        details.append("Author: ").append(book.getAuthorName()).append("\n\n");
        
        if (book.getPublisher() != null) {
            details.append("Publisher: ").append(book.getPublisher().getName()).append("\n\n");
        } else {
            details.append("Publisher: Unknown\n\n");
        }
        
        details.append("Publication Year: ").append(book.getPublicationYear()).append("\n\n");
        
        if (book.getEdition() != null && !book.getEdition().isEmpty()) {
            details.append("Edition: ").append(book.getEdition()).append("\n\n");
        }
        
        if (book.getLanguage() != null && !book.getLanguage().isEmpty()) {
            details.append("Language: ").append(book.getLanguage()).append("\n\n");
        }
        
        if (book.getPages() > 0) {
            details.append("Pages: ").append(book.getPages()).append("\n\n");
        }
        
        // Add categories
        if (book.getCategories() != null && !book.getCategories().isEmpty()) {
            details.append("Categories: ");
            for (int i = 0; i < book.getCategories().size(); i++) {
                Category category = book.getCategories().get(i);
                if (category != null) {
                    details.append(category.getName());
                    if (i < book.getCategories().size() - 1) {
                        details.append(", ");
                    }
                }
            }
            details.append("\n\n");
        }
        
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            details.append("Description:\n").append(book.getDescription());
        }
        
        // Set the details text
        detailsArea.setText(details.toString());
    }
    
    /**
     * Sets a placeholder for the image view when no image is available
     */
    private void setImagePlaceholder() {
        // Use a gray background as placeholder
        coverImageView.setImage(null);
        coverImageView.setStyle("-fx-background-color: lightgray;");
    }
}
