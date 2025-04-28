module id.ac.ukdw.todolist {
    requires javafx.controls;
    requires javafx.fxml;


    opens id.ac.ukdw.todolist to javafx.fxml;
    exports id.ac.ukdw.todolist;
}