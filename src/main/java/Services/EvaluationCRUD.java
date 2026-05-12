package Services;

import Entities.Evaluation;
import Interfaces.InterfaceCRUD;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationCRUD implements InterfaceCRUD<Evaluation> {

    private final Connection conn = MyDatabase.getInstance().getCnx();

    @Override
    public void ajouter(Evaluation e) throws SQLException {
        // Upsert logic: if user already rated, update it
        String sql = "INSERT INTO evaluation (id_produit, id_utilisateur, note, commentaire) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE note = VALUES(note), commentaire = VALUES(commentaire), date_evaluation = CURRENT_TIMESTAMP";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, e.getIdProduit());
        ps.setLong(2, e.getIdUtilisateur());
        ps.setInt(3, e.getNote());
        ps.setString(4, e.getCommentaire());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Evaluation e) throws SQLException {
        String sql = "UPDATE evaluation SET note=?, commentaire=?, date_evaluation=CURRENT_TIMESTAMP WHERE id_evaluation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, e.getNote());
        ps.setString(2, e.getCommentaire());
        ps.setLong(3, e.getIdEvaluation());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM evaluation WHERE id_evaluation=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Evaluation> afficher() throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM evaluation ORDER BY date_evaluation DESC";
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Evaluation e = new Evaluation();
            e.setIdEvaluation(rs.getLong("id_evaluation"));
            e.setIdProduit(rs.getLong("id_produit"));
            e.setIdUtilisateur(rs.getLong("id_utilisateur"));
            e.setNote(rs.getInt("note"));
            e.setCommentaire(rs.getString("commentaire"));
            e.setDateEvaluation(rs.getTimestamp("date_evaluation").toLocalDateTime());
            list.add(e);
        }
        return list;
    }

    public List<Evaluation> getForProduct(long idProduit) throws SQLException {
        List<Evaluation> list = new ArrayList<>();
        String sql = "SELECT * FROM evaluation WHERE id_produit = ? ORDER BY date_evaluation DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, idProduit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Evaluation e = new Evaluation();
            e.setIdEvaluation(rs.getLong("id_evaluation"));
            e.setIdProduit(rs.getLong("id_produit"));
            e.setIdUtilisateur(rs.getLong("id_utilisateur"));
            e.setNote(rs.getInt("note"));
            e.setCommentaire(rs.getString("commentaire"));
            e.setDateEvaluation(rs.getTimestamp("date_evaluation").toLocalDateTime());
            list.add(e);
        }
        return list;
    }

    public int getUserRating(long idProduit, long idUtilisateur) throws SQLException {
        String sql = "SELECT note FROM evaluation WHERE id_produit = ? AND id_utilisateur = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, idProduit);
        ps.setLong(2, idUtilisateur);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("note");
        }
        return 0;
    }
}
