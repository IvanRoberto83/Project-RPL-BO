module id.ac.ukdw.todolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.poi.ooxml;
    requires java.desktop;

    exports id.ac.ukdw.todolist;
    exports id.ac.ukdw.todolist.Controller;

    opens id.ac.ukdw.todolist to javafx.fxml;
    opens id.ac.ukdw.todolist.Login to javafx.fxml;
    opens id.ac.ukdw.todolist.Dashboard to javafx.fxml;
    opens id.ac.ukdw.todolist.Controller to javafx.fxml;
}