package id.ac.ukdw.todolist.Manager;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class NotificationManager {

    public static void checkAndNotifyTasks(int userId) {
        if (!SystemTray.isSupported()) return;

        String query = "SELECT title, due_date FROM task WHERE status = 'in_progress' AND user_id = ?";
        Image iconImage = Toolkit.getDefaultToolkit().createImage(new byte[0]);

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            SystemTray tray = SystemTray.getSystemTray();

            while (rs.next()) {
                String title = rs.getString("title");
                LocalDate dueDate = LocalDate.parse(rs.getString("due_date"));

                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);

                if (daysLeft < 0) continue;

                TrayIcon icon = new TrayIcon(iconImage, "To-Do List", null);
                icon.setImageAutoSize(true);

                String message = switch ((int) daysLeft) {
                    case 0 -> "Tugas '" + title + "' jatuh tempo HARI INI!";
                    case 1 -> "Tugas '" + title + "' tersisa 1 hari lagi.";
                    case 2 -> "Tugas '" + title + "' tersisa 2 hari lagi.";
                    case 3 -> "Tugas '" + title + "' tersisa 3 hari lagi.";
                    default -> null;
                };

                if (message != null) {
                    try {
                        tray.add(icon);
                        icon.displayMessage("Deadline Task", message, TrayIcon.MessageType.WARNING);
                    } catch (AWTException ignored) {}
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}