package com.example.lms.controller;

import com.example.lms.Main;
import com.example.lms.model.User;
import com.example.lms.model.AppSettingDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the admin dashboard.
 * Provides system administration functionality including user management,
 * system settings, reports, and backup/restore operations.
 */
public class AdminDashboardController implements DashboardController {

    @FXML
    private Label userNameLabel;
    
    @FXML
    private Button logoutBtn;
    
    @FXML
    private Button dashboardBtn;
    
    @FXML
    private Button usersBtn;
    
    @FXML
    private Button booksBtn;
    
    // staffBtn removed as per user request
    
    @FXML
    private Button settingsBtn;
    
    @FXML
    private StackPane contentArea;
    
    @FXML
    private PieChart categoryChart;
    
    @FXML
    private BarChart<String, Number> monthlyChart;
    
    private User currentUser;
    private AppSettingDAO appSettingDAO;
    
    // Track the currently loaded view
    private String currentView = null;
    
    // Track the current child controller
    private ChildController currentChildController = null;
    
    /**
     * Initializes the controller.
     * This method is automatically called after the FXML has been loaded.
     */
    @FXML
    private void initialize() {
        appSettingDAO = new AppSettingDAO();
        
        // Initialize charts with dummy data for now
        initializeCharts();
        
        // Set dashboard as the default view
        loadDashboardView();
    }
    
    /**
     * Sets the user data in the dashboard.
     * 
     * @param user The authenticated admin user
     */
    @Override
    public void initData(User user) {
        this.currentUser = user;
        userNameLabel.setText("Welcome, " + user.getName());
        
        // Load actual system statistics when an admin logs in
        loadSystemStatistics();
    }
    
    /**
     * Initializes charts with placeholder data.
     * In a real implementation, this would use actual data from the database.
     */
    private void initializeCharts() {
        // This would be replaced with actual data in a real implementation
    }
    
    /**
     * Loads system statistics from the database.
     * In a real implementation, this would query the database for actual statistics.
     */
    private void loadSystemStatistics() {
        // This would be implemented to load real statistics
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
    
    /**
     * This would handle user management functions like adding, editing, or removing users.
     * For now, this is just a placeholder.
     */
    /**
     * Event handler for dashboard button click
     */
    @FXML
    private void onDashboardClick() {
        setActiveButton(dashboardBtn);
        loadDashboardView();
    }
    
    /**
     * Event handler for users button click
     */
    @FXML
    private void onUsersClick() {
        setActiveButton(usersBtn);
        loadView("admin-users.fxml");
    }
    
    /**
     * Event handler for books button click
     */
    @FXML
    private void onBooksClick() {
        setActiveButton(booksBtn);
        loadView("admin-books.fxml");
    }
    
    // Staff button click handler removed as the button no longer exists
    
    /**
     * Event handler for settings button click
     */
    @FXML
    private void onSettingsClick() {
        setActiveButton(settingsBtn);
        loadView("admin-settings.fxml");
    }
    
    /**
     * Loads the dashboard view into the content area
     */
    private void loadDashboardView() {
        setActiveButton(dashboardBtn);
        loadView("admin-home.fxml");
    }
    
    /**
     * Helper method to load a view into the content area
     * 
     * @param fxmlFile The FXML file to load
     */
    private void loadView(String fxmlFile) {
        try {
            // First close the current controller if it exists
            if (currentChildController != null) {
                try {
                    currentChildController.close();
                    System.out.println("Closed previous controller: " + currentChildController.getClass().getSimpleName());
                } catch (Exception e) {
                    System.err.println("Error closing previous controller: " + e.getMessage());
                    e.printStackTrace();
                }
                currentChildController = null;
            }
            
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/" + fxmlFile));
            Parent view = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
            // If the loaded view has a controller that implements ChildController
            Object controller = loader.getController();
            if (controller instanceof ChildController) {
                // Save reference to current child controller
                currentChildController = (ChildController) controller;
                currentChildController.initData(currentUser);
            }
            
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFile);
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the active button in the sidebar
     * 
     * @param activeButton The button to set as active
     */
    private void setActiveButton(Button activeButton) {
        // dashboardBtn.getStyleClass().add("sidebar-button");
        // Remove active class from all buttons
        dashboardBtn.getStyleClass().remove("sidebar-button-active");
        usersBtn.getStyleClass().remove("sidebar-button-active");
        booksBtn.getStyleClass().remove("sidebar-button-active");
        // staffBtn removed from here
        settingsBtn.getStyleClass().remove("sidebar-button-active");

        // Add active class to selected button
        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }
}
