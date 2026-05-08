package controllers;

import entities.Commentaire;
import entities.Jaime;
import entities.Poste;
import entities.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import services.CommentaireService;
import services.JaimeService;
import services.PosteService;
import utils.Session;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles the Community (social feed) page logic.
 * Called from MainController to populate and refresh the feed.
 */
public class CommunityController {

    private final PosteService posteService = new PosteService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final JaimeService jaimeService = new JaimeService();

    private VBox feedContainer;
    private TextField newPostTitleField;
    private TextArea newPostContentArea;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d, yyyy  HH:mm");

    /**
     * Bind the UI containers managed by MainController's FXML.
     */
    public void init(VBox feedContainer, TextField newPostTitleField, TextArea newPostContentArea) {
        this.feedContainer = feedContainer;
        this.newPostTitleField = newPostTitleField;
        this.newPostContentArea = newPostContentArea;
    }

    /** Reload all posts from DB and rebuild the feed. */
    public void loadFeed() {
        if (feedContainer == null) return;
        feedContainer.getChildren().clear();
        try {
            List<Poste> posts = posteService.getAll();
            if (posts.isEmpty()) {
                Label empty = new Label("No posts yet. Be the first to share something!");
                empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 40;");
                feedContainer.getChildren().add(empty);
            } else {
                for (Poste p : posts) {
                    feedContainer.getChildren().add(buildPostCard(p));
                }
            }
        } catch (SQLException e) {
            showError("Could not load posts: " + e.getMessage());
        }
    }

