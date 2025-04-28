package id.ac.ukdw.todolist.Database;

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
