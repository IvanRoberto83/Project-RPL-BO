package id.ac.ukdw.todolist.Dashboard;

import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox; // Import VBox

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Arrays; // Import Arrays for dummy categories

public class ToDoDashboardController implements Initializable {
    // ========== UI COMPONENT DECLARATIONS ==========
    @FXML public Label userText;
    @FXML public Label dateLabel;
    @FXML public Button statusBtn;
    @FXML public Button todayBtn;
    @FXML public Button importantBtn;
    @FXML public Button categoryBtn;
    @FXML public Button logOutBtn;

    // NEW FXML COMPONENTS FOR CATEGORY FILTER
    @FXML private VBox categoryFilterPanel;
    @FXML private VBox categoryCheckBoxContainer;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Map<String, Object> userData = ToDoListApplication.getUserData();
        userText.setText(userData.get("username").toString());

        // ========== TODAY'S TIME ==========
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID")); // Added yyyy for full date
        String formattedDate = localDate.format(formatter);
        dateLabel.setText(formattedDate);
        setupNavButtons();
    }

    private void setupNavButtons() {
        todayBtn.setOnAction(e -> {
            setActiveButton(todayBtn);
            // Tambahkan logika untuk menampilkan tugas hari ini
            hideAllFilterPanels(); // Sembunyikan panel filter lain
            // refreshTaskList(FilterType.TODAY); // Contoh pemanggilan refresh
        });
        statusBtn.setOnAction(e -> {
            setActiveButton(statusBtn);
            // Tambahkan logika untuk menampilkan tugas berdasarkan status
            hideAllFilterPanels(); // Sembunyikan panel filter lain
            // refreshTaskList(FilterType.STATUS);
        });
        importantBtn.setOnAction(e -> {
            setActiveButton(importantBtn);
            // Tambahkan logika untuk menampilkan tugas penting
            hideAllFilterPanels(); // Sembunyikan panel filter lain
            // refreshTaskList(FilterType.IMPORTANT);
        });
        categoryBtn.setOnAction(e -> {
            setActiveButton(categoryBtn);
            // Logika khusus untuk tombol kategori
            toggleCategoryFilterPanel(); // Memunculkan/menyembunyikan panel kategori
            // refreshTaskList(FilterType.CATEGORY); // Mungkin akan membutuhkan parameter kategori yang dipilih
        });

        // Set tombol 'Today' sebagai aktif secara default saat aplikasi dimulai
        setActiveButton(todayBtn);
        hideAllFilterPanels(); // Pastikan semua filter panel tersembunyi saat inisialisasi
    }

    // Perubahan di sini: Implementasi setActiveButton
    private void setActiveButton(Button activeBtn) {
        // Hapus style "active" dari semua tombol navigasi
        todayBtn.getStyleClass().remove("active");
        statusBtn.getStyleClass().remove("active");
        importantBtn.getStyleClass().remove("active");
        categoryBtn.getStyleClass().remove("active");

        // Tambahkan style "active" ke tombol yang sedang diklik
        activeBtn.getStyleClass().add("active");
    }

    private void removeActiveStyle(Button btn) {
        // Metode ini sekarang tidak diperlukan secara langsung karena setActiveButton menanganinya
        // Anda bisa menghapus metode ini jika tidak ada penggunaan lain.
        btn.getStyleClass().remove("active");
    }


    // NEW METHODS FOR CATEGORY FILTER
    private void toggleCategoryFilterPanel() {
        boolean isVisible = categoryFilterPanel.isVisible();
        hideAllFilterPanels(); // Sembunyikan panel filter lain terlebih dahulu
        categoryFilterPanel.setVisible(!isVisible);
        categoryFilterPanel.setManaged(!isVisible);

        // Jika panel kategori akan ditampilkan, isi checkboxnya
        if (!isVisible) {
            populateCategoryCheckBoxes();
        }
    }

    private void hideAllFilterPanels() {
        // Sembunyikan semua panel filter yang mungkin ada di masa mendatang
        categoryFilterPanel.setVisible(false);
        categoryFilterPanel.setManaged(false);
        // Tambahkan panel filter lain di sini jika ada (misal statusFilterPanel dll)
    }

    private void populateCategoryCheckBoxes() {
        categoryCheckBoxContainer.getChildren().clear(); // Hapus checkbox yang ada sebelumnya

        // Contoh kategori (nantinya bisa diambil dari database)
        String[] categories = {"Work", "Personal", "Study", "Shopping", "Health", "Fitness", "Financial", "Home"};

        for (String category : categories) {
            CheckBox checkBox = new CheckBox(category);
            checkBox.getStyleClass().add("category-checkbox"); // Tambahkan style class jika diperlukan

            // Tambahkan listener ke checkbox jika ingin memfilter saat diklik
            // Anda bisa menyimpan daftar kategori yang dipilih dalam sebuah List<String>
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    System.out.println("Kategori dipilih: " + category);
                    // Tambahkan kategori ke daftar filter aktif
                } else {
                    System.out.println("Kategori tidak dipilih: " + category);
                    // Hapus kategori dari daftar filter aktif
                }
                // Panggil metode untuk me-refresh daftar tugas dengan filter yang baru
                // refreshTaskList();
            });
            categoryCheckBoxContainer.getChildren().add(checkBox);
        }
    }

    public void onLogOut(ActionEvent actionEvent) {
        ToDoListApplication.setUserData(null);
        ToDoListApplication.setRoot("Login/ToDoLogin", "Login", false);
    }

    public void onTheme(ActionEvent actionEvent) {
        // Logika untuk mengganti tema
        System.out.println("Tombol Tema diklik!");
        // Anda bisa menambahkan toggle light/dark theme di sini
    }

    // Anda perlu mengimplementasikan metode onAction untuk TextField, DatePicker, TextArea, ComboBox, dan Buttons lainnya
    // Contoh:
    // public void onCreateTaskBtn(ActionEvent event) { ... }
    // public void onResetFormBtn(ActionEvent event) { ... }
    // public void onImportBtn(ActionEvent event) { ... }
    // public void onExportBtn(ActionEvent event) { ... }
    // public void onDownloadTemplateBtn(ActionEvent event) { ... }
    // public void onSearchFieldAction(ActionEvent event) { ... }
}