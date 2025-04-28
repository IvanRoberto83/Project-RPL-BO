package id.ac.ukdw.todolist.Login;

import id.ac.ukdw.todolist.Database.DBConnectionManager;

import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ToDoSignController {
    @FXML
    private Pane bgKanan;

    @FXML
    private Pane bgKiri;

    @FXML
    private TextField regUser;

    @FXML
    private TextField txtUser;

    @FXML
    private PasswordField regPass;

    @FXML
    private PasswordField txtPass;

    @FXML
    private PasswordField newPass;

    @FXML
    private PasswordField confirmPass;

    private static String resetUsername;

    @FXML
    public void onRegister(ActionEvent actionEvent) throws IOException {
        this.loadScene("Login/ToDoRegister.fxml", "ToDoList Register", actionEvent);
    }

    @FXML
    public void onCreate(ActionEvent actionEvent) throws IOException {
        String username = regUser.getText();
        String password = regPass.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Register Failed", "Please fill all the fields");
            return;
        }

        try {
            boolean success = registerUser(username, password);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Registration Successful",
                        "You have successfully registered.");
                loadScene("Login/ToDoLogin.fxml", "ToDoList Login", actionEvent);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Registration Failed",
                        "An error occurred while registering. Please try again.");
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    @FXML
    public void onForgotPassword(MouseEvent mouseEvent) throws IOException {
        String username = txtUser.getText();

        if (username.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Forgot Password Failed", "Please enter your username first");
            return;
        }

        try {
            if (validateUserExists(username)) {
                resetUsername = username;
                loadScene("Login/ToDoForgot.fxml", "ToDoList Forgot Password", mouseEvent);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "User Not Found", "No account found with username: " + username);
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    @FXML
    public void onConfirm(ActionEvent actionEvent) throws IOException {
        String newPassword = newPass.getText();
        String confirmPassword = confirmPass.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password Reset Failed", "Please fill all the fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password Reset Failed", "Passwords do not match");
            return;
        }

        try {
            if (resetUsername == null || resetUsername.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Session Expired", "Please restart the password reset process");
                loadScene("Login/ToDoLogin.fxml", "ToDoList Login", actionEvent);
                return;
            }

            boolean success = updatePassword(resetUsername, newPassword);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password Changed", "Your password has been successfully changed.");
                resetUsername = null;
                loadScene("Login/ToDoLogin.fxml", "ToDoList Login", actionEvent);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Password Change Failed", "An error occurred while updating the password.");
            }
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private void loadScene(String fxmlFile, String title, Event event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource(fxmlFile));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    //    Database
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
                return rs.next(); // Returns true if user exists
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
        showAlert(Alert.AlertType.ERROR, "Database Error", "Error Accessing Database",
                "An error occurred while accessing the database. Please try again.");
    }
}
