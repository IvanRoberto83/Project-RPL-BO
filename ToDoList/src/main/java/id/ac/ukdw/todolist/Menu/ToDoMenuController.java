package id.ac.ukdw.todolist.Menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class ToDoMenuController {

    @FXML
    private Pane bgKanan;

    @FXML
    private Pane bgKiri;

    @FXML
    private CheckBox check1;

    @FXML
    private CheckBox check2;

    @FXML
    private CheckBox check3;

    @FXML
    private CheckBox check4;

    @FXML
    private CheckBox check5;

    @FXML
    private Button createBtn;

    @FXML
    private Label progress1;

    @FXML
    private Label progress2;

    @FXML
    private Label progress3;

    @FXML
    private Label progress4;

    @FXML
    private Label progress5;

    @FXML
    private Label tanggal;

    @FXML
    private Label today;

    @FXML
    private Label user;

    @FXML
    void checkDone(ActionEvent event) {
        if (check1.isSelected()) {
            progress1.setText("Done");
            progress1.setStyle("-fx-text-fill: #22970b;");
        } else {
            progress1.setText("In Progress");
            progress1.setStyle("-fx-text-fill: #f20000;");
        }

        if (check2.isSelected()) {
            progress2.setText("Done");
            progress2.setStyle("-fx-text-fill: #22970b;");
        } else {
            progress2.setText("In Progress");
            progress2.setStyle("-fx-text-fill: #f20000;");
        }

        if (check3.isSelected()) {
            progress3.setText("Done");
            progress3.setStyle("-fx-text-fill: #22970b;");
        } else {
            progress3.setText("In Progress");
            progress3.setStyle("-fx-text-fill: #f20000;");
        }

        if (check4.isSelected()) {
            progress4.setText("Done");
            progress4.setStyle("-fx-text-fill: #22970b;");
        } else {
            progress4.setText("In Progress");
            progress4.setStyle("-fx-text-fill: #f20000;");
        }

        if (check5.isSelected()) {
            progress5.setText("Done");
            progress5.setStyle("-fx-text-fill: #22970b;");
        } else {
            progress5.setText("In Progress");
            progress5.setStyle("-fx-text-fill: #f20000;");
        }
    }

    @FXML
    void onCreate(ActionEvent event) {

    }

    @FXML
    void onDelete(ActionEvent event) {

    }

    @FXML
    void onEdit(ActionEvent event) {

    }

}
