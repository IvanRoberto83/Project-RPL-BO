package id.ac.ukdw.todolist.Manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnectionManager {
    private static final String DB_URL = "jdbc:sqlite:todolist.db";
    private static Connection connection;

    private DBConnectionManager() {}

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void setConnection(Connection connection) {
        DBConnectionManager.connection = connection;
    }

    public static void createTables() {
        getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `user` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`username` TEXT NOT NULL," +
                    "`password` TEXT NOT NULL)");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `category` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`user_id` INTEGER NOT NULL," +
                    "`name` TEXT NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES user(id))");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS `task` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "`user_id` INTEGER NOT NULL," +
                    "`title` TEXT NOT NULL," +
                    "`due_date` TEXT," +
                    "`description` TEXT," +
                    "`category_id` INTEGER," +
                    "`important` INTEGER DEFAULT 0," +
                    "`status` TEXT DEFAULT 'in_progress'," +
                    "FOREIGN KEY(category_id) REFERENCES category(id)," +
                    "FOREIGN KEY(user_id) REFERENCES user(id))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
