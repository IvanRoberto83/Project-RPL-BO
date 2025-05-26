package id.ac.ukdw.todolist.Controller;

import id.ac.ukdw.todolist.Manager.*;
import id.ac.ukdw.todolist.Model.Task;
import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ToDoDashboardController implements Initializable {
    @FXML public Label userText;
    @FXML public Label dateLabel;
    @FXML public Button statusBtn;
    @FXML public Button todayBtn;
    @FXML public Button importantBtn;
    @FXML public Button categoryBtn;
    // Load Task
    @FXML public VBox taskListContainer;
    @FXML public VBox outdatedTasksContainer;
    // Create Task Field
    @FXML public TextField taskTitleField;
    @FXML public DatePicker dueDatePicker;
    @FXML public TextArea taskDescriptionField;
    @FXML public ComboBox<String> categoryCreateTask;
    @FXML public CheckBox priorityHighCheckBox;
    @FXML public Button createTaskBtn;
    @FXML public Button resetFormBtn;
    @FXML public TextField searchField;
    @FXML public Label featureTitleTask;
    @FXML public CheckBox completedCheckBox;
    // Category Filter
    @FXML private VBox categoryFilterPanel;
    @FXML private VBox categoryCheckBoxContainer;
    // Status Filter
    @FXML private VBox statusFilterPanel;
    @FXML private VBox statusCheckBoxContainer;

    // Manager classes
    private TaskManager taskManager;
    private CategoryManager categoryManager;
    private FilterManager filterManager;
    private UIManager uiManager;

    // Core state
    private Timeline outdatedChecker;
    private int userId;
    private boolean isEditMode = false;
    private int currentEditingTaskId = -1;
    private List<Task> allTasks = new ArrayList<>();
    private List<Task> allOutdatedTasks = new ArrayList<>();

    // Filter state
    private boolean isTodayFilterActive = false;
    private boolean isImportantFilterActive = false;
    private boolean isCategoryFilterActive = false;
    private boolean isStatusFilterActive = false;
    private final Set<String> selectedCategories = new HashSet<>();
    private final Set<String> selectedStatuses = new HashSet<>();

    // Constants
    private static final String TASK_STATUS_COMPLETED = "completed";
    private static final String TASK_STATUS_IN_PROGRESS = "in_progress";
    private final Map<String, String> statusMapping = new HashMap<>();

    public ToDoDashboardController() {
        statusMapping.put("In Progress", "in_progress");
        statusMapping.put("Completed", "completed");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeUserData();
        initializeManagers();
        setupUI();
        setupEventHandlers();
        refreshTasks();
    }

    private void initializeUserData() {
        Map<String, Object> userData = SessionManager.getInstance().getUserData();
        userText.setText(userData.get("username").toString());
        userId = (int) userData.get("user_id");
    }

    private void initializeManagers() {
        taskManager = new TaskManager(userId);
        categoryManager = new CategoryManager(userId);
        filterManager = new FilterManager(taskManager);
        uiManager = new UIManager(userId, taskManager, this::setEditMode, this::deleteTask);
    }

    private void setupUI() {
        setupCategoryComboBox();
        setupDateLabel();
        setupNavButtons();
        setupOutdatedChecker();
        setCreateMode();
    }

    private void setupEventHandlers() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterTasks());
    }

    private void setupDateLabel() {
        LocalDate localDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        dateLabel.setText(localDate.format(formatter));
    }

    // TASK FILTERING METHODS
    private void filterTasks() {
        applyAllFilters();
    }

    // MODE MANAGEMENT
    private void setCreateMode() {
        setMode(false, -1, "Create Task", "CREATE TASK", "RESET");
        hideCompletedCheckbox();
        resetForm();
    }

    private void setEditMode(Task task) {
        setMode(true, task.getId(), "Edit Task", "SAVE TASK", "CANCEL");
        showCompletedCheckbox(task.isFinished());
        fillFormWithTask(task);
    }

    private void setMode(boolean editMode, int taskId, String titleText, String btnText, String resetText) {
        isEditMode = editMode;
        currentEditingTaskId = taskId;
        if (featureTitleTask != null) featureTitleTask.setText(titleText);
        createTaskBtn.setText(btnText);
        resetFormBtn.setText(resetText);
    }

    private void hideCompletedCheckbox() {
        if (completedCheckBox != null) {
            completedCheckBox.setVisible(false);
            completedCheckBox.setManaged(false);
        }
    }

    private void showCompletedCheckbox(boolean selected) {
        if (completedCheckBox != null) {
            completedCheckBox.setVisible(true);
            completedCheckBox.setManaged(true);
            completedCheckBox.setSelected(selected);
        }
    }

    private void fillFormWithTask(Task task) {
        taskTitleField.setText(task.getTitle());
        taskDescriptionField.setText(task.getDescription());
        if (task.getDueDate() != null) {
            dueDatePicker.setValue(task.getDueDate());
        }
        priorityHighCheckBox.setSelected(task.getIsImportant());

        String categoryName = categoryManager.getTaskCategory(task.getId());
        if (categoryName != null) {
            categoryCreateTask.setValue(categoryName);
        } else {
            categoryCreateTask.setValue(null);
            categoryCreateTask.getEditor().clear();
        }
    }

    // TASK MONITORING
    private void setupOutdatedChecker() {
        outdatedChecker = new Timeline(new KeyFrame(Duration.minutes(1), e -> {
            taskManager.updateOutdatedTasks();
            refreshTasks();
        }));
        outdatedChecker.setCycleCount(Timeline.INDEFINITE);
        outdatedChecker.play();
    }

    // TASK REFRESH AND DISPLAY
    private void refreshTasks() {
        allTasks = taskManager.fetchTasks(false);
        allOutdatedTasks = taskManager.fetchTasks(true);
        applyAllFilters();
    }

    // NAVIGATION BUTTONS
    private void setupNavButtons() {
        Button[] navButtons = {todayBtn, statusBtn, importantBtn, categoryBtn};
        for (Button btn : navButtons) {
            btn.setOnAction(e -> setActiveButton(btn, navButtons));
        }
    }

    private void setActiveButton(Button activeBtn, Button[] allButtons) {
        boolean wasActive = activeBtn.getStyleClass().contains("active");

        if (wasActive) {
            activeBtn.getStyleClass().remove("active");
            if (activeBtn == todayBtn) {
                isTodayFilterActive = false;
            } else if (activeBtn == statusBtn) {
                isStatusFilterActive = false;
                hideStatusFilterPanels();
                selectedStatuses.clear();
            } else if (activeBtn == importantBtn) {
                isImportantFilterActive = false;
            } else if (activeBtn == categoryBtn) {
                isCategoryFilterActive = false;
                hideCategoryFilterPanels();
                selectedCategories.clear();
            }
        } else {
            activeBtn.getStyleClass().add("active");
            if (activeBtn == todayBtn) {
                isTodayFilterActive = true;
            } else if (activeBtn == statusBtn) {
                isStatusFilterActive = true;
                toggleStatusFilterPanel();
            } else if (activeBtn == importantBtn) {
                isImportantFilterActive = true;
            } else if (activeBtn == categoryBtn) {
                isCategoryFilterActive = true;
                toggleCategoryFilterPanel();
            }
        }

        applyAllFilters();
    }

    // FILTER APPLICATION
    private void applyAllFilters() {
        List<Task> filteredTasks = filterManager.filterTasks(
                allTasks,
                searchField.getText(),
                isTodayFilterActive,
                isImportantFilterActive,
                isStatusFilterActive,
                isCategoryFilterActive,
                selectedStatuses,
                selectedCategories
        );

        List<Task> filteredOutdatedTasks = filterManager.filterTasks(
                allOutdatedTasks,
                searchField.getText(),
                isTodayFilterActive,
                isImportantFilterActive,
                isStatusFilterActive,
                isCategoryFilterActive,
                selectedStatuses,
                selectedCategories
        );

        uiManager.loadTasks(filteredTasks, taskListContainer);
        uiManager.loadTasks(filteredOutdatedTasks, outdatedTasksContainer);
    }

    public void resetAllFilters() {
        isTodayFilterActive = false;
        isImportantFilterActive = false;
        isCategoryFilterActive = false;
        isStatusFilterActive = false;

        selectedCategories.clear();
        selectedStatuses.clear();

        Button[] navButtons = {todayBtn, statusBtn, importantBtn, categoryBtn};
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active");
        }

        hideCategoryFilterPanels();
        hideStatusFilterPanels();

        searchField.clear();

        applyAllFilters();
    }

    // USER SESSION METHODS
    @FXML
    public void onLogout(ActionEvent actionEvent) {
        if (outdatedChecker != null) {
            outdatedChecker.stop();
        }
        SessionManager.getInstance().logout();
        ToDoListApplication.setRoot("Login/ToDoLogin", "Login", false);
    }

    // TASK CRUD OPERATIONS
    @FXML
    public void onCreateTask(ActionEvent actionEvent) {
        if (taskTitleField.getText().isBlank()) {
            showErrorAlert(isEditMode ? "Update Task Failed" : "Create Task Failed",
                    "Please fill all the fields");
            return;
        }

        if (isEditMode) {
            updateTask();
        } else {
            createTask();
        }
    }

    private void createTask() {
        String title = taskTitleField.getText();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = dueDatePicker.getValue();
        String category = getCategoryValue();
        boolean isHighPriority = priorityHighCheckBox.isSelected();

        int categoryId = categoryManager.handleCategory(category);

        if (taskManager.createTask(title, description, dueDate, categoryId, isHighPriority)) {
            showSuccessAlert("Create Task Successful", "You have successfully created a new task.");
            setCreateMode();
            refreshTasks();
        } else {
            showErrorAlert("Create Task Failed", "Failed to create task.");
        }
    }

    private void updateTask() {
        String title = taskTitleField.getText();
        String description = taskDescriptionField.getText();
        LocalDate dueDate = dueDatePicker.getValue();
        String category = getCategoryValue();
        boolean isHighPriority = priorityHighCheckBox.isSelected();
        boolean isCompleted = completedCheckBox != null && completedCheckBox.isSelected();

        int categoryId = categoryManager.handleCategory(category);

        if (taskManager.updateTask(currentEditingTaskId, title, description, dueDate, categoryId, isHighPriority, isCompleted)) {
            showSuccessAlert("Update Task Successful", "Task has been updated successfully.");
            setCreateMode();
            refreshTasks();
        } else {
            showErrorAlert("Update Task Failed", "Failed to update task.");
        }
    }

    private String getCategoryValue() {
        String category = "";
        if (categoryCreateTask.getValue() != null) {
            category = categoryCreateTask.getValue().trim();
        } else if (categoryCreateTask.getEditor().getText() != null) {
            category = categoryCreateTask.getEditor().getText().trim();
        }
        return category;
    }

    private void deleteTask(int taskId) {
        if (taskManager.deleteTask(taskId)) {
            showSuccessAlert("Task Deleted", "Task deleted successfully!");
            refreshTasks();
        } else {
            showErrorAlert("Delete Failed", "Failed to delete task!");
        }
    }

    @FXML
    public void onReset(ActionEvent event) {
        resetTaskForm();
    }

    private void resetTaskForm() {
        if (isEditMode) {
            setCreateMode();
        } else {
            resetForm();
        }
    }

    // EXCEL IMPORT/EXPORT
    @FXML
    public void onExportTask(ActionEvent actionEvent) {
        ToDoExcelController exporter = new ToDoExcelController();
        boolean success = exporter.exportTaskToExcel(userId, ToDoListApplication.getPrimaryStage());
        if (success) {
            showSuccessAlert("Export Successful", "Tasks exported successfully!");
        } else {
            showErrorAlert("Export Failed", "Failed to export tasks!");
        }
    }

    @FXML
    public void onImportTask(ActionEvent actionEvent) {
        ToDoExcelController importer = new ToDoExcelController();
        boolean success = importer.importTaskFromExcel(ToDoListApplication.getPrimaryStage(), userId);
        if (success) {
            showSuccessAlert("Import Successful", "Tasks imported successfully!");
            refreshTasks();

            List<String> categories = categoryManager.getAllCategories();
            categoryCreateTask.getItems().setAll(categories);
        } else {
            showErrorAlert("Import Failed", "Tasks import failed!");
        }
    }

    // UTILITY METHODS
    private void showSuccessAlert(String title, String message) {
        ToDoListApplication.showAlert(Alert.AlertType.INFORMATION, "Success", title, message);
    }

    private void showErrorAlert(String title, String message) {
        ToDoListApplication.showAlert(Alert.AlertType.ERROR, "Error", title, message);
    }

    public void resetForm() {
        taskTitleField.setText("");
        dueDatePicker.setValue(null);
        taskDescriptionField.setText("");
        categoryCreateTask.getSelectionModel().clearSelection();
        categoryCreateTask.getEditor().setText("");
        categoryCreateTask.setStyle("");
        priorityHighCheckBox.setSelected(false);
    }

    // CATEGORY FILTER METHODS
    private void toggleCategoryFilterPanel() {
        boolean isVisible = categoryFilterPanel.isVisible();

        if (isVisible) {
            hideCategoryFilterPanels();
        } else {
            categoryFilterPanel.setVisible(true);
            categoryFilterPanel.setManaged(true);
            buildCategoryFilter();
        }
    }

    private void hideCategoryFilterPanels() {
        categoryFilterPanel.setVisible(false);
        categoryFilterPanel.setManaged(false);
        categoryCheckBoxContainer.getChildren().clear();
    }

    private void hideStatusFilterPanels() {
        statusFilterPanel.setVisible(false);
        statusFilterPanel.setManaged(false);
        statusCheckBoxContainer.getChildren().clear();
    }

    public void buildCategoryFilter() {
        categoryCheckBoxContainer.getChildren().clear();
        String sql = "SELECT DISTINCT name FROM category WHERE user_id = ? ORDER BY name";
        uiManager.buildFilterPanel(
                sql,
                categoryCheckBoxContainer,
                selectedCategories,
                this::applyAllFilters,
                "No categories available",
                "Failed to load categories"
        );
    }

    // STATUS FILTER METHODS
    private void toggleStatusFilterPanel() {
        boolean isVisible = statusFilterPanel.isVisible();

        if (isVisible) {
            hideStatusFilterPanels();
        } else {
            statusFilterPanel.setVisible(true);
            statusFilterPanel.setManaged(true);
            buildStatusFilter();
        }
    }

    public void buildStatusFilter() {
        statusCheckBoxContainer.getChildren().clear();
        for (String label : statusMapping.keySet()) {
            CheckBox checkBox = new CheckBox(label);
            String statusValue = statusMapping.get(label);
            checkBox.setSelected(selectedStatuses.contains(statusValue));
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    selectedStatuses.add(statusValue);
                } else {
                    selectedStatuses.remove(statusValue);
                }
                applyAllFilters();
            });
            statusCheckBoxContainer.getChildren().add(checkBox);
        }
    }

    // CATEGORY MANAGEMENT
    private void setupCategoryComboBox() {
        List<String> categories = categoryManager.getAllCategories();
        categoryCreateTask.getItems().setAll(categories);
        categoryCreateTask.setEditable(true);
        categoryCreateTask.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                String borderStyle = categoryCreateTask.getItems().contains(newValue.trim()) ?
                        "-fx-border-color: #4CAF50; -fx-border-width: 2px; -fx-border-radius: 5px;" :
                        "-fx-border-color: #FF9800; -fx-border-width: 2px; -fx-border-radius: 5px;";
                categoryCreateTask.setStyle(borderStyle);
            } else {
                categoryCreateTask.setStyle("");
            }
        });
    }
}