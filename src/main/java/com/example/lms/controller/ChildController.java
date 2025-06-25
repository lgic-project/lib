package com.example.lms.controller;

import com.example.lms.model.User;

import java.sql.SQLException;

/**
 * Interface for child controllers in the admin dashboard.
 * Extends AutoCloseable to ensure proper resource management.
 */
public interface ChildController extends AutoCloseable {
    
    /**
     * Initialize the controller with user data.
     * 
     * @param user The current authenticated user
     */
    void initData(User user);
    
    /**
     * Default implementation of close method to make it optional for subclasses.
     * Controllers that use database resources should override this method.
     */
    @Override
    default void close() throws Exception {
        // Default implementation does nothing
        // Child classes should override this if they need to clean up resources
    }
}
