package com.example.lms.controller;

import com.example.lms.Main;
import com.example.lms.model.User;
import com.example.lms.model.UserDAO;
import com.example.lms.util.SecurityUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Controller class for the signup screen.
 */
public class SignupController {

    @FXML
    private TextField nameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private ComboBox<String> roleComboBox;
    
    @FXML
    private Button registerBtn;
    
    @FXML
    private Button loginBtn;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ImageView illustrationImg;
    
    private UserDAO userDAO;
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    /**
     * Initialize the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        
        // Populate role dropdown
        roleComboBox.getItems().addAll("user", "librarian", "admin");
        roleComboBox.setValue("user"); // Default selection
        
        // Load SVG image
        try {
            InputStream is = getClass().getResourceAsStream("/com/example/lms/images/signup-illustration.svg");
            if (is != null) {
                Image image = new Image(is);
                illustrationImg.setImage(image);
            } else {
                System.err.println("Could not load image: signup-illustration.svg");
            }
        } catch (Exception e) {
            System.err.println("Error loading illustration: " + e.getMessage());
        }
    }
    
    /**
     * Handle register button click.
     */
    @FXML
    private void onRegisterClick() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();
        
        // Validate inputs
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        
        if (name.length() < 3) {
            showError("Name must be at least 3 characters.");
            return;
        }
        
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }
        
        // Create user object
        try {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password); // The DAO will hash this
            user.setRole(User.UserRole.valueOf(role));
            
            // Attempt to register user
            boolean success = userDAO.registerUser(user);
            
            if (success) {
                // Registration successful, show success message and redirect to login
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Registration Successful");
                alert.setHeaderText(null);
                alert.setContentText("Your account has been created successfully. You can now log in.");
                alert.showAndWait();
                
                onLoginClick(); // Redirect to login
            } else {
                showError("Registration failed. Email may already be in use.");
            }
        } catch (Exception e) {
            showError("Error during registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle login link button click.
     */
    @FXML
    private void onLoginClick() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            
            // Get current stage and set the new scene
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setTitle("Library Management System - Login");
            stage.setScene(scene);
            
        } catch (IOException e) {
            showError("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Validate email format.
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Show error message to the user.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
