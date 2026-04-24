package com.skillora.entities;

public class UserStats {

    private int id;
    private int userId;
    private int currentXp;
    private int level;
    private String rankTier;

    // Constructors
    public UserStats() {}

    public UserStats(int userId, int currentXp, int level, String rankTier) {
        this.userId = userId;
        this.currentXp = currentXp;
        this.level = level;
        this.rankTier = rankTier;
    }

    public UserStats(int id, int userId, int currentXp, int level, String rankTier) {
        this.id = id;
        this.userId = userId;
        this.currentXp = currentXp;
        this.level = level;
        this.rankTier = rankTier;
    }

    // Getters & Setters
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

    public int getCurrentXp() {
        return currentXp;
    }

    public void setCurrentXp(int currentXp) {
        this.currentXp = currentXp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getRankTier() {
        return rankTier;
    }

    public void setRankTier(String rankTier) {
        this.rankTier = rankTier;
    }

    @Override
    public String toString() {
        return "UserStats{" +
                "id=" + id +
                ", userId=" + userId +
                ", currentXp=" + currentXp +
                ", level=" + level +
                ", rankTier='" + rankTier + '\'' +
                '}';
    }
}