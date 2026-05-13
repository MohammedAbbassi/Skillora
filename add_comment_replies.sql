-- Add support for comment replies (nested comments)
-- Run this SQL to update your database

USE `skillora`;

-- Add parent_comment_id column to commentaire table
ALTER TABLE `commentaire` 
ADD COLUMN `id_commentaire_parent` INT(11) DEFAULT NULL AFTER `id_post`,
ADD KEY `idx_commentaire_parent` (`id_commentaire_parent`),
ADD CONSTRAINT `fk_commentaire_parent` 
  FOREIGN KEY (`id_commentaire_parent`) 
  REFERENCES `commentaire` (`id_commentaire`) 
  ON DELETE CASCADE 
  ON UPDATE CASCADE;
