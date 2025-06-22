module com.example.lms {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.mail;

    // Open main package
    opens com.example.lms to javafx.fxml;
    exports com.example.lms;
    
    // Open controller package for FXML access
    opens com.example.lms.controller to javafx.fxml;
    exports com.example.lms.controller;
    
    // Open model and util packages
    opens com.example.lms.model to javafx.base;
    exports com.example.lms.model;
    exports com.example.lms.util;
}