package com.example.lms.controller;

import com.example.lms.Main;
import com.example.lms.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the user dashboard.
 * Displays books available for borrowing and books currently borrowed by the user.
 */
public class UserDashboardController implements DashboardController {

    @FXML
    private Label userNameLabel;
    
    @FXML
    private Button logoutBtn;
    
    private User currentUser;
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize any controls or load data here
    }
    
    /**
     * Sets the user data in the dashboard.
     * 
     * @param user The authenticated user
     */
    @Override
    public void initData(User user) {
        this.currentUser = user;
        userNameLabel.setText("Welcome, " + user.getName());
    }
    
    /**
     * Handles the logout button click.
     * Returns to the login screen.
     */
    @FXML
    private void onLogoutClick() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/login.fxml"));
            Scene scene = new Scene(loader.load(), 800, 600);
            
            // Get current stage and set the new scene
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setTitle("Library Management System - Login");
            stage.setScene(scene);
            
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
