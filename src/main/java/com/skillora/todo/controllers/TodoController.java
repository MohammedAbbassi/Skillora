package com.skillora.todo.controllers;

import com.skillora.todo.entities.Task;
import com.skillora.todo.services.TaskService;
import com.skillora.todo.services.AchievementService;
import com.skillora.todo.services.ProductivityService;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import utils.Session;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TodoController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private ComboBox<String> filterPriorityCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private ListView<Task> taskListView;
    @FXML private Label taskCountLabel;
    @FXML private Label pendingCount;
    @FXML private Label inProgressCount;
    @FXML private Label completedCount;
    @FXML private Label overdueCount;
    @FXML private Button addTaskBtn;

    private final TaskService taskService = new TaskService();
    private final AchievementService achievementService = new AchievementService();
    private final ProductivityService productivityService = new ProductivityService();
    private final ObservableList<Task> taskData = FXCollections.observableArrayList();

    private static final String[] CATEGORIES = {"All", "Learning", "Work", "Personal", "Projects", "Exams", "Fitness"};
    private static final String[] PRIORITIES = {"All", "LOW", "MEDIUM", "HIGH", "URGENT"};
    private static final String[] STATUSES = {"All", "PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterCategoryCombo.getItems().setAll(CATEGORIES);
        filterCategoryCombo.setValue("All");
        filterPriorityCombo.getItems().setAll(PRIORITIES);
        filterPriorityCombo.setValue("All");
        filterStatusCombo.getItems().setAll(STATUSES);
        filterStatusCombo.setValue("All");

        taskListView.setCellFactory(lv -> new TaskCell());

        searchField.textProperty().addListener((o, ov, nv) -> refreshList());
        filterCategoryCombo.valueProperty().addListener((o, ov, nv) -> refreshList());
        filterPriorityCombo.valueProperty().addListener((o, ov, nv) -> refreshList());
        filterStatusCombo.valueProperty().addListener((o, ov, nv) -> refreshList());

        refreshList();
    }

    private int getUserId() {
        var user = Session.getUser();
        return user != null ? user.getId() : 0;
    }

    @FXML
    private void onAddTask() {
        showTaskDialog(null);
    }

    private void showTaskDialog(Task existing) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "New Task" : "Edit Task");

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16;");

        Label titleLabel = new Label(existing == null ? "Create Task" : "Edit Task");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        titleField.getStyleClass().add("todo-dialog-field");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description (optional)");
        descArea.setPrefRowCount(3);
        descArea.getStyleClass().add("todo-dialog-field");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().setAll("LOW", "MEDIUM", "HIGH", "URGENT");
        priorityCombo.setValue("MEDIUM");
        priorityCombo.getStyleClass().add("todo-dialog-field");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().setAll("Learning", "Work", "Personal", "Projects", "Exams", "Fitness");
        categoryCombo.setValue("Learning");
        categoryCombo.getStyleClass().add("todo-dialog-field");

        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Deadline (optional)");
        deadlinePicker.getStyleClass().add("todo-dialog-field");

        if (existing != null) {
            titleField.setText(existing.getTitle());
            descArea.setText(existing.getDescription());
            priorityCombo.setValue(existing.getPriority().name());
            categoryCombo.setValue(existing.getCategory());
            deadlinePicker.setValue(existing.getDeadline());
        }

        HBox fieldRow1 = new HBox(12, priorityCombo, categoryCombo);
        fieldRow1.setHgrow(priorityCombo, Priority.ALWAYS);
        fieldRow1.setHgrow(categoryCombo, Priority.ALWAYS);

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(existing == null ? "Create" : "Save");
        saveBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        saveBtn.setDefaultButton(true);

        btnRow.getChildren().addAll(cancelBtn, saveBtn);

        root.getChildren().addAll(titleLabel, titleField, descArea, fieldRow1, deadlinePicker, btnRow);

        Scene scene = new Scene(root, 420, -1);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);

        saveBtn.setOnAction(e -> {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                titleField.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");
                return;
            }
            int uid = getUserId();
            if (uid == 0) {
                showAlert("Error", "No user session found.");
                dialog.close();
                return;
            }

            if (existing == null) {
                Task t = new Task(uid, title, categoryCombo.getValue(), Task.Priority.valueOf(priorityCombo.getValue()), deadlinePicker.getValue());
                t.setDescription(descArea.getText());
                taskService.add(t);
            } else {
                existing.setTitle(title);
                existing.setDescription(descArea.getText());
                existing.setPriority(Task.Priority.valueOf(priorityCombo.getValue()));
                existing.setCategory(categoryCombo.getValue());
                existing.setDeadline(deadlinePicker.getValue());
                taskService.update(existing);
            }
            dialog.close();
            refreshList();
        });

        dialog.showAndWait();
    }

    private void toggleTaskComplete(Task t) {
        if (t.isCompleted()) {
            t.setStatus(Task.Status.PENDING);
            t.setProgress(0);
            taskService.update(t);
        } else {
            t.setStatus(Task.Status.COMPLETED);
            t.setProgress(100);
            taskService.update(t);
            productivityService.logTaskCompletion(getUserId(), 10);
            checkAchievements();
        }
        refreshList();
    }

    private void checkAchievements() {
        int uid = getUserId();
        long completed = taskService.countCompleted(uid);
        int streak = productivityService.getCurrentStreak(uid);

        if (completed >= 1 && !achievementService.hasBadge(uid, "first_task"))
            achievementService.awardBadge(uid, "first_task", "First Step", "🎯");
        if (completed >= 5 && !achievementService.hasBadge(uid, "five_tasks"))
            achievementService.awardBadge(uid, "five_tasks", "Getting Started", "🔥");
        if (completed >= 10 && !achievementService.hasBadge(uid, "ten_tasks"))
            achievementService.awardBadge(uid, "ten_tasks", "Task Master", "💪");
        if (completed >= 50 && !achievementService.hasBadge(uid, "fifty_tasks"))
            achievementService.awardBadge(uid, "fifty_tasks", "Productivity Pro", "🚀");
        if (completed >= 100 && !achievementService.hasBadge(uid, "hundred_tasks"))
            achievementService.awardBadge(uid, "hundred_tasks", "Century Club", "🏆");
        if (streak >= 3 && !achievementService.hasBadge(uid, "streak_3"))
            achievementService.awardBadge(uid, "streak_3", "Consistent", "🔥");
        if (streak >= 7 && !achievementService.hasBadge(uid, "streak_7"))
            achievementService.awardBadge(uid, "streak_7", "Week Warrior", "⭐");
        if (streak >= 30 && !achievementService.hasBadge(uid, "streak_30"))
            achievementService.awardBadge(uid, "streak_30", "Unstoppable", "💎");
    }

    private void deleteTask(Task t) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + t.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                taskService.delete(t.getId());
                refreshList();
            }
        });
    }

    private void refreshList() {
        int uid = getUserId();
        if (uid == 0) return;
        List<Task> tasks = taskService.getByUser(uid);
        String q = searchField.getText().toLowerCase();
        String cat = filterCategoryCombo.getValue();
        String pri = filterPriorityCombo.getValue();
        String sts = filterStatusCombo.getValue();

        var filtered = tasks.stream()
            .filter(t -> q.isEmpty() || t.getTitle().toLowerCase().contains(q) || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)))
            .filter(t -> "All".equals(cat) || t.getCategory().equals(cat))
            .filter(t -> "All".equals(pri) || t.getPriority().name().equals(pri))
            .filter(t -> "All".equals(sts) || t.getStatus().name().equals(sts))
            .toList();

        taskData.setAll(filtered);
        taskListView.setItems(taskData);
        taskCountLabel.setText(taskData.size() + " task(s)");

        pendingCount.setText(String.valueOf(taskService.countByStatus(uid, "PENDING")));
        inProgressCount.setText(String.valueOf(taskService.countByStatus(uid, "IN_PROGRESS")));
        completedCount.setText(String.valueOf(taskService.countByStatus(uid, "COMPLETED")));
        overdueCount.setText(String.valueOf(taskService.getOverdue(uid).size()));
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private class TaskCell extends ListCell<Task> {
        private final HBox root;
        private final Circle statusCircle;
        private final Label titleLabel;
        private final Label descLabel;
        private final Label metaLabel;
        private final Label priorityBadge;
        private final Region spacer;
        private final Button editBtn;
        private final Button deleteBtn;
        private final VBox info;
        private final HBox actions;

        TaskCell() {
            setStyle("-fx-background-color: transparent; -fx-padding: 4 0;");

            statusCircle = new Circle(10);
            statusCircle.setStroke(Color.valueOf("#dbeafe"));
            statusCircle.setStrokeWidth(2);

            titleLabel = new Label();
            titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

            descLabel = new Label();
            descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(300);

            metaLabel = new Label();
            metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-font-weight: 700;");

            priorityBadge = new Label();
            priorityBadge.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999; -fx-padding: 3 10;");

            info = new VBox(4, titleLabel, descLabel, metaLabel);

            spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            editBtn = new Button("Edit");
            editBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4F46E5; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
            editBtn.setOnAction(e -> showTaskDialog(getItem()));

            deleteBtn = new Button("✕");
            deleteBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 6 10; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> deleteTask(getItem()));

            actions = new HBox(6, editBtn, deleteBtn);
            actions.setAlignment(Pos.CENTER);

            root = new HBox(14, statusCircle, info, spacer, priorityBadge, actions);
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new Insets(12, 16, 12, 16));
            root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 14; -fx-border-color: #dbeafe; -fx-border-radius: 14;");
            root.setOnMouseClicked(e -> {
                if (e.getTarget() == statusCircle || e.getTarget() == root) {
                    Task item = getItem();
                    if (item != null) toggleTaskComplete(item);
                }
            });

            setPadding(new Insets(4, 0, 4, 0));
        }

        @Override
        protected void updateItem(Task t, boolean empty) {
            super.updateItem(t, empty);
            if (empty || t == null) {
                setGraphic(null);
                return;
            }

            titleLabel.setText(t.getTitle());
            descLabel.setText(t.getDescription() != null && !t.getDescription().isEmpty() ? t.getDescription() : "");
            descLabel.setManaged(t.getDescription() != null && !t.getDescription().isEmpty());

            String cat = t.getCategory();
            metaLabel.setText(cat + " | Due: " + (t.getDeadline() != null ? t.getDeadline().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) : "No deadline"));

            String pri = t.getPriority().name();
            priorityBadge.setText(pri);
            switch (pri) {
                case "URGENT":
                    priorityBadge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999; -fx-padding: 3 10;");
                    break;
                case "HIGH":
                    priorityBadge.setStyle("-fx-background-color: #ffedd5; -fx-text-fill: #c2410c; -fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999; -fx-padding: 3 10;");
                    break;
                case "LOW":
                    priorityBadge.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #047857; -fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999; -fx-padding: 3 10;");
                    break;
                default:
                    priorityBadge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4F46E5; -fx-font-size: 11px; -fx-font-weight: 800; -fx-background-radius: 999; -fx-padding: 3 10;");
            }

            if (t.isCompleted()) {
                titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-strikethrough: true;");
                statusCircle.setFill(Color.valueOf("#10b981"));
                root.setOpacity(0.7);
            } else if (t.isOverdue()) {
                titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #b91c1c;");
                statusCircle.setFill(Color.valueOf("#ef4444"));
                root.setOpacity(1.0);
            } else {
                titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
                statusCircle.setFill(Color.valueOf("#e2e8f0"));
                root.setOpacity(1.0);
            }

            setGraphic(root);
        }
    }
}
