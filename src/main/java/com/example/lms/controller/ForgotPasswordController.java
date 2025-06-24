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
    private Label statusLabel;
    
    @FXML
    private Label resetStatusLabel;
    
    @FXML
    private ImageView illustrationImg;
    
    @FXML
    private VBox emailFormContainer;
    
    @FXML
    private VBox resetFormContainer;
    
    @FXML
    private TextField resetTokenField;
    
    @FXML
    private PasswordField newPasswordField;
    
    @FXML
    private PasswordField confirmNewPasswordField;
    
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
        
        // Initially show only the email form
        emailFormContainer.setVisible(true);
        resetFormContainer.setVisible(false);
        
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
                    // Switch to reset password form first
                    emailFormContainer.setVisible(false);
                    resetFormContainer.setVisible(true);
                    
                    // Show success message on the reset form
                    showResetSuccess("Reset code has been sent to your email address. Please check your inbox and enter the code above.");
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
        System.out.println("Reset password button clicked");
        String email = emailField.getText().trim();
        String token = resetTokenField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmNewPasswordField.getText();
        
        System.out.println("Email: " + email);
        System.out.println("Token: " + token);
        System.out.println("New password length: " + newPassword.length());
        
        // Clear previous error messages
        resetStatusLabel.setVisible(false);
        
        // Validate inputs
        if (token.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showResetError("All fields are required.");
            return;
        }
        
        if (newPassword.length() < 6) {
            showResetError("Password must be at least 6 characters.");
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showResetError("Passwords do not match.");
            return;
        }
        
        // Attempt to reset password
        try {
            System.out.println("Attempting to reset password with userDAO");
            boolean success = userDAO.resetPassword(email, token, newPassword);
            System.out.println("Reset password result: " + success);
            
            if (success) {
                // Password reset successful
                showResetSuccess("Password reset successful!");
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Password Reset Successful");
                alert.setHeaderText(null);
                alert.setContentText("Your password has been reset successfully. You can now log in with your new password.");
                alert.showAndWait();
                
                // Navigate back to login
                onBackToLoginClick();
            } else {
                showResetError("Invalid or expired reset code. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Exception during password reset: " + e.getMessage());
            e.printStackTrace();
            showResetError("Error during password reset: " + e.getMessage());
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
     * Handle back to email form button click.
     * Returns from the reset password form to the email form.
     */
    @FXML
    private void onBackToEmailFormClick() {
        // Switch back to the email form
        emailFormContainer.setVisible(true);
        resetFormContainer.setVisible(false);
        
        // Clear any error or success messages
        statusLabel.setVisible(false);
    }
    
    /**
     * Validate email format.
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Show error message to the user on the email form.
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add("error-label");
    }
    
    /**
     * Show error message to the user on the reset password form.
     */
    private void showResetError(String message) {
        resetStatusLabel.setText(message);
        resetStatusLabel.setVisible(true);
        resetStatusLabel.getStyleClass().clear();
        resetStatusLabel.getStyleClass().add("error-label");
    }
    
    /**
     * Show success message to the user on the reset password form.
     */
    private void showResetSuccess(String message) {
        resetStatusLabel.setText(message);
        resetStatusLabel.setVisible(true);
        resetStatusLabel.getStyleClass().clear();
        resetStatusLabel.getStyleClass().add("success-label");
    }
}
