package com.skillora.todo.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Task {
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }
    public enum Status { PENDING, IN_PROGRESS, COMPLETED, CANCELLED }

    private int id;
    private int userId;
    private String title;
    private String description;
    private Priority priority = Priority.MEDIUM;
    private LocalDate deadline;
    private Status status = Status.PENDING;
    private String category = "Learning";
    private int progress;
    private int sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task() {}

    public Task(int userId, String title, String category, Priority priority, LocalDate deadline) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isOverdue() {
        return deadline != null && deadline.isBefore(LocalDate.now()) && status != Status.COMPLETED;
    }

    public boolean isCompleted() { return status == Status.COMPLETED; }
}
