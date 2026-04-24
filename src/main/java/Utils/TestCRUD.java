package Utils;

import Entities.Evenement;
import Services.EvenementCRUD;

import java.sql.Date;

public class TestCRUD {
    public static void main(String[] args) {

        EvenementCRUD ec = new EvenementCRUD();

        try {

            // ================== AJOUT ==================
            Evenement e = new Evenement();
            e.setNom("Event Test");
            e.setDate_evenement(new Date(System.currentTimeMillis()));
            e.setLieu("Tunis");
            e.setImage("img.jpg");
            e.setDuree_minutes(60);
            e.setId_utilisateur(1);

            ec.ajouter(e);
            System.out.println("Ajout OK");


            // ================== AFFICHAGE ==================
            /*
            List<Evenement> list = ec.afficher();
            for (Evenement ev : list) {
                System.out.println(ev);
            }
            */


            // ================== MODIFICATION ==================
            /*
            List<Evenement> list = ec.afficher();
            if (!list.isEmpty()) {
                Evenement last = list.get(list.size() - 1);
                last.setNom("Modifié");
                ec.modifier(last);
                System.out.println("Modification OK");
            }
            */


            // ================== SUPPRESSION ==================
            /*
            List<Evenement> list = ec.afficher();
            if (!list.isEmpty()) {
                Evenement last = list.get(list.size() - 1);
                ec.supprimer(last.getId_evenement());
                System.out.println("Suppression OK");
            }
            */


        } catch (Exception ex) {
            System.out.println("Erreur : " + ex.getMessage());
        }
    }
}