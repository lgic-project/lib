package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.model.UserDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for the admin users management view
 */
public class AdminUsersController implements ChildController {

    @FXML
    private Button addUserBtn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> roleFilter;
    
    @FXML
    private Button searchBtn;
    
    @FXML
    private TableView<User> usersTable;
    
    @FXML
    private TableColumn<User, Integer> idColumn;
    
    @FXML
    private TableColumn<User, String> nameColumn;
    
    @FXML
    private TableColumn<User, String> emailColumn;
    
    @FXML
    private TableColumn<User, String> phoneColumn;
    
    @FXML
    private TableColumn<User, String> roleColumn;
    
    @FXML
    private TableColumn<User, String> statusColumn;
    
    @FXML
    private TableColumn<User, Void> actionsColumn;
    
    private User currentUser;
    private UserDAO userDAO;
    private ObservableList<User> users = FXCollections.observableArrayList();
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        
        // Set up role filter options
        roleFilter.setItems(FXCollections.observableArrayList(
                "All", "Admin", "Librarian", "User"
        ));
        roleFilter.getSelectionModel().select("All");
        
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().isActive() ? "Active" : "Inactive"));
        
        // Set up action buttons
        setupActionButtons();
        
        // Load initial data
        loadUsers();
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
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    
                    {
                        editBtn.getStyleClass().add("button-small");
                        deleteBtn.getStyleClass().add("button-small");
                        deleteBtn.getStyleClass().add("button-danger");
                        
                        editBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            editUser(user);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            User user = getTableView().getItems().get(getIndex());
                            deleteUser(user);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Create a container for the buttons
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
     * Event handler for add user button
     */
    @FXML
    private void onAddUserClick() {
        try {
            // Load the user dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lms/views/user-dialog.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set up for add mode
            UserDialogController controller = loader.getController();
            controller.setupAddMode();
            
            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addUserBtn.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Show dialog and wait for it to close
            dialogStage.showAndWait();
            
            // If user was added successfully, refresh the table
            if (controller.isSuccessful()) {
                loadUsers();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading user dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Event handler for search button
     */
    @FXML
    private void onSearchClick() {
        String searchTerm = searchField.getText().trim();
        String role = roleFilter.getValue().equals("All") ? null : roleFilter.getValue();
        
        loadUsers(searchTerm, role);
    }
    
    /**
     * Load all users
     */
    private void loadUsers() {
        loadUsers(null, null);
    }
    
    /**
     * Load users with filtering
     * 
     * @param searchTerm Term to search by name or email
     * @param role Role to filter by
     */
    private void loadUsers(String searchTerm, String role) {
        try {
            List<User> userList;
            
            if (searchTerm == null || searchTerm.isEmpty()) {
                if (role == null) {
                    userList = userDAO.getAllUsers();
                } else {
                    userList = userDAO.getUsersByRole(role);
                }
            } else {
                userList = userDAO.searchUsers(searchTerm, role);
            }
            
            users.setAll(userList);
            usersTable.setItems(users);
            
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
            
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load users: " + e.getMessage());
            alert.showAndWait();
            
            // Clear the table
            users.clear();
        }
    }
    
    /**
     * Edit a user
     * 
     * @param user The user to edit
     */
    private void editUser(User user) {
        try {
            // Load the user dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/lms/views/user-dialog.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set up for edit mode
            UserDialogController controller = loader.getController();
            controller.setupEditMode(user);
            
            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addUserBtn.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            
            // Show dialog and wait for it to close
            dialogStage.showAndWait();
            
            // If user was edited successfully, refresh the table
            if (controller.isSuccessful()) {
                loadUsers();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Error loading user dialog: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Delete a user
     * 
     * @param user The user to delete
     */
    private void deleteUser(User user) {
        // Prevent deleting the current logged-in user
        if (user.getId() == currentUser.getId()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Delete User");
            alert.setHeaderText(null);
            alert.setContentText("You cannot delete your own account while logged in.");
            alert.showAndWait();
            return;
        }
        
        // Ask for confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setHeaderText("Delete User: " + user.getName());
        confirmation.setContentText("Are you sure you want to delete this user? This action cannot be undone.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = userDAO.deleteUser(user.getId());
                    if (deleted) {
                        users.remove(user);
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("User Deleted");
                        success.setHeaderText(null);
                        success.setContentText("User has been successfully deleted.");
                        success.showAndWait();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Error");
                        error.setHeaderText(null);
                        error.setContentText("Failed to delete user. The user may have associated records.");
                        error.showAndWait();
                    }
                } catch (SQLException e) {
                    System.err.println("Error deleting user: " + e.getMessage());
                    
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText(null);
                    error.setContentText("Failed to delete user: " + e.getMessage());
                    error.showAndWait();
                }
            }
        });
    }
}
