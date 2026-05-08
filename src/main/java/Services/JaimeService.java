package services;

import entities.Jaime;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JaimeService implements IService<Jaime> {

    private final Connection cnx;

    public JaimeService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Jaime jaime) throws SQLException {
        String req = "INSERT INTO `jaime`(`id_post`, `id_utilisateur`) VALUES (?, ?)";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, jaime.getIdPost());
        pst.setLong(2, jaime.getIdUtilisateur());
        pst.executeUpdate();
    }

    @Override
    public void update(Jaime jaime) throws SQLException {
        // Composite key only — update not applicable
    }

    @Override
    public void delete(Jaime jaime) throws SQLException {
        String req = "DELETE FROM jaime WHERE id_post = ? AND id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, jaime.getIdPost());
        pst.setLong(2, jaime.getIdUtilisateur());
        pst.executeUpdate();
    }

    @Override
    public List<Jaime> getAll() throws SQLException {
        List<Jaime> jaimes = new ArrayList<>();
        String req = "SELECT * FROM jaime";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Jaime jaime = new Jaime();
            jaime.setIdPost(rs.getInt("id_post"));
            jaime.setIdUtilisateur(rs.getLong("id_utilisateur"));
            jaime.setDateJaime(rs.getTimestamp("date_jaime"));
            jaimes.add(jaime);
        }
        return jaimes;
    }

    public int getCountByPost(int idPost) throws SQLException {
        String req = "SELECT COUNT(*) FROM jaime WHERE id_post = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, idPost);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }

    public boolean hasLiked(int idPost, long idUtilisateur) throws SQLException {
        String req = "SELECT COUNT(*) FROM jaime WHERE id_post = ? AND id_utilisateur = ?";
        PreparedStatement pst = cnx.prepareStatement(req);
        pst.setInt(1, idPost);
        pst.setLong(2, idUtilisateur);
        ResultSet rs = pst.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }
}
