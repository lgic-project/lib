package com.example.lms.controller;

import com.example.lms.model.LibraryStaff;
import com.example.lms.model.LibraryStaffDAO;
import com.example.lms.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the admin staff management view
 */
public class AdminStaffController implements ChildController {

    @FXML
    private Button addStaffBtn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> departmentFilter;
    
    @FXML
    private Button searchBtn;
    
    @FXML
    private TableView<LibraryStaff> staffTable;
    
    @FXML
    private TableColumn<LibraryStaff, Integer> idColumn;
    
    @FXML
    private TableColumn<LibraryStaff, String> nameColumn;
    
    @FXML
    private TableColumn<LibraryStaff, String> positionColumn;
    
    @FXML
    private TableColumn<LibraryStaff, String> departmentColumn;
    
    @FXML
    private TableColumn<LibraryStaff, String> emailColumn;
    
    @FXML
    private TableColumn<LibraryStaff, String> hireDateColumn;
    
    @FXML
    private TableColumn<LibraryStaff, Void> actionsColumn;
    
    private User currentUser;
    private LibraryStaffDAO staffDAO;
    private ObservableList<LibraryStaff> staffList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        staffDAO = new LibraryStaffDAO();
        
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        hireDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getHireDate().format(dateFormatter)));
        
        // Set up action buttons
        setupActionButtons();
        
        // Load department filter options
        loadDepartmentFilter();
        
        // Load initial staff data
        loadStaff();
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
        Callback<TableColumn<LibraryStaff, Void>, TableCell<LibraryStaff, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<LibraryStaff, Void> call(final TableColumn<LibraryStaff, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    
                    {
                        editBtn.getStyleClass().add("button-small");
                        deleteBtn.getStyleClass().add("button-small");
                        deleteBtn.getStyleClass().add("button-danger");
                        
                        editBtn.setOnAction(event -> {
                            LibraryStaff staff = getTableView().getItems().get(getIndex());
                            editStaff(staff);
                        });
                        
                        deleteBtn.setOnAction(event -> {
                            LibraryStaff staff = getTableView().getItems().get(getIndex());
                            deleteStaff(staff);
                        });
                    }
                    
                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Create a container for buttons
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
     * Load department filter options
     */
    private void loadDepartmentFilter() {
        try {
            List<String> departments = staffDAO.getAllDepartments();
            
            // Add "All Departments" option
            departments.add(0, "All Departments");
            
            departmentFilter.setItems(FXCollections.observableArrayList(departments));
            departmentFilter.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            System.err.println("Error loading departments: " + e.getMessage());
            
            // Add default option
            departmentFilter.setItems(FXCollections.observableArrayList("All Departments"));
            departmentFilter.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Event handler for add staff button
     */
    @FXML
    private void onAddStaffClick() {
        // In a real implementation, this would open a dialog to add a new staff member
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Staff");
        alert.setHeaderText(null);
        alert.setContentText("Add Staff functionality will be implemented here.");
        alert.showAndWait();
    }
    
    /**
     * Event handler for search button
     */
    @FXML
    private void onSearchClick() {
        String searchTerm = searchField.getText().trim();
        String department = departmentFilter.getValue();
        
        if (department != null && department.equals("All Departments")) {
            department = null;
        }
        
        loadStaff(searchTerm, department);
    }
    
    /**
     * Load all staff
     */
    private void loadStaff() {
        loadStaff(null, null);
    }
    
    /**
     * Load staff with filtering
     * 
     * @param searchTerm Term to search by name or email
     * @param department Department to filter by
     */
    private void loadStaff(String searchTerm, String department) {
        try {
            List<LibraryStaff> staff;
            
            if (searchTerm == null || searchTerm.isEmpty()) {
                if (department == null) {
                    staff = staffDAO.getAllStaff();
                } else {
                    staff = staffDAO.getStaffByDepartment(department);
                }
            } else {
                staff = staffDAO.searchStaff(searchTerm, department);
            }
            
            staffList.setAll(staff);
            staffTable.setItems(staffList);
            
        } catch (SQLException e) {
            System.err.println("Error loading staff: " + e.getMessage());
            
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to load staff: " + e.getMessage());
            alert.showAndWait();
            
            // Clear the table
            staffList.clear();
        }
    }
    
    /**
     * Edit a staff member
     * 
     * @param staff The staff member to edit
     */
    private void editStaff(LibraryStaff staff) {
        // In a real implementation, this would open a dialog to edit the staff member
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Edit Staff");
        alert.setHeaderText(null);
        alert.setContentText("Edit Staff functionality will be implemented here.\nStaff: " + staff.getName());
        alert.showAndWait();
    }
    
    /**
     * Delete a staff member
     * 
     * @param staff The staff member to delete
     */
    private void deleteStaff(LibraryStaff staff) {
        // Ask for confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Staff");
        confirmation.setHeaderText("Delete Staff: " + staff.getName());
        confirmation.setContentText("Are you sure you want to delete this staff record? This action cannot be undone.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = staffDAO.deleteStaff(staff.getId());
                    if (deleted) {
                        staffList.remove(staff);
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Staff Deleted");
                        success.setHeaderText(null);
                        success.setContentText("Staff record has been successfully deleted.");
                        success.showAndWait();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Error");
                        error.setHeaderText(null);
                        error.setContentText("Failed to delete staff record. This record may be referenced elsewhere.");
                        error.showAndWait();
                    }
                } catch (SQLException e) {
                    System.err.println("Error deleting staff: " + e.getMessage());
                    
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText(null);
                    error.setContentText("Failed to delete staff: " + e.getMessage());
                    error.showAndWait();
                }
            }
        });
    }
}
