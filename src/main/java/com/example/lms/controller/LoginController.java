package com.example.lms.controller;

import com.example.lms.Main;
import com.example.lms.model.User;
import com.example.lms.model.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Controller class for the login screen.
 */
public class LoginController {

    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginBtn;
    
    @FXML
    private Button forgotPasswordBtn;
    
    @FXML
    private Button signupBtn;
    
    @FXML
    private CheckBox rememberMeCheck;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private ImageView illustrationImg;
    
    private UserDAO userDAO;
    
    /**
     * Initialize the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        
        // Load SVG image
        try {
            InputStream is = getClass().getResourceAsStream("/com/example/lms/images/library-illustration.svg");
            if (is != null) {
                Image image = new Image(is);
                illustrationImg.setImage(image);
            } else {
                System.err.println("Could not load image: library-illustration.svg");
            }
        } catch (Exception e) {
            System.err.println("Error loading illustration: " + e.getMessage());
        }
    }
    
    /**
     * Handle login button click.
     */
    @FXML
    private void onLoginClick() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Validate inputs
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }
        
        // Attempt authentication
        try {
            User user = userDAO.authenticate(email, password);
            
            if (user != null) {
                // Authentication successful
                navigateToDashboard(user);
            } else {
                // Authentication failed
                showError("Invalid email or password. Please try again.");
            }
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Navigate to appropriate dashboard based on user role.
     */
    private void navigateToDashboard(User user) {
        try {
            String dashboardPath;
            
            // Determine which dashboard to load based on user role
            switch (user.getRole()) {
                case admin:
                    dashboardPath = "views/admin-dashboard.fxml";
                    break;
                case librarian:
                    dashboardPath = "views/librarian-dashboard.fxml";
                    break;
                case user:
                default:
                    dashboardPath = "views/user-dashboard.fxml";
                    break;
            }
            
            // Load the dashboard
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(dashboardPath));
            Scene scene = new Scene(loader.load(), 1024, 768);
            
            // Get current stage and set the new scene
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setTitle("Library Management System - " + user.getRole().toString().toUpperCase());
            stage.setScene(scene);
            
            // Pass user data to the dashboard controller
            Object controller = loader.getController();
            if (controller instanceof DashboardController) {
                ((DashboardController) controller).initData(user);
            }
            
        } catch (IOException e) {
            showError("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle forgot password button click.
     */
    @FXML
    private void onForgotPasswordClick() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/forgot-password.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            
            // Get current stage and set the new scene
            Stage stage = (Stage) forgotPasswordBtn.getScene().getWindow();
            stage.setTitle("Library Management System - Forgot Password");
            stage.setScene(scene);
            
        } catch (IOException e) {
            showError("Error loading forgot password screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle signup button click.
     */
    @FXML
    private void onSignupClick() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/signup.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            
            // Get current stage and set the new scene
            Stage stage = (Stage) signupBtn.getScene().getWindow();
            stage.setTitle("Library Management System - Sign Up");
            stage.setScene(scene);
            
        } catch (IOException e) {
            showError("Error loading signup screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Show error message to the user.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
