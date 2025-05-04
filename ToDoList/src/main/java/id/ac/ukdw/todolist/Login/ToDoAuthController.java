package id.ac.ukdw.todolist.Login;

import id.ac.ukdw.todolist.Database.DBConnectionManager;
import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ToDoAuthController {
    // ========== UI COMPONENT DECLARATIONS ==========
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML public PasswordField regRePass;

    @FXML private PasswordField newPass;
    @FXML private PasswordField confirmPass;

    private static String resetUsername;

    // ========== NAVIGATION FUNCTIONS ==========

    @FXML
    public void onRegister(MouseEvent mouseEvent) {
        ToDoListApplication.setRoot("Login/ToDoRegister", "Register", false);
    }

    @FXML
    public void onSignUp(MouseEvent mouseEvent) {
        ToDoListApplication.setRoot("Login/ToDoLogin", "Login", false);
    }

    @FXML
    public void onForgotPassword(MouseEvent mouseEvent) {
        String username = txtUser.getText();

        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Forgot Password Failed",
                    "Please enter your username first");
            return;
        }

        try {
            if (validateUserExists(username)) {
                resetUsername = username;
                ToDoListApplication.setRoot("Login/ToDoForgot", "Forgot Password", false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "User Not Found",
                        "No account found with username: " + username);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    // ========== PRIMARY ACTION FUNCTIONS ==========

    @FXML
    public void onCreate(ActionEvent actionEvent) {
        String username = regUser.getText();
        String password = regPass.getText();
        String rePassword = regRePass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Registration Failed",
                    "Please fill all the fields");
            return;
        }

        if (!password.equals(rePassword)) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Registration Failed",
                    "Passwords do not match");
            return;
        }

        try {
            boolean success = registerUser(username, password);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Registration Successful",
                        "You have successfully registered.");
                ToDoListApplication.setRoot("Login/ToDoLogin", "Login", false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Registration Failed",
                        "An error occurred while registering. Please try again.");
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    @FXML
    public void onConfirm(ActionEvent actionEvent) {
        String newPassword = newPass.getText();
        String confirmPassword = confirmPass.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Password Reset Failed",
                    "Please fill all the fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Password Reset Failed",
                    "Passwords do not match");
            return;
        }

        try {
            if (resetUsername == null || resetUsername.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Session Expired",
                        "Please restart the password reset process");
                ToDoListApplication.setRoot("Login/ToDoLogin", "Login", false);
                return;
            }

            boolean success = updatePassword(resetUsername, newPassword);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Password Changed",
                        "Your password has been successfully changed.");
                resetUsername = null;
                ToDoListApplication.setRoot("Login/ToDoLogin", "Login", false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Password Change Failed",
                        "An error occurred while updating the password.");
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    @FXML
    public void onLogin(ActionEvent actionEvent) {
        String username = txtUser.getText();
        String password = txtPass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Login Failed",
                    "Please fill all the fields");
            return;
        }

        try {
            Map<String, Object> userData = authenticateUser(username, password);
            if (userData != null) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Login Successful",
                        "You have successfully logged in.");

                ToDoListApplication.setUserData(userData);
                ToDoListApplication.setRoot("Dashboard/ToDoDashboard", "ToDoList", false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Login Failed",
                        "Invalid username or password.");
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    // ========== UTILITY FUNCTIONS ==========

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ========== DATABASE FUNCTIONS ==========

    private boolean registerUser(String username, String password) throws SQLException {
        String sql = "INSERT INTO user (username, password) VALUES (?, ?)";

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private boolean validateUserExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE username = ?";

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE username = ? AND password = ?";

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Map<String, Object> authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT id, username FROM user WHERE username = ? AND password = ?";

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("user_id", rs.getInt("id"));
                    userData.put("username", rs.getString("username"));
                    return userData;
                }
                return null;
            }
        }
    }

    private boolean updatePassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE username = ?";

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, username);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private void handleDatabaseError(SQLException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Database Error",
                "Error Accessing Database",
                "An error occurred while accessing the database. Please try again.");
    }
}