package services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursCRUD implements InterfaceCRUD<CoursCrud> {

    Connection conn;

    public CoursCRUD() {
        conn = MyBD.getInstance().getConn();
    }

    @Override
    public void ajouter(CoursCRUD cours) throws SQLException {

        String req = "INSERT INTO cours (titre, description, domaine, niveau, duree, date_creation, instructeur) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, cours.getTitre());
        pst.setString(2, cours.getDescription());
        pst.setString(3, cours.getDomaine());
        pst.setString(4, cours.getNiveau());
        pst.setInt(5, cours.getDuree());
        pst.setDate(6, new java.sql.Date(cours.getDate_creation().getTime()));
        pst.setString(7, cours.getInstructeur());

        pst.executeUpdate();
        System.out.println("Cours ajouté !");
    }

    @Override
    public void modifier(CoursCRUD cours) throws SQLException {

        String req = "UPDATE cours SET titre=?, description=?, domaine=?, niveau=?, duree=?, date_creation=?, instructeur=? WHERE id_cours=?";

        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, cours.getTitre());
        pst.setString(2, cours.getDescription());
        pst.setString(3, cours.getDomaine());
        pst.setString(4, cours.getNiveau());
        pst.setInt(5, cours.getDuree());
        pst.setDate(6, new java.sql.Date(cours.getDate_creation().getTime()));
        pst.setString(7, cours.getInstructeur());
        pst.setInt(8, cours.getId_cours());

        pst.executeUpdate();
        System.out.println("Cours modifié !");
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String req = "DELETE FROM cours WHERE id_cours=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();

        System.out.println("Cours supprimé !");
    }

    @Override
    public List<CoursCRUD> afficher() throws SQLException {

        String req = "SELECT * FROM cours";
        List<CoursCRUD> coursList = new ArrayList<>();

        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            CoursCRUD c = new CoursCRUD();

            c.setid_cours(rs.getInt("id_cours"));
            c.setTitre(rs.getString("titre"));
            c.setDescription(rs.getString("description"));
            c.setDomaine(rs.getString("domaine"));
            c.setNiveau(rs.getString("niveau"));
            c.setDuree(rs.getInt("duree"));
            c.setDate_creation(rs.getDate("date_creation"));
            c.setInstructeur(rs.getString("instructeur"));

            coursList.add(c);
        }

        return coursList;
    }
}
