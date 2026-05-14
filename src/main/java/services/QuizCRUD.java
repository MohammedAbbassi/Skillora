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

    public QuizCRUD() {
        conn = MyBD.getInstance().getConn();
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
                "SELECT id_quiz AS id, titre, description, niveau, matiere, date_creation AS date_ajout FROM quiz WHERE 1=1"
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

        sql.append(" ORDER BY date_creation ")
                .append(dateDesc ? "DESC" : "ASC")
                .append(", id_quiz ")
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
        String req = "INSERT INTO quiz (titre, description, niveau, matiere, id_createur) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, quiz.getNomQuiz());
        pst.setString(2, quiz.getDescription());
        pst.setString(3, quiz.getNiveau());
        pst.setString(4, quiz.getMatiere().name());
        long currentUserId = utils.SessionManager.getCurrentUserId();
        if (currentUserId > 0) {
            pst.setLong(5, currentUserId);
        } else {
            pst.setNull(5, Types.BIGINT);
        }
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            quiz.setId(rs.getInt(1));
        }
    }

    public void modifierQuiz(Quiz quiz) throws SQLException {
        String req = "UPDATE quiz SET titre=?, description=?, niveau=?, matiere=? WHERE id_quiz=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, quiz.getNomQuiz());
        pst.setString(2, quiz.getDescription());
        pst.setString(3, quiz.getNiveau());
        pst.setString(4, quiz.getMatiere().name());
        pst.setInt(5, quiz.getId());
        pst.executeUpdate();
    }

    public void supprimerQuiz(int id) throws SQLException {
        String req = "DELETE FROM quiz WHERE id_quiz=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
    }

    public List<Quiz> afficherQuiz() throws SQLException {
        String req = "SELECT id_quiz AS id, titre, description, niveau, matiere, date_creation AS date_ajout FROM quiz ORDER BY date_creation DESC, id_quiz DESC";
        List<Quiz> quizzes = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            quizzes.add(mapQuiz(rs));
        }

        return quizzes;
    }

    public Quiz getQuizById(int id) throws SQLException {
        String req = "SELECT id_quiz AS id, titre, description, niveau, matiere, date_creation AS date_ajout FROM quiz WHERE id_quiz=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapQuiz(rs) : null;
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
