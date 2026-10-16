-- ============================================================
-- AegisComm Database Schema
-- Production-ready schema matching the application code.
-- ============================================================

CREATE DATABASE IF NOT EXISTS `aegiscomm` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `aegiscomm`;

-- -----------------------------------------------------------
-- Table: users
-- Stores all user accounts with BCrypt-hashed passwords.
-- Columns: name, email, password, role, profileImg
-- Referenced by: LoginServlet, SignupServlet, AddUserServlet,
--                InboxServlet (JOIN), SendMessageServlet, etc.
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
  `id`         INT          NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(100) NOT NULL,
  `email`      VARCHAR(255) NOT NULL,
  `password`   VARCHAR(255) NOT NULL,
  `role`       VARCHAR(50)  NOT NULL DEFAULT 'Soldier',
  `profileImg` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------
-- Table: messages
-- Stores encrypted messages with per-message AES keys.
-- Each message body is AES-encrypted; the AES key is wrapped
-- with the MASTER_KEY before storage.
-- Referenced by: SendMessageServlet, InboxServlet,
--                ForwardMessageServlet, DeleteMessageServlet
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `messages` (
  `id`                INT      NOT NULL AUTO_INCREMENT,
  `sender_id`         INT      NOT NULL,
  `receiver_id`       INT      NOT NULL,
  `encrypted_text`    TEXT     NOT NULL,
  `encrypted_aes_key` TEXT     NOT NULL,
  `timestamp`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_messages_receiver` (`receiver_id`),
  KEY `idx_messages_sender` (`sender_id`),
  CONSTRAINT `fk_messages_sender`   FOREIGN KEY (`sender_id`)   REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_messages_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------
-- Table: audit_logs  (hash-linked chain)
-- Tamper-evident audit trail. Each row's hash_current is
-- SHA-256(hash_previous + user_email + action + timestamp).
-- Referenced by: AuditLogger, AuditLogServlet
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id`            INT          NOT NULL AUTO_INCREMENT,
  `user_email`    VARCHAR(255) DEFAULT NULL,
  `action`        VARCHAR(500) DEFAULT NULL,
  `hash_previous` VARCHAR(64)  DEFAULT NULL,
  `hash_current`  VARCHAR(64)  DEFAULT NULL,
  `timestamp`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_email` (`user_email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- -----------------------------------------------------------
-- Table: password_reset
-- Stores time-limited reset tokens for forgot-password flow.
-- Referenced by: ForgotPasswordServlet, ResetPasswordServlet
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `password_reset` (
  `id`     INT          NOT NULL AUTO_INCREMENT,
  `email`  VARCHAR(255) NOT NULL,
  `token`  VARCHAR(255) NOT NULL,
  `expiry` TIMESTAMP    NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reset_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- -----------------------------------------------------------
-- Seed: Default admin account
-- Password: admin123 (BCrypt hashed)
-- -----------------------------------------------------------
INSERT INTO `users` (`name`, `email`, `password`, `role`) VALUES
  ('Admin', 'admin@aegiscomm.mil', CONCAT('$2a$10$', 'L4b2xGQmIlmIQzw3fyUncuBoNq0Evb4pp2vdBOFgGdK1G0UtbNY9O'), 'Admin')
ON DUPLICATE KEY UPDATE `id` = `id`;
