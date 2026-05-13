-- Add test user "pam" for easy testing
-- Login: email=pam, password=pam

USE `skillora`;

-- Insert test user pam
INSERT INTO `utilisateurs` 
  (`id_utilisateur`, `nom_utilisateur`, `email`, `mot_de_passe`, `prenom`, `nom`, `role`, `est_actif`, `xp_points`, `streak_days`) 
VALUES 
  (999, 'pam', 'pam', 'pam', 'Pam', 'Test', 'ETUDIANT', 1, 0, 0)
ON DUPLICATE KEY UPDATE 
  `email` = 'pam', 
  `mot_de_passe` = 'pam',
  `nom_utilisateur` = 'pam',
  `prenom` = 'Pam',
  `nom` = 'Test';

-- Add preferences for pam
INSERT INTO `preferences_utilisateur` 
  (`id_preference`, `id_utilisateur`, `type_police`, `taille_police`, `interligne`, `espacement_lettres`, 
   `couleur_fond`, `couleur_texte`, `synthese_vocale`, `surlignage_lecture`, `reduire_animations`)
VALUES 
  (999, 999, 'ARIAL', 16, 1.5, 0.1, '#FFFFFF', '#000000', 0, 0, 0)
ON DUPLICATE KEY UPDATE 
  `id_utilisateur` = 999;

-- Add statistics for pam
INSERT INTO `statistiques_utilisateur` 
  (`id_statistique`, `id_utilisateur`, `total_cours_completes`, `total_quiz_passes`, `moyenne_quiz`, 
   `temps_total_minutes`, `serie_actuelle_jours`, `meilleure_serie_jours`, `points_xp`, `niveau`)
VALUES 
  (999, 999, 0, 0, 0.00, 0, 0, 0, 0, 1)
ON DUPLICATE KEY UPDATE 
  `id_utilisateur` = 999;

SELECT 'Test user PAM created successfully! Login with email=pam, password=pam' AS message;
