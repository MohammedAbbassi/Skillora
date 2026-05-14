package com.skillora.todo.controllers;

import com.skillora.todo.entities.Objective;
import com.skillora.todo.entities.Task;
import com.skillora.todo.services.ObjectiveService;
import com.skillora.todo.services.TaskService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.Session;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ObjectivesController implements Initializable {

    @FXML private VBox pendingColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox completedColumn;
    @FXML private Label pendingCount;
    @FXML private Label inProgressCount;
    @FXML private Label completedCount;
    @FXML private Button addObjectiveBtn;

    private final ObjectiveService objectiveService = new ObjectiveService();
    private final TaskService taskService = new TaskService();

    private static final DataFormat OBJECTIVE_FORMAT = new DataFormat("application/x-objective");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupDropTarget(pendingColumn, "PENDING");
        setupDropTarget(inProgressColumn, "IN_PROGRESS");
        setupDropTarget(completedColumn, "COMPLETED");
        refreshAll();
    }

    private int getUserId() {
        var user = Session.getUser();
        return user != null ? user.getId() : 0;
    }

    private void setupDropTarget(VBox column, String targetStatus) {
        column.setOnDragOver(e -> {
            if (e.getGestureSource() != column && e.getDragboard().hasContent(OBJECTIVE_FORMAT)) {
                e.acceptTransferModes(TransferMode.MOVE);
            }
            e.consume();
        });
        column.setOnDragEntered(e -> column.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 12; -fx-border-color: #4F46E5; -fx-border-radius: 12; -fx-border-width: 2; -fx-padding: 8;"));
        column.setOnDragExited(e -> column.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 8;"));
        column.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasContent(OBJECTIVE_FORMAT)) {
                int objectiveId = (int) db.getContent(OBJECTIVE_FORMAT);
                objectiveService.updateStatus(objectiveId, targetStatus);
                refreshAll();
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    @FXML
    private void onAddObjective() {
        showObjectiveDialog(null);
    }

    private void showObjectiveDialog(Objective existing) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(existing == null ? "New Objective" : "Edit Objective");

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16;");

        Label titleLabel = new Label(existing == null ? "New Objective" : "Edit Objective");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        TextField titleField = new TextField();
        titleField.setPromptText("Objective title (e.g. Finish Java Course)");
        titleField.getStyleClass().add("todo-dialog-field");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description (optional)");
        descArea.setPrefRowCount(3);
        descArea.getStyleClass().add("todo-dialog-field");

        DatePicker targetPicker = new DatePicker();
        targetPicker.setPromptText("Target date (optional)");
        targetPicker.getStyleClass().add("todo-dialog-field");

        TextField statusField = new TextField();
        statusField.setPromptText("Motivational status (e.g. Keep going!)");
        statusField.getStyleClass().add("todo-dialog-field");

        if (existing != null) {
            titleField.setText(existing.getTitle());
            descArea.setText(existing.getDescription());
            targetPicker.setValue(existing.getTargetDate());
            statusField.setText(existing.getMotivationalStatus());
        }

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(existing == null ? "Create" : "Save");
        saveBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        saveBtn.setDefaultButton(true);

        btnRow.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(titleLabel, titleField, descArea, targetPicker, statusField, btnRow);

        Scene scene = new Scene(root, 420, -1);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);

        saveBtn.setOnAction(e -> {
            String t = titleField.getText().trim();
            if (t.isEmpty()) return;
            int uid = getUserId();
            if (uid == 0) { dialog.close(); return; }

            if (existing == null) {
                Objective o = new Objective(uid, t, targetPicker.getValue());
                o.setDescription(descArea.getText());
                o.setMotivationalStatus(statusField.getText());
                objectiveService.add(o);
            } else {
                existing.setTitle(t);
                existing.setDescription(descArea.getText());
                existing.setTargetDate(targetPicker.getValue());
                existing.setMotivationalStatus(statusField.getText());
                objectiveService.update(existing);
            }
            dialog.close();
            refreshAll();
        });

        dialog.showAndWait();
    }

    private void deleteObjective(Objective o) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete \"" + o.getTitle() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                objectiveService.delete(o.getId());
                refreshAll();
            }
        });
    }

    private void showLinkTasksDialog(Objective o) {
        int uid = getUserId();
        List<Task> allTasks = taskService.getByUser(uid);
        List<Integer> linkedIds = objectiveService.getLinkedTaskIds(o.getId());

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Link Tasks to: " + o.getTitle());

        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16;");

        Label lbl = new Label("Select tasks to link to this objective:");
        lbl.setStyle("-fx-font-weight: 800; -fx-text-fill: #0f172a;");

        ListView<Task> taskList = new ListView<>();
        taskList.getItems().setAll(allTasks.stream().filter(t -> !t.isCompleted()).toList());
        taskList.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setGraphic(null); return; }
                CheckBox cb = new CheckBox(t.getTitle());
                cb.setSelected(linkedIds.contains(t.getId()));
                cb.selectedProperty().addListener((o2, ov, nv) -> {
                    if (nv) objectiveService.linkTask(o.getId(), t.getId());
                    else objectiveService.unlinkTask(o.getId(), t.getId());
                    objectiveService.recalculateProgress(o.getId());
                });
                setGraphic(cb);
            }
        });

        Button doneBtn = new Button("Done");
        doneBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;");
        doneBtn.setOnAction(e -> dialog.close());

        root.getChildren().addAll(lbl, taskList, doneBtn);

        Scene scene = new Scene(root, 420, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void refreshAll() {
        int uid = getUserId();
        if (uid == 0) return;

        List<Objective> all = objectiveService.getByUser(uid);

        pendingColumn.getChildren().clear();
        inProgressColumn.getChildren().clear();
        completedColumn.getChildren().clear();

        int pc = 0, ipc = 0, cc = 0;

        for (Objective o : all) {
            VBox card = buildObjectiveCard(o);
            switch (o.getStatus()) {
                case IN_PROGRESS:
                    inProgressColumn.getChildren().add(card);
                    ipc++;
                    break;
                case COMPLETED:
                    completedColumn.getChildren().add(card);
                    cc++;
                    break;
                default:
                    pendingColumn.getChildren().add(card);
                    pc++;
            }
        }

        pendingCount.setText(String.valueOf(pc));
        inProgressCount.setText(String.valueOf(ipc));
        completedCount.setText(String.valueOf(cc));
    }

    private VBox buildObjectiveCard(Objective o) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setUserData(o.getId());

        String bg, border, titleColor;
        if (o.isCompleted()) {
            bg = "#f0fdf4"; border = "#bbf7d0"; titleColor = "#16a34a";
        } else if (o.getStatus() == Objective.Status.IN_PROGRESS) {
            bg = "#fefce8"; border = "#fde68a"; titleColor = "#ca8a04";
        } else {
            bg = "#ffffff"; border = "#dbeafe"; titleColor = "#0f172a";
        }
        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 12; -fx-border-color: " + border + "; -fx-border-radius: 12; -fx-cursor: hand;");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label emoji = new Label(o.getProgressEmoji());
        emoji.setStyle("-fx-font-size: 20px;");

        Label title = new Label(o.getTitle());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: " + titleColor + ";");
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label pctLabel = new Label(o.getProgress() + "%");
        pctLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: #4F46E5; -fx-background-color: #eef2ff; -fx-background-radius: 999; -fx-padding: 2 8;");

        header.getChildren().addAll(emoji, title, pctLabel);

        if (o.getDescription() != null && !o.getDescription().isEmpty()) {
            Label desc = new Label(o.getDescription());
            desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            desc.setWrapText(true);
            desc.setMaxWidth(280);
            card.getChildren().add(desc);
        }

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        if (o.getTargetDate() != null) {
            Label date = new Label("📅 " + o.getTargetDate().format(DateTimeFormatter.ofPattern("MMM d")));
            date.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-font-weight: 700;");
            meta.getChildren().add(date);
        }

        String mot = o.getMotivationalStatus();
        if (mot != null && !mot.isEmpty()) {
            Label motLabel = new Label("✨ " + mot);
            motLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #ca8a04; -fx-font-weight: 700;");
            meta.getChildren().add(motLabel);
        }

        card.getChildren().add(header);
        if (!meta.getChildren().isEmpty()) card.getChildren().add(meta);

        // Action buttons
        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button linkBtn = new Button("Link");
        linkBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4F46E5; -fx-font-weight: 700; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand;");
        linkBtn.setOnAction(e -> showLinkTasksDialog(o));

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #334155; -fx-font-weight: 700; -fx-background-radius: 6; -fx-padding: 4 10; -fx-font-size: 11px; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showObjectiveDialog(o));

        Button delBtn = new Button("✕");
        delBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: 700; -fx-background-radius: 6; -fx-padding: 4 8; -fx-font-size: 11px; -fx-cursor: hand;");
        delBtn.setOnAction(e -> deleteObjective(o));

        actions.getChildren().addAll(linkBtn, editBtn, delBtn);
        card.getChildren().add(actions);

        // Drag source
        card.setOnDragDetected(e -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(OBJECTIVE_FORMAT, o.getId());
            db.setContent(content);
            e.consume();
        });

        return card;
    }
}
