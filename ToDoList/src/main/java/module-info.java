module id.ac.ukdw.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports id.ac.ukdw.todolist;
    exports id.ac.ukdw.todolist.Login;
    exports id.ac.ukdw.todolist.Dashboard;

    opens id.ac.ukdw.todolist to javafx.fxml;
    opens id.ac.ukdw.todolist.Login to javafx.fxml;
    opens id.ac.ukdw.todolist.Dashboard to javafx.fxml;
}