package services;

import entities.Quiz;
import entities.Quiz.Matiere;
import interfaces.InterfaceCRUD;
import utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizCRUD implements InterfaceCRUD<Quiz> {

    private final Connection conn;
    private static boolean dateColumnChecked;

    public QuizCRUD() {
        conn = MyBD.getInstance().getConn();
        ensureDateAjoutColumn();
    }

    /**
     * Recherche simple, rapide et insensible a la casse.
     * Tous les filtres sont optionnels (null / vide => ignore).
     * La recherche est partielle via LIKE (%terme%).
     */
    public List<Quiz> rechercherQuizzes(String titre, String niveau, String matiere) throws SQLException {
        return rechercherQuizzes(titre, niveau, matiere, null);
    }

    public List<Quiz> rechercherQuizzes(String titre, String niveau, String matiere, String niveauFiltre) throws SQLException {
        return rechercherQuizzes(titre, niveau, matiere, niveauFiltre, true);
    }

    public List<Quiz> rechercherQuizzes(String titre, String matiere, String niveauFiltre, boolean dateDesc) throws SQLException {
        return rechercherQuizzes(titre, null, matiere, niveauFiltre, dateDesc);
    }

    /**
     * @param niveauFiltre exact match (Tous => null). Utile pour filtrer/tri par niveau depuis l'UI.
     */
    public List<Quiz> rechercherQuizzes(String titre, String niveau, String matiere, String niveauFiltre,
                                        boolean dateDesc) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id, titre, description, niveau, matiere, date_ajout FROM quiz WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (titre != null && !titre.isBlank()) {
            sql.append(" AND LOWER(titre) LIKE ?");
            params.add("%" + titre.trim().toLowerCase() + "%");
        }
        if (niveau != null && !niveau.isBlank()) {
            sql.append(" AND LOWER(niveau) LIKE ?");
            params.add("%" + niveau.trim().toLowerCase() + "%");
        }
        if (matiere != null && !matiere.isBlank()) {
            sql.append(" AND LOWER(matiere) LIKE ?");
            params.add("%" + matiere.trim().toLowerCase() + "%");
        }
        if (niveauFiltre != null && !niveauFiltre.isBlank()) {
            sql.append(" AND LOWER(niveau) = ?");
            params.add(niveauFiltre.trim().toLowerCase());
        }

        sql.append(" ORDER BY date_ajout ")
                .append(dateDesc ? "DESC" : "ASC")
                .append(", id ")
                .append(dateDesc ? "DESC" : "ASC");

        List<Quiz> quizzes = new ArrayList<>();
        PreparedStatement pst = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            pst.setObject(i + 1, params.get(i));
        }
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            quizzes.add(mapQuiz(rs));
        }
        return quizzes;
    }

    public void ajouterQuiz(Quiz quiz) throws SQLException {
        String req = "INSERT INTO quiz (titre, description, niveau, matiere, date_ajout) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, quiz.getNomQuiz());
        pst.setString(2, quiz.getDescription());
        pst.setString(3, quiz.getNiveau());
        pst.setString(4, quiz.getMatiere().name());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            quiz.setId(rs.getInt(1));
        }
    }

    public void modifierQuiz(Quiz quiz) throws SQLException {
        String req = "UPDATE quiz SET titre=?, description=?, niveau=?, matiere=? WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, quiz.getNomQuiz());
        pst.setString(2, quiz.getDescription());
        pst.setString(3, quiz.getNiveau());
        pst.setString(4, quiz.getMatiere().name());
        pst.setInt(5, quiz.getId());
        pst.executeUpdate();
    }

    public void supprimerQuiz(int id) throws SQLException {
        String req = "DELETE FROM quiz WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    public List<Quiz> afficherQuiz() throws SQLException {
        String req = "SELECT id, titre, description, niveau, matiere, date_ajout FROM quiz ORDER BY date_ajout DESC, id DESC";
        List<Quiz> quizzes = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            quizzes.add(mapQuiz(rs));
        }

        return quizzes;
    }

    public Quiz getQuizById(int id) throws SQLException {
        String req = "SELECT id, titre, description, niveau, matiere, date_ajout FROM quiz WHERE id=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapQuiz(rs) : null;
    }

    private void ensureDateAjoutColumn() {
        if (conn == null || dateColumnChecked) {
            return;
        }

        synchronized (QuizCRUD.class) {
            if (dateColumnChecked) {
                return;
            }
            try {
                try (Statement check = conn.createStatement();
                     ResultSet rs = check.executeQuery("SHOW COLUMNS FROM quiz LIKE 'date_ajout'")) {
                    if (!rs.next()) {
                        try (Statement st = conn.createStatement()) {
                            st.executeUpdate("ALTER TABLE quiz ADD COLUMN date_ajout TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
                        }
                    }
                }
                dateColumnChecked = true;
            } catch (SQLException e) {
                System.out.println("Verification date_ajout impossible: " + e.getMessage());
            }
        }
    }

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getInt("id"));
        quiz.setNomQuiz(rs.getString("titre"));
        quiz.setDescription(rs.getString("description"));
        quiz.setNiveau(rs.getString("niveau"));
        String matiere = rs.getString("matiere");
        if (matiere != null && !matiere.isBlank()) {
            quiz.setMatiere(Matiere.valueOf(matiere));
        }
        Timestamp dateAjout = rs.getTimestamp("date_ajout");
        if (dateAjout != null) {
            quiz.setDateAjout(dateAjout.toLocalDateTime());
        }
        return quiz;
    }

    @Override
    public void ajouter(Quiz quiz) throws SQLException {
        ajouterQuiz(quiz);
    }

    @Override
    public void modifier(Quiz quiz) throws SQLException {
        modifierQuiz(quiz);
    }

    @Override
    public void supprimer(int id) throws SQLException {
        supprimerQuiz(id);
    }

    @Override
    public List<Quiz> afficher() throws SQLException {
        return afficherQuiz();
    }
}
