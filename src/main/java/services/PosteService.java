package services;

import entities.Poste;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PosteService implements IService<Poste> {
    private Connection connection;

    public PosteService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void add(Poste poste) throws SQLException {
        String req = "INSERT INTO post (titre, contenu, image, id_utilisateur) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, poste.getTitre());
        ps.setString(2, poste.getContenu());
        ps.setString(3, poste.getImage());
        ps.setInt(4, poste.getIdUtilisateur());
        ps.executeUpdate();
    }

    @Override
    public void update(Poste poste) throws SQLException {
        String req = "UPDATE post SET titre=?, contenu=?, image=? WHERE id_post=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, poste.getTitre());
        ps.setString(2, poste.getContenu());
        ps.setString(3, poste.getImage());
        ps.setInt(4, poste.getIdPost());
        ps.executeUpdate();
    }

    @Override
    public void delete(Poste poste) throws SQLException {
        String req = "DELETE FROM post WHERE id_post=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, poste.getIdPost());
        ps.executeUpdate();
    }

    @Override
    public List<Poste> getAll() throws SQLException {
        List<Poste> list = new ArrayList<>();
        String req = "SELECT * FROM post";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Poste p = new Poste();
            p.setIdPost(rs.getInt("id_post"));
            p.setTitre(rs.getString("titre"));
            p.setContenu(rs.getString("contenu"));
            p.setImage(rs.getString("image"));
            p.setDateCreation(rs.getTimestamp("date_creation"));
            p.setIdUtilisateur(rs.getInt("id_utilisateur"));
            list.add(p);
        }
        return list;
    }
}
