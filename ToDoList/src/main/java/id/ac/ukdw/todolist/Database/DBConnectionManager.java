package id.ac.ukdw.todolist.Database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnectionManager {
    private static String DB_URL;
    private static Connection connection;

    private DBConnectionManager() {}

    static {
        // Load DB path from config.properties
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("config.properties"));
            String dbPath = props.getProperty("db.path");
            if (dbPath == null || dbPath.isEmpty()) {
                dbPath = "todolist.db"; // fallback default
            }
            DB_URL = "jdbc:sqlite:" + dbPath;
        } catch (IOException e) {
            e.printStackTrace();
            DB_URL = "jdbc:sqlite:todolist.db"; // fallback default
        }
        createTables();  // initialize tables on static init
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String userTable = "CREATE TABLE IF NOT EXISTS user (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "password TEXT NOT NULL" +
                    ");";

            stmt.executeUpdate(userTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
