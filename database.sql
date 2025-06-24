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
    phone VARCHAR(20),
    address TEXT,
    profile_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    reset_token VARCHAR(255) NULL,
    reset_token_expiry DATETIME NULL DEFAULT NULL,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB;

-- Create app_setting table for application settings
CREATE TABLE IF NOT EXISTS app_setting (
    id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Create publishers table
CREATE TABLE IF NOT EXISTS publishers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(100),
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_publisher_name (name)
) ENGINE=InnoDB;

-- Create authors table
CREATE TABLE IF NOT EXISTS authors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    biography TEXT,
    birth_date DATE,
    nationality VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_author_name (name)
) ENGINE=InnoDB;

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_name (name)
) ENGINE=InnoDB;

-- Create books table
CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    publisher_id INT,
    publication_year INT,
    edition VARCHAR(20),
    language VARCHAR(50) DEFAULT 'English',
    pages INT,
    description TEXT,
    cover_image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES publishers(id) ON DELETE SET NULL,
    INDEX idx_book_title (title),
    INDEX idx_book_isbn (isbn)
) ENGINE=InnoDB;

-- Create book_authors junction table
CREATE TABLE IF NOT EXISTS book_authors (
    book_id INT NOT NULL,
    author_id INT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Create book_categories junction table
CREATE TABLE IF NOT EXISTS book_categories (
    book_id INT NOT NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (book_id, category_id),
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Create book_copies table to track individual copies of a book
CREATE TABLE IF NOT EXISTS book_copies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    copy_number VARCHAR(20) NOT NULL,
    status ENUM('AVAILABLE', 'BORROWED', 'RESERVED', 'LOST', 'DAMAGED') DEFAULT 'AVAILABLE',
    acquisition_date DATE,
    price DECIMAL(10, 2),
    shelf_location VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    UNIQUE KEY (book_id, copy_number),
    INDEX idx_book_copy_status (status)
) ENGINE=InnoDB;

-- Create borrowings table to track book borrowing history
CREATE TABLE IF NOT EXISTS borrowings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_copy_id INT NOT NULL,
    user_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    status ENUM('ACTIVE', 'RETURNED', 'OVERDUE', 'LOST') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_copy_id) REFERENCES book_copies(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_borrowing_status (status),
    INDEX idx_borrowing_due_date (due_date)
) ENGINE=InnoDB;

-- Create reservations table
CREATE TABLE IF NOT EXISTS reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    user_id INT NOT NULL,
    reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP NULL DEFAULT NULL,
    status ENUM('PENDING', 'FULFILLED', 'EXPIRED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reservation_status (status)
) ENGINE=InnoDB;

-- Create fines table
CREATE TABLE IF NOT EXISTS fines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    borrowing_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    reason ENUM('LATE_RETURN', 'DAMAGED', 'LOST') NOT NULL,
    payment_status ENUM('UNPAID', 'PAID', 'WAIVED') DEFAULT 'UNPAID',
    payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (borrowing_id) REFERENCES borrowings(id) ON DELETE RESTRICT,
    INDEX idx_fine_payment_status (payment_status)
) ENGINE=InnoDB;

