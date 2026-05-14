package com.skillora.todo.services;

import utils.MyDatabase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

public class ProductivityService {
    private Connection cnx = MyDatabase.getInstance().getCnx();

    private void ensureTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS todo_productivity_stats (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                date DATE NOT NULL,
                tasks_completed INT DEFAULT 0,
                xp_earned INT DEFAULT 0,
                streak_count INT DEFAULT 0,
                productivity_score DOUBLE DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_user_date (user_id, date),
                FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
            )""";
        try (Statement stmt = cnx.createStatement()) { stmt.execute(sql); }
        catch (SQLException e) { System.err.println("ProductivityService.ensureTable: " + e.getMessage()); }
    }

    public ProductivityService() { ensureTable(); }

    public void logTaskCompletion(int userId, int xp) {
        LocalDate today = LocalDate.now();
        String sql = "INSERT INTO todo_productivity_stats (user_id,date,tasks_completed,xp_earned,streak_count,productivity_score) VALUES (?,?,1,?,1,0) ON DUPLICATE KEY UPDATE tasks_completed=tasks_completed+1, xp_earned=xp_earned+?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(today));
            ps.setInt(3, xp);
            ps.setInt(4, xp);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("ProductivityService.logTaskCompletion: " + e.getMessage()); }
        updateStreak(userId);
    }

    private void updateStreak(int userId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        int streak = 1;
        try (PreparedStatement ps = cnx.prepareStatement("SELECT streak_count FROM todo_productivity_stats WHERE user_id=? AND date=?")) {
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(yesterday));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) streak = rs.getInt("streak_count") + 1;
        } catch (SQLException e) { /* ignore */ }

        try (PreparedStatement up = cnx.prepareStatement("UPDATE todo_productivity_stats SET streak_count=? WHERE user_id=? AND date=?")) {
            up.setInt(1, streak);
            up.setInt(2, userId);
            up.setDate(3, java.sql.Date.valueOf(today));
            up.executeUpdate();
        } catch (SQLException e) { /* ignore */ }
    }

    public int getCurrentStreak(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT streak_count FROM todo_productivity_stats WHERE user_id=? ORDER BY date DESC LIMIT 1")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("streak_count");
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    public double getProductivityScore(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT AVG(productivity_score) as avg_score FROM todo_productivity_stats WHERE user_id=? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("avg_score");
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    public Map<String, Long> getWeeklyStats(int userId) {
        Map<String, Long> stats = new LinkedHashMap<>();
        String sql = "SELECT date, tasks_completed FROM todo_productivity_stats WHERE user_id=? AND date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ORDER BY date ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) stats.put(rs.getDate("date").toString(), rs.getLong("tasks_completed"));
        } catch (SQLException e) { /* ignore */ }
        return stats;
    }

    public int getTotalXpEarned(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COALESCE(SUM(xp_earned),0) FROM todo_productivity_stats WHERE user_id=?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    public long getTotalTasksCompleted(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COALESCE(SUM(tasks_completed),0) FROM todo_productivity_stats WHERE user_id=?")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }
}
