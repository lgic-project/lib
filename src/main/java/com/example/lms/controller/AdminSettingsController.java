package com.example.lms.controller;

import com.example.lms.model.AppSetting;
import com.example.lms.model.AppSettingDAO;
import com.example.lms.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the admin settings view
 */
public class AdminSettingsController implements ChildController {

    @FXML
    private Button addSettingBtn;
    
    @FXML
    private Button backupBtn;
    
    @FXML
    private Button restoreBtn;
    
    @FXML
    private TableView<AppSetting> settingsTable;
    
    @FXML
    private TableColumn<AppSetting, String> settingKeyColumn;
    
    @FXML
    private TableColumn<AppSetting, String> settingValueColumn;
    
    @FXML
    private TableColumn<AppSetting, Void> actionsColumn;
    
    private User currentUser;
    private AppSettingDAO appSettingDAO;
    private ObservableList<AppSetting> settings = FXCollections.observableArrayList();
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        appSettingDAO = new AppSettingDAO();
        
        // Set up table columns
        settingKeyColumn.setCellValueFactory(new PropertyValueFactory<>("settingKey"));
        settingValueColumn.setCellValueFactory(cellData -> {
            String value = cellData.getValue().getSettingValue();
            String key = cellData.getValue().getSettingKey();
            
            // Mask sensitive values like passwords
            if (key.toLowerCase().contains("password") || key.toLowerCase().contains("secret") || 
                    key.toLowerCase().contains("key")) {
                value = "••••••••";
            }
            
            return new SimpleStringProperty(value);
        });
        
        // Set up action buttons
        setupActionButtons();
        
