package com.skillora.todo.entities;

import java.time.LocalDateTime;

public class Achievement {
    private int id;
    private int userId;
    private String type;
    private String name;
    private String description;
    private String icon = "🎯";
    private int xpRewarded;
    private LocalDateTime unlockedAt;

    public Achievement() {}

    public Achievement(int userId, String type, String name, String icon, int xpRewarded) {
        this.userId = userId;
        this.type = type;
        this.name = name;
        this.icon = icon;
        this.xpRewarded = xpRewarded;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int getXpRewarded() { return xpRewarded; }
    public void setXpRewarded(int xpRewarded) { this.xpRewarded = xpRewarded; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
}
