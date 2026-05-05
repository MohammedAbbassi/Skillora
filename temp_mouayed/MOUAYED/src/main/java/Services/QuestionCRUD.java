package Services;

import Entities.Question;
import Interface.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionCRUD implements InterfaceCRUD<Question> {
    Connection conn;

    public QuestionCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Question question) throws SQLException {
        String req = "INSERT INTO question (libelle, niveau, score, categorie, auteur, active) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        pst.setString(1, question.getLibelle());
        pst.setString(2, question.getNiveau());
        pst.setInt(3, question.getScore());
        pst.setString(4, question.getCategorie());
        pst.setString(5, question.getAuteur());
        pst.setBoolean(6, question.isActive());

        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            question.setId(rs.getInt(1));
        }
    }

    @Override
    public void modifier(Question question) throws SQLException {
        String req = "UPDATE question SET libelle=?, niveau=?, score=?, categorie=?, auteur=?, active=? WHERE id=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, question.getLibelle());
        pst.setString(2, question.getNiveau());
        pst.setInt(3, question.getScore());
        pst.setString(4, question.getCategorie());
        pst.setString(5, question.getAuteur());
        pst.setBoolean(6, question.isActive());
        pst.setInt(7, question.getId());

        pst.executeUpdate();
        System.out.println("Question modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM question WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Question supprimée");
    }

    @Override
    public List<Question> afficher() throws SQLException {
        String req = "SELECT * FROM question";
        List<Question> questions = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Question q = new Question();
            q.setId(rs.getInt("id"));
            q.setLibelle(rs.getString("libelle"));
            q.setNiveau(rs.getString("niveau"));
            q.setScore(rs.getInt("score"));
            q.setCategorie(rs.getString("categorie"));
            q.setAuteur(rs.getString("auteur"));
            q.setActive(rs.getBoolean("active"));
            questions.add(q);
        }

        return questions;
    }
}