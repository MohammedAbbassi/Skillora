-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 13, 2026 at 09:11 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `skillora`
--

-- --------------------------------------------------------

--
-- Table structure for table `chapitre`
--

CREATE TABLE `chapitre` (
  `id_chapitre` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` text DEFAULT NULL,
  `ordre` int(11) NOT NULL DEFAULT 1,
  `duree` int(11) DEFAULT NULL COMMENT 'en minutes',
  `pdf_url` text DEFAULT NULL,
  `resume` text DEFAULT NULL,
  `type_explication` enum('TEXTE','VIDEO') NOT NULL DEFAULT 'TEXTE',
  `explication` text DEFAULT NULL,
  `quiz_json` text DEFAULT NULL,
  `id_cours` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `commande`
--

CREATE TABLE `commande` (
  `id_commande` int(11) NOT NULL,
  `date_commande` timestamp NOT NULL DEFAULT current_timestamp(),
  `total` decimal(10,2) NOT NULL DEFAULT 0.00,
  `statut` enum('EN_ATTENTE','PAYEE','ANNULEE') NOT NULL DEFAULT 'EN_ATTENTE',
  `id_utilisateur` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `commande_produit`
--

CREATE TABLE `commande_produit` (
  `id_commande` int(11) NOT NULL,
  `id_produit` int(11) NOT NULL,
  `quantite` int(11) NOT NULL DEFAULT 1,
  `prix_unitaire` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `commentaire`
--

CREATE TABLE `commentaire` (
  `id_commentaire` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `id_utilisateur` bigint(20) NOT NULL,
  `id_post` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `cours`
--

CREATE TABLE `cours` (
  `id_cours` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `domaine` varchar(100) DEFAULT NULL,
  `niveau` enum('DEBUTANT','INTERMEDIAIRE','AVANCE') NOT NULL DEFAULT 'DEBUTANT',
  `duree` int(11) DEFAULT NULL COMMENT 'en minutes',
  `date_creation` date NOT NULL DEFAULT curdate(),
  `progression` int(11) NOT NULL DEFAULT 0 COMMENT 'pourcentage 0-100',
  `id_instructeur` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `evenement`
--

CREATE TABLE `evenement` (
  `id_evenement` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `date_evenement` date NOT NULL,
  `lieu` varchar(50) NOT NULL,
  `image` longtext DEFAULT NULL,
  `duree_minutes` int(11) NOT NULL,
  `id_utilisateur` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `jaime`
--

CREATE TABLE `jaime` (
  `id_post` int(11) NOT NULL,
  `id_utilisateur` bigint(20) NOT NULL,
  `date_jaime` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE `messages` (
  `id` int(11) NOT NULL,
  `expediteur_id` bigint(20) NOT NULL,
  `destinataire_id` bigint(20) NOT NULL,
  `contenu` text NOT NULL,
  `date_envoi` timestamp NOT NULL DEFAULT current_timestamp(),
  `est_lu` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `post`
--

CREATE TABLE `post` (
  `id_post` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` text DEFAULT NULL,
  `image` longtext DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `id_utilisateur` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `preferences_utilisateur`
--

CREATE TABLE `preferences_utilisateur` (
  `id_preference` bigint(20) NOT NULL,
  `id_utilisateur` bigint(20) NOT NULL,
  `type_police` enum('OPENDYSLEXIC','ARIAL','VERDANA','LEXIE_READABLE') NOT NULL DEFAULT 'OPENDYSLEXIC',
  `taille_police` int(11) NOT NULL DEFAULT 18,
  `interligne` decimal(3,1) NOT NULL DEFAULT 1.5,
  `espacement_lettres` decimal(3,1) NOT NULL DEFAULT 0.1,
  `couleur_fond` varchar(7) NOT NULL DEFAULT '#FFFDE7',
  `couleur_texte` varchar(7) NOT NULL DEFAULT '#333333',
  `synthese_vocale` tinyint(1) NOT NULL DEFAULT 1,
  `surlignage_lecture` tinyint(1) NOT NULL DEFAULT 1,
  `reduire_animations` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `produit`
--

CREATE TABLE `produit` (
  `id_produit` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `prix` decimal(10,2) NOT NULL,
  `categorie` enum('LIVRE','SERIE','FORMATION','KIT') NOT NULL,
  `langue` varchar(40) DEFAULT NULL,
  `niveau` enum('DEBUTANT','INTERMEDIAIRE','AVANCE') DEFAULT NULL,
  `id_cours` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `progression_cours`
--

CREATE TABLE `progression_cours` (
  `id_progression` bigint(20) NOT NULL,
  `id_utilisateur` bigint(20) NOT NULL,
  `id_cours` int(11) NOT NULL,
  `id_chapitre` int(11) DEFAULT NULL,
  `statut` enum('NON_COMMENCE','EN_COURS','TERMINE') NOT NULL DEFAULT 'NON_COMMENCE',
  `pourcentage` decimal(5,2) NOT NULL DEFAULT 0.00,
  `dernier_acces` timestamp NULL DEFAULT NULL,
  `date_completion` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `question`
--

CREATE TABLE `question` (
  `id` int(11) NOT NULL,
  `libelle` varchar(255) NOT NULL,
  `niveau` varchar(100) DEFAULT 'Debutant',
  `score` int(11) DEFAULT 1,
  `active` tinyint(1) DEFAULT 1,
  `quiz_id` int(11) NOT NULL,
  `enonce` text NOT NULL,
  `type_question` enum('QCU','QCM') NOT NULL,
  `image_path` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `quiz`
--

CREATE TABLE `quiz` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `niveau` varchar(100) DEFAULT NULL,
  `matiere` enum('JAVA','HTML','CSS','JAVASCRIPT','PHP','PYTHON','SQL') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `relations`
--

CREATE TABLE `relations` (
  `id` int(11) NOT NULL,
  `utilisateur_id` bigint(20) NOT NULL,
  `utilisateur_cible_id` bigint(20) NOT NULL,
  `type` enum('FRIEND','BLOCKED') NOT NULL DEFAULT 'FRIEND',
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reponse`
--

CREATE TABLE `reponse` (
  `id` int(11) NOT NULL,
  `texte` text NOT NULL,
  `correcte` tinyint(1) DEFAULT 0,
  `type_reponse` varchar(100) DEFAULT 'Texte',
  `active` tinyint(1) DEFAULT 1,
  `question_id` int(11) NOT NULL,
  `auteur` varchar(255) DEFAULT 'Systeme'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reservation`
--

CREATE TABLE `reservation` (
  `id_reservation` int(11) NOT NULL,
  `nb_places` int(11) NOT NULL DEFAULT 1,
  `date_reservation` date NOT NULL,
  `id_evenement` int(11) NOT NULL,
  `id_utilisateur` bigint(20) NOT NULL,
  `statut` varchar(20) NOT NULL DEFAULT 'EN_ATTENTE',
  `chaises` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `statistiques_utilisateur`
--

CREATE TABLE `statistiques_utilisateur` (
  `id_statistique` bigint(20) NOT NULL,
  `id_utilisateur` bigint(20) NOT NULL,
  `total_cours_completes` int(11) NOT NULL DEFAULT 0,
  `total_quiz_passes` int(11) NOT NULL DEFAULT 0,
  `moyenne_quiz` decimal(5,2) NOT NULL DEFAULT 0.00,
  `temps_total_minutes` int(11) NOT NULL DEFAULT 0,
  `serie_actuelle_jours` int(11) NOT NULL DEFAULT 0,
  `meilleure_serie_jours` int(11) NOT NULL DEFAULT 0,
  `points_xp` int(11) NOT NULL DEFAULT 0,
  `niveau` int(11) NOT NULL DEFAULT 1,
  `derniere_activite` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `utilisateurs`
--

CREATE TABLE `utilisateurs` (
  `id_utilisateur` bigint(20) NOT NULL,
  `nom_utilisateur` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `mot_de_passe` varchar(255) NOT NULL,
  `prenom` varchar(50) DEFAULT NULL,
  `nom` varchar(50) DEFAULT NULL,
  `photo_profil` longtext DEFAULT NULL,
  `role` enum('ETUDIANT','INSTRUCTEUR','ENSEIGNANT','ADMIN') NOT NULL DEFAULT 'ETUDIANT',
  `pays` varchar(100) DEFAULT 'Tunisia',
  `est_actif` tinyint(1) NOT NULL DEFAULT 1,
  `est_en_ligne` tinyint(1) NOT NULL DEFAULT 0,
  `xp_points` int(11) NOT NULL DEFAULT 0,
  `ranked_points` int(11) NOT NULL DEFAULT 0,
  `streak_days` int(11) NOT NULL DEFAULT 0,
  `certificates_count` int(11) NOT NULL DEFAULT 0,
  `quizzes_done` int(11) NOT NULL DEFAULT 0,
  `reset_token` varchar(255) DEFAULT NULL,
  `reset_token_expiry` timestamp NULL DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_modification` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `utilisateurs`
--

INSERT INTO `utilisateurs` (`id_utilisateur`, `nom_utilisateur`, `email`, `mot_de_passe`, `prenom`, `nom`, `photo_profil`, `role`, `pays`, `est_actif`, `est_en_ligne`, `xp_points`, `ranked_points`, `streak_days`, `certificates_count`, `quizzes_done`, `date_creation`, `date_modification`) VALUES
(1, 'samy_sand', 'testuser1@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Samy', 'Sandbox', NULL, 'ETUDIANT', 'Tunisia', 1, 0, 150, 150, 1, 0, 0, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(2, 'jody_joy', 'testuser11@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Jody', 'Joy', NULL, 'ETUDIANT', 'France', 1, 0, 450, 450, 3, 0, 1, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(3, 'bill_bin', 'testuser2@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Bill', 'Binary', NULL, 'ETUDIANT', 'USA', 1, 0, 1100, 1100, 5, 0, 2, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(4, 'bob_bit', 'testuser12@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Bob', 'Bit', NULL, 'ETUDIANT', 'Japan', 1, 0, 1800, 1800, 8, 1, 3, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(5, 'anna_asm', 'testuser3@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Anna', 'Assembly', NULL, 'ETUDIANT', 'Tunisia', 1, 0, 2200, 2300, 10, 1, 6, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(6, 'alex_asm', 'testuser13@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Alex', 'Asm', NULL, 'ETUDIANT', 'Germany', 1, 0, 2900, 2700, 12, 2, 7, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(7, 'kevin_kern', 'testuser4@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Kevin', 'Kernel', NULL, 'ETUDIANT', 'Tunisia', 1, 0, 3300, 3400, 15, 2, 8, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(8, 'kim_core', 'testuser14@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Kim', 'Core', NULL, 'ETUDIANT', 'Canada', 1, 0, 3800, 3900, 18, 3, 9, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(9, 'chris_comp', 'testuser5@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Chris', 'Compiler', NULL, 'ETUDIANT', 'Tunisia', 1, 0, 4200, 4500, 20, 3, 10, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(10, 'clara_code', 'testuser15@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Clara', 'Code', NULL, 'ETUDIANT', 'UK', 1, 0, 4900, 4100, 22, 4, 11, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(11, 'sarah_stat', 'testuser6@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Sarah', 'Static', NULL, 'ETUDIANT', 'Tunisia', 1, 0, 5100, 5600, 25, 4, 12, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(12, 'steve_stack', 'testuser16@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Steve', 'Stack', NULL, 'ETUDIANT', 'Brazil', 1, 0, 5900, 5200, 28, 5, 13, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(13, 'dan_dyn', 'testuser7@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Dan', 'Dynamic', NULL, 'ETUDIANT', 'Tunisia', 1, 0, 6300, 6700, 30, 5, 14, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(14, 'diana_data', 'testuser17@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Diana', 'Data', NULL, 'ETUDIANT', 'India', 1, 0, 6900, 6100, 35, 6, 15, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(15, 'paul_proto', 'testuser8@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Paul', 'Protocol', NULL, 'ETUDIANT', 'Tunisia', 1, 0, 7200, 7800, 40, 6, 16, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(16, 'pam_packet', 'testuser18@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Pam', 'Packet', NULL, 'ETUDIANT', 'Spain', 1, 0, 7900, 7500, 45, 7, 17, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(17, 'alice_arch', 'testuser9@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Alice', 'Architect', NULL, 'ADMIN', 'Tunisia', 1, 0, 8200, 8900, 50, 8, 20, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(18, 'art_api', 'testuser19@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Art', 'Api', NULL, 'ADMIN', 'Egypt', 1, 0, 8900, 8100, 60, 10, 25, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(19, 'rick_root', 'testuser10@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Rick', 'Root', NULL, 'ADMIN', 'Tunisia', 1, 0, 9500, 15000, 100, 15, 50, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(20, 'linus_legend', 'testuser20@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Linus', 'Legend', NULL, 'ADMIN', 'Finland', 1, 0, 25000, 25000, 365, 50, 100, '2026-05-13 16:55:00', '2026-05-13 16:55:00'),
(32, 'admin', 'admin@skillora.tn', '$2a$10$gtvEkS37MWRhO7pMOoVsZ.m3orsFO6cZ3q3DMUyWaUazXFHa6m3/G', 'admin', '', NULL, 'ETUDIANT', 'Tunisia', 1, 0, 0, 0, 0, 0, 0, '2026-05-13 17:10:43', '2026-05-13 19:03:16');

-- --------------------------------------------------------

--
-- Stand-in structure for view `vue_cours_details`
-- (See below for the actual view)
--
CREATE TABLE `vue_cours_details` (
`id_cours` int(11)
,`titre` varchar(255)
,`description` text
,`domaine` varchar(100)
,`niveau` enum('DEBUTANT','INTERMEDIAIRE','AVANCE')
,`duree` int(11)
,`date_creation` date
,`progression` int(11)
,`nom_instructeur` varchar(101)
,`nombre_chapitres` bigint(21)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `vue_post_stats`
-- (See below for the actual view)
--
CREATE TABLE `vue_post_stats` (
`id_post` int(11)
,`titre` varchar(255)
,`date_creation` timestamp
,`auteur` varchar(101)
,`nb_jaimes` bigint(21)
,`nb_commentaires` bigint(21)
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `vue_profil_utilisateur`
-- (See below for the actual view)
--
CREATE TABLE `vue_profil_utilisateur` (
`id_utilisateur` bigint(20)
,`nom_utilisateur` varchar(50)
,`email` varchar(100)
,`prenom` varchar(50)
,`nom` varchar(50)
,`photo_profil` longtext
,`role` enum('ETUDIANT','INSTRUCTEUR','ENSEIGNANT','ADMIN')
,`est_actif` tinyint(1)
,`date_creation` timestamp
,`type_police` enum('OPENDYSLEXIC','ARIAL','VERDANA','LEXIE_READABLE')
,`taille_police` int(11)
,`synthese_vocale` tinyint(1)
,`surlignage_lecture` tinyint(1)
,`reduire_animations` tinyint(1)
,`couleur_fond` varchar(7)
,`couleur_texte` varchar(7)
,`points_xp` int(11)
,`niveau` int(11)
,`serie_actuelle_jours` int(11)
,`total_cours_completes` int(11)
,`moyenne_quiz` decimal(5,2)
,`derniere_activite` timestamp
);

-- --------------------------------------------------------

--
-- Structure for view `vue_cours_details`
--
DROP TABLE IF EXISTS `vue_cours_details`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vue_cours_details`  AS SELECT `c`.`id_cours` AS `id_cours`, `c`.`titre` AS `titre`, `c`.`description` AS `description`, `c`.`domaine` AS `domaine`, `c`.`niveau` AS `niveau`, `c`.`duree` AS `duree`, `c`.`date_creation` AS `date_creation`, `c`.`progression` AS `progression`, concat(`u`.`prenom`,' ',`u`.`nom`) AS `nom_instructeur`, count(`ch`.`id_chapitre`) AS `nombre_chapitres` FROM ((`cours` `c` left join `utilisateurs` `u` on(`u`.`id_utilisateur` = `c`.`id_instructeur`)) left join `chapitre` `ch` on(`ch`.`id_cours` = `c`.`id_cours`)) GROUP BY `c`.`id_cours` ;

-- --------------------------------------------------------

--
-- Structure for view `vue_post_stats`
--
DROP TABLE IF EXISTS `vue_post_stats`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vue_post_stats`  AS SELECT `p`.`id_post` AS `id_post`, `p`.`titre` AS `titre`, `p`.`date_creation` AS `date_creation`, concat(`u`.`prenom`,' ',`u`.`nom`) AS `auteur`, count(distinct `j`.`id_utilisateur`) AS `nb_jaimes`, count(distinct `co`.`id_commentaire`) AS `nb_commentaires` FROM (((`post` `p` left join `utilisateurs` `u` on(`u`.`id_utilisateur` = `p`.`id_utilisateur`)) left join `jaime` `j` on(`j`.`id_post` = `p`.`id_post`)) left join `commentaire` `co` on(`co`.`id_post` = `p`.`id_post`)) GROUP BY `p`.`id_post` ;

-- --------------------------------------------------------

--
-- Structure for view `vue_profil_utilisateur`
--
DROP TABLE IF EXISTS `vue_profil_utilisateur`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vue_profil_utilisateur`  AS SELECT `u`.`id_utilisateur` AS `id_utilisateur`, `u`.`nom_utilisateur` AS `nom_utilisateur`, `u`.`email` AS `email`, `u`.`prenom` AS `prenom`, `u`.`nom` AS `nom`, `u`.`photo_profil` AS `photo_profil`, `u`.`role` AS `role`, `u`.`est_actif` AS `est_actif`, `u`.`date_creation` AS `date_creation`, `p`.`type_police` AS `type_police`, `p`.`taille_police` AS `taille_police`, `p`.`synthese_vocale` AS `synthese_vocale`, `p`.`surlignage_lecture` AS `surlignage_lecture`, `p`.`reduire_animations` AS `reduire_animations`, `p`.`couleur_fond` AS `couleur_fond`, `p`.`couleur_texte` AS `couleur_texte`, `s`.`points_xp` AS `points_xp`, `s`.`niveau` AS `niveau`, `s`.`serie_actuelle_jours` AS `serie_actuelle_jours`, `s`.`total_cours_completes` AS `total_cours_completes`, `s`.`moyenne_quiz` AS `moyenne_quiz`, `s`.`derniere_activite` AS `derniere_activite` FROM ((`utilisateurs` `u` left join `preferences_utilisateur` `p` on(`p`.`id_utilisateur` = `u`.`id_utilisateur`)) left join `statistiques_utilisateur` `s` on(`s`.`id_utilisateur` = `u`.`id_utilisateur`)) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `chapitre`
--
ALTER TABLE `chapitre`
  ADD PRIMARY KEY (`id_chapitre`),
  ADD KEY `idx_chapitre_cours` (`id_cours`);

--
-- Indexes for table `commande`
--
ALTER TABLE `commande`
  ADD PRIMARY KEY (`id_commande`);

--
-- Indexes for table `commande_produit`
--
ALTER TABLE `commande_produit`
  ADD PRIMARY KEY (`id_commande`,`id_produit`);

--
-- Indexes for table `commentaire`
--
ALTER TABLE `commentaire`
  ADD PRIMARY KEY (`id_commentaire`),
  ADD KEY `idx_commentaire_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_commentaire_post` (`id_post`);

--
-- Indexes for table `cours`
--
ALTER TABLE `cours`
  ADD PRIMARY KEY (`id_cours`),
  ADD KEY `idx_cours_instructeur` (`id_instructeur`),
  ADD KEY `idx_cours_niveau` (`niveau`);

--
-- Indexes for table `evenement`
--
ALTER TABLE `evenement`
  ADD PRIMARY KEY (`id_evenement`),
  ADD KEY `idx_evenement_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_evenement_date` (`date_evenement`);

--
-- Indexes for table `jaime`
--
ALTER TABLE `jaime`
  ADD PRIMARY KEY (`id_post`,`id_utilisateur`),
  ADD KEY `idx_jaime_utilisateur` (`id_utilisateur`);

--
-- Indexes for table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `expediteur_id` (`expediteur_id`),
  ADD KEY `destinataire_id` (`destinataire_id`);

--
-- Indexes for table `post`
--
ALTER TABLE `post`
  ADD PRIMARY KEY (`id_post`),
  ADD KEY `idx_post_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_post_date` (`date_creation`);

--
-- Indexes for table `preferences_utilisateur`
--
ALTER TABLE `preferences_utilisateur`
  ADD PRIMARY KEY (`id_preference`),
  ADD UNIQUE KEY `uq_pref_utilisateur` (`id_utilisateur`);

--
-- Indexes for table `produit`
--
ALTER TABLE `produit`
  ADD PRIMARY KEY (`id_produit`);

--
-- Indexes for table `progression_cours`
--
ALTER TABLE `progression_cours`
  ADD PRIMARY KEY (`id_progression`),
  ADD UNIQUE KEY `uq_progression_utilisateur_cours` (`id_utilisateur`,`id_cours`),
  ADD KEY `idx_progression_cours` (`id_cours`),
  ADD KEY `idx_progression_chapitre` (`id_chapitre`);

--
-- Indexes for table `question`
--
ALTER TABLE `question`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_question_quiz` (`quiz_id`),
  ADD KEY `idx_question_quiz` (`quiz_id`);

--
-- Indexes for table `quiz`
--
ALTER TABLE `quiz`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `relations`
--
ALTER TABLE `relations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_relation` (`utilisateur_id`,`utilisateur_cible_id`),
  ADD KEY `idx_relation_utilisateur` (`utilisateur_id`),
  ADD KEY `idx_relation_cible` (`utilisateur_cible_id`);

--
-- Indexes for table `reponse`
--
ALTER TABLE `reponse`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_reponse_question` (`question_id`);

--
-- Indexes for table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  ADD PRIMARY KEY (`id_utilisateur`),
  ADD UNIQUE KEY `uq_nom_utilisateur` (`nom_utilisateur`),
  ADD UNIQUE KEY `uq_email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `commande`
--
ALTER TABLE `commande`
  MODIFY `id_commande` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `messages`
--
ALTER TABLE `messages`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `produit`
--
ALTER TABLE `produit`
  MODIFY `id_produit` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `question`
--
ALTER TABLE `question`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `quiz`
--
ALTER TABLE `quiz`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `relations`
--
ALTER TABLE `relations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reponse`
--
ALTER TABLE `reponse`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  MODIFY `id_utilisateur` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`expediteur_id`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE,
  ADD CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`destinataire_id`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Constraints for table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `fk_question_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `relations`
--
ALTER TABLE `relations`
  ADD CONSTRAINT `fk_relation_cible` FOREIGN KEY (`utilisateur_cible_id`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_relation_utilisateur` FOREIGN KEY (`utilisateur_id`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE;

--
-- Constraints for table `reponse`
--
ALTER TABLE `reponse`
  ADD CONSTRAINT `fk_reponse_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
