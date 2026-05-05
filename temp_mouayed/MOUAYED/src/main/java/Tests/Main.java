package Tests;

import Entities.Question;
import Entities.Reponse;
import Services.QuestionCRUD;
import Services.ReponseCRUD;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        QuestionCRUD qc = new QuestionCRUD();
        ReponseCRUD rc = new ReponseCRUD();

        try {
            Question q1 = new Question(
                    "Quelle est la capitale de la France ?",
                    "facile",
                    10,
                    "Géographie",
                    "Mouayed",
                    true
            );
            qc.ajouter(q1);

            int idQuestion = q1.getId();

            Reponse r1 = new Reponse(
                    "Paris",
                    true,
                    idQuestion,
                    "Bonne réponse",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    true,
                    "Mouayed",
                    "cours geographie",
                    "texte"
            );

            Reponse r2 = new Reponse(
                    "Lyon",
                    false,
                    idQuestion,
                    "Mauvaise réponse",
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    true,
                    "mouayed",
                    "cours geographie",
                    "texte"
            );
            rc.ajouter(r1);
            rc.ajouter(r2);
            System.out.println(qc.afficher());
            System.out.println(rc.afficher());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }}}