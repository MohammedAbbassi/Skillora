-- Base minimale pour l'extrait « boutique & commandes » uniquement.
-- Exécuter dans MySQL/MariaDB, puis ajuster db.url dans application.properties si besoin.

CREATE DATABASE IF NOT EXISTS skillora_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE skillora_shop;

CREATE TABLE utilisateurs (
  id_utilisateur BIGINT(20) NOT NULL AUTO_INCREMENT,
  nom_utilisateur VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL,
  mot_de_passe VARCHAR(255) NOT NULL,
  prenom VARCHAR(50) DEFAULT NULL,
  nom VARCHAR(50) DEFAULT NULL,
  photo_profil VARCHAR(255) DEFAULT NULL,
  role ENUM('ETUDIANT','INSTRUCTEUR','ADMIN') NOT NULL DEFAULT 'ETUDIANT',
  est_actif TINYINT(1) NOT NULL DEFAULT 1,
  date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_utilisateur),
  UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE produit (
  id_produit INT(11) NOT NULL AUTO_INCREMENT,
  nom VARCHAR(255) NOT NULL,
  description TEXT DEFAULT NULL,
  prix DECIMAL(10,2) NOT NULL,
  categorie ENUM('LIVRE','SERIE','FORMATION','KIT') NOT NULL,
  langue VARCHAR(40) DEFAULT NULL,
  niveau ENUM('DEBUTANT','INTERMEDIAIRE','AVANCE') DEFAULT NULL,
  id_cours INT(11) DEFAULT NULL,
  PRIMARY KEY (id_produit),
  KEY idx_produit_categorie (categorie)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE commande (
  id_commande INT(11) NOT NULL AUTO_INCREMENT,
  date_commande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  statut ENUM('EN_ATTENTE','PAYEE','ANNULEE') NOT NULL DEFAULT 'EN_ATTENTE',
  id_utilisateur BIGINT(20) NOT NULL,
  PRIMARY KEY (id_commande),
  KEY idx_commande_utilisateur (id_utilisateur),
  CONSTRAINT fk_commande_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateurs (id_utilisateur) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE commande_produit (
  id_commande INT(11) NOT NULL,
  id_produit INT(11) NOT NULL,
  quantite INT(11) NOT NULL DEFAULT 1,
  prix_unitaire DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id_commande, id_produit),
  KEY idx_cp_produit (id_produit),
  CONSTRAINT fk_cp_commande FOREIGN KEY (id_commande) REFERENCES commande (id_commande) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_cp_produit FOREIGN KEY (id_produit) REFERENCES produit (id_produit) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Démo : mot de passe en clair (supporté par UserService.authenticate)
INSERT INTO utilisateurs (nom_utilisateur, email, mot_de_passe, prenom, nom, role)
VALUES ('demo', 'demo@shop.local', 'demo123', 'Démo', 'Client', 'ETUDIANT');

INSERT INTO utilisateurs (nom_utilisateur, email, mot_de_passe, prenom, nom, role)
VALUES ('admin', 'admin@shop.local', 'admin123', 'Admin', 'Boutique', 'ADMIN');

INSERT INTO produit (nom, description, prix, categorie, langue, niveau) VALUES
('Pack formation Java', 'Accès complet formation.', 79.90, 'FORMATION', 'FR', 'DEBUTANT'),
('Livre UX', 'Design interfaces.', 49.00, 'LIVRE', 'FR', 'INTERMEDIAIRE'),
('Série exercices', '50 exercices.', 19.99, 'SERIE', 'FR', 'DEBUTANT'),
('Kit Docker & DevOps', 'Kit pratique (Docker, CI/CD, déploiement).', 59.50, 'KIT', 'FR', 'INTERMEDIAIRE'),
('Livre Python Avancé', 'Techniques avancées, bonnes pratiques et performance.', 72.00, 'LIVRE', 'FR', 'AVANCE');
