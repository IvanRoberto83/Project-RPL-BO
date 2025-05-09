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
    @FXML public Label dateLabel;
    @FXML public Button statusBtn;
    @FXML public Button todayBtn;
    @FXML public Button importantBtn;
    @FXML public Button categoryBtn;

    @FXML private ComboBox<String> categoryComboBox; // Tambahan dropdown kategori

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
        initializeCategoryDropdown(); // Tambahan untuk inisialisasi dropdown
    }

    private void setupNavButtons() {
        todayBtn.setOnAction(e -> setActiveButton(todayBtn));
        statusBtn.setOnAction(e -> setActiveButton(statusBtn));
        importantBtn.setOnAction(e -> setActiveButton(importantBtn));
        categoryBtn.setOnAction(e -> setActiveButton(categoryBtn));
    }

    private void setActiveButton(Button activeButton) {
        todayBtn.getStyleClass().remove("active");
        statusBtn.getStyleClass().remove("active");
        importantBtn.getStyleClass().remove("active");
        categoryBtn.getStyleClass().remove("active");

        activeButton.getStyleClass().add("active");
    }

    // ========== Tambahan Method Dropdown ==========
    private void initializeCategoryDropdown() {
        categoryComboBox.getItems().addAll("Work", "Personal", "Study", "Shopping");

        // Contoh: tampilkan pilihan ke console saat dipilih
        categoryComboBox.setOnAction(e -> {
            String selected = categoryComboBox.getValue();
            System.out.println("Kategori dipilih: " + selected);
        });
    }
}
