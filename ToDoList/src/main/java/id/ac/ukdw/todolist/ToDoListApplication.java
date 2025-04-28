package id.ac.ukdw.todolist;

import id.ac.ukdw.todolist.Database.DBConnectionManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ToDoListApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ToDoListApplication.class.getResource("Login/ToDoLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("ToDoList Login");
        stage.setScene(scene);
        stage.show();
    }

    static {
        DBConnectionManager.createTables();
    }

    public static void main(String[] args) {
        launch();
    }
}