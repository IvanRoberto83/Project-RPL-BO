package id.ac.ukdw.todolist.Model;

import java.time.LocalDate;

public class Task {
    private int id; // Add id field for database-generated ID
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private Boolean isImportant;

    public Task(int id, String title, String description, String status, Boolean isImportant) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.isImportant = isImportant;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public Boolean getIsImportant() { return isImportant; }

    public void setId(int id) { this.id = id; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isFinished() {
        return "completed".equals(status);
    }

    public boolean isInProgress() {
        return "in_progress".equals(status);
    }

    public boolean isOutdated() {
        return "outdated".equals(status);
    }
}