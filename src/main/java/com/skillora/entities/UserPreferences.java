package com.skillora.entities;

public class UserPreferences {

    private int id;
    private int userId;
    private String font;
    private int fontSize;
    private String theme;
    private String contrastLevel;

    // Constructors
    public UserPreferences() {}

    public UserPreferences(int userId, String font, int fontSize, String theme, String contrastLevel) {
        this.userId = userId;
        this.font = font;
        this.fontSize = fontSize;
        this.theme = theme;
        this.contrastLevel = contrastLevel;
    }

    public UserPreferences(int id, int userId, String font, int fontSize, String theme, String contrastLevel) {
        this.id = id;
        this.userId = userId;
        this.font = font;
        this.fontSize = fontSize;
        this.theme = theme;
        this.contrastLevel = contrastLevel;
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

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getContrastLevel() {
        return contrastLevel;
    }

    public void setContrastLevel(String contrastLevel) {
        this.contrastLevel = contrastLevel;
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "id=" + id +
                ", userId=" + userId +
                ", font='" + font + '\'' +
                ", fontSize=" + fontSize +
                ", theme='" + theme + '\'' +
                ", contrastLevel='" + contrastLevel + '\'' +
                '}';
    }
}