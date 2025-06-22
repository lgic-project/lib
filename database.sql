-- Create database if not exists
CREATE DATABASE IF NOT EXISTS lms;

-- Use the database
USE lms;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'librarian', 'user') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    reset_token VARCHAR(255) NULL,
    reset_token_expiry DATETIME NULL
);

-- Create app_setting table for application settings
CREATE TABLE IF NOT EXISTS app_setting (
    id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default app settings
INSERT INTO app_setting (setting_key, setting_value) VALUES 
('email_address', ''),
('email_password', ''),
('theme_mode', 'light'),
('app_name', 'Library Management System'),
('smtp_host', 'smtp.gmail.com'),
('smtp_port', '587');

-- Insert default admin user (password: admin123)
INSERT INTO users (name, email, password, role) VALUES 
('Admin', 'admin@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'admin');

-- Insert some test users for development
INSERT INTO users (name, email, password, role) VALUES 
('Librarian Test', 'librarian@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'librarian'),
('User Test', 'user@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'user');
 