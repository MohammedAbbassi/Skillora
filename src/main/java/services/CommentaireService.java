package services;

import entities.Commentaire;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IService<Commentaire> {

    private Connection cnx;

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
        System.out.println("Comment added");
    }

    @Override
    public void update(Commentaire commentaire) throws SQLException {
        String req = "UPDATE commentaire SET contenu = ? WHERE id_commentaire = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, commentaire.getContenu());
        pst.setInt(2, commentaire.getIdCommentaire());
        pst.executeUpdate();
        System.out.println("Comment modified");
    }

    @Override
    public void delete(Commentaire commentaire) throws SQLException {
        String req = "DELETE FROM commentaire WHERE id_commentaire = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, commentaire.getIdCommentaire());
        pst.executeUpdate();
        System.out.println("Comment deleted");
    }

    @Override
    public List<Commentaire> getAll() throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String req = "SELECT * FROM commentaire";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Commentaire commentaire = new Commentaire();
            commentaire.setIdCommentaire(rs.getInt("id_commentaire"));
            commentaire.setContenu(rs.getString("contenu"));
            commentaire.setDateCreation(rs.getTimestamp("date_creation"));
            commentaire.setIdUtilisateur(rs.getLong("id_utilisateur"));
            commentaire.setIdPost(rs.getInt("id_post"));
            commentaires.add(commentaire);
        }
        return commentaires;
    }

    public List<Commentaire> getByPost(int idPost) throws SQLException {
        List<Commentaire> commentaires = new ArrayList<>();
        String req = "SELECT * FROM commentaire WHERE id_post = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, idPost);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            Commentaire commentaire = new Commentaire();
            commentaire.setIdCommentaire(rs.getInt("id_commentaire"));
            commentaire.setContenu(rs.getString("contenu"));
            commentaire.setDateCreation(rs.getTimestamp("date_creation"));
            commentaire.setIdUtilisateur(rs.getLong("id_utilisateur"));
            commentaire.setIdPost(rs.getInt("id_post"));
            commentaires.add(commentaire);
        }
        return commentaires;
    }
}