    /** Called when the user clicks "Post" to submit a new post. */
    public void onSubmitPost() {
        User user = Session.getUser();
        if (user == null) {
            showError("You must be logged in to post.");
            return;
        }
        String title = newPostTitleField != null ? newPostTitleField.getText().trim() : "";
        String content = newPostContentArea != null ? newPostContentArea.getText().trim() : "";

        if (title.isEmpty() || content.isEmpty()) {
            showError("Title and content cannot be empty.");
            return;
        }

        Poste poste = new Poste(title, content, null, user.getId());
        try {
            posteService.add(poste);
            if (newPostTitleField != null) newPostTitleField.clear();
            if (newPostContentArea != null) newPostContentArea.clear();
            loadFeed();
        } catch (SQLException e) {
            showError("Failed to create post: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CARD BUILDER
    // ─────────────────────────────────────────────────────────────

    private VBox buildPostCard(Poste post) {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 16;" +
            "-fx-padding: 20;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 2);"
        );

        // ── Header row ──────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle with initial
        StackPane avatar = new StackPane();
        Circle circle = new Circle(20, Color.web("#4F46E5"));
        Label initial = new Label(String.valueOf(post.getIdUtilisateur()).substring(0, 1));
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;");
        avatar.getChildren().addAll(circle, initial);

        VBox authorInfo = new VBox(2);
        Label authorLabel = new Label("User #" + post.getIdUtilisateur());
        authorLabel.setStyle("-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
        String dateStr = post.getDateCreation() != null
                ? post.getDateCreation().toLocalDateTime().format(DATE_FMT)
                : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        authorInfo.getChildren().addAll(authorLabel, dateLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, authorInfo, spacer);

        // Own-post controls (edit / delete)
        User currentUser = Session.getUser();
        if (currentUser != null &&
                (currentUser.getId() == post.getIdUtilisateur() ||
                 "ADMIN".equalsIgnoreCase(currentUser.getRole()))) {
            Button editBtn = new Button("Edit");
            editBtn.setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;" +
                "-fx-background-radius: 8; -fx-font-size: 11px; -fx-cursor: hand;"
            );
            editBtn.setOnAction(e -> showEditDialog(post));

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle(
                "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;" +
                "-fx-background-radius: 8; -fx-font-size: 11px; -fx-cursor: hand;"
            );
            deleteBtn.setOnAction(e -> deletePost(post));
            header.getChildren().addAll(editBtn, deleteBtn);
        }

        // ── Title & content ──────────────────────────────────────
        Label titleLabel = new Label(post.getTitre());
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);

        Label contentLabel = new Label(post.getContenu());
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-line-spacing: 3;");
        contentLabel.setWrapText(true);

        // ── Like / comment counts ────────────────────────────────
        int likeCount = 0;
        int commentCount = 0;
        boolean alreadyLiked = false;
        try {
            likeCount = jaimeService.getCountByPost(post.getIdPost());
            commentCount = commentaireService.getByPost(post.getIdPost()).size();
            if (currentUser != null) {
                alreadyLiked = jaimeService.hasLiked(post.getIdPost(), currentUser.getId());
            }
        } catch (SQLException ignored) {}

        final boolean[] liked = {alreadyLiked};
        final int[] likes = {likeCount};

        Button likeBtn = new Button((liked[0] ? "♥ " : "♡ ") + likes[0] + " Likes");
        likeBtn.setStyle(
            "-fx-background-color: " + (liked[0] ? "#fee2e2" : "#f1f5f9") + ";" +
            "-fx-text-fill: " + (liked[0] ? "#dc2626" : "#64748b") + ";" +
            "-fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 14;"
        );
        likeBtn.setOnAction(e -> {
            if (currentUser == null) return;
            try {
                if (liked[0]) {
                    jaimeService.delete(new Jaime(post.getIdPost(), currentUser.getId()));
                    liked[0] = false;
                    likes[0]--;
                } else {
                    jaimeService.add(new Jaime(post.getIdPost(), currentUser.getId()));
                    liked[0] = true;
                    likes[0]++;
                }
                likeBtn.setText((liked[0] ? "♥ " : "♡ ") + likes[0] + " Likes");
                likeBtn.setStyle(
                    "-fx-background-color: " + (liked[0] ? "#fee2e2" : "#f1f5f9") + ";" +
                    "-fx-text-fill: " + (liked[0] ? "#dc2626" : "#64748b") + ";" +
                    "-fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 14;"
                );
            } catch (SQLException ex) {
                showError("Like error: " + ex.getMessage());
            }
        });

        final int[] commentCountRef = {commentCount};
        Button commentToggleBtn = new Button("💬 " + commentCountRef[0] + " Comments");
        commentToggleBtn.setStyle(
            "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b;" +
            "-fx-background-radius: 20; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 14;"
        );

        HBox actions = new HBox(10, likeBtn, commentToggleBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        // ── Comments section (collapsible) ───────────────────────
        VBox commentsSection = new VBox(8);
        commentsSection.setVisible(false);
        commentsSection.setManaged(false);
        commentsSection.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; -fx-padding: 12;");

        commentToggleBtn.setOnAction(e -> {
            boolean nowVisible = !commentsSection.isVisible();
            commentsSection.setVisible(nowVisible);
            commentsSection.setManaged(nowVisible);
            if (nowVisible) refreshComments(post, commentsSection, commentToggleBtn, commentCountRef);
        });

        card.getChildren().addAll(header, titleLabel, contentLabel, actions, commentsSection);
        return card;
    }

    private void refreshComments(Poste post, VBox commentsSection,
                                  Button toggleBtn, int[] countRef) {
        commentsSection.getChildren().clear();
        try {
            List<Commentaire> comments = commentaireService.getByPost(post.getIdPost());
            countRef[0] = comments.size();
            toggleBtn.setText("💬 " + countRef[0] + " Comments");

            for (Commentaire c : comments) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);

                Circle mini = new Circle(14, Color.web("#7C3AED"));
                Label miniInit = new Label(String.valueOf(c.getIdUtilisateur()).substring(0, 1));
                miniInit.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 700;");
                StackPane miniAvatar = new StackPane(mini, miniInit);

                VBox commentBody = new VBox(2);
                Label who = new Label("User #" + c.getIdUtilisateur());
                who.setStyle("-fx-font-weight: 700; -fx-font-size: 11px; -fx-text-fill: #475569;");
                Label text = new Label(c.getContenu());
                text.setStyle("-fx-font-size: 12px; -fx-text-fill: #1e293b;");
                text.setWrapText(true);
                commentBody.getChildren().addAll(who, text);
                HBox.setHgrow(commentBody, Priority.ALWAYS);

                row.getChildren().addAll(miniAvatar, commentBody);

                // Delete own comment
                User cu = Session.getUser();
                if (cu != null && (cu.getId() == c.getIdUtilisateur() ||
                        "ADMIN".equalsIgnoreCase(cu.getRole()))) {
                    Button del = new Button("✕");
                    del.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-cursor: hand;");
                    del.setOnAction(ev -> {
                        try {
                            commentaireService.delete(c);
                            refreshComments(post, commentsSection, toggleBtn, countRef);
                        } catch (SQLException ex) {
                            showError("Delete error: " + ex.getMessage());
                        }
                    });
                    row.getChildren().add(del);
                }
                commentsSection.getChildren().add(row);
            }

            // Add comment input
            HBox inputRow = new HBox(8);
            inputRow.setAlignment(Pos.CENTER_LEFT);
            inputRow.setPadding(new Insets(8, 0, 0, 0));
            TextField commentInput = new TextField();
            commentInput.setPromptText("Write a comment...");
            commentInput.setStyle(
                "-fx-background-color: #ffffff; -fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 6 10;"
            );
            HBox.setHgrow(commentInput, Priority.ALWAYS);

            Button sendBtn = new Button("Send");
            sendBtn.setStyle(
                "-fx-background-color: #4F46E5; -fx-text-fill: white;" +
                "-fx-background-radius: 8; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 14;"
            );
            sendBtn.setOnAction(ev -> {
                User cu = Session.getUser();
                if (cu == null) return;
                String txt = commentInput.getText().trim();
                if (txt.isEmpty()) return;
                try {
                    commentaireService.add(new Commentaire(txt, cu.getId(), post.getIdPost()));
                    commentInput.clear();
                    refreshComments(post, commentsSection, toggleBtn, countRef);
                } catch (SQLException ex) {
                    showError("Comment error: " + ex.getMessage());
                }
            });
            commentInput.setOnAction(ev -> sendBtn.fire());
            inputRow.getChildren().addAll(commentInput, sendBtn);
            commentsSection.getChildren().add(inputRow);

        } catch (SQLException e) {
            showError("Could not load comments: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EDIT / DELETE
    // ─────────────────────────────────────────────────────────────

    private void showEditDialog(Poste post) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Post");
        dialog.setHeaderText(null);

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setPrefWidth(420);

        TextField titleField = new TextField(post.getTitre());
        titleField.setStyle("-fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 8;");

        TextArea contentArea = new TextArea(post.getContenu());
        contentArea.setPrefHeight(120);
        contentArea.setWrapText(true);
        contentArea.setStyle("-fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 8;");

        content.getChildren().addAll(
            new Label("Title"), titleField,
            new Label("Content"), contentArea
        );
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                String newTitle = titleField.getText().trim();
                String newContent = contentArea.getText().trim();
                if (newTitle.isEmpty() || newContent.isEmpty()) return;
                post.setTitre(newTitle);
                post.setContenu(newContent);
                try {
                    posteService.update(post);
                    loadFeed();
                } catch (SQLException e) {
                    showError("Update failed: " + e.getMessage());
                }
            }
        });
    }

    private void deletePost(Poste post) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this post and all its comments?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                try {
                    posteService.delete(post);
                    loadFeed();
                } catch (SQLException e) {
                    showError("Delete failed: " + e.getMessage());
                }
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
