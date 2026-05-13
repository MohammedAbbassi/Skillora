USE `skillora`;

-- Clear existing test users
DELETE FROM `utilisateurs` WHERE `email` LIKE 'testuser%@skillora.com';

INSERT INTO `utilisateurs` (`nom_utilisateur`, `email`, `mot_de_passe`, `prenom`, `nom`, `role`, `pays`, `xp_points`, `ranked_points`, `streak_days`, `quizzes_done`, `certificates_count`, `est_actif`) VALUES
('samy_sand', 'testuser1@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Samy', 'Sandbox', 'ETUDIANT', 'Tunisia', 150, 150, 1, 0, 0, 1),
('jody_joy', 'testuser11@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Jody', 'Joy', 'ETUDIANT', 'France', 450, 450, 3, 1, 0, 1),
('bill_bin', 'testuser2@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Bill', 'Binary', 'ETUDIANT', 'USA', 1100, 1100, 5, 2, 0, 1),
('bob_bit', 'testuser12@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Bob', 'Bit', 'ETUDIANT', 'Japan', 1800, 1800, 8, 3, 1, 1),
('anna_asm', 'testuser3@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Anna', 'Assembly', 'ETUDIANT', 'Tunisia', 2200, 2300, 10, 6, 1, 1),
('alex_asm', 'testuser13@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Alex', 'Asm', 'ETUDIANT', 'Germany', 2900, 2700, 12, 7, 2, 1),
('kevin_kern', 'testuser4@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Kevin', 'Kernel', 'ETUDIANT', 'Tunisia', 3300, 3400, 15, 8, 2, 1),
('kim_core', 'testuser14@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Kim', 'Core', 'ETUDIANT', 'Canada', 3800, 3900, 18, 9, 3, 1),
('chris_comp', 'testuser5@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Chris', 'Compiler', 'ETUDIANT', 'Tunisia', 4200, 4500, 20, 10, 3, 1),
('clara_code', 'testuser15@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Clara', 'Code', 'ETUDIANT', 'UK', 4900, 4100, 22, 11, 4, 1),
('sarah_stat', 'testuser6@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Sarah', 'Static', 'ETUDIANT', 'Tunisia', 5100, 5600, 25, 12, 4, 1),
('steve_stack', 'testuser16@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Steve', 'Stack', 'ETUDIANT', 'Brazil', 5900, 5200, 28, 13, 5, 1),
('dan_dyn', 'testuser7@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Dan', 'Dynamic', 'ETUDIANT', 'Tunisia', 6300, 6700, 30, 14, 5, 1),
('diana_data', 'testuser17@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Diana', 'Data', 'ETUDIANT', 'India', 6900, 6100, 35, 15, 6, 1),
('paul_proto', 'testuser8@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Paul', 'Protocol', 'ETUDIANT', 'Tunisia', 7200, 7800, 40, 16, 6, 1),
('pam_packet', 'testuser18@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Pam', 'Packet', 'ETUDIANT', 'Spain', 7900, 7500, 45, 17, 7, 1),
('alice_arch', 'testuser9@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Alice', 'Architect', 'ADMIN', 'Tunisia', 8200, 8900, 50, 20, 8, 1),
('art_api', 'testuser19@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Art', 'Api', 'ADMIN', 'Egypt', 8900, 8100, 60, 25, 10, 1),
('rick_root', 'testuser10@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Rick', 'Root', 'ADMIN', 'Tunisia', 9500, 15000, 100, 50, 15, 1),
('linus_legend', 'testuser20@skillora.com', '$2a$10$vI8A7.7.7.7.7.7.7.7.7.OuB8m9m9m9m9m9m9m9m9m9m9m9m9m9m', 'Linus', 'Legend', 'ADMIN', 'Finland', 25000, 25000, 365, 100, 50, 1);
