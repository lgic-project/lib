package com.example.lms.controller;

import com.example.lms.model.User;
import com.example.lms.model.BookDAO;
import com.example.lms.model.BorrowingDAO;
import com.example.lms.model.FineDAO;
import com.example.lms.model.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the admin dashboard home view.
 */
public class AdminHomeController implements ChildController, AutoCloseable {

    @FXML
    private Label totalBooksLabel;
    
    @FXML
    private Label borrowedBooksLabel;
    
    @FXML
    private Label totalUsersLabel;
    
    @FXML
    private Label pendingFinesLabel;
    
    @FXML
    private PieChart categoryChart;
    
    @FXML
    private BarChart<String, Number> monthlyChart;
    
    @FXML
    private VBox recentActivitiesContainer;
    
    private User currentUser;
    private BookDAO bookDAO;
    private UserDAO userDAO;
    private BorrowingDAO borrowingDAO;
    private FineDAO fineDAO;
    
    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Initialize DAOs
        bookDAO = new BookDAO();
        userDAO = new UserDAO();
        borrowingDAO = new BorrowingDAO();
        fineDAO = new FineDAO();
        
        // Set placeholder data for charts
        initCharts();
    }
    
    /**
     * Initialize the controller with user data
     * @param user The current user
     */
    @Override
    public void initData(User user) {
        this.currentUser = user;
        
        // Load real data
        loadDashboardStatistics();
        loadRecentActivities();
    }
    
    /**
     * Load statistics for the dashboard
     */
    private void loadDashboardStatistics() {
        try {
            // Load each statistic independently to isolate errors
            
            // Book statistics
            try {
                int totalBooks = bookDAO.getTotalBooks();
                totalBooksLabel.setText(String.valueOf(totalBooks));
            } catch (SQLException e) {
                System.err.println("Error loading book statistics: " + e.getMessage());
                totalBooksLabel.setText("0");
            }
            
            // Borrowing statistics
            try {
                int borrowedBooks = borrowingDAO.getActiveBorrowingsCount();
                borrowedBooksLabel.setText(String.valueOf(borrowedBooks));
            } catch (SQLException e) {
                System.err.println("Error loading borrowing statistics: " + e.getMessage());
                borrowedBooksLabel.setText("0");
            }
            
            // User statistics
            int totalUsers = userDAO.getTotalUsers();
            totalUsersLabel.setText(String.valueOf(totalUsers));

            // Fine statistics
            try {
                double totalFines = fineDAO.getTotalPendingFines();
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                pendingFinesLabel.setText(currencyFormat.format(totalFines));
            } catch (SQLException e) {
                System.err.println("Error loading fine statistics: " + e.getMessage());
                pendingFinesLabel.setText("$0.00");
            }
            
            // Update charts with real data - only if we loaded the basic stats successfully
            try {
                loadCategoryChart();
            } catch (SQLException e) {
                System.err.println("Error loading category chart: " + e.getMessage());
                // Keep placeholder chart data
            }
            
            try {
                loadMonthlyBorrowingChart();
            } catch (SQLException e) {
                System.err.println("Error loading monthly borrowing chart: " + e.getMessage());
                // Keep placeholder chart data
            }
            
        } catch (Exception e) {
            System.err.println("Unexpected error loading dashboard statistics: " + e.getMessage());
            e.printStackTrace();
            // Fallback to placeholder data if database access fails
            totalBooksLabel.setText("0");
            borrowedBooksLabel.setText("0");
            totalUsersLabel.setText("0");
            pendingFinesLabel.setText("$0.00");
        }
    }
    
    /**
     * Initialize charts with placeholder data
     */
    private void initCharts() {
        // Category chart
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Fiction", 35),
            new PieChart.Data("Science", 25),
            new PieChart.Data("History", 15),
            new PieChart.Data("Business", 10),
            new PieChart.Data("Other", 15)
        );
        categoryChart.setData(pieChartData);
        
        // Monthly borrowing chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Borrowings");
        series.getData().add(new XYChart.Data<>("Jan", 25));
        series.getData().add(new XYChart.Data<>("Feb", 30));
        series.getData().add(new XYChart.Data<>("Mar", 40));
        series.getData().add(new XYChart.Data<>("Apr", 35));
        series.getData().add(new XYChart.Data<>("May", 45));
        series.getData().add(new XYChart.Data<>("Jun", 38));
        
        
        monthlyChart.getData().add(series);
    }
    
    /**
     * Load category chart with real data
     * @throws SQLException if database access fails
     */
    private void loadCategoryChart() throws SQLException {
        // Create a fresh DAO instance just for this operation to avoid connection sharing issues
        BookDAO localBookDAO = null;
        try {
            localBookDAO = new BookDAO();
            
            // Get category statistics
            Map<String, Integer> categoryStats = localBookDAO.getBookCountByCategory();
            
            // Create chart data
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            // Add each category
            for (Map.Entry<String, Integer> entry : categoryStats.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            
            // Update chart
            categoryChart.setData(pieChartData);
            
        } finally {
            // Always close the local DAO to ensure connection is properly released
            if (localBookDAO != null) {
                try {
                    localBookDAO.close();
                } catch (SQLException e) {
                    System.err.println("Error closing local book DAO: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Load monthly borrowing chart with real data
     * @throws SQLException if database access fails
     */
    private void loadMonthlyBorrowingChart() throws SQLException {
        // Create a fresh DAO instance just for this operation to avoid connection sharing issues
        BorrowingDAO localBorrowingDAO = null;
        try {
            localBorrowingDAO = new BorrowingDAO();
            
            // Get current year
            int currentYear = LocalDate.now().getYear();
            
            // Get monthly borrowing counts
            Map<Integer, Integer> monthlyBorrowings = localBorrowingDAO.getMonthlyBorrowingCounts(currentYear);
            
            // Create series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Borrowings " + currentYear);
            
            // Add data in correct month order
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            
            for (int i = 0; i < months.length; i++) {
                // Month in database is 1-based (January = 1)
                Integer count = monthlyBorrowings.getOrDefault(i + 1, 0);
                series.getData().add(new XYChart.Data<>(months[i], count));
            }
            
            // Update chart
            monthlyChart.getData().clear();
            monthlyChart.getData().add(series);
            
        } finally {
            // Always close the local DAO to ensure connection is properly released
            if (localBorrowingDAO != null) {
                try {
                    localBorrowingDAO.close();
                } catch (SQLException e) {
                    System.err.println("Error closing local borrowing DAO: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Load recent activities for display
     */
    private void loadRecentActivities() {
        recentActivitiesContainer.getChildren().clear();
        
        // Use a separate try-catch block to isolate errors in this method from other parts of the dashboard
        BorrowingDAO localBorrowingDAO = null;
        try {
            // Create a fresh DAO instance just for this operation to avoid connection sharing issues
            localBorrowingDAO = new BorrowingDAO();
            
            // Get recent borrowings (limit to 5)
            List<Map<String, String>> borrowingActivities = localBorrowingDAO.getRecentBorrowingActivities(5);
            
            // Display each activity
            for (Map<String, String> activity : borrowingActivities) {
                String message = "Book borrowed: '" + activity.get("title") + "' by " + activity.get("user");
                addActivityItem(message, activity.get("date"));
            }
            
            // If no activities were found, add a message
            if (borrowingActivities.isEmpty()) {
                addActivityItem("No recent borrowing activities", LocalDateTime.now().toString());
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading recent activities: " + e.getMessage());
            e.printStackTrace();
            
            // Add fallback message if database access fails
            addActivityItem("Could not load recent activities", LocalDateTime.now().toString());
        } finally {
            // Always close the local DAO to ensure connection is properly released
            if (localBorrowingDAO != null) {
                try {
                    localBorrowingDAO.close();
                } catch (SQLException e) {
                    System.err.println("Error closing local borrowing DAO: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Add an activity item to the recent activities container
     * 
     * @param activity The activity description
     * @param timestamp The timestamp
     */
    private void addActivityItem(String activity, String timestamp) {
        Label activityLabel = new Label(activity + " - " + timestamp);
        activityLabel.setWrapText(true);
        recentActivitiesContainer.getChildren().add(activityLabel);
    }
    
    /**
     * Close all resources used by this controller
     * Called when switching views or closing the application
     */
    @Override
    public void close() throws Exception {
        try {
            // Close all DAOs
            if (bookDAO != null) {
                bookDAO.close();
                bookDAO = null;
            }
            if (userDAO != null) {
//                userDAO.close();
                userDAO = null;
            }
            if (borrowingDAO != null) {
                borrowingDAO.close();
                borrowingDAO = null;
            }
            if (fineDAO != null) {
                try {
                    fineDAO.close();
                } catch (SQLException e) {
                    System.err.println("Error closing FineDAO: " + e.getMessage());
                }
                fineDAO = null;
            }
            System.out.println("AdminHomeController resources closed successfully");
        } catch (SQLException e) {
            System.err.println("Error closing AdminHomeController resources: " + e.getMessage());
            throw e;
        }
    }
}
