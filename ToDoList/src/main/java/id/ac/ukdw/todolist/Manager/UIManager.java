package id.ac.ukdw.todolist.Manager;

import id.ac.ukdw.todolist.Model.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class UIManager {
    private final int userId;
    private final TaskManager taskManager;
    private final Consumer<Task> onEditTask;
    private final Consumer<Integer> onDeleteTask;

    // Constants
    private static final String ICON_COMPLETE = "/id/ac/ukdw/assets/complete.png";
    private static final String ICON_ONGOING = "/id/ac/ukdw/assets/ongoing.png";
    private static final String ICON_NOT_FINISH = "/id/ac/ukdw/assets/not_finish.png";
    private static final String ICON_DELETE = "/id/ac/ukdw/assets/delete.png";

    public UIManager(int userId, TaskManager taskManager, Consumer<Task> onEditTask, Consumer<Integer> onDeleteTask) {
        this.userId = userId;
        this.taskManager = taskManager;
        this.onEditTask = onEditTask;
        this.onDeleteTask = onDeleteTask;
    }

    public void loadTasks(List<Task> tasks, VBox container) {
        container.getChildren().clear();
        for (Task task : tasks) {
            VBox taskCard = createTaskCard(task);
            container.getChildren().add(taskCard);
        }
    }

    private VBox createTaskCard(Task task) {
        VBox taskCard = new VBox();
        taskCard.getStyleClass().add("taskCard");
        taskCard.setStyle("-fx-cursor: hand;");
        taskCard.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                onEditTask.accept(task);
            }
        });

        HBox mainTitleBox = createTitleBox(task);
        Label descLabel = createDescriptionLabel(task);
        HBox statusBox = createStatusBox(task);

        taskCard.getChildren().addAll(mainTitleBox, descLabel, statusBox);
        return taskCard;
    }

    private HBox createTitleBox(Task task) {
        HBox mainTitleBox = new HBox();
        mainTitleBox.setAlignment(Pos.CENTER_LEFT);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label titleLabel = createTitleLabel(task);
        ImageView statusIcon = createStatusIcon(task);
        Button deleteBtn = createDeleteButton(task);

        titleBox.getChildren().addAll(titleLabel, statusIcon);
        mainTitleBox.getChildren().addAll(titleBox, deleteBtn);

        return mainTitleBox;
    }

    private Label createTitleLabel(Task task) {
        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("taskTitle");
        if (task.getIsImportant()) {
            titleLabel.setText("[!] " + task.getTitle());
        }
        return titleLabel;
    }

    private ImageView createStatusIcon(Task task) {
        ImageView statusIcon = new ImageView();
        statusIcon.setFitWidth(24);
        statusIcon.setFitHeight(24);
        statusIcon.setPreserveRatio(true);
        String iconPath = getStatusIconPath(task);
        setImageFromResource(statusIcon, iconPath);
        return statusIcon;
    }

    private String getStatusIconPath(Task task) {
        if (task.isFinished()) {
            return ICON_COMPLETE;
        } else if (task.isInProgress()) {
            return ICON_ONGOING;
        } else {
            return ICON_NOT_FINISH;
        }
    }

    }