-- Create library_staff table (for non-user system staff)
CREATE TABLE IF NOT EXISTS library_staff (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    position VARCHAR(100),
    department VARCHAR(100),
    hire_date DATE,
    salary DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Insert default app settings
INSERT INTO app_setting (setting_key, setting_value) VALUES 
('email_address', ''),
('email_password', ''),
('theme_mode', 'light'),
('app_name', 'Library Management System'),
('smtp_host', 'smtp.gmail.com'),
('smtp_port', '587'),
('fine_rate_per_day', '0.50'),
('max_books_per_user', '3'),
('loan_period_days', '14'),
('reservation_expiry_days', '3');

-- Insert default admin user (password: admin123)
INSERT INTO users (name, email, password, role) VALUES 
('Admin', 'admin@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'admin');

-- Insert some test users for development
INSERT INTO users (name, email, password, role, phone, address) VALUES 
('John Librarian', 'librarian@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'librarian', '123-456-7890', '123 Library St, Book Town'),
('Jane User', 'user@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'user', '987-654-3210', '456 Reader Ave, Book City');

-- Insert sample publishers
INSERT INTO publishers (name, address, phone, email, website) VALUES
('O\'Reilly Media', '1005 Gravenstein Highway North, Sebastopol, CA 95472', '707-827-7000', 'info@oreilly.com', 'https://www.oreilly.com'),
('Penguin Random House', '1745 Broadway, New York, NY 10019', '212-782-9000', 'consumerservices@penguinrandomhouse.com', 'https://www.penguinrandomhouse.com'),
('HarperCollins', '195 Broadway, New York, NY 10007', '212-207-7000', 'info@harpercollins.com', 'https://www.harpercollins.com'),
('Pearson Education', '221 River Street, Hoboken, NJ 07030', '201-236-7000', 'info@pearson.com', 'https://www.pearson.com');

-- Insert sample authors
INSERT INTO authors (name, biography, nationality) VALUES
('J.K. Rowling', 'British author best known for the Harry Potter series.', 'British'),
('Stephen King', 'American author of horror, supernatural fiction, suspense, crime, science-fiction, and fantasy novels.', 'American'),
('Robert C. Martin', 'American software engineer and author known for developing many software design principles.', 'American'),
('George Orwell', 'English novelist, essayist, journalist, and critic.', 'British'),
('Harper Lee', 'American novelist widely known for her novel "To Kill a Mockingbird".', 'American');

-- Insert sample categories
INSERT INTO categories (name, description) VALUES
('Fiction', 'Literary works based on imagination'),
('Non-fiction', 'Literary works based on facts'),
('Science Fiction', 'Fiction based on imagined future scientific or technological advances'),
('Fantasy', 'Fiction involving magic and supernatural phenomena'),
('Mystery', 'Fiction dealing with solving a puzzle or crime'),
('Romance', 'Fiction focusing on romantic relationships'),
('Biography', 'Non-fiction account of someone\'s life'),
('Computer Science', 'Books about computer technology and programming'),
('History', 'Books about historical events'),
('Philosophy', 'Books about philosophical concepts');

-- Insert sample books
INSERT INTO books (title, isbn, publisher_id, publication_year, edition, pages, description, cover_image_url) VALUES
('Clean Code', '9780132350884', 1, 2008, '1st Edition', 464, 'A handbook of agile software craftsmanship', '/images/books/clean_code.jpg'),
('Harry Potter and the Philosopher\'s Stone', '9780747532743', 2, 1997, '1st Edition', 223, 'The first novel in the Harry Potter series', '/images/books/harry_potter.jpg'),
('To Kill a Mockingbird', '9780061120084', 3, 1960, 'Reissue', 336, 'A novel set in the American South during the 1930s', '/images/books/to_kill_mockingbird.jpg'),
('1984', '9780451524935', 2, 1949, 'Reprint', 328, 'A dystopian novel set in a totalitarian regime', '/images/books/1984.jpg'),
('The Shining', '9780307743657', 3, 1977, 'Reissue', 688, 'A horror novel set in an isolated hotel', '/images/books/shining.jpg');

-- Link books with authors
INSERT INTO book_authors (book_id, author_id) VALUES
(1, 3), -- Clean Code by Robert C. Martin
(2, 1), -- Harry Potter by J.K. Rowling
(3, 5), -- To Kill a Mockingbird by Harper Lee
(4, 4), -- 1984 by George Orwell
(5, 2); -- The Shining by Stephen King

-- Link books with categories
INSERT INTO book_categories (book_id, category_id) VALUES
(1, 8), -- Clean Code: Computer Science
(1, 2), -- Clean Code: Non-fiction
(2, 4), -- Harry Potter: Fantasy
(2, 1), -- Harry Potter: Fiction
(3, 1), -- To Kill a Mockingbird: Fiction
(4, 1), -- 1984: Fiction
(4, 3), -- 1984: Science Fiction
(5, 1), -- The Shining: Fiction
(5, 5); -- The Shining: Mystery

-- Create book copies
INSERT INTO book_copies (book_id, copy_number, status, acquisition_date, price, shelf_location) VALUES
(1, 'C001', 'AVAILABLE', '2023-01-15', 34.99, 'A1-S2'),
(1, 'C002', 'AVAILABLE', '2023-01-15', 34.99, 'A1-S2'),
(2, 'C001', 'AVAILABLE', '2023-02-20', 24.99, 'B3-S1'),
(2, 'C002', 'BORROWED', '2023-02-20', 24.99, 'B3-S1'),
(2, 'C003', 'AVAILABLE', '2023-02-20', 24.99, 'B3-S1'),
(3, 'C001', 'AVAILABLE', '2023-03-10', 19.99, 'C2-S3'),
(4, 'C001', 'RESERVED', '2023-03-15', 22.99, 'D1-S4'),
(5, 'C001', 'AVAILABLE', '2023-04-05', 29.99, 'E2-S2');

-- Create some sample borrowing records
INSERT INTO borrowings (book_copy_id, user_id, borrow_date, due_date, status) VALUES
(4, 3, CURDATE() - INTERVAL 10 DAY, CURDATE() + INTERVAL 4 DAY, 'ACTIVE');

-- Create a sample reservation
INSERT INTO reservations (book_id, user_id, reservation_date, expiry_date, status) VALUES
(4, 3, CURDATE() - INTERVAL 2 DAY, CURDATE() + INTERVAL 1 DAY, 'PENDING');