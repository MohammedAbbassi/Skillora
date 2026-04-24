package com.skillora.entities;

public class UserProgress {

    private int id;
    private int userId;
    private int courseId;
    private boolean completed;
    private float progressPercent;
    private float averageScore;

    public UserProgress() {}

    public UserProgress(int userId, int courseId, boolean completed, float progressPercent, float averageScore) {
        this.userId = userId;
        this.courseId = courseId;
        this.completed = completed;
        this.progressPercent = progressPercent;
        this.averageScore = averageScore;
    }

    public UserProgress(int id, int userId, int courseId, boolean completed, float progressPercent, float averageScore) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.completed = completed;
        this.progressPercent = progressPercent;
        this.averageScore = averageScore;
    }

    // Getters w Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public float getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(float progressPercent) {
        this.progressPercent = progressPercent;
    }

    public float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(float averageScore) {
        this.averageScore = averageScore;
    }

    @Override
    public String toString() {
        return "UserProgress{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseId=" + courseId +
                ", completed=" + completed +
                ", progressPercent=" + progressPercent +
                ", averageScore=" + averageScore +
                '}';
    }
}