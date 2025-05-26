package id.ac.ukdw.todolist.Manager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryManager {
    private final int userId;

    public CategoryManager(int userId) {
        this.userId = userId;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT name FROM category WHERE user_id = ? ORDER BY name")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public String getTaskCategory(int taskId) {
        String sql = "SELECT c.name FROM task t LEFT JOIN category c ON t.category_id = c.id WHERE t.id = ? AND t.user_id = ?";
        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, taskId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int handleCategory(String category) {
        if (category == null || category.isBlank()) return -1;

        try (Connection conn = DBConnectionManager.getConnection()) {
            int existingCategoryId = findExistingCategory(conn, category);
            if (existingCategoryId != -1) {
                return existingCategoryId;
            }

            return createNewCategory(conn, category);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int findExistingCategory(Connection conn, String category) throws SQLException {
        String checkSql = "SELECT id FROM category WHERE name = ? AND user_id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, category);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    private int createNewCategory(Connection conn, String category) throws SQLException {
        String insertCatSql = "INSERT INTO category(name, user_id) VALUES (?, ?)";
        try (PreparedStatement catStmt = conn.prepareStatement(insertCatSql, Statement.RETURN_GENERATED_KEYS)) {
            catStmt.setString(1, category);
            catStmt.setInt(2, userId);
            catStmt.executeUpdate();
            ResultSet generatedKeys = catStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
        }
        return -1;
    }
}