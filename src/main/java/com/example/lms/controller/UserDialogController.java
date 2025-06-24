package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.model.User.UserRole;
import com.example.lms.model.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Controller for the add/edit user dialog.
 */
public class UserDialogController {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Label passwordNote;
    
    @FXML
    private ComboBox<String> roleComboBox;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private User user;
    private UserDAO userDAO;
    private boolean editMode = false;
    private boolean success = false;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        
        // Set up role options
        roleComboBox.setItems(FXCollections.observableArrayList(
            "admin", "librarian", "user"
        ));
        roleComboBox.getSelectionModel().select("user");
        
        // Clear error message initially
        errorLabel.setText("");
        
        // Hide password note initially (shown only in edit mode)
        passwordNote.setVisible(false);
    }
    
    /**
     * Set up the dialog for adding a new user
     */
    public void setupAddMode() {
        editMode = false;
        titleLabel.setText("Add New User");
        saveButton.setText("Add User");
        user = new User(); // Create new empty user
    }
    
    /**
     * Set up the dialog for editing an existing user
     * 
     * @param existingUser The user to edit
     */
    public void setupEditMode(User existingUser) {
        if (existingUser == null) {
            throw new IllegalArgumentException("User cannot be null in edit mode");
        }
        
        editMode = true;
        user = existingUser;
        titleLabel.setText("Edit User");
        saveButton.setText("Update User");
        
        // Populate fields with existing data
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        roleComboBox.getSelectionModel().select(user.getRole().toString());
        
        // Show password note in edit mode
        passwordNote.setVisible(true);
        
        // Clear password field since we don't want to display the hashed password
        passwordField.clear();
    }
    
    /**
     * Handle save button click
     */
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            // Populate user object from form fields
            user.setName(nameField.getText() != null ? nameField.getText().trim() : "");
            user.setEmail(emailField.getText() != null ? emailField.getText().trim() : "");
            user.setPhone(phoneField.getText() != null ? phoneField.getText().trim() : "");
            
            // Set password only if provided (for edit mode)
            String password = passwordField.getText() != null ? passwordField.getText() : "";
            if (!password.isEmpty()) {
                user.setPassword(password);
            }
            
            // Set role
            String roleStr = roleComboBox.getValue();
            user.setRole(UserRole.valueOf(roleStr));
            
            // Save to database
            if (editMode) {
                success = userDAO.updateUser(user);
            } else {
                // For new users, password is required
                if (password.isEmpty()) {
                    errorLabel.setText("Password is required for new users");
                    return;
                }
                success = userDAO.registerUser(user);
            }
            
            if (success) {
                closeDialog();
            } else {
                errorLabel.setText("Failed to save user. Please try again.");
            }
            
        } catch (SQLException e) {
            errorLabel.setText("Database error: " + e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
    
    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }
    
    /**
     * Validate form input
     * 
     * @return true if input is valid, false otherwise
     */
    private boolean validateInput() {
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String role = roleComboBox.getValue();
        
        // Reset error message
        errorLabel.setText("");
        
        // Validate name
        if (name.isEmpty()) {
            errorLabel.setText("Name is required");
            return false;
        }
        
        // Validate email
        if (email.isEmpty()) {
            errorLabel.setText("Email is required");
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Email format is invalid");
            return false;
        }
        
        // Validate role selection
        if (role == null) {
            errorLabel.setText("Please select a role");
            return false;
        }
        
        // Check if email exists (only for new users)
        if (!editMode) {
            try {
                if (userDAO.emailExists(email)) {
                    errorLabel.setText("Email already exists");
                    return false;
                }
            } catch (Exception e) {
                errorLabel.setText("Error checking email: " + e.getMessage());
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Close the dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Check if the operation was successful
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccessful() {
        return success;
    }
    
    /**
     * Get the user being edited or created
     * 
     * @return User object
     */
    public User getUser() {
        return user;
    }
}
