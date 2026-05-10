package tests;

import entities.Question;
import entities.Quiz;
import entities.Reponse;
import services.QuestionCRUD;
import services.QuizCRUD;
import services.ReponseCRUD;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        QuizCRUD quizCRUD = new QuizCRUD();
        QuestionCRUD questionCRUD = new QuestionCRUD();
        ReponseCRUD reponseCRUD = new ReponseCRUD();

        try {
            Quiz quiz = new Quiz("Java basics", "Controle des fondamentaux Java", "Debutant", Quiz.Matiere.JAVA);
            quizCRUD.ajouterQuiz(quiz);

            Question question = new Question(quiz.getId(), "Quel mot-cle declare une classe ?", Question.TypeQuestion.QCU);
            questionCRUD.ajouterQuestion(question);

            reponseCRUD.ajouterReponse(new Reponse(question.getId(), "class", true));
            reponseCRUD.ajouterReponse(new Reponse(question.getId(), "function", false));

            System.out.println("Quiz cree avec ID: " + quiz.getId());
        } catch (SQLException e) {
            System.out.println("Erreur SQL: " + e.getMessage());
        }
    }
}
