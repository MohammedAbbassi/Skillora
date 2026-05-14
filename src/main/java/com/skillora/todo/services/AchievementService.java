package com.skillora.todo.services;

import com.skillora.todo.entities.Achievement;
import utils.MyDatabase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AchievementService {
    private Connection cnx = MyDatabase.getInstance().getCnx();

    private void ensureTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS todo_achievements (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                type VARCHAR(50) NOT NULL,
                name VARCHAR(255) NOT NULL,
                description TEXT,
                icon VARCHAR(50) DEFAULT '\ud83c\udfaf',
                xp_rewarded INT DEFAULT 0,
                unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
            )""";
        try (Statement stmt = cnx.createStatement()) { stmt.execute(sql); }
        catch (SQLException e) { System.err.println("AchievementService.ensureTable: " + e.getMessage()); }

        String badgeSql = """
            CREATE TABLE IF NOT EXISTS todo_badges (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                badge_key VARCHAR(50) NOT NULL,
                badge_name VARCHAR(255) NOT NULL,
                badge_icon VARCHAR(50) DEFAULT '\ud83c\udfc5',
                unlocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_user_badge (user_id, badge_key),
                FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
            )""";
        try (Statement stmt = cnx.createStatement()) { stmt.execute(badgeSql); }
        catch (SQLException e) { System.err.println("AchievementService.ensureBadgeTable: " + e.getMessage()); }
    }

    public AchievementService() { ensureTable(); }

    public void addAchievement(Achievement a) {
        String sql = "INSERT INTO todo_achievements (user_id,type,name,description,icon,xp_rewarded) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, a.getUserId());
            ps.setString(2, a.getType());
            ps.setString(3, a.getName());
            ps.setString(4, a.getDescription());
            ps.setString(5, a.getIcon());
            ps.setInt(6, a.getXpRewarded());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("AchievementService.addAchievement: " + e.getMessage()); }
    }

    public List<Achievement> getByUser(int userId) {
        List<Achievement> list = new ArrayList<>();
        String sql = "SELECT * FROM todo_achievements WHERE user_id=? ORDER BY unlocked_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("AchievementService.getByUser: " + e.getMessage()); }
        return list;
    }

    public boolean hasBadge(int userId, String badgeKey) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM todo_badges WHERE user_id=? AND badge_key=?")) {
            ps.setInt(1, userId);
            ps.setString(2, badgeKey);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { /* ignore */ }
        return false;
    }

    public void awardBadge(int userId, String key, String name, String icon) {
        if (hasBadge(userId, key)) return;
        String sql = "INSERT INTO todo_badges (user_id,badge_key,badge_name,badge_icon) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, key);
            ps.setString(3, name);
            ps.setString(4, icon);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("AchievementService.awardBadge: " + e.getMessage()); }
    }

    public List<String[]> getBadges(int userId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT badge_key,badge_name,badge_icon FROM todo_badges WHERE user_id=? ORDER BY unlocked_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new String[]{rs.getString("badge_key"), rs.getString("badge_name"), rs.getString("badge_icon")});
        } catch (SQLException e) { /* ignore */ }
        return list;
    }

    private Achievement map(ResultSet rs) throws SQLException {
        Achievement a = new Achievement();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setType(rs.getString("type"));
        a.setName(rs.getString("name"));
        a.setDescription(rs.getString("description"));
        a.setIcon(rs.getString("icon"));
        a.setXpRewarded(rs.getInt("xp_rewarded"));
        a.setUnlockedAt(rs.getTimestamp("unlocked_at") != null ? rs.getTimestamp("unlocked_at").toLocalDateTime() : null);
        return a;
    }
}
