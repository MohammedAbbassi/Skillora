package com.skillora.todo.services;

import com.skillora.todo.entities.Task;
import utils.MyDatabase;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskService {
    private Connection cnx = MyDatabase.getInstance().getCnx();

    private void ensureTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS todo_tasks (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                priority ENUM('LOW','MEDIUM','HIGH','URGENT') DEFAULT 'MEDIUM',
                deadline DATE,
                status ENUM('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT 'PENDING',
                category VARCHAR(50) DEFAULT 'Learning',
                progress INT DEFAULT 0,
                sort_order INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES utilisateurs(id_utilisateur) ON DELETE CASCADE
            )""";
        try (Statement stmt = cnx.createStatement()) { stmt.execute(sql); }
        catch (SQLException e) { System.err.println("TaskService.ensureTable: " + e.getMessage()); }
    }

    public TaskService() { ensureTable(); }

    public int add(Task t) {
        String sql = "INSERT INTO todo_tasks (user_id,title,description,priority,deadline,status,category,progress,sort_order) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getUserId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getDescription());
            ps.setString(4, t.getPriority().name());
            ps.setDate(5, t.getDeadline() != null ? Date.valueOf(t.getDeadline()) : null);
            ps.setString(6, t.getStatus().name());
            ps.setString(7, t.getCategory());
            ps.setInt(8, t.getProgress());
            ps.setInt(9, t.getSortOrder());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println("TaskService.add: " + e.getMessage()); }
        return -1;
    }

    public void update(Task t) {
        String sql = "UPDATE todo_tasks SET title=?,description=?,priority=?,deadline=?,status=?,category=?,progress=?,sort_order=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getPriority().name());
            ps.setDate(4, t.getDeadline() != null ? Date.valueOf(t.getDeadline()) : null);
            ps.setString(5, t.getStatus().name());
            ps.setString(6, t.getCategory());
            ps.setInt(7, t.getProgress());
            ps.setInt(8, t.getSortOrder());
            ps.setInt(9, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("TaskService.update: " + e.getMessage()); }
    }

    public void delete(int id) {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM todo_tasks WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("TaskService.delete: " + e.getMessage()); }
    }

    public List<Task> getByUser(int userId) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM todo_tasks WHERE user_id=? ORDER BY sort_order ASC, created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("TaskService.getByUser: " + e.getMessage()); }
        return list;
    }

    public List<Task> getByStatus(int userId, String status) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM todo_tasks WHERE user_id=? AND status=? ORDER BY sort_order ASC, created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("TaskService.getByStatus: " + e.getMessage()); }
        return list;
    }

    public List<Task> search(int userId, String q) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM todo_tasks WHERE user_id=? AND (title LIKE ? OR description LIKE ?) ORDER BY sort_order ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            String p = "%" + q + "%";
            ps.setString(2, p);
            ps.setString(3, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("TaskService.search: " + e.getMessage()); }
        return list;
    }

    public List<Task> getByCategory(int userId, String category) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM todo_tasks WHERE user_id=? AND category=? ORDER BY sort_order ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("TaskService.getByCategory: " + e.getMessage()); }
        return list;
    }

    public List<Task> getOverdue(int userId) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM todo_tasks WHERE user_id=? AND deadline IS NOT NULL AND deadline < CURDATE() AND status != 'COMPLETED' ORDER BY deadline ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("TaskService.getOverdue: " + e.getMessage()); }
        return list;
    }

    public List<Task> getDueSoon(int userId, int days) {
        List<Task> list = new ArrayList<>();
        LocalDate future = LocalDate.now().plusDays(days);
        String sql = "SELECT * FROM todo_tasks WHERE user_id=? AND deadline IS NOT NULL AND deadline BETWEEN CURDATE() AND ? AND status != 'COMPLETED' ORDER BY deadline ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(future));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { System.err.println("TaskService.getDueSoon: " + e.getMessage()); }
        return list;
    }

    public void updateStatus(int id, String status) {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE todo_tasks SET status=?, progress=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, "COMPLETED".equals(status) ? 100 : 0);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("TaskService.updateStatus: " + e.getMessage()); }
    }

    public void reorder(int id, int sortOrder) {
        try (PreparedStatement ps = cnx.prepareStatement("UPDATE todo_tasks SET sort_order=? WHERE id=?")) {
            ps.setInt(1, sortOrder);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("TaskService.reorder: " + e.getMessage()); }
    }

    public long countCompleted(int userId) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM todo_tasks WHERE user_id=? AND status='COMPLETED'")) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { System.err.println("TaskService.countCompleted: " + e.getMessage()); }
        return 0;
    }

    public long countByStatus(int userId, String status) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM todo_tasks WHERE user_id=? AND status=?")) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { System.err.println("TaskService.countByStatus: " + e.getMessage()); }
        return 0;
    }

    public long countByCategory(int userId, String category) {
        try (PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM todo_tasks WHERE user_id=? AND category=?")) {
            ps.setInt(1, userId);
            ps.setString(2, category);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) { /* ignore */ }
        return 0;
    }

    private Task map(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setPriority(Task.Priority.valueOf(rs.getString("priority")));
        Date dl = rs.getDate("deadline");
        if (dl != null) t.setDeadline(dl.toLocalDate());
        t.setStatus(Task.Status.valueOf(rs.getString("status")));
        t.setCategory(rs.getString("category"));
        t.setProgress(rs.getInt("progress"));
        t.setSortOrder(rs.getInt("sort_order"));
        t.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        t.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
        return t;
    }
}
