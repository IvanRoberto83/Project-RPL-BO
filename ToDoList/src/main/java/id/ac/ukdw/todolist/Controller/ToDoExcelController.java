package id.ac.ukdw.todolist.Controller;

import id.ac.ukdw.todolist.Manager.DBConnectionManager;
import id.ac.ukdw.todolist.ToDoListApplication;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ToDoExcelController {

    // Class untuk menyimpan error validasi
    public static class ValidationError {
        private int rowNumber;
        private String message;

        public ValidationError(int rowNumber, String message) {
            this.rowNumber = rowNumber;
            this.message = message;
        }

        public int getRowNumber() { return rowNumber; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return "Row " + rowNumber + ": " + message;
        }
    }

    private List<ValidationError> validationErrors = new ArrayList<>();

    public boolean exportTaskToExcel(int userId, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Tasks to Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        fileChooser.setInitialFileName("tasks_export_" + timestamp + ".xlsx");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            return writeTasksToExcel(userId, file);
        }
        return false;
    }

    private boolean writeTasksToExcel(int userId, File file) {
        Connection conn = DBConnectionManager.getConnection();

        String sql = "SELECT t.id, t.title, t.description, t.due_date, " +
                "CASE WHEN c.name IS NULL THEN 'No Category' ELSE c.name END as category_name, " +
                "t.important, t.status " +
                "FROM task t " +
                "LEFT JOIN category c ON t.category_id = c.id " +
                "WHERE t.user_id = ?" +
                "ORDER BY t.id ASC";

        try (Workbook workbook = new XSSFWorkbook();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            Sheet sheet = workbook.createSheet("Tasks");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dataStyle = workbook.createCellStyle();

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Title", "Description", "Due Date", "Category", "Important"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);

                    Cell titleCell = row.createCell(0);
                    titleCell.setCellValue(rs.getString("title"));
                    titleCell.setCellStyle(dataStyle);

                    Cell descCell = row.createCell(1);
                    String description = rs.getString("description");
                    descCell.setCellValue(description != null ? description : "");
                    descCell.setCellStyle(dataStyle);

                    Cell dueDateCell = row.createCell(2);
                    String dueDate = rs.getString("due_date");
                    dueDateCell.setCellValue(dueDate != null ? dueDate : "No due date");
                    dueDateCell.setCellStyle(dataStyle);

                    Cell categoryCell = row.createCell(3);
                    categoryCell.setCellValue(rs.getString("category_name"));
                    categoryCell.setCellStyle(dataStyle);

                    Cell importantCell = row.createCell(4);
                    int important = rs.getInt("important");
                    importantCell.setCellValue(important == 1 ? "Yes" : "No");
                    importantCell.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, (int) (currentWidth * 1.1));
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                workbook.write(outputStream);
                return true;
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Import dengan validasi
    public boolean importTaskFromExcel(Stage stage, int userId) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Tasks from Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            validationErrors.clear();
            boolean result = readTasksFromExcel(file, userId);

            if (!validationErrors.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder("Ditemukan beberapa kesalahan pada data yang diimpor:\n\n");
                for (ValidationError error : validationErrors) {
                    errorMessage.append("• ").append(error.toString()).append("\n");
                }
                errorMessage.append("\nBaris dengan kesalahan akan dilewati. Hanya data yang valid yang berhasil diimpor.");

                showWarnAlert("Import System", errorMessage.toString());
            }

            return result;
        }
        return false;
    }

    private boolean readTasksFromExcel(File file, int userId) {
        try (Workbook workbook = WorkbookFactory.create(file);
             Connection conn = DBConnectionManager.getConnection()) {

            Sheet sheet = workbook.getSheet("Tasks");
            if (sheet == null) {
                System.out.println("Sheet 'Tasks' not found.");
                return false;
            }

            int validRowCount = 0;
            int totalRowCount = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                totalRowCount++;

                String title = getCellValueAsString(row.getCell(0));
                String description = getCellValueAsString(row.getCell(1));
                String dueDateStr = getCellValueAsString(row.getCell(2));
                String categoryName = getCellValueAsString(row.getCell(3));
                String importantStr = getCellValueAsString(row.getCell(4));

                // Validasi data
                if (!validateRowData(i + 1, title, dueDateStr, categoryName)) {
                    continue;
                }

                boolean isImportant = "Yes".equalsIgnoreCase(importantStr);
                Integer categoryId = null;

                // Proses category
                if (categoryName != null && !categoryName.equalsIgnoreCase("No Category")) {
                    try (PreparedStatement categoryStmt = conn.prepareStatement(
                            "SELECT id FROM category WHERE name = ? AND user_id = ?")) {
                        categoryStmt.setString(1, categoryName);
                        categoryStmt.setInt(2, userId);
                        ResultSet rs = categoryStmt.executeQuery();

                        if (rs.next()) {
                            categoryId = rs.getInt("id");
                        } else {
                            // Buat category baru
                            try (PreparedStatement insertCategoryStmt = conn.prepareStatement(
                                    "INSERT INTO category (name, user_id) VALUES (?, ?)",
                                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                                insertCategoryStmt.setString(1, categoryName);
                                insertCategoryStmt.setInt(2, userId);
                                insertCategoryStmt.executeUpdate();

                                ResultSet keys = insertCategoryStmt.getGeneratedKeys();
                                if (keys.next()) {
                                    categoryId = keys.getInt(1);
                                }
                            }
                        }
                    }
                }

                // Cek apakah task sudah ada
                try (PreparedStatement checkTaskStmt = conn.prepareStatement(
                        "SELECT id FROM task WHERE title = ? AND due_date = ? AND user_id = ?")) {
                    checkTaskStmt.setString(1, title);
                    checkTaskStmt.setString(2, parseDate(dueDateStr));
                    checkTaskStmt.setInt(3, userId);
                    ResultSet rs = checkTaskStmt.executeQuery();

                    if (rs.next()) {
                        // Update existing task
                        int taskId = rs.getInt("id");

                        try (PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE task SET description = ?, category_id = ?, important = ? WHERE id = ?")) {
                            updateStmt.setString(1, description);
                            if (categoryId != null)
                                updateStmt.setInt(2, categoryId);
                            else
                                updateStmt.setNull(2, java.sql.Types.INTEGER);
                            updateStmt.setInt(3, isImportant ? 1 : 0);
                            updateStmt.setInt(4, taskId);
                            updateStmt.executeUpdate();
                        }

                    } else {
                        // Insert new task
                        try (PreparedStatement insertStmt = conn.prepareStatement(
                                "INSERT INTO task (title, description, due_date, category_id, important, user_id, status) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, 'in_progress')")) {
                            insertStmt.setString(1, title);
                            insertStmt.setString(2, description);
                            insertStmt.setString(3, parseDate(dueDateStr));
                            if (categoryId != null)
                                insertStmt.setInt(4, categoryId);
                            else
                                insertStmt.setNull(4, java.sql.Types.INTEGER);
                            insertStmt.setInt(5, isImportant ? 1 : 0);
                            insertStmt.setInt(6, userId);
                            insertStmt.executeUpdate();
                        }
                    }
                }

                validRowCount++;
            }

            // Tampilkan jumlah baris yang valid
            showWarnAlert("Import System", "Berhasil mengimpor " + validRowCount + " baris dari " + totalRowCount + " total baris yang diproses.\n");
            return validRowCount > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateRowData(int rowNumber, String title, String dueDateStr, String categoryName) {
        boolean isValid = true;

        // Validasi Title
        if (title == null || title.trim().isEmpty()) {
            validationErrors.add(new ValidationError(rowNumber, "Title tidak boleh kosong"));
            isValid = false;
        } else if (title.trim().length() > 50) {
            validationErrors.add(new ValidationError(rowNumber, "Title tidak boleh lebih dari 50 karakter (saat ini: " + title.trim().length() + ")"));
            isValid = false;
        }

        // Validasi Due Date
        if (dueDateStr == null || dueDateStr.trim().isEmpty() || dueDateStr.equalsIgnoreCase("No due date")) {
            validationErrors.add(new ValidationError(rowNumber, "Due Date tidak boleh kosong"));
            isValid = false;
        } else {
            // Validasi format tanggal
            try {
                LocalDate.parse(dueDateStr.trim());
            } catch (Exception e) {
                validationErrors.add(new ValidationError(rowNumber, "Format Due Date tidak valid (gunakan format: YYYY-MM-DD)"));
                isValid = false;
            }
        }

        // Validasi Category
        if (categoryName == null || categoryName.trim().isEmpty()) {
            validationErrors.add(new ValidationError(rowNumber, "Category tidak boleh kosong"));
            isValid = false;
        } else if (!categoryName.trim().equalsIgnoreCase("No Category") && categoryName.trim().length() > 30) {
            validationErrors.add(new ValidationError(rowNumber, "Category tidak boleh lebih dari 30 karakter (saat ini: " + categoryName.trim().length() + ")"));
            isValid = false;
        }

        return isValid;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            default: return "";
        }
    }

    // Parsing
    private String parseDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.trim().isEmpty()) {
                return null;
            }
            LocalDate date = LocalDate.parse(dateStr.trim());
            return date.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private void showWarnAlert(String title, String message) {
        ToDoListApplication.showAlert(Alert.AlertType.WARNING, "Warning", title, message);
    }
}