        // Load initial settings data
        loadSettings();
    }
    
    /**
     * Initialize the controller with user data
     * @param user The current user
     */
    @Override
    public void initData(User user) {
        this.currentUser = user;
    }
    
    /**
     * Set up action buttons in the table
     */
    private void setupActionButtons() {
        Callback<TableColumn<AppSetting, Void>, TableCell<AppSetting, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<AppSetting, Void> call(final TableColumn<AppSetting, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    
                    {
                        editBtn.getStyleClass().add("button-small");
                        deleteBtn.getStyleClass().add("button-small");
                        deleteBtn.getStyleClass().add("button-danger");
                        
                        editBtn.setOnAction(event -> {
                            AppSetting setting = getTableView().getItems().get(getIndex());
                            editSetting(setting);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            AppSetting setting = getTableView().getItems().get(getIndex());
                            deleteSetting(setting);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            ButtonBar buttonBar = new ButtonBar();
                            buttonBar.getButtons().addAll(editBtn, deleteBtn);
                            setGraphic(buttonBar);
                        }
                    }
                };
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    /**
     * Load settings into the table
     */
    private void loadSettings() {
        List<AppSetting> settingsList = appSettingDAO.getAllAppSettings();
        settings.setAll(settingsList);
        settingsTable.setItems(settings);
    }

    
    /**
     * Event handler for add setting button
     */
    @FXML
    private void onAddSettingClick() {
        // Create dialog for adding a new setting
        Dialog<AppSetting> dialog = new Dialog<>();
        dialog.setTitle("Add New Setting");
        dialog.setHeaderText("Enter the setting details:");
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create form fields
        TextField keyField = new TextField();
        keyField.setPromptText("Setting Key");
        TextField valueField = new TextField();
        valueField.setPromptText("Setting Value");
        
        // Create grid for form layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Key:"), 0, 0);
        grid.add(keyField, 1, 0);
        grid.add(new Label("Value:"), 0, 1);
        grid.add(valueField, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Focus on key field by default
        keyField.requestFocus();
        
        // Convert result to setting object when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String key = keyField.getText().trim();
                String value = valueField.getText().trim();
                
                if (key.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Setting key cannot be empty.");
                    alert.showAndWait();
                    return null;
                }
                
                AppSetting newSetting = new AppSetting();
                newSetting.setSettingKey(key);
                newSetting.setSettingValue(value);
                return newSetting;
            }
            return null;
        });
        
        Optional<AppSetting> result = dialog.showAndWait();
        
        result.ifPresent(setting -> {
            boolean added = appSettingDAO.addSetting(setting);
            
            if (added) {
                // Reload settings
                loadSettings();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Setting added successfully.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to add setting. The key may already exist.");
                alert.showAndWait();
            }
        });
    }
    
    /**
     * Edit a setting
     * 
     * @param setting The setting to edit
     */
    private void editSetting(AppSetting setting) {
        // Create dialog for editing the setting
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Edit Setting");
        dialog.setHeaderText("Edit value for: " + setting.getSettingKey());
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create form fields
        TextField valueField = new TextField(setting.getSettingValue());
        
        // Create grid for form layout
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Value:"), 0, 0);
        grid.add(valueField, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Focus on value field by default
        valueField.requestFocus();
        
        // Convert result to setting value when save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return valueField.getText().trim();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(value -> {
            setting.setSettingValue(value);
            boolean updated = appSettingDAO.updateSetting(setting);
            
            if (updated) {
                // Reload settings
                loadSettings();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Setting updated successfully.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to update setting.");
                alert.showAndWait();
            }
        });
    }
    
    /**
     * Delete a setting
     * 
     * @param setting The setting to delete
     */
    private void deleteSetting(AppSetting setting) {
        // Ask for confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Setting");
        confirmation.setHeaderText("Delete Setting: " + setting.getSettingKey());
        confirmation.setContentText("Are you sure you want to delete this setting? This action cannot be undone.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean deleted = appSettingDAO.deleteSetting(setting.getSettingKey());
                
                if (deleted) {
                    settings.remove(setting);
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Setting Deleted");
                    success.setHeaderText(null);
                    success.setContentText("Setting has been successfully deleted.");
                    success.showAndWait();
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText(null);
                    error.setContentText("Failed to delete setting.");
                    error.showAndWait();
                }
            }
        });
    }
    
    /**
     * Event handler for backup database button
     */
    @FXML
    private void onBackupClick() {
        // Show directory chooser dialog
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Backup Directory");
        File selectedDirectory = directoryChooser.showDialog(backupBtn.getScene().getWindow());
        
        if (selectedDirectory != null) {
            try {
                // Generate backup filename with timestamp
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String timestamp = dateFormat.format(new Date());
                String backupFilename = "lms_backup_" + timestamp + ".sql";
                String backupPath = selectedDirectory.getAbsolutePath() + File.separator + backupFilename;
                
                // Execute backup (this would typically call a database utility)
                boolean success = performDatabaseBackup(backupPath);
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Backup Complete");
                    alert.setHeaderText(null);
                    alert.setContentText("Database backup completed successfully.\nBackup saved to: " + backupPath);
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Backup Failed");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to backup database.");
                    alert.showAndWait();
                }
            } catch (Exception e) {
                System.err.println("Error during backup: " + e.getMessage());
                
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Backup Error");
                alert.setHeaderText(null);
                alert.setContentText("An error occurred during backup: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
    
    /**
     * Event handler for restore database button
     */
    @FXML
    private void onRestoreClick() {
        // Show warning message
        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setTitle("Database Restore");
        warning.setHeaderText("Warning: Restoring will replace current data");
        warning.setContentText("This operation will replace all current data with the data from the backup file. This action cannot be undone. Proceed with caution.");
        
        warning.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Show file chooser dialog
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Backup File");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("SQL Files", "*.sql"),
                        new FileChooser.ExtensionFilter("All Files", "*.*")
                );
                File selectedFile = fileChooser.showOpenDialog(restoreBtn.getScene().getWindow());
                
                if (selectedFile != null) {
                    try {
                        // Execute restore (this would typically call a database utility)
                        boolean success = performDatabaseRestore(selectedFile.getAbsolutePath());
                        
                        if (success) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Restore Complete");
                            alert.setHeaderText(null);
                            alert.setContentText("Database restore completed successfully.");
                            alert.showAndWait();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Restore Failed");
                            alert.setHeaderText(null);
                            alert.setContentText("Failed to restore database.");
                            alert.showAndWait();
                        }
                    } catch (Exception e) {
                        System.err.println("Error during restore: " + e.getMessage());
                        
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Restore Error");
                        alert.setHeaderText(null);
                        alert.setContentText("An error occurred during restore: " + e.getMessage());
                        alert.showAndWait();
                    }
                }
            }
        });
    }
    
    /**
     * Perform database backup
     * 
     * @param backupPath Path to save the backup file
     * @return true if successful, false otherwise
     */
    private boolean performDatabaseBackup(String backupPath) {
        // This is a placeholder. In a real implementation, this would use the MySQL
        // dump utility or JDBC to export the database
        
        try {
            // Placeholder implementation that would be replaced with actual backup logic
            System.out.println("Backing up database to: " + backupPath);
            
            // For now, we'll just return true to simulate success
            return true;
        } catch (Exception e) {
            System.err.println("Error performing backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Perform database restore
     * 
     * @param restorePath Path to the backup file
     * @return true if successful, false otherwise
     */
    private boolean performDatabaseRestore(String restorePath) {
        // This is a placeholder. In a real implementation, this would use the MySQL
        // utility or JDBC to import the database
        
        try {
            // Placeholder implementation that would be replaced with actual restore logic
            System.out.println("Restoring database from: " + restorePath);
            
            // For now, we'll just return true to simulate success
            return true;
        } catch (Exception e) {
            System.err.println("Error performing restore: " + e.getMessage());
            return false;
        }
    }
}
