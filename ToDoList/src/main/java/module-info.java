module id.ac.ukdw.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens id.ac.ukdw.todolist to javafx.fxml;
    exports id.ac.ukdw.todolist;
    exports id.ac.ukdw.todolist.Login;
    opens id.ac.ukdw.todolist.Login to javafx.fxml;
}