package services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChapitreService implements CoursChapitre<Chapitre> {

    Connection conn;

    public ChapitreService() throws SQLException {
        conn = DBConnection.getInstance().getConn();
    }

    @Override
    public void ajouter(Chapitre chapitre) throws SQLException {

        String req = "INSERT INTO chapitre (titre, contenu, ordre, duree, pdf_url, cours_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, chapitre.getTitre());
        pst.setString(2, chapitre.getContenu());
        pst.setInt(3, chapitre.getOrdre());
        pst.setInt(4, chapitre.getDuree());
        pst.setString(5, chapitre.getPdf_url());
        pst.setInt(6, chapitre.getCours_id());

        pst.executeUpdate();
        System.out.println("Chapitre ajouté !");
    }

    @Override
    public void modifier(Chapitre chapitre) throws SQLException {

        String req = "UPDATE chapitre SET titre=?, contenu=?, ordre=?, duree=?, pdf_url=?, cours_id=? WHERE id_chapitre=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, chapitre.getTitre());
        pst.setString(2, chapitre.getContenu());
        pst.setInt(3, chapitre.getOrdre());
        pst.setInt(4, chapitre.getDuree());
        pst.setString(5, chapitre.getPdf_url());
        pst.setInt(6, chapitre.getCours_id());
        pst.setInt(7, chapitre.getId_chapitre());

        pst.executeUpdate();
        System.out.println("Chapitre modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM chapitre WHERE id_chapitre=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();

        System.out.println("Chapitre supprimé !");
    }

    @Override
    public List<Chapitre> afficher() throws SQLException {

        String req = "SELECT * FROM chapitre";
        List<Chapitre> chapitres = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Chapitre ch = new Chapitre();

            ch.setId_chapitre(rs.getInt("id_chapitre"));
            ch.setTitre(rs.getString("titre"));
            ch.setContenu(rs.getString("contenu"));
            ch.setOrdre(rs.getInt("ordre"));
            ch.setDuree(rs.getInt("duree"));
            ch.setPdf_url(rs.getString("pdf_url"));
            ch.setCours_id(rs.getInt("cours_id"));

            chapitres.add(ch);
        }

        return chapitres;
    }
}