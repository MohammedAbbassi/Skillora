-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 24, 2026 at 10:49 AM
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
  `image` varchar(255) DEFAULT NULL,
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
-- Table structure for table `post`
--

CREATE TABLE `post` (
  `id_post` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` text DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
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
  `type` enum('FORMATION','COURS','SERIE') NOT NULL,
  `duree` int(11) DEFAULT NULL COMMENT 'en heures',
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
  `id_question` int(11) NOT NULL,
  `libelle` varchar(255) NOT NULL,
  `niveau` enum('DEBUTANT','INTERMEDIAIRE','AVANCE') NOT NULL DEFAULT 'DEBUTANT',
  `score` int(11) NOT NULL DEFAULT 0,
  `categorie` varchar(100) DEFAULT NULL,
  `est_active` tinyint(1) NOT NULL DEFAULT 1,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `id_utilisateur` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reponse`
--

CREATE TABLE `reponse` (
  `id_reponse` int(11) NOT NULL,
  `contenu` text NOT NULL,
  `est_correcte` tinyint(1) NOT NULL DEFAULT 0,
  `commentaire` text DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `type_reponse` enum('CHOIX_UNIQUE','CHOIX_MULTIPLE','VRAI_FAUX') NOT NULL DEFAULT 'CHOIX_UNIQUE',
  `est_active` tinyint(1) NOT NULL DEFAULT 1,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_modification` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `id_question` int(11) NOT NULL
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
  `id_utilisateur` bigint(20) NOT NULL
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
  `photo_profil` varchar(255) DEFAULT NULL,
  `role` enum('ETUDIANT','INSTRUCTEUR','ADMIN') NOT NULL DEFAULT 'ETUDIANT',
  `est_actif` tinyint(1) NOT NULL DEFAULT 1,
  `date_creation` timestamp NOT NULL DEFAULT current_timestamp(),
  `date_modification` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
,`photo_profil` varchar(255)
,`role` enum('ETUDIANT','INSTRUCTEUR','ADMIN')
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
  ADD PRIMARY KEY (`id_commande`),
  ADD KEY `idx_commande_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_commande_statut` (`statut`);

--
-- Indexes for table `commande_produit`
--
ALTER TABLE `commande_produit`
  ADD PRIMARY KEY (`id_commande`,`id_produit`),
  ADD KEY `idx_commande_produit_produit` (`id_produit`);

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
  ADD PRIMARY KEY (`id_produit`),
  ADD KEY `idx_produit_cours` (`id_cours`),
  ADD KEY `idx_produit_type` (`type`);

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
  ADD PRIMARY KEY (`id_question`),
  ADD KEY `idx_question_utilisateur` (`id_utilisateur`),
  ADD KEY `idx_question_categorie` (`categorie`);

--
-- Indexes for table `reponse`
--
ALTER TABLE `reponse`
  ADD PRIMARY KEY (`id_reponse`),
  ADD KEY `idx_reponse_question` (`id_question`);

--
-- Indexes for table `reservation`
--
ALTER TABLE `reservation`
  ADD PRIMARY KEY (`id_reservation`),
  ADD UNIQUE KEY `uq_reservation` (`id_evenement`,`id_utilisateur`),
  ADD KEY `idx_reservation_utilisateur` (`id_utilisateur`);

--
-- Indexes for table `statistiques_utilisateur`
--
ALTER TABLE `statistiques_utilisateur`
  ADD PRIMARY KEY (`id_statistique`),
  ADD UNIQUE KEY `uq_stat_utilisateur` (`id_utilisateur`);

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
-- AUTO_INCREMENT for table `chapitre`
--
ALTER TABLE `chapitre`
  MODIFY `id_chapitre` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `commande`
--
ALTER TABLE `commande`
  MODIFY `id_commande` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `commentaire`
--
ALTER TABLE `commentaire`
  MODIFY `id_commentaire` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cours`
--
ALTER TABLE `cours`
  MODIFY `id_cours` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `evenement`
--
ALTER TABLE `evenement`
  MODIFY `id_evenement` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `post`
--
ALTER TABLE `post`
  MODIFY `id_post` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `preferences_utilisateur`
--
ALTER TABLE `preferences_utilisateur`
  MODIFY `id_preference` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `produit`
--
ALTER TABLE `produit`
  MODIFY `id_produit` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `progression_cours`
--
ALTER TABLE `progression_cours`
  MODIFY `id_progression` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `question`
--
ALTER TABLE `question`
  MODIFY `id_question` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reponse`
--
ALTER TABLE `reponse`
  MODIFY `id_reponse` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reservation`
--
ALTER TABLE `reservation`
  MODIFY `id_reservation` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `statistiques_utilisateur`
--
ALTER TABLE `statistiques_utilisateur`
  MODIFY `id_statistique` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `utilisateurs`
--
ALTER TABLE `utilisateurs`
  MODIFY `id_utilisateur` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `chapitre`
--
ALTER TABLE `chapitre`
  ADD CONSTRAINT `fk_chapitre_cours` FOREIGN KEY (`id_cours`) REFERENCES `cours` (`id_cours`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `commande`
--
ALTER TABLE `commande`
  ADD CONSTRAINT `fk_commande_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `commande_produit`
--
ALTER TABLE `commande_produit`
  ADD CONSTRAINT `fk_cp_commande` FOREIGN KEY (`id_commande`) REFERENCES `commande` (`id_commande`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_cp_produit` FOREIGN KEY (`id_produit`) REFERENCES `produit` (`id_produit`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `commentaire`
--
ALTER TABLE `commentaire`
  ADD CONSTRAINT `fk_commentaire_post` FOREIGN KEY (`id_post`) REFERENCES `post` (`id_post`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_commentaire_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `cours`
--
ALTER TABLE `cours`
  ADD CONSTRAINT `fk_cours_instructeur` FOREIGN KEY (`id_instructeur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `evenement`
--
ALTER TABLE `evenement`
  ADD CONSTRAINT `fk_evenement_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `jaime`
--
ALTER TABLE `jaime`
  ADD CONSTRAINT `fk_jaime_post` FOREIGN KEY (`id_post`) REFERENCES `post` (`id_post`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_jaime_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `post`
--
ALTER TABLE `post`
  ADD CONSTRAINT `fk_post_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `preferences_utilisateur`
--
ALTER TABLE `preferences_utilisateur`
  ADD CONSTRAINT `fk_pref_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `produit`
--
ALTER TABLE `produit`
  ADD CONSTRAINT `fk_produit_cours` FOREIGN KEY (`id_cours`) REFERENCES `cours` (`id_cours`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `progression_cours`
--
ALTER TABLE `progression_cours`
  ADD CONSTRAINT `fk_progression_chapitre` FOREIGN KEY (`id_chapitre`) REFERENCES `chapitre` (`id_chapitre`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_progression_cours` FOREIGN KEY (`id_cours`) REFERENCES `cours` (`id_cours`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_progression_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `question`
--
ALTER TABLE `question`
  ADD CONSTRAINT `fk_question_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `reponse`
--
ALTER TABLE `reponse`
  ADD CONSTRAINT `fk_reponse_question` FOREIGN KEY (`id_question`) REFERENCES `question` (`id_question`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `reservation`
--
ALTER TABLE `reservation`
  ADD CONSTRAINT `fk_reservation_evenement` FOREIGN KEY (`id_evenement`) REFERENCES `evenement` (`id_evenement`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_reservation_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `statistiques_utilisateur`
--
ALTER TABLE `statistiques_utilisateur`
  ADD CONSTRAINT `fk_stat_utilisateur` FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateurs` (`id_utilisateur`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
