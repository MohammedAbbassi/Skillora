package services;

import entities.Reponse;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseService implements IService<Reponse> {

    private final Connection conn;

    public ReponseService() {
        conn = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Reponse reponse) throws SQLException {
        String req = "INSERT INTO reponse (texte, correcte, type_reponse, active, question_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, reponse.getTexte());
        pst.setBoolean(2, reponse.isCorrecte());
        pst.setString(3, "Texte");
        pst.setBoolean(4, true);
        pst.setInt(5, reponse.getQuestionId());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            reponse.setId(rs.getInt(1));
        }
    }

    @Override
    public void update(Reponse reponse) throws SQLException {
        String req = "UPDATE reponse SET texte=?, correcte=?, question_id=? WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, reponse.getTexte());
        pst.setBoolean(2, reponse.isCorrecte());
        pst.setInt(3, reponse.getQuestionId());
        pst.setInt(4, reponse.getId());
        pst.executeUpdate();
    }

    @Override
    public void delete(Reponse reponse) throws SQLException {
        String req = "DELETE FROM reponse WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, reponse.getId());
        pst.executeUpdate();
    }

    @Override
    public List<Reponse> getAll() throws SQLException {
        String req = "SELECT id, question_id, texte, correcte FROM reponse ORDER BY id";
        List<Reponse> reponses = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            reponses.add(mapReponse(rs));
        }
        return reponses;
    }

    public List<Reponse> getByQuestion(int questionId) throws SQLException {
        String req = "SELECT id, question_id, texte, correcte FROM reponse WHERE question_id=? ORDER BY id";
        List<Reponse> reponses = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, questionId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            reponses.add(mapReponse(rs));
        }
        return reponses;
    }

    public List<Reponse> getCorrectByQuestion(int questionId) throws SQLException {
        String req = "SELECT id, question_id, texte, correcte FROM reponse WHERE question_id=? AND correcte=TRUE ORDER BY id";
        List<Reponse> reponses = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, questionId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            reponses.add(mapReponse(rs));
        }
        return reponses;
    }

    public Reponse getById(int id) throws SQLException {
        String req = "SELECT id, question_id, texte, correcte FROM reponse WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return mapReponse(rs);
        return null;
    }

    private Reponse mapReponse(ResultSet rs) throws SQLException {
        Reponse reponse = new Reponse();
        reponse.setId(rs.getInt("id"));
        reponse.setQuestionId(rs.getInt("question_id"));
        reponse.setTexte(rs.getString("texte"));
        reponse.setCorrecte(rs.getBoolean("correcte"));
        return reponse;
    }
}
