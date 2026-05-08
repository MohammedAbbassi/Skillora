package services;

import entities.Commentaire;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IService<Commentaire> {

    private final Connection cnx;

    public CommentaireService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Commentaire commentaire) throws SQLException {
        String req = "INSERT INTO `commentaire`(`contenu`, `id_utilisateur`, `id_post`) VALUES (?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, commentaire.getContenu());
        pst.setLong(2, commentaire.getIdUtilisateur());
        pst.setInt(3, commentaire.getIdPost());
        pst.executeUpdate();
        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            commentaire.setIdCommentaire(rs.getInt(1));
        }
    }

    @Override
    public void update(Commentaire commentaire) throws SQLException {
        String req = "UPDATE commentaire SET contenu = ? WHERE id_commentaire = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, commentaire.getContenu());
        pst.setInt(2, commentaire.getIdCommentaire());
        pst.executeUpdate();
    }

    @Override
    public void delete(Commentaire commentaire) throws SQLException {
        String req = "DELETE FROM commentaire WHERE id_commentaire = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, commentaire.getIdCommentaire());
        pst.executeUpdate();
    }

    @Override
    public List<Commentaire> getAll() throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String req = "SELECT * FROM commentaire ORDER BY date_creation ASC";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            commentaires.add(mapRow(rs));
        }
        return commentaires;
    }

    public List<Commentaire> getByPost(int idPost) throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String req = "SELECT * FROM commentaire WHERE id_post = ? ORDER BY date_creation ASC";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, idPost);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            commentaires.add(mapRow(rs));
        }
        return commentaires;
    }

    private Commentaire mapRow(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setIdCommentaire(rs.getInt("id_commentaire"));
        c.setContenu(rs.getString("contenu"));
        c.setDateCreation(rs.getTimestamp("date_creation"));
        c.setIdUtilisateur(rs.getLong("id_utilisateur"));
        c.setIdPost(rs.getInt("id_post"));
        return c;
    }
}
