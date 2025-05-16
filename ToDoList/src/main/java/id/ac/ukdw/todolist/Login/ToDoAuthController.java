package id.ac.ukdw.todolist.Login;

import id.ac.ukdw.todolist.Database.DBConnectionManager;
import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ToDoAuthController {
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPass;

    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML public PasswordField regRePass;

    @FXML private PasswordField newPass;
    @FXML private PasswordField confirmPass;

    private static String resetUsername;

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

        if (username.length() > 9) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Registration Failed",
                    "Username must be less than 8 characters");
            return;
        }

        try {
            String hashedPassword = hashPassword(password);
            boolean success = registerUser(username, hashedPassword);
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Hashing Failed",
                    "An error occurred while processing the password.");
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

            String hashedPassword = hashPassword(newPassword);
            boolean success = updatePassword(resetUsername, hashedPassword);
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Hashing Failed",
                    "An error occurred while processing the password.");
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
            String hashedPassword = hashPassword(password);
            Map<String, Object> userData = authenticateUser(username, hashedPassword);
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Hashing Failed",
                    "An error occurred while processing the password.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean registerUser(String username, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO user (username, password) VALUES (?, ?)";

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

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

    private Map<String, Object> authenticateUser(String username, String hashedPassword) throws SQLException {
        String sql = "SELECT id, username FROM user WHERE username = ? AND password = ?";

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);

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

    private boolean updatePassword(String username, String hashedPassword) throws SQLException {
        String sql = "UPDATE user SET password = ? WHERE username = ?";

        try (Connection connection = DBConnectionManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, hashedPassword);
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

    // Fungsi sederhana hashing SHA-256
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
