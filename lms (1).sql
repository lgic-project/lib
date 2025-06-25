-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jun 25, 2025 at 06:21 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `lms`
--

-- --------------------------------------------------------

--
-- Table structure for table `app_setting`
--

CREATE TABLE `app_setting` (
  `id` int(11) NOT NULL,
  `setting_key` varchar(100) NOT NULL,
  `setting_value` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `app_setting`
--

INSERT INTO `app_setting` (`id`, `setting_key`, `setting_value`, `created_at`, `updated_at`) VALUES
(1, 'email_address', '', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(2, 'email_password', '', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(3, 'theme_mode', 'light', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(4, 'app_name', 'Library Management System', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(5, 'smtp_host', 'smtp.gmail.com', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(6, 'smtp_port', '587', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(7, 'fine_rate_per_day', '0.50', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(8, 'max_books_per_user', '3', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(9, 'loan_period_days', '14', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(10, 'reservation_expiry_days', '9', '2025-06-25 15:59:42', '2025-06-25 16:16:42');

-- --------------------------------------------------------

--
-- Table structure for table `books`
--

CREATE TABLE `books` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `author_name` varchar(100) NOT NULL,
  `isbn` varchar(20) DEFAULT NULL,
  `publisher_id` int(11) DEFAULT NULL,
  `publication_year` int(11) DEFAULT NULL,
  `edition` varchar(20) DEFAULT NULL,
  `language` varchar(50) DEFAULT 'English',
  `pages` int(11) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `cover_image_url` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `books`
--

INSERT INTO `books` (`id`, `title`, `author_name`, `isbn`, `publisher_id`, `publication_year`, `edition`, `language`, `pages`, `description`, `cover_image_url`, `created_at`, `updated_at`) VALUES
(1, 'Clean Code', 'Robert C. Martin', '978013235088', 1, 2008, '', 'English', 0, 'A handbook of agile software craftsmanship', '/com/example/lms/images/covers/1536c784-24b1-426b-b387-95029b885bff.jpg', '2025-06-25 15:59:42', '2025-06-25 16:14:46'),
(2, 'Harry Potter and the Philosopher\'s Stone', 'J.K. Rowling', '9780747532743', 2, 1997, '1st Edition', 'English', 223, 'The first novel in the Harry Potter series', '/images/books/harry_potter.jpg', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(3, 'To Kill a Mockingbird', 'Harper Lee', '9780061120084', 3, 1960, '', 'English', 0, 'A novel set in the American South during the 1930s', '/images/books/to_kill_mockingbird.jpg', '2025-06-25 15:59:42', '2025-06-25 16:20:09'),
(4, '1984', 'George Orwell', '9780451524935', 2, 1949, '', 'English', 0, 'A dystopian novel set in a totalitarian regime', '/com/example/lms/images/covers/186f504f-b83c-4995-a8fe-8e0d335eaf01.jpg', '2025-06-25 15:59:42', '2025-06-25 16:13:39'),
(5, 'The Shining', 'Stephen King', '9780307743657', 3, 1977, 'Reissue', 'English', 688, 'A horror novel set in an isolated hotel', '/images/books/shining.jpg', '2025-06-25 15:59:42', '2025-06-25 15:59:42');

-- --------------------------------------------------------

--
-- Table structure for table `book_categories`
--

CREATE TABLE `book_categories` (
  `book_id` int(11) NOT NULL,
  `category_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `book_categories`
--

INSERT INTO `book_categories` (`book_id`, `category_id`) VALUES
(1, 2),
(1, 8),
(2, 1),
(2, 4),
(3, 1),
(4, 1),
(4, 3),
(5, 1),
(5, 5);

-- --------------------------------------------------------

--
-- Table structure for table `book_copies`
--

CREATE TABLE `book_copies` (
  `id` int(11) NOT NULL,
  `book_id` int(11) NOT NULL,
  `copy_number` varchar(20) NOT NULL,
  `status` enum('AVAILABLE','BORROWED','RESERVED','LOST','DAMAGED') DEFAULT 'AVAILABLE',
  `acquisition_date` date DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `shelf_location` varchar(50) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `book_copies`
--

INSERT INTO `book_copies` (`id`, `book_id`, `copy_number`, `status`, `acquisition_date`, `price`, `shelf_location`, `notes`, `created_at`, `updated_at`) VALUES
(1, 1, 'C001', 'AVAILABLE', '2023-01-15', 34.99, 'A1-S2', NULL, '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(2, 1, 'C002', 'AVAILABLE', '2023-01-15', 34.99, 'A1-S2', NULL, '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(3, 2, 'C001', 'AVAILABLE', '2023-02-20', 24.99, 'B3-S1', NULL, '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(4, 2, 'C002', 'AVAILABLE', '2023-02-20', 24.99, 'B3-S1', NULL, '2025-06-25 15:59:42', '2025-06-25 16:21:05'),
(5, 2, 'C003', 'AVAILABLE', '2023-02-20', 24.99, 'B3-S1', NULL, '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(6, 3, 'C001', 'AVAILABLE', '2023-03-10', 19.99, 'C2-S3', NULL, '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(7, 4, 'C001', 'RESERVED', '2023-03-15', 22.99, 'D1-S4', NULL, '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(8, 5, 'C001', 'BORROWED', '2023-04-05', 29.99, 'E2-S2', NULL, '2025-06-25 15:59:42', '2025-06-25 16:20:56'),
(9, 1, '1', 'AVAILABLE', '2025-06-25', NULL, 'B12', 'new book', '2025-06-25 16:14:33', '2025-06-25 16:14:33'),
(10, 4, '1', 'AVAILABLE', '2025-06-25', NULL, 'D12', '', '2025-06-25 16:20:36', '2025-06-25 16:20:36');

-- --------------------------------------------------------

--
-- Table structure for table `borrowings`
--

CREATE TABLE `borrowings` (
  `id` int(11) NOT NULL,
  `book_copy_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `issued_by` int(11) DEFAULT NULL,
  `returned_to` int(11) DEFAULT NULL,
  `borrow_date` date NOT NULL,
  `due_date` date NOT NULL,
  `return_date` date DEFAULT NULL,
  `status` enum('ACTIVE','RETURNED','OVERDUE','LOST') DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `borrowings`
--

INSERT INTO `borrowings` (`id`, `book_copy_id`, `user_id`, `issued_by`, `returned_to`, `borrow_date`, `due_date`, `return_date`, `status`, `created_at`, `updated_at`) VALUES
(1, 4, 3, NULL, 4, '2025-06-15', '2025-06-29', '2025-06-25', 'ACTIVE', '2025-06-25 15:59:42', '2025-06-25 16:21:05'),
(2, 8, 6, 4, NULL, '2025-06-25', '2025-07-09', NULL, 'ACTIVE', '2025-06-25 16:20:56', '2025-06-25 16:20:56');

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`id`, `name`, `description`, `created_at`, `updated_at`) VALUES
(1, 'Fiction', 'Literary works based on imagination', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(2, 'Non-fiction', 'Literary works based on facts', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(3, 'Science Fiction', 'Fiction based on imagined future scientific or technological advances', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(4, 'Fantasy', 'Fiction involving magic and supernatural phenomena', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(5, 'Mystery', 'Fiction dealing with solving a puzzle or crime', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(6, 'Romance', 'Fiction focusing on romantic relationships', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(7, 'Biography', 'Non-fiction account of someone\'s life', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(8, 'Computer Science', 'Books about computer technology and programming', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(9, 'History', 'Books about historical events', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(10, 'Philosophy', 'Books about philosophical concepts', '2025-06-25 15:59:42', '2025-06-25 15:59:42');

-- --------------------------------------------------------

--
-- Table structure for table `fines`
--

CREATE TABLE `fines` (
  `id` int(11) NOT NULL,
  `borrowing_id` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `reason` enum('LATE_RETURN','DAMAGED','LOST') NOT NULL,
  `payment_status` enum('UNPAID','PAID','WAIVED') DEFAULT 'UNPAID',
  `payment_date` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `library_staff`
--

CREATE TABLE `library_staff` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `position` varchar(100) DEFAULT NULL,
  `department` varchar(100) DEFAULT NULL,
  `hire_date` date DEFAULT NULL,
  `salary` decimal(10,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `publishers`
--

CREATE TABLE `publishers` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `address` text DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `publishers`
--

INSERT INTO `publishers` (`id`, `name`, `address`, `phone`, `email`, `website`, `created_at`, `updated_at`) VALUES
(1, 'O\'Reilly Media', '1005 Gravenstein Highway North, Sebastopol, CA 95472', '707-827-7000', 'info@oreilly.com', 'https://www.oreilly.com', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(2, 'Penguin Random House', '1745 Broadway, New York, NY 10019', '212-782-9000', 'consumerservices@penguinrandomhouse.com', 'https://www.penguinrandomhouse.com', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(3, 'HarperCollins', '195 Broadway, New York, NY 10007', '212-207-7000', 'info@harpercollins.com', 'https://www.harpercollins.com', '2025-06-25 15:59:42', '2025-06-25 15:59:42'),
(4, 'Pearson Education', '221 River Street, Hoboken, NJ 07030', '201-236-7000', 'info@pearson.com', 'https://www.pearson.com', '2025-06-25 15:59:42', '2025-06-25 15:59:42');

-- --------------------------------------------------------

--
-- Table structure for table `reservations`
--

CREATE TABLE `reservations` (
  `id` int(11) NOT NULL,
  `book_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `reservation_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `expiry_date` timestamp NULL DEFAULT NULL,
  `status` enum('PENDING','FULFILLED','EXPIRED','CANCELLED','NOTIFIED') DEFAULT 'PENDING',
  `notes` text DEFAULT NULL,
  `notification_date` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reservations`
--

INSERT INTO `reservations` (`id`, `book_id`, `user_id`, `reservation_date`, `expiry_date`, `status`, `notes`, `notification_date`, `created_at`, `updated_at`) VALUES
(1, 4, 3, '2025-06-22 18:15:00', '2025-06-25 18:15:00', 'PENDING', NULL, NULL, '2025-06-25 15:59:42', '2025-06-25 15:59:42');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','librarian','user') NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `reset_token` varchar(255) DEFAULT NULL,
  `reset_token_expiry` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `name`, `email`, `password`, `role`, `phone`, `address`, `profile_image`, `is_active`, `created_at`, `updated_at`, `reset_token`, `reset_token_expiry`) VALUES
(1, 'Admin', 'admin@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'admin', NULL, NULL, NULL, 1, '2025-06-25 15:59:42', '2025-06-25 15:59:42', NULL, NULL),
(2, 'John Librarian', 'librarian@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'librarian', '123-456-7890', '123 Library St, Book Town', NULL, 1, '2025-06-25 15:59:42', '2025-06-25 15:59:42', NULL, NULL),
(3, 'Jane User', 'user@lms.com', 'e64b78fc3bc91bcbc7dc232ba8ec59e0', 'user', '987-654-3210', '456 Reader Ave, Book City', NULL, 1, '2025-06-25 15:59:42', '2025-06-25 15:59:42', NULL, NULL),
(4, 'sunil', 'sunil@gmail.com', '48ccc87cd7afb85704a63e8d5953d326', 'librarian', '9876546547', NULL, NULL, 1, '2025-06-25 16:11:52', '2025-06-25 16:19:20', NULL, NULL),
(6, 'user', 'user@gmail.com', '48ccc87cd7afb85704a63e8d5953d326', 'user', NULL, NULL, NULL, 1, '2025-06-25 16:18:08', '2025-06-25 16:18:08', NULL, NULL);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `app_setting`
--
ALTER TABLE `app_setting`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `setting_key` (`setting_key`);

--
-- Indexes for table `books`
--
ALTER TABLE `books`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `isbn` (`isbn`),
  ADD KEY `publisher_id` (`publisher_id`),
  ADD KEY `idx_book_title` (`title`),
  ADD KEY `idx_book_isbn` (`isbn`),
  ADD KEY `idx_book_author` (`author_name`);

--
-- Indexes for table `book_categories`
--
ALTER TABLE `book_categories`
  ADD PRIMARY KEY (`book_id`,`category_id`),
  ADD KEY `category_id` (`category_id`);

--
-- Indexes for table `book_copies`
--
ALTER TABLE `book_copies`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `book_id` (`book_id`,`copy_number`),
  ADD KEY `idx_book_copy_status` (`status`);

--
-- Indexes for table `borrowings`
--
ALTER TABLE `borrowings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `book_copy_id` (`book_copy_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `issued_by` (`issued_by`),
  ADD KEY `returned_to` (`returned_to`),
  ADD KEY `idx_borrowing_status` (`status`),
  ADD KEY `idx_borrowing_due_date` (`due_date`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`),
  ADD KEY `idx_category_name` (`name`);

--
-- Indexes for table `fines`
--
ALTER TABLE `fines`
  ADD PRIMARY KEY (`id`),
  ADD KEY `borrowing_id` (`borrowing_id`),
  ADD KEY `idx_fine_payment_status` (`payment_status`);

--
-- Indexes for table `library_staff`
--
ALTER TABLE `library_staff`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`);

--
-- Indexes for table `publishers`
--
ALTER TABLE `publishers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_publisher_name` (`name`);

--
-- Indexes for table `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `book_id` (`book_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `idx_reservation_status` (`status`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_user_email` (`email`),
  ADD KEY `idx_user_role` (`role`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `app_setting`
--
ALTER TABLE `app_setting`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `books`
--
ALTER TABLE `books`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `book_copies`
--
ALTER TABLE `book_copies`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `borrowings`
--
ALTER TABLE `borrowings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `fines`
--
ALTER TABLE `fines`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `library_staff`
--
ALTER TABLE `library_staff`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `publishers`
--
ALTER TABLE `publishers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `reservations`
--
ALTER TABLE `reservations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `books`
--
ALTER TABLE `books`
  ADD CONSTRAINT `books_ibfk_1` FOREIGN KEY (`publisher_id`) REFERENCES `publishers` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `book_categories`
--
ALTER TABLE `book_categories`
  ADD CONSTRAINT `book_categories_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `book_categories_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `book_copies`
--
ALTER TABLE `book_copies`
  ADD CONSTRAINT `book_copies_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `borrowings`
--
ALTER TABLE `borrowings`
  ADD CONSTRAINT `borrowings_ibfk_1` FOREIGN KEY (`book_copy_id`) REFERENCES `book_copies` (`id`),
  ADD CONSTRAINT `borrowings_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `borrowings_ibfk_3` FOREIGN KEY (`issued_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `borrowings_ibfk_4` FOREIGN KEY (`returned_to`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `fines`
--
ALTER TABLE `fines`
  ADD CONSTRAINT `fines_ibfk_1` FOREIGN KEY (`borrowing_id`) REFERENCES `borrowings` (`id`);

--
-- Constraints for table `library_staff`
--
ALTER TABLE `library_staff`
  ADD CONSTRAINT `library_staff_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`book_id`) REFERENCES `books` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
