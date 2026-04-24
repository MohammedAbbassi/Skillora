package tests;

import entities.User;
import entities.Poste;
import entities.Commentaire;
import entities.Jaime;
import services.UserService;
import services.PosteService;
import services.CommentaireService;
import services.JaimeService;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        PosteService posteService = new PosteService();
        CommentaireService commentaireService = new CommentaireService();
        JaimeService jaimeService = new JaimeService();

        try {
            // 1. Créer un utilisateur pour être l'auteur
            User user = new User("testuser", "test@skillora.com", "pass123", "Jean", "Dupont");
            if (!userService.emailExists(user.getEmail())) {
                userService.add(user);
            } else {
                user = userService.getUserByEmail(user.getEmail());
            }
            System.out.println("Utilisateur utilisé: " + user.getNomUtilisateur() + " (ID: " + user.getIdUtilisateur() + ")");

            // 2. Créer un Poste
            Poste poste = new Poste(" Skillora", " premier post .", "1.jpg", user.getIdUtilisateur());
            posteService.add(poste);
            System.out.println("Nouveau poste ajouté: " + poste);
            Poste poste1 = new Poste("text 1", "dexieme post.", "2.jpg", user.getIdUtilisateur());
            posteService.add(poste1);
            Poste poste2 = new Poste("text 2", "trisieme post.", "3.jpg", user.getIdUtilisateur());
            posteService.add(poste1);
            System.out.println("Nouveau poste ajouté: " + poste);

            // 3. Ajouter un Commentaire
            Commentaire commentaire = new Commentaire("Super projet !", user.getIdUtilisateur(), poste.getIdPost());
            commentaireService.add(commentaire);
            System.out.println("Nouveau commentaire ajouté: " + commentaire);

            // 4. Ajouter un Like (Jaime)
            Jaime jaime = new Jaime(poste.getIdPost(), user.getIdUtilisateur());
            jaimeService.add(jaime);
            System.out.println("Like ajouté au poste.");

            // 5. Afficher les statistiques
            System.out.println("\n--- Statistiques du Poste ---");
            System.out.println("Titre: " + poste.getTitre());
            System.out.println("Commentaires: " + commentaireService.getByPost(poste.getIdPost()).size());
            System.out.println("Likes: " + jaimeService.getCountByPost(poste.getIdPost()));

            // 6. Lister tous les postes
            System.out.println("\n--- Liste de tous les postes ---");
            posteService.getAll().forEach(System.out::println);

        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
