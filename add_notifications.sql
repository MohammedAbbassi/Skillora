-- Add notification system for mentions and other events
-- Run this SQL to add notifications to your database

USE `skillora`;

-- Create notifications table
CREATE TABLE `notification` (
  `id_notification` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `type` ENUM('MENTION','LIKE','COMMENT','REPLY') NOT NULL DEFAULT 'MENTION',
  `message` TEXT NOT NULL,
  `lu` TINYINT(1) NOT NULL DEFAULT 0,
  `date_creation` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `id_utilisateur` BIGINT(20) NOT NULL COMMENT 'User who receives the notification',
  `id_declencheur` BIGINT(20) DEFAULT NULL COMMENT 'User who triggered the notification',
  `id_post` INT(11) DEFAULT NULL,
  `id_commentaire` INT(11) DEFAULT NULL,
  PRIMARY KEY (`id_notification`),
  KEY `idx_notification_utilisateur` (`id_utilisateur`),
  KEY `idx_notification_lu` (`lu`),
  KEY `idx_notification_date` (`date_creation`),
  CONSTRAINT `fk_notification_utilisateur` 
    FOREIGN KEY (`id_utilisateur`) 
    REFERENCES `utilisateurs` (`id_utilisateur`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_notification_declencheur` 
    FOREIGN KEY (`id_declencheur`) 
    REFERENCES `utilisateurs` (`id_utilisateur`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_notification_post` 
    FOREIGN KEY (`id_post`) 
    REFERENCES `post` (`id_post`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_notification_commentaire` 
    FOREIGN KEY (`id_commentaire`) 
    REFERENCES `commentaire` (`id_commentaire`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
