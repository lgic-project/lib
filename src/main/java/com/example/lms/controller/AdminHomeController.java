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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the admin dashboard home view.
 */
public class AdminHomeController implements ChildController {

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
            // Book statistics
            int totalBooks = bookDAO.getTotalBooks();
            int borrowedBooks = borrowingDAO.getActiveBorrowingsCount();
            
            // User statistics
            int totalUsers = userDAO.getTotalUsers();
            
            // Fine statistics
            double totalFines = fineDAO.getTotalPendingFines();
            
            // Update UI
            totalBooksLabel.setText(String.valueOf(totalBooks));
            borrowedBooksLabel.setText(String.valueOf(borrowedBooks));
            totalUsersLabel.setText(String.valueOf(totalUsers));
            
            // Format currency
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            pendingFinesLabel.setText(currencyFormat.format(totalFines));
            
            // Update charts with real data
            loadCategoryChart();
            loadMonthlyBorrowingChart();
            
        } catch (SQLException e) {
            System.err.println("Error loading dashboard statistics: " + e.getMessage());
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
     */
    private void loadCategoryChart() {
        try {
            Map<String, Integer> categories = bookDAO.getBookCountByCategory();
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            for (Map.Entry<String, Integer> entry : categories.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
            
            categoryChart.setData(pieChartData);
            
        } catch (SQLException e) {
            System.err.println("Error loading category chart: " + e.getMessage());
            // Chart will keep the placeholder data
        }
    }
    
    /**
     * Load monthly borrowing chart with real data
     */
    private void loadMonthlyBorrowingChart() {
        try {
            // Get current year
            int currentYear = LocalDate.now().getYear();
            
            // Get monthly borrowing counts
            Map<Integer, Integer> monthlyBorrowings = borrowingDAO.getMonthlyBorrowingCounts(currentYear);
            
            // Create series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Borrowings " + currentYear);
            
            // Add data in correct month order
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            
            for (String month : months) {
                Integer count = monthlyBorrowings.getOrDefault(month, 0);
                series.getData().add(new XYChart.Data<>(month, count));
            }
            
            // Update chart
            monthlyChart.getData().clear();
            monthlyChart.getData().add(series);
            
        } catch (SQLException e) {
            System.err.println("Error loading monthly borrowing chart: " + e.getMessage());
            // Chart will keep the placeholder data
        }
    }
    
    /**
     * Load recent activities for display
     */
    private void loadRecentActivities() {
        // This would typically load recent events from the database
        // For now, we'll add placeholder data
        recentActivitiesContainer.getChildren().clear();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDate today = LocalDate.now();
        
        addActivityItem("New user registered: Sarah Johnson", today.atTime(9, 30).format(formatter));
        addActivityItem("Book borrowed: 'Clean Code' by Robert Martin", today.atTime(10, 15).format(formatter));
        addActivityItem("Fine payment received: $12.50", today.atTime(11, 45).format(formatter));
        addActivityItem("New book added: 'Design Patterns'", today.atTime(13, 20).format(formatter));
        addActivityItem("User updated profile: John Smith", today.atTime(14, 5).format(formatter));
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
}
