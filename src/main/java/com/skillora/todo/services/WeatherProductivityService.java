package com.skillora.todo.services;

import services.WeatherService;
import services.WeatherService.WeatherForecast;
import utils.Session;

import java.time.LocalDate;
import java.util.*;

public class WeatherProductivityService {
    private final WeatherService weatherService = new WeatherService();

    public static class WeatherAdvice {
        public final String condition;
        public final String emoji;
        public final double tempMax;
        public final double tempMin;
        public final int rainChance;
        public final String location;
        public final String productivityTip;
        public final String studyRecommendation;
        public final int focusScore; // 0-100

        public WeatherAdvice(String condition, String emoji, double tempMax, double tempMin, int rainChance, String location, String tip, String study, int focus) {
            this.condition = condition;
            this.emoji = emoji;
            this.tempMax = tempMax;
            this.tempMin = tempMin;
            this.rainChance = rainChance;
            this.location = location;
            this.productivityTip = tip;
            this.studyRecommendation = study;
            this.focusScore = focus;
        }
    }

    public Optional<WeatherAdvice> getAdvice() {
        String location = getUserLocation();
        if (location == null || location.isBlank()) return Optional.empty();
        try {
            WeatherForecast f = weatherService.getForecast(location, LocalDate.now());
            return Optional.of(buildAdvice(f));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getUserLocation() {
        var user = Session.getUser();
        String pays = user != null ? user.getPays() : null;
        if (pays != null && !pays.isBlank()) return pays;
        return "Tunisia";
    }

    private WeatherAdvice buildAdvice(WeatherForecast f) {
        String desc = f.getDescription();
        double temp = f.getMaxTemperature();
        int rain = f.getRainProbability();

        String emoji = getWeatherEmoji(desc);
        String tip = getProductivityTip(desc, temp, rain);
        String study = getStudyRecommendation(desc, temp, rain);
        int focus = calculateFocusScore(desc, temp, rain);

        return new WeatherAdvice(desc, emoji, f.getMaxTemperature(), f.getMinTemperature(), rain, f.getLocation(), tip, study, focus);
    }

    private String getWeatherEmoji(String desc) {
        String d = desc.toLowerCase();
        if (d.contains("orage")) return "\u26C8";
        if (d.contains("pluie")) return "\uD83C\uDF27";
        if (d.contains("neige")) return "\u2744";
        if (d.contains("brouillard")) return "\uD83C\uDF2B";
        if (d.contains("dégagé") || d.contains("clair")) return "\u2600\uFE0F";
        if (d.contains("nuageux")) return "\u26C5";
        return "\uD83C\uDF24";
    }

    private String getProductivityTip(String desc, double temp, int rain) {
        String d = desc.toLowerCase();
        if (d.contains("orage") || d.contains("pluie")) {
            return "Rainy day \u2014 perfect for deep work indoors. Tackle your most complex tasks.";
        }
        if (d.contains("neige")) {
            return "Snowy and quiet \u2014 great for focused reading and research.";
        }
        if (d.contains("brouillard")) {
            return "Low visibility outside, high clarity inside. Review your goals and priorities.";
        }
        if (temp > 35) {
            return "Very hot \u2014 work in short focused bursts. Try the Pomodoro technique.";
        }
        if (temp > 28) {
            return "Warm day \u2014 energy is high. Good for collaborative work and brainstorming.";
        }
        if (temp < 10) {
            return "Cold outside \u2014 stay cozy and power through your task list with hot coffee.";
        }
        if (d.contains("dégagé") || d.contains("clair")) {
            return "Beautiful weather! Consider studying outside or taking active breaks.";
        }
        if (d.contains("nuageux")) {
            return "Mild and calm \u2014 a balanced day for steady, consistent progress.";
        }
        return "Standard conditions \u2014 keep up your rhythm and stay consistent.";
    }

    private String getStudyRecommendation(String desc, double temp, int rain) {
        String d = desc.toLowerCase();
        if (rain > 70 || d.contains("orage")) {
            return "Best to study indoors today. Try flashcards or quiz yourself on recent topics.";
        }
        if (temp > 30) {
            return "Heat can reduce concentration. Study in short 25min blocks with breaks.";
        }
        if (d.contains("dégagé") || d.contains("clair")) {
            return "Great day for outdoor learning! Listen to educational podcasts during a walk.";
        }
        if (d.contains("neige")) {
            return "Cozy indoor study day. Perfect for deep reading or writing summaries.";
        }
        if (d.contains("brouillard")) {
            return "Use this calm day for revision and organizing your study notes.";
        }
        return "A solid day for learning. Review your objectives and make progress on your goals.";
    }

    private int calculateFocusScore(String desc, double temp, int rain) {
        int score = 70;
        if (rain > 80) score += 15;
        else if (rain > 50) score += 5;
        if (temp > 30) score -= 20;
        else if (temp > 20 && temp < 26) score += 15;
        else if (temp < 5) score -= 10;
        String d = desc.toLowerCase();
        if (d.contains("orage")) score -= 15;
        if (d.contains("dégagé") || d.contains("clair")) score += 10;
        if (d.contains("brouillard")) score -= 5;
        return Math.max(0, Math.min(100, score));
    }

    public static String getFocusLabel(int score) {
        if (score >= 85) return "Excellent focus";
        if (score >= 70) return "Good focus";
        if (score >= 50) return "Moderate focus";
        if (score >= 30) return "Low focus";
        return "Challenging focus";
    }
}
