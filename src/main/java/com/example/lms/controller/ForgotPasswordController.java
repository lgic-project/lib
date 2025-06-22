package com.example.lms.controller;

import com.example.lms.Main;
import com.example.lms.model.User;
import com.example.lms.model.UserDAO;
import com.example.lms.util.EmailUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Controller class for the forgot password screen.
 */
public class ForgotPasswordController {

    @FXML
    private TextField emailField;
    
    @FXML
    private Button sendTokenBtn;
    
    @FXML
    private Button backToLoginBtn;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Label successLabel;
    
    @FXML
    private ImageView illustrationImg;
    
    @FXML
    private VBox requestTokenForm;
    
    @FXML
    private VBox resetPasswordForm;
    
    @FXML
    private TextField tokenField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private Button resetPasswordBtn;
    
    private UserDAO userDAO;
    private EmailUtil emailUtil;
    
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
        emailUtil = new EmailUtil();
        
        // Initially show only the request token form
        requestTokenForm.setVisible(true);
        resetPasswordForm.setVisible(false);
        
        // Load SVG image
        try {
            InputStream is = getClass().getResourceAsStream("/com/example/lms/images/forgot-password-illustration.svg");
            if (is != null) {
                Image image = new Image(is);
                illustrationImg.setImage(image);
            } else {
                System.err.println("Could not load image: forgot-password-illustration.svg");
            }
        } catch (Exception e) {
            System.err.println("Error loading illustration: " + e.getMessage());
        }
    }
    
    /**
     * Handle send token button click.
     */
    @FXML
    private void onSendTokenClick() {
        String email = emailField.getText().trim();
        
        // Validate email
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            return;
        }
        
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }
        
        // Check if email exists in database
        try {
            if (!userDAO.emailExists(email)) {
                showError("No account found with this email address.");
                return;
            }
            
            // Generate and save reset token
            String resetToken = userDAO.generatePasswordResetToken(email);
            
            if (resetToken != null) {
                // Get user name for the email
                User user = userDAO.getUserByEmail(email);
                if (user == null) {
                    showError("Error retrieving user information.");
                    return;
                }
                
                // Send email with reset token and user name
                boolean emailSent = emailUtil.sendPasswordResetEmail(email, resetToken, user.getName());
                
                if (emailSent) {
                    // Show success message and reveal the reset password form
                    successLabel.setText("Reset code has been sent to your email address.");
                    successLabel.setVisible(true);
                    errorLabel.setVisible(false);
                    
                    // Switch to reset password form
                    requestTokenForm.setVisible(false);
                    resetPasswordForm.setVisible(true);
                } else {
                    showError("Failed to send reset email. Please try again later.");
                }
            } else {
                showError("Failed to generate reset token. Please try again later.");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle reset password button click.
     */
    @FXML
    private void onResetPasswordClick() {
        String email = emailField.getText().trim();
        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate inputs
        if (token.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        
        if (newPassword.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }
        
        // Attempt to reset password
        try {
            boolean success = userDAO.resetPassword(email, token, newPassword);
            
            if (success) {
                // Password reset successful
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Password Reset Successful");
                alert.setHeaderText(null);
                alert.setContentText("Your password has been reset successfully. You can now log in with your new password.");
                alert.showAndWait();
                
                // Navigate back to login
                onBackToLoginClick();
            } else {
                showError("Invalid or expired reset code. Please try again.");
            }
        } catch (Exception e) {
            showError("Error during password reset: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle back to login button click.
     */
    @FXML
    private void onBackToLoginClick() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            
            // Get current stage and set the new scene
            Stage stage = (Stage) backToLoginBtn.getScene().getWindow();
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
        successLabel.setVisible(false);
    }
}
