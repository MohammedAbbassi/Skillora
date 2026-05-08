package services;

import entities.Question;
import entities.Question.TypeQuestion;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService implements IService<Question> {

    private final Connection conn;

    public QuestionService() {
        conn = MyDatabase.getInstance().getCnx();
    }

    public List<Question> rechercherParEnonce(String texte) throws SQLException {
        String req = "SELECT id_question, quiz_id, libelle, enonce, type_question, score, image_path, niveau, est_active " +
            "FROM question WHERE LOWER(enonce) LIKE ? ORDER BY id_question";
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

    @Override
    public void add(Question question) throws SQLException {
        String req = "INSERT INTO question (libelle, niveau, score, est_active, quiz_id, enonce, type_question, image_path, id_utilisateur) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, buildLibelle(question));
        pst.setString(2, question.getNiveau() != null ? question.getNiveau() : "DEBUTANT");
        pst.setInt(3, question.getPoint());
        pst.setBoolean(4, question.isEstActive());
        pst.setInt(5, question.getQuizId());
        pst.setString(6, question.getEnonce());
        pst.setString(7, question.getTypeQuestion().name());
        pst.setString(8, question.getImagePath());
        pst.setInt(9, question.getIdUtilisateur());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            question.setId(rs.getInt(1));
        }
    }

    @Override
    public void update(Question question) throws SQLException {
        String req = "UPDATE question SET libelle=?, niveau=?, score=?, est_active=?, quiz_id=?, enonce=?, type_question=?, image_path=? " +
            "WHERE id_question=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, buildLibelle(question));
        pst.setString(2, question.getNiveau() != null ? question.getNiveau() : "DEBUTANT");
        pst.setInt(3, question.getPoint());
        pst.setBoolean(4, question.isEstActive());
        pst.setInt(5, question.getQuizId());
        pst.setString(6, question.getEnonce());
        pst.setString(7, question.getTypeQuestion().name());
        pst.setString(8, question.getImagePath());
        pst.setInt(9, question.getId());
        pst.executeUpdate();
    }

    @Override
    public void delete(Question question) throws SQLException {
        String req = "DELETE FROM question WHERE id_question=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, question.getId());
        pst.executeUpdate();
    }

    @Override
    public List<Question> getAll() throws SQLException {
        String req = "SELECT id_question, quiz_id, libelle, enonce, type_question, score, image_path, niveau, est_active " +
            "FROM question ORDER BY id_question";
        List<Question> questions = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            questions.add(mapQuestion(rs));
        }
        return questions;
    }

    public List<Question> getByQuiz(int quizId) throws SQLException {
        String req = "SELECT id_question, quiz_id, libelle, enonce, type_question, score, image_path, niveau, est_active " +
            "FROM question WHERE quiz_id=? ORDER BY id_question";
        List<Question> questions = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, quizId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            questions.add(mapQuestion(rs));
        }
        return questions;
    }

    public Question getById(int id) throws SQLException {
        String req = "SELECT id_question, quiz_id, libelle, enonce, type_question, score, image_path, niveau, est_active " +
            "FROM question WHERE id_question=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapQuestion(rs) : null;
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getInt("id_question"));
        question.setQuizId(rs.getInt("quiz_id"));
        question.setLibelle(rs.getString("libelle"));
        question.setEnonce(rs.getString("enonce"));
        String type = rs.getString("type_question");
        if (type != null) {
            question.setTypeQuestion(TypeQuestion.valueOf(type));
        }
        question.setPoint(rs.getInt("score"));
        question.setImagePath(rs.getString("image_path"));
        question.setNiveau(rs.getString("niveau"));
        question.setEstActive(rs.getBoolean("est_active"));
        return question;
    }

    private String buildLibelle(Question question) {
        String enonce = question.getEnonce() == null ? "Question" : question.getEnonce().trim();
        if (enonce.length() > 200) enonce = enonce.substring(0, 200);
        return "Q" + question.getQuizId() + " - " + enonce;
    }
}
