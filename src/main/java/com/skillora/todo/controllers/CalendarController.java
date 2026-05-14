package com.skillora.todo.controllers;

import com.skillora.todo.entities.Task;
import com.skillora.todo.services.GoogleCalendarService;
import com.skillora.todo.services.TaskService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import utils.Session;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarController implements Initializable {

    @FXML private Label monthLabel;
    @FXML private GridPane calendarGrid;
    @FXML private VBox taskDetailPanel;
    @FXML private Label selectedDateLabel;
    @FXML private Label gcalIndicator;
    @FXML private VBox taskDetailList;
    @FXML private Button connectCalendarBtn;
    @FXML private Button syncTasksBtn;

    private final TaskService taskService = new TaskService();
    private final GoogleCalendarService gcalService = GoogleCalendarService.getInstance();
    private YearMonth currentMonth = YearMonth.now();
    private LocalDate selectedDate;
    private List<Map<String, String>> gcalEvents = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateConnectionUI();
        render();
    }

    private int getUserId() {
        var user = Session.getUser();
        return user != null ? user.getId() : 0;
    }

    @FXML
    private void onConnectCalendar() {
        if (gcalService.isConnected()) {
            gcalService.disconnect();
            updateConnectionUI();
            render();
            return;
        }
        connectCalendarBtn.setDisable(true);
        connectCalendarBtn.setText("Connecting...");
        gcalService.connect().thenAccept(success -> {
            Platform.runLater(() -> {
                connectCalendarBtn.setDisable(false);
                updateConnectionUI();
                if (success) render();
            });
        });
    }

    @FXML
    private void onSyncTasks() {
        if (!gcalService.isConnected()) return;
        syncTasksBtn.setDisable(true);
        syncTasksBtn.setText("Syncing...");
        new Thread(() -> {
            gcalService.syncAllTasks(getUserId());
            Platform.runLater(() -> {
                syncTasksBtn.setDisable(false);
                syncTasksBtn.setText("Sync Tasks");
                render();
            });
        }).start();
    }

    private void updateConnectionUI() {
        if (gcalService.isConnected()) {
            connectCalendarBtn.setText("❌ Disconnect Calendar");
            connectCalendarBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 8 16; -fx-cursor: hand;");
            syncTasksBtn.setVisible(true);
            syncTasksBtn.setManaged(true);
        } else {
            connectCalendarBtn.setText("🔗 Connect Google Calendar");
            connectCalendarBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4F46E5; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 8 16; -fx-cursor: hand;");
            syncTasksBtn.setVisible(false);
            syncTasksBtn.setManaged(false);
        }
    }

    @FXML
    private void onPrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        render();
    }

    @FXML
    private void onNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        render();
    }

    @FXML
    private void onToday() {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        render();
    }

    private void render() {
        monthLabel.setText(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear());
        calendarGrid.getChildren().clear();

        List<Task> allTasks = taskService.getByUser(getUserId());
        Map<LocalDate, List<Task>> tasksByDate = allTasks.stream()
            .filter(t -> t.getDeadline() != null)
            .collect(Collectors.groupingBy(Task::getDeadline));

        // Fetch Google Calendar events if connected
        if (gcalService.isConnected()) {
            try {
                LocalDate start = currentMonth.atDay(1);
                LocalDate end = currentMonth.atEndOfMonth();
                gcalEvents = gcalService.fetchEvents(start, end);
            } catch (Exception e) {
                System.err.println("GCal fetch: " + e.getMessage());
            }
        }

        Map<LocalDate, List<Map<String, String>>> gcalByDate = gcalEvents.stream()
            .filter(e -> e.get("start") != null && e.get("start").length() >= 10)
            .collect(Collectors.groupingBy(e -> {
                try { return LocalDate.parse(e.get("start").substring(0, 10)); }
                catch (Exception ex) { return null; }
            }));
        gcalByDate.values().removeIf(Objects::isNull);

        LocalDate firstOfMonth = currentMonth.atDay(1);
        DayOfWeek startDay = firstOfMonth.getDayOfWeek();
        int colOffset = (startDay.getValue() + 6) % 7;
        int daysInMonth = currentMonth.lengthOfMonth();
        int rows = (int) Math.ceil((colOffset + daysInMonth) / 7.0);

        calendarGrid.getRowConstraints().clear();
        calendarGrid.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setFillWidth(true);
            calendarGrid.getColumnConstraints().add(cc);
        }
        for (int i = 0; i < rows; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setFillHeight(true);
            rc.setVgrow(Priority.ALWAYS);
            rc.setMinHeight(60);
            calendarGrid.getRowConstraints().add(rc);
        }

        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            int col = (colOffset + day - 1) % 7;
            int row = (colOffset + day - 1) / 7;

            VBox cell = new VBox(4);
            cell.setAlignment(Pos.TOP_CENTER);
            cell.setPadding(new Insets(6, 4, 6, 4));
            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(cell, Priority.ALWAYS);

            String bg = "#ffffff";
            String fg = "#0f172a";
            if (date.equals(today)) { bg = "#eef2ff"; fg = "#4F46E5"; }
            String border = date.equals(today) ? "#4F46E5" : "#e2e8f0";
            cell.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10; -fx-border-color: " + border + "; -fx-border-radius: 10; -fx-border-width: " + (date.equals(today) ? "2" : "1") + ";");

            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: " + (date.equals(today) ? "900" : "700") + "; -fx-text-fill: " + fg + ";");

            VBox indicators = new VBox(2);
            indicators.setAlignment(Pos.CENTER);

            List<Task> dayTasks = tasksByDate.getOrDefault(date, Collections.emptyList());
            long overdue = dayTasks.stream().filter(Task::isOverdue).count();
            long pending = dayTasks.stream().filter(t -> !t.isCompleted()).count();
            long completed = dayTasks.stream().filter(Task::isCompleted).count();

            HBox dotRow = new HBox(3);
            dotRow.setAlignment(Pos.CENTER);

            if (overdue > 0) {
                Circle dot = new Circle(4, Color.valueOf("#ef4444"));
                dotRow.getChildren().add(dot);
            } else if (pending > 0) {
                Circle dot = new Circle(4, Color.valueOf("#4F46E5"));
                dotRow.getChildren().add(dot);
            }
            if (completed > 0) {
                Circle dot = new Circle(4, Color.valueOf("#10b981"));
                dotRow.getChildren().add(dot);
            }

            List<Map<String, String>> dayGcal = gcalByDate.getOrDefault(date, Collections.emptyList());
            if (!dayGcal.isEmpty()) {
                Label gcalDot = new Label("G");
                gcalDot.setStyle("-fx-font-size: 8px; -fx-font-weight: 900; -fx-text-fill: white; -fx-background-color: #4285F4; -fx-background-radius: 50%; -fx-pref-width: 14; -fx-pref-height: 14; -fx-alignment: center;");
                dotRow.getChildren().add(gcalDot);
            }

            if (!dotRow.getChildren().isEmpty()) indicators.getChildren().add(dotRow);

            cell.getChildren().addAll(dayLabel, indicators);

            LocalDate clickDate = date;
            cell.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    selectedDate = clickDate;
                    List<Map<String, String>> gcalEvts = gcalByDate.getOrDefault(clickDate, Collections.emptyList());
                    showDayTasks(clickDate, tasksByDate.getOrDefault(clickDate, Collections.emptyList()), gcalEvts);
                }
            });

            calendarGrid.add(cell, col, row);
        }
    }

    private void showDayTasks(LocalDate date, List<Task> tasks, List<Map<String, String>> gcalEvts) {
        taskDetailPanel.setVisible(true);
        taskDetailPanel.setManaged(true);
        selectedDateLabel.setText("📅 " + date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        taskDetailList.getChildren().clear();

        if (gcalService.isConnected() && !gcalEvts.isEmpty()) {
            gcalIndicator.setText("☁️ " + gcalEvts.size() + " Google event(s)");
            gcalIndicator.setVisible(true);
        } else {
            gcalIndicator.setVisible(false);
        }

        for (Map<String, String> ev : gcalEvts) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setMaxWidth(Double.MAX_VALUE);
            row.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 10; -fx-border-color: #4285F444; -fx-border-radius: 10;");

            Label icon = new Label("📅");
            icon.setStyle("-fx-font-size: 14px;");

            Label title = new Label(ev.get("summary"));
            title.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #1e3a8a;");
            HBox.setHgrow(title, Priority.ALWAYS);

            Label source = new Label("Google");
            source.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: #4285F4; -fx-background-color: #4285F418; -fx-background-radius: 999; -fx-padding: 2 8;");

            row.getChildren().addAll(icon, title, source);
            taskDetailList.getChildren().add(row);
        }

        if (tasks.isEmpty() && gcalEvts.isEmpty()) {
            Label empty = new Label("No tasks for this day.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-font-weight: 600; -fx-padding: 10;");
            taskDetailList.getChildren().add(empty);
            return;
        }

        tasks.sort(Comparator.comparing(Task::getPriority));

        for (Task t : tasks) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 14, 10, 14));
            row.setMaxWidth(Double.MAX_VALUE);

            String statusColor, bgColor;
            if (t.isCompleted()) { statusColor = "#10b981"; bgColor = "#f0fdf4"; }
            else if (t.isOverdue()) { statusColor = "#ef4444"; bgColor = "#fef2f2"; }
            else { statusColor = "#3b82f6"; bgColor = "#ffffff"; }

            row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; -fx-border-color: " + statusColor + "44; -fx-border-radius: 10;");

            Circle dot = new Circle(5);
            dot.setFill(Color.valueOf(statusColor));

            Label title = new Label(t.getTitle());
            title.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + (t.isCompleted() ? "#94a3b8" : "#0f172a") + ";");
            HBox.setHgrow(title, Priority.ALWAYS);

            Label prio = new Label(t.getPriority().name());
            prio.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: " + statusColor + "; -fx-background-color: " + statusColor + "18; -fx-background-radius: 999; -fx-padding: 2 8;");

            Label cat = new Label(t.getCategory());
            cat.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #94a3b8;");

            row.getChildren().addAll(dot, title, prio, cat);
            taskDetailList.getChildren().add(row);
        }
    }
}
