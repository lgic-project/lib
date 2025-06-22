package com.example.lms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the Library Management System.
 * This is the entry point of the application.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Load the login screen first
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Library Management System - Login");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        // Start the JavaFX application
        launch();
    }
}
