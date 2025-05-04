package id.ac.ukdw.todolist.Dashboard;

import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ToDoDashboardController implements Initializable {
    // ========== UI COMPONENT DECLARATIONS ==========
    @FXML
    private Label welcomeText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Map<String, Object> userData = ToDoListApplication.getUserData();
        welcomeText.setText("Welcome, " + userData.get("username"));
    }
}
