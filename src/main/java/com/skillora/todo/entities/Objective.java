package com.skillora.todo.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Objective {
    public enum Status { PENDING, IN_PROGRESS, COMPLETED }

    private int id;
    private int userId;
    private String title;
    private String description;
    private LocalDate targetDate;
    private int progress;
    private Status status = Status.PENDING;
    private String motivationalStatus = "";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Objective() {}

    public Objective(int userId, String title, LocalDate targetDate) {
        this.userId = userId;
        this.title = title;
        this.targetDate = targetDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getMotivationalStatus() { return motivationalStatus; }
    public void setMotivationalStatus(String motivationalStatus) { this.motivationalStatus = motivationalStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isCompleted() { return status == Status.COMPLETED || progress >= 100; }

    public String getProgressEmoji() {
        if (progress >= 100) return "\uD83C\uDF89";
        if (progress >= 75) return "\uD83D\uDE80";
        if (progress >= 50) return "\uD83D\uDCAA";
        if (progress >= 25) return "\uD83D\uDD25";
        return "\uD83C\uDFAF";
    }
}
