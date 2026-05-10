package services;

import entities.Reponse;
import interffaces.InterfaceCRUD;
import utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseCRUD implements InterfaceCRUD<Reponse> {

    public static final int MIN_REPONSES_PAR_QUESTION = 2;

    private final Connection conn;

    public ReponseCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    /**
     * Recherche simple et insensible a la casse sur le texte d'une reponse (contenu).
     * Recherche partielle via LIKE (%terme%).
     */
    public List<Reponse> rechercherParTexte(String texte) throws SQLException {
        String req = "SELECT id, question_id AS id_question, contenu, correcte " +
                "FROM reponse " +
                "WHERE LOWER(contenu) LIKE ? " +
                "ORDER BY id";
        List<Reponse> reponses = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(req);
        String term = texte == null ? "" : texte.trim().toLowerCase();
        pst.setString(1, "%" + term + "%");
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            reponses.add(mapReponse(rs));
        }
        return reponses;
    }

    public void ajouterReponse(Reponse reponse) throws SQLException {
        String req = "INSERT INTO reponse (contenu, estCorrecte, type_reponse, active, id_question, auteur, question_id, texte, correcte) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, reponse.getTexte());
        pst.setBoolean(2, reponse.isCorrecte());
        pst.setString(3, "Texte");
        pst.setBoolean(4, true);
        pst.setInt(5, reponse.getQuestionId());
        pst.setString(6, "Système");
        pst.setInt(7, reponse.getQuestionId());
        pst.setString(8, reponse.getTexte());
        pst.setBoolean(9, reponse.isCorrecte());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            reponse.setId(rs.getInt(1));
        }
    }

    public void modifierReponse(Reponse reponse) throws SQLException {
        String req = "UPDATE reponse SET contenu=?, estCorrecte=?, type_reponse=?, active=?, id_question=?, auteur=?, question_id=?, texte=?, correcte=? WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, reponse.getTexte());
        pst.setBoolean(2, reponse.isCorrecte());
        pst.setString(3, "Texte");
        pst.setBoolean(4, true);
        pst.setInt(5, reponse.getQuestionId());
        pst.setString(6, "Système");
        pst.setInt(7, reponse.getQuestionId());
        pst.setString(8, reponse.getTexte());
        pst.setBoolean(9, reponse.isCorrecte());
        pst.setInt(10, reponse.getId());
        pst.executeUpdate();
    }

    public void supprimerReponse(int id) throws SQLException {
        Reponse reponse = getReponseById(id);
        if (reponse != null && compterReponsesParQuestion(reponse.getQuestionId()) <= MIN_REPONSES_PAR_QUESTION) {
            throw new SQLException("Une question doit avoir au minimum "
                    + MIN_REPONSES_PAR_QUESTION + " reponses.");
        }

        String req = "DELETE FROM reponse WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    public Reponse getReponseById(int id) throws SQLException {
        String req = "SELECT id, question_id AS id_question, contenu, correcte FROM reponse WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return mapReponse(rs);
        }
        return null;
    }

    public List<Reponse> afficherReponsesParQuestion(int questionId) throws SQLException {
        String req = "SELECT id, question_id  AS id_question, contenu, correcte FROM reponse WHERE question_id=? ORDER BY id";
        List<Reponse> reponses = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, questionId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            reponses.add(mapReponse(rs));
        }
        return reponses;
    }

    public int compterReponsesParQuestion(int questionId) throws SQLException {
        String req = "SELECT COUNT(*) FROM reponse WHERE question_id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, questionId);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    public List<Reponse> getCorrectAnswersByQuestion(int questionId) throws SQLException {
        String req = "SELECT id, question_id AS id_question, contenu, correcte FROM reponse WHERE question_id=? AND (correcte=TRUE OR estCorrecte=TRUE) ORDER BY id";
        List<Reponse> reponses = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, questionId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            reponses.add(mapReponse(rs));
        }
        return reponses;
    }

    public List<Reponse> afficherReponses() throws SQLException {
        String req = "SELECT id, question_id AS id_question, contenu, correcte FROM reponse ORDER BY id";
        List<Reponse> reponses = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            reponses.add(mapReponse(rs));
        }
        return reponses;
    }

    private Reponse mapReponse(ResultSet rs) throws SQLException {
        Reponse reponse = new Reponse();
        reponse.setId(rs.getInt("id"));
        reponse.setQuestionId(rs.getInt("id_question"));
        reponse.setTexte(rs.getString("contenu"));
        reponse.setCorrecte(rs.getBoolean("correcte"));
        return reponse;
    }

    @Override
    public void ajouter(Reponse reponse) throws SQLException {
        ajouterReponse(reponse);
    }

    @Override
    public void modifier(Reponse reponse) throws SQLException {
        modifierReponse(reponse);
    }

    @Override
    public void supprimer(int id) throws SQLException {
        supprimerReponse(id);
    }

    @Override
    public List<Reponse> afficher() throws SQLException {
        return afficherReponses();
    }
}
