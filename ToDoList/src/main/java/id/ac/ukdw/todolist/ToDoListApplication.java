package id.ac.ukdw.todolist;

import id.ac.ukdw.todolist.Database.DBConnectionManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ToDoListApplication extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("ToDoList");
        primaryStage.setScene(new Scene(loadFXML("Login/ToDoLogin")));
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

    static {
        DBConnectionManager.createTables();
    }

    public static void main(String[] args) {
        launch();
    }
}