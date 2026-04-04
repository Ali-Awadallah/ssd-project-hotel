-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: hotel_system
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audit_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `action` varchar(255) NOT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,1,'System initialized','2026-04-04 09:52:45'),(2,2,'Created reservation for John Smith','2026-04-04 09:52:45'),(3,2,'Created reservation for Mary Johnson','2026-04-04 09:52:45'),(4,2,'Checked-in Emily Davis','2026-04-04 09:52:45'),(5,1,'Login success','2026-04-04 10:23:24'),(6,1,'Login success','2026-04-04 10:52:17'),(7,1,'Reset password for user: reception','2026-04-04 10:53:34'),(8,1,'Reset password for user: manager','2026-04-04 10:53:58'),(9,1,'Reset password for user: admin','2026-04-04 10:54:12'),(10,1,'Reset password for user: reception','2026-04-04 10:54:25'),(11,1,'Reset password for user: manager','2026-04-04 10:54:35'),(20,NULL,'Login failed for user: manager','2026-04-04 11:14:30'),(21,NULL,'Login failed for user: manager','2026-04-04 11:14:35'),(22,2,'Login success','2026-04-04 11:16:53'),(23,NULL,'Login failed for user: admin','2026-04-04 16:00:45'),(24,NULL,'Login failed for user: admin','2026-04-04 16:01:00'),(25,NULL,'Login failed for user: manager','2026-04-04 16:01:27'),(26,NULL,'Login failed for user: manager','2026-04-04 16:01:58'),(27,2,'Login success','2026-04-04 16:02:38'),(28,NULL,'Login failed for user: admin','2026-04-04 16:02:54'),(30,1,'Login success','2026-04-04 16:05:52'),(31,1,'Reset password for user: admin','2026-04-04 16:06:12'),(32,1,'Login success','2026-04-04 16:06:42'),(33,1,'Created new user: Abdulrahman','2026-04-04 16:07:10'),(34,1,'Deleted user: Abdulrahman','2026-04-04 16:07:18'),(35,1,'Login success','2026-04-04 16:14:16'),(36,2,'Login success','2026-04-04 16:17:02'),(37,NULL,'Login failed for user: manager','2026-04-04 16:17:25'),(38,NULL,'Login failed for user: manager','2026-04-04 16:17:31'),(39,NULL,'Login failed for user: manager','2026-04-04 16:17:41'),(40,NULL,'Login failed for user: manager','2026-04-04 16:22:56'),(41,1,'Login success','2026-04-04 16:23:27'),(42,1,'Reset password for user: admin','2026-04-04 16:23:36'),(43,1,'Reset password for user: reception','2026-04-04 16:23:49'),(44,1,'Reset password for user: manager','2026-04-04 16:23:56'),(45,3,'Login success','2026-04-04 16:24:04'),(46,1,'Login success','2026-04-04 16:25:57'),(47,1,'Login success','2026-04-04 16:28:57');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `backup_records`
--

DROP TABLE IF EXISTS `backup_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `backup_records` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `backup_file_name` varchar(255) NOT NULL,
  `backup_size` bigint(20) DEFAULT NULL,
  `backup_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `verified` tinyint(1) DEFAULT 0,
  `created_by` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `created_by` (`created_by`),
  KEY `idx_backup_date` (`backup_date`),
  CONSTRAINT `backup_records_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `backup_records`
--

LOCK TABLES `backup_records` WRITE;
/*!40000 ALTER TABLE `backup_records` DISABLE KEYS */;
/*!40000 ALTER TABLE `backup_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `payments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservation_id` int(11) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `date` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `reservation_id` (`reservation_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,4,450.00,'2026-04-04 09:52:35'),(2,5,100.00,'2026-04-04 09:52:35');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `reservations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `guest_name` varchar(100) NOT NULL,
  `room_id` int(11) NOT NULL,
  `check_in` date NOT NULL,
  `check_out` date NOT NULL,
  `status` varchar(20) DEFAULT 'Booked',
  `total_amount` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `room_id` (`room_id`),
  KEY `idx_reservations_status_dates` (`status`,`check_in`,`check_out`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (1,'John Smith',1,'2026-04-04','2026-04-06','Booked',NULL),(2,'Mary Johnson',2,'2026-04-05','2026-04-08','Booked',NULL),(3,'Robert Brown',5,'2026-04-07','2026-04-09','Booked',NULL),(4,'Emily Davis',3,'2026-04-02','2026-04-05','Checked-in',NULL),(5,'Michael Wilson',4,'2026-04-03','2026-04-04','Checked-out',NULL);
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rooms` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `room_number` int(11) NOT NULL,
  `type` varchar(20) NOT NULL,
  `status` varchar(20) DEFAULT 'Available',
  `price_per_night` decimal(10,2) DEFAULT 100.00,
  PRIMARY KEY (`id`),
  UNIQUE KEY `room_number` (`room_number`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES (1,101,'Single','Available',100.00),(2,102,'Double','Available',150.00),(3,103,'Double','Available',150.00),(4,104,'Single','Available',100.00),(5,201,'Suite','Available',250.00),(6,202,'Suite','Available',250.00),(7,203,'Double','Available',160.00);
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_requests`
--

DROP TABLE IF EXISTS `service_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `service_requests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservation_id` int(11) NOT NULL,
  `request_type` varchar(50) NOT NULL,
  `description` text NOT NULL,
  `status` varchar(20) DEFAULT 'Open',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`request_type`),
  KEY `idx_service_requests_reservation` (`reservation_id`),
  CONSTRAINT `service_requests_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservations` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_requests`
--

LOCK TABLES `service_requests` WRITE;
/*!40000 ALTER TABLE `service_requests` DISABLE KEYS */;
INSERT INTO `service_requests` VALUES (1,1,'Housekeeping','Need extra towels and pillows','Open','2026-04-04 09:53:56','2026-04-04 09:53:56'),(2,4,'Maintenance','Air conditioning not working properly','In Progress','2026-04-04 09:53:56','2026-04-04 09:53:56');
/*!40000 ALTER TABLE `service_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(20) NOT NULL,
  `failed_attempts` int(11) DEFAULT 0,
  `locked_until` datetime DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$10$a42opSGEq1CXllEeNe/cG.1x/NbdqlRnxOikDRSyCSQdjOQa6w9g.','Admin',0,NULL,'2026-04-04 19:28:57',1),(2,'reception','$2a$10$lVGkEoPbnK20fNxQZ35sxOd9FnuKWj4drhMnCcW9KFl9F01EphBBq','Receptionist',0,NULL,'2026-04-04 19:17:02',1),(3,'manager','$2a$10$mzy2/90wHJL4qBPyzlTXNulKxjRnhk3BMaB0FOCrpf5tpK2/gXLMC','Manager',0,NULL,'2026-04-04 19:24:04',1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-04 19:29:01
