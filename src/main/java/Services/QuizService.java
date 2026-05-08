package services;

import entities.Quiz;
import entities.Quiz.Matiere;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService implements IService<Quiz> {

    private final Connection conn;

    public QuizService() {
        conn = MyDatabase.getInstance().getCnx();
    }

    private static final String SELECT_COLS =
        "q.id_quiz, q.titre, q.description, q.niveau, q.matiere, q.id_createur, " +
        "COALESCE(u.prenom, u.nom_utilisateur, '') AS createur_nom";

    private static final String FROM_JOIN =
        "FROM quiz q LEFT JOIN utilisateurs u ON q.id_createur = u.id_utilisateur";

    public List<Quiz> rechercher(String titre, String niveau, String matiere, String niveauFiltre) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT " + SELECT_COLS + " " + FROM_JOIN + " WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (titre != null && !titre.isBlank()) {
            sql.append(" AND LOWER(q.titre) LIKE ?");
            params.add("%" + titre.trim().toLowerCase() + "%");
        }
        if (niveau != null && !niveau.isBlank()) {
            sql.append(" AND LOWER(q.niveau) LIKE ?");
            params.add("%" + niveau.trim().toLowerCase() + "%");
        }
        if (matiere != null && !matiere.isBlank()) {
            sql.append(" AND LOWER(q.matiere) LIKE ?");
            params.add("%" + matiere.trim().toLowerCase() + "%");
        }
        if (niveauFiltre != null && !niveauFiltre.isBlank()) {
            sql.append(" AND LOWER(q.niveau) = ?");
            params.add(niveauFiltre.trim().toLowerCase());
        }

        sql.append(" ORDER BY CASE ")
            .append("WHEN LOWER(q.niveau)='debutant' THEN 1 ")
            .append("WHEN LOWER(q.niveau)='intermediaire' THEN 2 ")
            .append("WHEN LOWER(q.niveau)='avance' THEN 3 ")
            .append("WHEN LOWER(q.niveau)='expert' THEN 4 ")
            .append("ELSE 5 END, q.id_quiz DESC");

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

    @Override
    public void add(Quiz quiz) throws SQLException {
        String req = "INSERT INTO quiz (titre, description, niveau, matiere, id_createur) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        pst.setString(1, quiz.getTitre());
        pst.setString(2, quiz.getDescription());
        pst.setString(3, quiz.getNiveau());
        pst.setString(4, quiz.getMatiere().name());
        pst.setInt(5, quiz.getIdCreateur());
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            quiz.setId(rs.getInt(1));
        }
    }

    @Override
    public void update(Quiz quiz) throws SQLException {
        String req = "UPDATE quiz SET titre=?, description=?, niveau=?, matiere=? WHERE id_quiz=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setString(1, quiz.getTitre());
        pst.setString(2, quiz.getDescription());
        pst.setString(3, quiz.getNiveau());
        pst.setString(4, quiz.getMatiere().name());
        pst.setInt(5, quiz.getId());
        pst.executeUpdate();
    }

    @Override
    public void delete(Quiz quiz) throws SQLException {
        String req = "DELETE FROM quiz WHERE id_quiz=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, quiz.getId());
        pst.executeUpdate();
    }

    @Override
    public List<Quiz> getAll() throws SQLException {
        String req = "SELECT " + SELECT_COLS + " " + FROM_JOIN + " ORDER BY q.id_quiz DESC";
        List<Quiz> quizzes = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            quizzes.add(mapQuiz(rs));
        }
        return quizzes;
    }

    public Quiz getById(int id) throws SQLException {
        String req = "SELECT " + SELECT_COLS + " " + FROM_JOIN + " WHERE q.id_quiz=?";
        PreparedStatement pst = conn.prepareStatement(req);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapQuiz(rs) : null;
    }

    private Quiz mapQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz();
        quiz.setId(rs.getInt("id_quiz"));
        quiz.setTitre(rs.getString("titre"));
        quiz.setDescription(rs.getString("description"));
        quiz.setNiveau(rs.getString("niveau"));
        String matiere = rs.getString("matiere");
        if (matiere != null && !matiere.isBlank()) {
            quiz.setMatiere(Matiere.valueOf(matiere));
        }
        quiz.setIdCreateur(rs.getInt("id_createur"));
        quiz.setCreateurNom(rs.getString("createur_nom"));
        return quiz;
    }
}
