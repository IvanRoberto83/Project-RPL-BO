package id.ac.ukdw.todolist.Manager;

import id.ac.ukdw.todolist.Model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private final int userId;
    private static final String TASK_STATUS_COMPLETED = "completed";
    private static final String TASK_STATUS_IN_PROGRESS = "in_progress";
    private static final String TASK_STATUS_OUTDATED = "outdated";

    public TaskManager(int userId) {
        this.userId = userId;
    }

    public List<Task> fetchTasks(boolean isOutdated) {
        List<Task> tasks = new ArrayList<>();
        String sql = isOutdated ?
                "SELECT id, title, description, important, status, due_date FROM task WHERE user_id = ? AND status = ?" :
                "SELECT id, title, description, important, status, due_date FROM task WHERE user_id = ? AND status != ?";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, TASK_STATUS_OUTDATED);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(createTaskFromResultSet(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public boolean createTask(String title, String description, LocalDate dueDate,
                              int categoryId, boolean isHighPriority) {
        String initialStatus = determineInitialStatus(dueDate);
        String insertSql = "INSERT INTO task (title, description, due_date, category_id, important, user_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, dueDate != null ? dueDate.toString() : null);
            if (categoryId != -1) {
                stmt.setInt(4, categoryId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setInt(5, isHighPriority ? 1 : 0);
            stmt.setInt(6, userId);
            stmt.setString(7, initialStatus);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTask(int taskId, String title, String description,
                              LocalDate dueDate, int categoryId,
                              boolean isHighPriority, boolean isCompleted) {
        String status = determineTaskStatus(dueDate, isCompleted);
        String updateSql = "UPDATE task SET title = ?, description = ?, due_date = ?, category_id = ?, important = ?, status = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, dueDate != null ? dueDate.toString() : null);
            if (categoryId != -1) {
                stmt.setInt(4, categoryId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setInt(5, isHighPriority ? 1 : 0);
            stmt.setString(6, status);
            stmt.setInt(7, taskId);
            stmt.setInt(8, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTask(int taskId) {
        String deleteSql = "DELETE FROM task WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateOutdatedTasks() {
        String updateSql = "UPDATE task SET status = ? WHERE due_date < ? AND status != ? AND status != ? AND user_id = ?";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, TASK_STATUS_OUTDATED);
            stmt.setString(2, LocalDate.now().toString());
            stmt.setString(3, TASK_STATUS_COMPLETED);
            stmt.setString(4, TASK_STATUS_OUTDATED);
            stmt.setInt(5, userId);
            int updatedRows = stmt.executeUpdate();
            if (updatedRows > 0) {
                System.out.println(updatedRows + " tasks marked as outdated");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Task createTaskFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String desc = rs.getString("description");
        boolean isImportant = rs.getInt("important") == 1;
        String status = rs.getString("status");
        String dueDate = rs.getString("due_date");
        Task task = new Task(id, title, desc, status, isImportant);
        if (dueDate != null) {
            task.setDueDate(LocalDate.parse(dueDate));
        }
        return task;
    }

    private String determineInitialStatus(LocalDate dueDate) {
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            return TASK_STATUS_OUTDATED;
        }
        return TASK_STATUS_IN_PROGRESS;
    }

    private String determineTaskStatus(LocalDate dueDate, boolean isCompleted) {
        if (isCompleted) {
            return TASK_STATUS_COMPLETED;
        } else if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            return TASK_STATUS_OUTDATED;
        } else {
            return TASK_STATUS_IN_PROGRESS;
        }
    }

    public String getTaskCategoryName(int taskId) {
        String sql = "SELECT c.name FROM task t LEFT JOIN category c ON t.category_id = c.id WHERE t.id = ?";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }
}