package services;

import entities.Poste;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PosteService implements IService<Poste> {

    private Connection cnx;

    public PosteService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Poste poste) throws SQLException {
        String req = "INSERT INTO `post`(`titre`, `contenu`, `image`, `id_utilisateur`) VALUES (?, ?, ?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, poste.getTitre());
        pst.setString(2, poste.getContenu());
        pst.setString(3, poste.getImage());
        pst.setLong(4, poste.getIdUtilisateur());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            poste.setIdPost(rs.getInt(1));
        }
        System.out.println("Post added");
    }

    @Override
    public void update(Poste poste) throws SQLException {
        String req = "UPDATE post SET titre = ?, contenu = ?, image = ? WHERE id_post = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setString(1, poste.getTitre());
        pst.setString(2, poste.getContenu());
        pst.setString(3, poste.getImage());
        pst.setInt(4, poste.getIdPost());
        pst.executeUpdate();
        System.out.println("Post modified");
    }

    @Override
    public void delete(Poste poste) throws SQLException {
        String req = "DELETE FROM post WHERE id_post = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, poste.getIdPost());
        pst.executeUpdate();
        System.out.println("Post deleted");
    }

    @Override
    public List<Poste> getAll() throws SQLException {
        List<Poste> postes = new ArrayList<>();
        String req = "SELECT * FROM post";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Poste poste = new Poste();
            poste.setIdPost(rs.getInt("id_post"));
            poste.setTitre(rs.getString("titre"));
            poste.setContenu(rs.getString("contenu"));
            poste.setImage(rs.getString("image"));
            poste.setDateCreation(rs.getTimestamp("date_creation"));
            poste.setIdUtilisateur(rs.getLong("id_utilisateur"));
            postes.add(poste);
        }
        return postes;
    }

    public Poste getById(int id) throws SQLException {
        String req = "SELECT * FROM post WHERE id_post = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            Poste poste = new Poste();
            poste.setIdPost(rs.getInt("id_post"));
            poste.setTitre(rs.getString("titre"));
            poste.setContenu(rs.getString("contenu"));
            poste.setImage(rs.getString("image"));
            poste.setDateCreation(rs.getTimestamp("date_creation"));
            poste.setIdUtilisateur(rs.getLong("id_utilisateur"));
            return poste;
        }
        return null;
    }
}
