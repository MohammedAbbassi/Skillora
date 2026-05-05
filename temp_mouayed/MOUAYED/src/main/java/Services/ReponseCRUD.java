package Services;

import Entities.Reponse;
import Interface.InterfaceCRUD;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReponseCRUD implements InterfaceCRUD<Reponse> {
    Connection conn;

    public ReponseCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(Reponse reponse) throws SQLException {
        String req = "INSERT INTO reponse (contenu, estCorrecte, id_question, commentaire, date_creation, date_modification, active, auteur, source, type_reponse) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);

        pst.setString(1, reponse.getContenu());
        pst.setBoolean(2, reponse.isEstCorrecte());
        pst.setInt(3, reponse.getQuestionId());
        pst.setString(4, reponse.getCommentaire());

        if (reponse.getDateCreation() != null) {
            pst.setTimestamp(5, Timestamp.valueOf(reponse.getDateCreation()));
        } else {
            pst.setTimestamp(5, null);
        }

        if (reponse.getDateModification() != null) {
            pst.setTimestamp(6, Timestamp.valueOf(reponse.getDateModification()));
        } else {
            pst.setTimestamp(6, null);
        }

        pst.setBoolean(7, reponse.isActive());
        pst.setString(8, reponse.getAuteur());
        pst.setString(9, reponse.getSource());
        pst.setString(10, reponse.getTypeReponse());

        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            reponse.setId(rs.getInt(1));
        }

        System.out.println("Réponse ajoutée !!");
    }

    @Override
    public void modifier(Reponse reponse) throws SQLException {
        String req = "UPDATE reponse SET contenu=?, estCorrecte=?, id_question=?, commentaire=?, date_creation=?, date_modification=?, active=?, auteur=?, source=?, type_reponse=? WHERE id=?";

        PreparedStatement pst = conn.prepareStatement(req);

        pst.setString(1, reponse.getContenu());
        pst.setBoolean(2, reponse.isEstCorrecte());
        pst.setInt(3, reponse.getQuestionId());
        pst.setString(4, reponse.getCommentaire());

        if (reponse.getDateCreation() != null) {
            pst.setTimestamp(5, Timestamp.valueOf(reponse.getDateCreation()));
        } else {
            pst.setTimestamp(5, null);
        }

        if (reponse.getDateModification() != null) {
            pst.setTimestamp(6, Timestamp.valueOf(reponse.getDateModification()));
        } else {
            pst.setTimestamp(6, null);
        }

        pst.setBoolean(7, reponse.isActive());
        pst.setString(8, reponse.getAuteur());
        pst.setString(9, reponse.getSource());
        pst.setString(10, reponse.getTypeReponse());
        pst.setInt(11, reponse.getId());

        pst.executeUpdate();
        System.out.println("Réponse modifiée");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM reponse WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Réponse supprimée");
    }

    @Override
    public List<Reponse> afficher() throws SQLException {
        String req = "SELECT * FROM reponse";
        List<Reponse> reponses = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Reponse r = new Reponse();
            r.setId(rs.getInt("id"));
            r.setContenu(rs.getString("contenu"));
            r.setEstCorrecte(rs.getBoolean("estCorrecte"));
            r.setQuestionId(rs.getInt("id_question"));
            r.setCommentaire(rs.getString("commentaire"));

            Timestamp dateCreation = rs.getTimestamp("date_creation");
            if (dateCreation != null) {
                r.setDateCreation(dateCreation.toLocalDateTime());
            }

            Timestamp dateModification = rs.getTimestamp("date_modification");
            if (dateModification != null) {
                r.setDateModification(dateModification.toLocalDateTime());
            }

            r.setActive(rs.getBoolean("active"));
            r.setAuteur(rs.getString("auteur"));
            r.setSource(rs.getString("source"));
            r.setTypeReponse(rs.getString("type_reponse"));

            reponses.add(r);
        }

        return reponses;
    }
}