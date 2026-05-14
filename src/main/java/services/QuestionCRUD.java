package services;

import entities.Question;
import entities.Question.TypeQuestion;
import interfaces.InterfaceCRUD;
import utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionCRUD implements InterfaceCRUD<Question> {

    private final Connection conn;

    public QuestionCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    /**
     * Recherche simple et insensible a la casse sur le texte de la question (enonce).
     * Recherche partielle via LIKE (%terme%).
     */
    public List<Question> rechercherParEnonce(String texte) throws SQLException {
        String req = "SELECT id_question AS id, quiz_id, enonce, type_question, score, image_path " +
                "FROM question " +
                "WHERE LOWER(enonce) LIKE ? " +
                "ORDER BY id_question";
        List<Question> questions = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(req);
        String term = texte == null ? "" : texte.trim().toLowerCase();
        pst.setString(1, "%" + term + "%");
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            questions.add(mapQuestion(rs));
        }
        return questions;
    }

    public void ajouterQuestion(Question question) throws SQLException {
        String req = "INSERT INTO question (libelle, niveau, score, est_active, quiz_id, enonce, type_question, image_path, id_utilisateur) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, buildUniqueLibelle(question));
        pst.setString(2, "DEBUTANT");
        pst.setInt(3, question.getPoint());
        pst.setBoolean(4, true);
        pst.setInt(5, question.getQuizId());
        pst.setString(6, question.getEnonce());
        pst.setString(7, question.getTypeQuestion().name());
        pst.setString(8, question.getImagePath());
        pst.setLong(9, resolveQuestionAuthorId());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            question.setId(rs.getInt(1));
        }
    }

    public void modifierQuestion(Question question) throws SQLException {
        String req = "UPDATE question SET libelle=?, niveau=?, score=?, est_active=?, quiz_id=?, enonce=?, type_question=?, image_path=? WHERE id_question=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, buildStableLibelle(question));
        pst.setString(2, "DEBUTANT");
        pst.setInt(3, question.getPoint());
        pst.setBoolean(4, true);
        pst.setInt(5, question.getQuizId());
        pst.setString(6, question.getEnonce());
        pst.setString(7, question.getTypeQuestion().name());
        pst.setString(8, question.getImagePath());
        pst.setInt(9, question.getId());
        pst.executeUpdate();
    }

    public void modifierPointQuestion(int questionId, int point) throws SQLException {
        String req = "UPDATE question SET score=? WHERE id_question=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, point);
        pst.setInt(2, questionId);
        pst.executeUpdate();
    }

    public void supprimerQuestion(int id) throws SQLException {
        String req = "DELETE FROM question WHERE id_question=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    public List<Question> afficherQuestions() throws SQLException {
        String req = "SELECT id_question AS id, quiz_id, enonce, type_question, score, image_path FROM question ORDER BY id_question";
        List<Question> questions = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            questions.add(mapQuestion(rs));
        }
        return questions;
    }

    public List<Question> afficherParQuiz(int quizId) throws SQLException {
        String req = "SELECT id_question AS id, quiz_id, enonce, type_question, score, image_path FROM question WHERE quiz_id=? ORDER BY id_question";
        List<Question> questions = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, quizId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            questions.add(mapQuestion(rs));
        }
        return questions;
    }

    public int compterParQuiz(int quizId) throws SQLException {
        String req = "SELECT COUNT(*) FROM question WHERE quiz_id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, quizId);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public Question getQuestionById(int id) throws SQLException {
        String req = "SELECT id_question AS id, quiz_id, enonce, type_question, score, image_path FROM question WHERE id_question=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapQuestion(rs) : null;
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getInt("id"));
        question.setQuizId(rs.getInt("quiz_id"));
        question.setEnonce(rs.getString("enonce"));
        String typeQuestion = rs.getString("type_question");
        question.setTypeQuestion(typeQuestion == null ? TypeQuestion.QCU : TypeQuestion.valueOf(typeQuestion));
        question.setPoint(rs.getInt("score"));
        question.setImagePath(rs.getString("image_path"));
        return question;
    }

    private String buildUniqueLibelle(Question question) {
        String enonce = question.getEnonce() == null ? "Question" : question.getEnonce().trim();
        if (enonce.length() > 180) {
            enonce = enonce.substring(0, 180);
        }
        return "Quiz " + question.getQuizId() + " - " + System.currentTimeMillis() + " - " + enonce;
    }

    private String buildStableLibelle(Question question) {
        String enonce = question.getEnonce() == null ? "Question" : question.getEnonce().trim();
        if (enonce.length() > 220) {
            enonce = enonce.substring(0, 220);
        }
        return "Question " + question.getId() + " - " + enonce;
    }

    private long resolveQuestionAuthorId() throws SQLException {
        long currentUserId = utils.SessionManager.getCurrentUserId();
        if (currentUserId > 0) {
            return currentUserId;
        }

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_utilisateur FROM utilisateurs ORDER BY id_utilisateur LIMIT 1")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        throw new SQLException("Aucun utilisateur disponible pour creer une question.");
    }

    @Override
    public void ajouter(Question question) throws SQLException {
        ajouterQuestion(question);
    }

    @Override
    public void modifier(Question question) throws SQLException {
        modifierQuestion(question);
    }

    @Override
    public void supprimer(int id) throws SQLException {
        supprimerQuestion(id);
    }

    @Override
    public List<Question> afficher() throws SQLException {
        return afficherQuestions();
    }
}
