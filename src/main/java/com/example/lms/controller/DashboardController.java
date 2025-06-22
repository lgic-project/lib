package com.example.lms.controller;

import com.example.lms.model.User;

/**
 * Interface for dashboard controllers.
 * All dashboard controllers should implement this interface to ensure
 * they have the necessary methods to handle user data and initialization.
 */
public interface DashboardController {
    
    /**
     * Initialize the controller with user data.
     * This method should be called after loading the dashboard FXML to pass the current user's data.
     *
     * @param user The authenticated user
     */
    void initData(User user);
}
