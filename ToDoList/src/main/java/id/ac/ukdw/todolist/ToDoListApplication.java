package id.ac.ukdw.todolist;

import id.ac.ukdw.todolist.Manager.DBConnectionManager;

import id.ac.ukdw.todolist.Manager.NotificationManager;
import id.ac.ukdw.todolist.Manager.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class ToDoListApplication extends Application {
    private static Stage primaryStage;
    private static Map<String, Object> userData;

    static {
        DBConnectionManager.createTables();
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Login");
        if (SessionManager.getInstance().isLoggedIn() ) {
            primaryStage.setTitle("To Do List");
            primaryStage.setScene(new Scene(loadFXML("Dashboard/ToDoDashboard")));
            primaryStage.setResizable(true);

            Map<String, Object> userData = SessionManager.getInstance().getUserData();
            int userId = (int) userData.get("user_id");

            NotificationManager.checkAndNotifyTasks(userId);
        } else {
            primaryStage.setTitle("Login");
            primaryStage.setScene(new Scene(loadFXML("Login/ToDoLogin")));
            primaryStage.setResizable(false);
        }
        primaryStage.show();
    }

    private static Parent loadFXML(String fxml) {
        FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource(fxml + ".fxml"));
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setRoot(String fxml, String title, boolean isResizeable){
        primaryStage.getScene().setRoot(loadFXML(fxml));
        primaryStage.setTitle(title);
        primaryStage.sizeToScene();
        primaryStage.setResizable(isResizeable);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}