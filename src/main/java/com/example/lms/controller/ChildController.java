package com.example.lms.controller;

import com.example.lms.model.User;

/**
 * Interface for controllers that can be loaded as children of a parent controller.
 * This allows parent controllers to pass data to child controllers.
 */
public interface ChildController {
    
    /**
     * Initialize the controller with user data.
     * 
     * @param user The current authenticated user
     */
    void initData(User user);
}
