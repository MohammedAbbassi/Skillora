package com.skillora.todo.controllers;

import com.skillora.todo.services.AchievementService;
import com.skillora.todo.services.ProductivityService;
import com.skillora.todo.services.WeatherProductivityService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import utils.Session;

import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProductivityDashboardController implements Initializable {

    @FXML private Label totalCompletedLabel;
    @FXML private Label streakLabel;
    @FXML private Label xpEarnedLabel;
    @FXML private Label productivityScoreLabel;
    @FXML private Label badgeCountLabel;
    @FXML private VBox weatherCard;
    @FXML private BarChart<String, Number> weeklyChart;
    @FXML private FlowPane badgeFlowPane;

    private final ProductivityService productivityService = new ProductivityService();
    private final AchievementService achievementService = new AchievementService();
    private final WeatherProductivityService weatherService = new WeatherProductivityService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refresh();
    }

    public void refresh() {
        int uid = getUserId();
        if (uid == 0) return;

        long completed = productivityService.getTotalTasksCompleted(uid);
        int streak = productivityService.getCurrentStreak(uid);
        int xp = productivityService.getTotalXpEarned(uid);
        double score = productivityService.getProductivityScore(uid);

        totalCompletedLabel.setText(String.valueOf(completed));
        streakLabel.setText(streak + " days");
        xpEarnedLabel.setText(String.valueOf(xp));
        productivityScoreLabel.setText(String.format("%.0f%%", score * 100));

        loadWeeklyChart(uid);
        loadBadges(uid);
        loadWeather();
    }

    private void loadWeeklyChart(int uid) {
        weeklyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tasks Completed");

        Map<String, Long> weekly = productivityService.getWeeklyStats(uid);
        if (weekly.isEmpty()) {
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (String d : days) series.getData().add(new XYChart.Data<>(d, 0));
        } else {
            for (Map.Entry<String, Long> e : weekly.entrySet()) {
                String label = e.getKey().length() > 5 ? e.getKey().substring(5) : e.getKey();
                series.getData().add(new XYChart.Data<>(label, e.getValue()));
            }
        }
        weeklyChart.getData().add(series);
    }

    private void loadBadges(int uid) {
        badgeFlowPane.getChildren().clear();
        var badges = achievementService.getBadges(uid);
        badgeCountLabel.setText(badges.size() + " badges");

        if (badges.isEmpty()) {
            Label empty = new Label("Complete tasks to earn badges!");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 700; -fx-padding: 20;");
            badgeFlowPane.getChildren().add(empty);
            return;
        }

        for (String[] b : badges) {
            VBox card = new VBox(6);
            card.setAlignment(Pos.CENTER);
            card.setPadding(new Insets(14, 18, 14, 18));
            card.setStyle("-fx-background-color: #f8fbff; -fx-background-radius: 14; -fx-border-color: #dbeafe; -fx-border-radius: 14;");

            Label icon = new Label(b[2]);
            icon.setStyle("-fx-font-size: 28px;");
            Label name = new Label(b[1]);
            name.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
            name.setTextAlignment(TextAlignment.CENTER);
            card.getChildren().addAll(icon, name);
            badgeFlowPane.getChildren().add(card);
        }
    }

    private void loadWeather() {
        weatherCard.getChildren().clear();
        new Thread(() -> {
            Optional<WeatherProductivityService.WeatherAdvice> opt = weatherService.getAdvice();
            Platform.runLater(() -> {
                if (opt.isEmpty()) {
                    Label err = new Label("Could not load weather data.");
                    err.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                    weatherCard.getChildren().add(err);
                    return;
                }
                var w = opt.get();
                weatherCard.getChildren().setAll(buildWeatherContent(w));
            });
        }).start();
    }

    private VBox buildWeatherContent(WeatherProductivityService.WeatherAdvice w) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(Double.MAX_VALUE);

        Label emojiLabel = new Label(w.emoji);
        emojiLabel.setStyle("-fx-font-size: 36px;");

        Label condition = new Label(w.condition);
        condition.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        Label tempLabel = new Label(String.format("%.0f\u00B0C / %.0f\u00B0C  \u2602 %d%%", w.tempMax, w.tempMin, w.rainChance));
        tempLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-weight: 700;");

        Label locLabel = new Label("📍 " + w.location);
        locLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        // Focus score bar
        HBox focusRow = new HBox(8);
        focusRow.setAlignment(Pos.CENTER_LEFT);
        focusRow.setMaxWidth(Double.MAX_VALUE);
        Label focusLabel = new Label("Focus: ");
        focusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #4F46E5;");
        ProgressBar focusBar = new ProgressBar(w.focusScore / 100.0);
        focusBar.setPrefWidth(120);
        focusBar.setPrefHeight(8);
        String barColor = w.focusScore >= 70 ? "#10b981" : w.focusScore >= 40 ? "#f59e0b" : "#ef4444";
        focusBar.setStyle("-fx-accent: " + barColor + "; -fx-background-radius: 999;");
        Label focusPct = new Label(w.focusScore + "%");
        focusPct.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: " + barColor + ";");
        focusRow.getChildren().addAll(focusLabel, focusBar, focusPct);

        // Separator
        Label sep = new Label("\u2501".repeat(20));
        sep.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 10px;");

        // Tips
        Label tipTitle = new Label("\uD83D\uDCA1 Productivity Tip");
        tipTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        Label tipText = new Label(w.productivityTip);
        tipText.setWrapText(true);
        tipText.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
        tipText.setMaxWidth(240);

        Label studyTitle = new Label("\uD83D\uDCDA Study Recommendation");
        studyTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        Label studyText = new Label(w.studyRecommendation);
        studyText.setWrapText(true);
        studyText.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
        studyText.setMaxWidth(240);

        box.getChildren().addAll(emojiLabel, condition, tempLabel, locLabel, focusRow, sep, tipTitle, tipText, studyTitle, studyText);
        return box;
    }

    private int getUserId() {
        var user = Session.getUser();
        return user != null ? user.getId() : 0;
    }
}
