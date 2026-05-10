package services;

import entities.QuizResult;
import entities.QuizResultDetail;
import utils.MyBD;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizResultHistoryService {

    private final Connection conn;
    private final Gson gson = new Gson();
    private static boolean tableChecked;
    private static final Type DETAILS_TYPE = new TypeToken<List<QuizResultDetail>>() {}.getType();

    public QuizResultHistoryService() {
        conn = MyBD.getInstance().getConn();
        ensureHistoryTable();
    }

    public void saveResult(QuizResult result) throws SQLException {
        if (result == null || conn == null) {
            return;
        }
        ensureHistoryTable();

        String sql = "INSERT INTO quiz_result_history "
                + "(quiz_id, user_name, quiz_title, final_points, total_points, correct_answers, total_questions, "
                + "bonus_points, anti_cheat_incident_count, anti_cheat_critical_count, terminated_by_anti_cheat, "
                + "anti_cheat_summary, anti_cheat_report_path, completed_at, details_json) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pst.setInt(1, result.getQuizId());
        pst.setString(2, result.getUserName());
        pst.setString(3, result.getQuizTitle());
        pst.setInt(4, result.getFinalPoints());
        pst.setInt(5, result.getTotalPoints());
        pst.setInt(6, result.getCorrectAnswers());
        pst.setInt(7, result.getTotalQuestions());
        pst.setInt(8, result.getBonusPoints());
        pst.setInt(9, result.getAntiCheatIncidentCount());
        pst.setInt(10, result.getAntiCheatCriticalCount());
        pst.setBoolean(11, result.isTerminatedByAntiCheat());
        pst.setString(12, result.getAntiCheatSummary());
        pst.setString(13, result.getAntiCheatReportPath());
        pst.setTimestamp(14, Timestamp.valueOf(result.getCompletedAt() == null ? LocalDateTime.now() : result.getCompletedAt()));
        pst.setString(15, gson.toJson(result.getDetails()));
        pst.executeUpdate();

        ResultSet rs = pst.getGeneratedKeys();
        if (rs.next()) {
            result.setId(rs.getInt(1));
        }
    }

    public List<QuizResult> searchResults(String searchText, String antiCheatFilter) throws SQLException {
        ensureHistoryTable();

        StringBuilder sql = new StringBuilder("SELECT * FROM quiz_result_history WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchText != null && !searchText.isBlank()) {
            sql.append(" AND (LOWER(user_name) LIKE ? OR LOWER(quiz_title) LIKE ?)");
            String term = "%" + searchText.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
        }

        if ("Avec incidents".equalsIgnoreCase(antiCheatFilter)) {
            sql.append(" AND anti_cheat_incident_count > 0");
        } else if ("Sans incidents".equalsIgnoreCase(antiCheatFilter)) {
            sql.append(" AND anti_cheat_incident_count = 0");
        } else if ("Termines anti-triche".equalsIgnoreCase(antiCheatFilter)) {
            sql.append(" AND terminated_by_anti_cheat = TRUE");
        }

        sql.append(" ORDER BY completed_at DESC, id DESC");

        PreparedStatement pst = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            pst.setObject(i + 1, params.get(i));
        }

        List<QuizResult> results = new ArrayList<>();
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            results.add(mapResult(rs));
        }
        return results;
    }

    public QuizResult getResultById(int id) throws SQLException {
        ensureHistoryTable();
        PreparedStatement pst = conn.prepareStatement("SELECT * FROM quiz_result_history WHERE id=?");
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next() ? mapResult(rs) : null;
    }

    private void ensureHistoryTable() {
        if (conn == null || tableChecked) {
            return;
        }

        synchronized (QuizResultHistoryService.class) {
            if (tableChecked) {
                return;
            }
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS quiz_result_history ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "quiz_id INT NULL,"
                        + "user_name VARCHAR(160) NOT NULL,"
                        + "quiz_title VARCHAR(255) NOT NULL,"
                        + "final_points INT NOT NULL DEFAULT 0,"
                        + "total_points INT NOT NULL DEFAULT 0,"
                        + "correct_answers INT NOT NULL DEFAULT 0,"
                        + "total_questions INT NOT NULL DEFAULT 0,"
                        + "bonus_points INT NOT NULL DEFAULT 0,"
                        + "anti_cheat_incident_count INT NOT NULL DEFAULT 0,"
                        + "anti_cheat_critical_count INT NOT NULL DEFAULT 0,"
                        + "terminated_by_anti_cheat BOOLEAN NOT NULL DEFAULT FALSE,"
                        + "anti_cheat_summary TEXT,"
                        + "anti_cheat_report_path TEXT,"
                        + "completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "details_json LONGTEXT,"
                        + "INDEX idx_completed_at (completed_at),"
                        + "INDEX idx_quiz_title (quiz_title),"
                        + "INDEX idx_user_name (user_name)"
                        + ")");
                tableChecked = true;
            } catch (SQLException e) {
                System.out.println("Creation table historique impossible: " + e.getMessage());
            }
        }
    }

    private QuizResult mapResult(ResultSet rs) throws SQLException {
        QuizResult result = new QuizResult();
        result.setId(rs.getInt("id"));
        result.setQuizId(rs.getInt("quiz_id"));
        result.setUserName(rs.getString("user_name"));
        result.setQuizTitle(rs.getString("quiz_title"));
        result.setFinalPoints(rs.getInt("final_points"));
        result.setTotalPoints(rs.getInt("total_points"));
        result.setCorrectAnswers(rs.getInt("correct_answers"));
        result.setTotalQuestions(rs.getInt("total_questions"));
        result.setBonusPoints(rs.getInt("bonus_points"));
        result.setAntiCheatIncidentCount(rs.getInt("anti_cheat_incident_count"));
        result.setAntiCheatCriticalCount(rs.getInt("anti_cheat_critical_count"));
        result.setTerminatedByAntiCheat(rs.getBoolean("terminated_by_anti_cheat"));
        result.setAntiCheatSummary(rs.getString("anti_cheat_summary"));
        result.setAntiCheatReportPath(rs.getString("anti_cheat_report_path"));
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            result.setCompletedAt(completedAt.toLocalDateTime());
        }

        String detailsJson = rs.getString("details_json");
        if (detailsJson != null && !detailsJson.isBlank()) {
            List<QuizResultDetail> details = gson.fromJson(detailsJson, DETAILS_TYPE);
            result.setDetails(details);
        }
        return result;
    }
}
