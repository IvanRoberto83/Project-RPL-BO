package id.ac.ukdw.todolist.Dashboard;

import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.event.ActionEvent;
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
    @FXML public Label dateLabel;
    @FXML public Button statusBtn;
    @FXML public Button todayBtn;
    @FXML public Button importantBtn;
    @FXML public Button categoryBtn;
    @FXML public Button logOutBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Map<String, Object> userData = ToDoListApplication.getUserData();
        userText.setText(userData.get("username").toString());

        // ========== TODAY'S TIME ==========
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        String formattedDate = localDate.format(formatter);
        dateLabel.setText(formattedDate);
        setupNavButtons();
    }

    private void setupNavButtons() {
        todayBtn.setOnAction(e -> setActiveButton(todayBtn));
        statusBtn.setOnAction(e -> setActiveButton(statusBtn));
        importantBtn.setOnAction(e -> setActiveButton(importantBtn));
        categoryBtn.setOnAction(e -> setActiveButton(categoryBtn));

        setActiveButton(todayBtn);
    }

    private void setActiveButton(Button activeBtn) {
        removeActiveStyle(todayBtn);
        removeActiveStyle(statusBtn);
        removeActiveStyle(importantBtn);
        removeActiveStyle(categoryBtn);

        if (activeBtn.getStyleClass().contains("btnTasks")) {
            activeBtn.getStyleClass().add("active");
        }
    }

    private void removeActiveStyle(Button btn) {
        btn.getStyleClass().remove("active");
    }

    public void onLogOut(ActionEvent actionEvent) {
        ToDoListApplication.setUserData(null);
        ToDoListApplication.setRoot("Login/ToDoLogin", null, false);
    }
}
