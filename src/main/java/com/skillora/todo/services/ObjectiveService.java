package com.skillora.todo.services;

import com.skillora.todo.entities.Objective;
import utils.MyDatabase;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ObjectiveService {
    private Connection cnx = MyDatabase.getInstance().getCnx();

    private void ensureTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS todo_objectives (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                target_date DATE,
                progress INT DEFAULT 0,
                status VARCHAR(20) DEFAULT 'PENDING',
                motivational_status VARCHAR(100) DEFAULT '',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
            )""";
        try (Statement stmt = cnx.createStatement()) { stmt.execute(sql); }
        catch (SQLException e) { System.err.println("ObjectiveService.ensureTable: " + e.getMessage()); }
        addStatusColumn();
    }

    private void addStatusColumn() {
        try (Statement stmt = cnx.createStatement()) {
            stmt.execute("ALTER TABLE todo_objectives ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING'");
        } catch (SQLException e) { /* column may already exist */ }
    }

    public ObjectiveService() { ensureTable(); }

    public int add(Objective o) {
        String sql = "INSERT INTO todo_objectives (user_id,title,description,target_date,progress,status,motivational_status) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getUserId());
            ps.setString(2, o.getTitle());
            ps.setString(3, o.getDescription());
            ps.setDate(4, o.getTargetDate() != null ? Date.valueOf(o.getTargetDate()) : null);
            ps.setInt(5, o.getProgress());
            ps.setString(6, o.getStatus().name());
            ps.setString(7, o.getMotivationalStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("ObjectiveService.add: " + e.getMessage()); }
        return -1;
    }

    public void update(Objective o) {
        String sql = "UPDATE todo_objectives SET title=?,description=?,target_date=?,progress=?,status=?,motivational_status=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, o.getTitle());
            ps.setString(2, o.getDescription());
            ps.setDate(3, o.getTargetDate() != null ? Date.valueOf(o.getTargetDate()) : null);
            ps.setInt(4, o.getProgress());
            ps.setString(5, o.getStatus().name());
            ps.setString(6, o.getMotivationalStatus());
            ps.setInt(7, o.getId());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("ObjectiveService.update: " + e.getMessage()); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM todo_objectives WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("ObjectiveService.delete: " + e.getMessage()); }
    }

    public List<Objective> getByUser(int userId) {
        List<Objective> list = new ArrayList<>();
        String sql = "SELECT * FROM todo_objectives WHERE user_id=? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("ObjectiveService.getByUser: " + e.getMessage()); }
        return list;
    }

    public List<Objective> getByStatus(int userId, String status) {
        List<Objective> list = new ArrayList<>();
        String sql = "SELECT * FROM todo_objectives WHERE user_id=? AND status=? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("ObjectiveService.getByStatus: " + e.getMessage()); }
        return list;
    }

    public void updateStatus(int id, String status) {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE todo_objectives SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("ObjectiveService.updateStatus: " + e.getMessage()); }
    }

    public void linkTask(int objectiveId, int taskId) {
        try (PreparedStatement ps = cnx.prepareStatement("INSERT IGNORE INTO todo_objective_tasks (objective_id, task_id) VALUES (?,?)")) {
            ps.setInt(1, objectiveId);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("ObjectiveService.linkTask: " + e.getMessage()); }
    }

    public void unlinkTask(int objectiveId, int taskId) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM todo_objective_tasks WHERE objective_id=? AND task_id=?")) {
            ps.setInt(1, objectiveId);
            ps.setInt(2, taskId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("ObjectiveService.unlinkTask: " + e.getMessage()); }
    }

    public List<Integer> getLinkedTaskIds(int objectiveId) {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement("SELECT task_id FROM todo_objective_tasks WHERE objective_id=?")) {
            ps.setInt(1, objectiveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (SQLException e) { System.err.println("ObjectiveService.getLinkedTaskIds: " + e.getMessage()); }
        return ids;
    }

    public void recalculateProgress(int objectiveId) {
        List<Integer> taskIds = getLinkedTaskIds(objectiveId);
        if (taskIds.isEmpty()) return;
        String placeholders = String.join(",", taskIds.stream().map(i -> "?").toArray(String[]::new));
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN status='COMPLETED' THEN 1 ELSE 0 END) as done FROM todo_tasks WHERE id IN (" + placeholders + ")";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            for (int i = 0; i < taskIds.size(); i++) ps.setInt(i + 1, taskIds.get(i));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int done = rs.getInt("done");
                int pct = total > 0 ? (int) ((double) done / total * 100) : 0;
                try (PreparedStatement up = cnx.prepareStatement("UPDATE todo_objectives SET progress=? WHERE id=?")) {
                    up.setInt(1, pct);
                    up.setInt(2, objectiveId);
                    up.executeUpdate();
                }
            }
        } catch (SQLException e) { System.err.println("ObjectiveService.recalculateProgress: " + e.getMessage()); }
    }

    public void markAsCompleted(int objectiveId) {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE todo_objectives SET progress=100, status='COMPLETED' WHERE id=?")) {
            ps.setInt(1, objectiveId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("ObjectiveService.markAsCompleted: " + e.getMessage()); }
    }

    private Objective map(ResultSet rs) throws SQLException {
        Objective o = new Objective();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setTitle(rs.getString("title"));
        o.setDescription(rs.getString("description"));
        Date d = rs.getDate("target_date");
        if (d != null) o.setTargetDate(d.toLocalDate());
        o.setProgress(rs.getInt("progress"));
        String st = rs.getString("status");
        o.setStatus(st != null ? Objective.Status.valueOf(st) : Objective.Status.PENDING);
        o.setMotivationalStatus(rs.getString("motivational_status"));
        o.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        o.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return o;
    }
}
