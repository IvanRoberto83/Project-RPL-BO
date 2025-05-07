package id.ac.ukdw.todolist.Dashboard;

import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class ToDoDashboardController implements Initializable {
    // ========== UI COMPONENT DECLARATIONS ==========
    @FXML public Label userText;
    @FXML public Label timeToday;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Map<String, Object> userData = ToDoListApplication.getUserData();
        userText.setText(userData.get("username").toString());

        // ========== TODAY'S TIME ==========
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        String formattedDate = localDate.format(formatter);
        timeToday.setText(formattedDate);
    }
}
