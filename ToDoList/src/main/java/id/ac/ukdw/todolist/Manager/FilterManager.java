package id.ac.ukdw.todolist.Manager;

import id.ac.ukdw.todolist.Model.Task;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterManager {
    private final TaskManager taskManager;

    public FilterManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public List<Task> filterTasks(
            List<Task> tasks,
            String searchText,
            boolean isTodayFilterActive,
            boolean isImportantFilterActive,
            boolean isStatusFilterActive,
            boolean isCategoryFilterActive,
            Set<String> selectedStatuses,
            Set<String> selectedCategories) {

        List<Task> filtered = tasks;

        // Apply search text filter
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchLower = searchText.toLowerCase().trim();
            filtered = filtered.stream()
                    .filter(task -> matchesSearch(task, searchLower))
                    .collect(Collectors.toList());
        }

        // Apply date filter
        if (isTodayFilterActive) {
            LocalDate today = LocalDate.now();
            filtered = filtered.stream()
                    .filter(task -> task.getDueDate() != null && task.getDueDate().equals(today))
                    .collect(Collectors.toList());
        }

        // Apply priority filter
        if (isImportantFilterActive) {
            filtered = filtered.stream()
                    .filter(Task::getIsImportant)
                    .collect(Collectors.toList());
        }

        // Apply status filter
        if (isStatusFilterActive && !selectedStatuses.isEmpty()) {
            filtered = filtered.stream()
                    .filter(task -> selectedStatuses.contains(task.getStatus()))
                    .collect(Collectors.toList());
        }

        // Apply category filter
        if (isCategoryFilterActive && !selectedCategories.isEmpty()) {
            filtered = filtered.stream()
                    .filter(task -> {
                        String category = taskManager.getTaskCategoryName(task.getId());
                        return selectedCategories.contains(category);
                    })
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    private boolean matchesSearch(Task task, String searchLower) {
        return task.getTitle().toLowerCase().contains(searchLower) ||
                task.getDescription().toLowerCase().contains(searchLower) ||
                taskManager.getTaskCategoryName(task.getId()).toLowerCase().contains(searchLower);
    }
}