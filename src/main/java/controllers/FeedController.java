package controllers;

import entities.Badge;
import entities.Commentaire;
import entities.Poste;
import entities.Role;
import entities.User;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.util.StringConverter;
import services.BadgeService;
import services.CommentaireService;
import services.ImageModerationService;
import services.JaimeService;
import services.ModerationService;
import services.PosteService;
import services.TranslationService;
import services.UserService;
import utils.SessionManager;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FeedController {

    @FXML private TextField      tfTitre;
    @FXML private TextField      tfContenu;
    @FXML private VBox           vbFeed;
    @FXML private Label          lblToast;
    @FXML private Label          lblImageSelected;
    @FXML private Label          lblRole;
    @FXML private Label          lblCreateAvatar;
    @FXML private Label          lblBadge;
    // Search
    @FXML private TextField      tfSearch;
    @FXML private Label          lblSearchResult;

    private final PosteService       posteService       = new PosteService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final JaimeService       jaimeService       = new JaimeService();
    private final UserService        userService        = new UserService();
    private final Map<Integer, User> userCache          = new HashMap<>();
    private final SessionManager     session            = SessionManager.getInstance();
    private       String             selectedImagePath  = "";

    private static final List<User> DEMO_USERS = Arrays.asList(
        makeUser(1, "Amine",   "amine@skillora.com",   Role.ADMIN),
        makeUser(2, "Sara",    "sara@skillora.com",    Role.ENSEIGNANT),
        makeUser(3, "Mohamed", "mohamed@skillora.com", Role.ETUDIANT),
        makeUser(4, "Lina",    "lina@skillora.com",    Role.ETUDIANT),
        makeUser(5, "Karim",   "karim@skillora.com",   Role.ENSEIGNANT)
    );

    private static User makeUser(int id, String name, String email, Role role) {
        User u = new User(name, email, "pass", role);
        u.setIdUtilisateur(id);
        return u;
    }

    @FXML
    public void initialize() {
        User current = session.getCurrentUser();
        if (current != null) {
            applySelectedUser(current);
        } else {
            applySelectedUser(DEMO_USERS.get(0));
        }
    }

    // @FXML
    // void handleUserSwitch(ActionEvent e) {
    //     User selected = cbUsers.getValue();
    //     if (selected != null) applySelectedUser(selected);
    // }

    private void applySelectedUser(User user) {
        session.setCurrentUser(user);
        lblCreateAvatar.setText(user.getNomUtilisateur().substring(0, 1).toUpperCase());
        String roleLabel;
        String roleStyle = "-fx-font-size:12px;-fx-font-weight:bold;-fx-padding:4 12;-fx-background-radius:20;-fx-text-fill:white;";
        switch (user.getRole()) {
            case ADMIN:      roleLabel = "ADMIN";      roleStyle += "-fx-background-color:#e74c3c;"; break;
            case ENSEIGNANT:
            case INSTRUCTEUR: roleLabel = "ENSEIGNANT"; roleStyle += "-fx-background-color:#f39c12;"; break;
            default:         roleLabel = "ETUDIANT"; roleStyle += "-fx-background-color:#27ae60;"; break;
        }
        lblRole.setText(roleLabel);
        lblRole.setStyle(roleStyle);

        // Show current user's badge in the header
        String badgeDisplay = BadgeService.getBadgeDisplay(user.getIdUtilisateur());
        lblBadge.setText(badgeDisplay);
        lblBadge.setStyle("-fx-font-size:12px;-fx-font-weight:bold;"
            + "-fx-padding:4 10;-fx-background-radius:20;-fx-text-fill:white;"
            + "-fx-background-color:#555;");

        loadFeed();
    }

    @FXML
    void handlePublish(ActionEvent e) {
        String titre   = tfTitre.getText().trim();
        String contenu = tfContenu.getText().trim();
        if (titre.isEmpty())   { showToast("Le titre est obligatoire.");   return; }
        if (contenu.isEmpty()) { showToast("Le contenu est obligatoire."); return; }

        // ── AI Moderation check ──────────────────────────────────────────────
        ModerationService.ModerationResult mod =
            ModerationService.analyze(titre + " " + contenu, session.getCurrentUserId());
        if (!mod.allowed) {
            showModerationAlert(mod);
            return;
        }

        Poste p = new Poste(titre, contenu, selectedImagePath, session.getCurrentUserId());
        try {
            posteService.add(p);
            tfTitre.clear(); tfContenu.clear();
            selectedImagePath = ""; lblImageSelected.setText("");
            showToast("Publication reussie !"); loadFeed();
        } catch (SQLException ex) { ex.printStackTrace(); showToast("Erreur lors de la publication."); }
    }

    @FXML
    void handleChooseImage(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.gif","*.bmp","*.webp"));
        java.io.File dir = new java.io.File(System.getProperty("user.home") + "/Pictures");
        if (dir.exists()) fc.setInitialDirectory(dir);
        java.io.File file = fc.showOpenDialog(tfTitre.getScene().getWindow());
        if (file != null) {
            // ── Image moderation check (Sightengine API) ───────────────────────
            ImageModerationService.ImageModerationResult imgMod =
                ImageModerationService.checkImage(file, session.getCurrentUserId());

            if (!imgMod.safe) {
                // Block the upload and show alert — do NOT set selectedImagePath
                showImageModerationAlert(imgMod);
                return;
            }

            // Image is safe — accept it
            selectedImagePath = file.getAbsolutePath();
            String name = file.getName();
            lblImageSelected.setText("Photo: " + (name.length() > 30 ? name.substring(0,27)+"..." : name));
        }
    }

    private void loadFeed() {
        vbFeed.getChildren().clear();
        lblSearchResult.setText("");
        try {
            List<Poste> posts = posteService.getAll();
            for (Poste p : posts) vbFeed.getChildren().add(buildPostCard(p));
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // ── Search handlers ────────────────────────────────────────

    @FXML
    void handleSearch(ActionEvent e) {
        String keyword = tfSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadFeed();
            return;
        }
        vbFeed.getChildren().clear();
        try {
            List<Poste> results = posteService.search(keyword);
            if (results.isEmpty()) {
                lblSearchResult.setText("Aucun post trouve pour : \"" + keyword + "\"");
            } else {
                lblSearchResult.setText(results.size() + " post(s) trouve(s) pour : \"" + keyword + "\"");
                for (Poste p : results) vbFeed.getChildren().add(buildPostCard(p));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showToast("Erreur lors de la recherche.");
        }
    }

    @FXML
    void handleClearSearch(ActionEvent e) {
        tfSearch.clear();
        loadFeed();
    }
    private VBox buildPostCard(Poste post) {
        VBox card = new VBox(0);
        card.getStyleClass().add("post-card");
        card.setMaxWidth(620);
        boolean canEdit = session.canModify((int) post.getIdUtilisateur());

        // HEADER
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(14, 16, 10, 16));
        String authorName = resolveUserName((int) post.getIdUtilisateur());
        StackPane avatar = buildAvatar(authorName, false);
        VBox userInfo = new VBox(2);
        Label lblUser = new Label(authorName);
        lblUser.getStyleClass().add("post-username");
        HBox metaRow = new HBox(6);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        String ts = post.getDateCreation() != null
                ? new SimpleDateFormat("d MMM a HH:mm").format(post.getDateCreation()) : "A l'instant";
        Label lblMeta = new Label(ts); lblMeta.getStyleClass().add("post-meta");
        // Show the author's role instead of "Public"
        String authorRoleLabel = resolveUserRole((int) post.getIdUtilisateur()) != null
                ? resolveUserRole((int) post.getIdUtilisateur()).name() : "ETUDIANT";
        Label lblPrivacy = new Label(authorRoleLabel);
        lblPrivacy.getStyleClass().add("post-privacy");
        // Badge of the post author
        String authorBadge = BadgeService.getBadgeDisplay((int) post.getIdUtilisateur());
        Label lblAuthorBadge = new Label(authorBadge);
        lblAuthorBadge.setStyle("-fx-font-size:11px;-fx-font-weight:bold;"
            + "-fx-padding:2 8;-fx-background-radius:12;-fx-text-fill:white;"
            + "-fx-background-color:" + badgeColor(BadgeService.getBadge((int) post.getIdUtilisateur())) + ";");
        metaRow.getChildren().addAll(lblMeta, lblPrivacy, lblAuthorBadge);
        userInfo.getChildren().addAll(lblUser, metaRow);
        HBox.setHgrow(userInfo, Priority.ALWAYS);
        if (canEdit) {
            MenuButton menuBtn = new MenuButton("... v");
            menuBtn.getStyleClass().add("menu-button");
            MenuItem mi1 = new MenuItem("Modifier");
            MenuItem mi2 = new MenuItem("Supprimer");
            mi1.setOnAction(ev -> startEditPost(post, card));
            mi2.setOnAction(ev -> { try { posteService.delete(post); loadFeed(); } catch (SQLException ex) { ex.printStackTrace(); } });
            menuBtn.getItems().addAll(mi1, mi2);
            header.getChildren().addAll(avatar, userInfo, menuBtn);
        } else {
            header.getChildren().addAll(avatar, userInfo);
        }

        // CONTENT
        VBox contentArea = buildPostContentArea(post);

        // STATS
        HBox statsRow = new HBox(16);
        statsRow.setPadding(new Insets(6, 16, 6, 16));
        statsRow.setAlignment(Pos.CENTER_LEFT);
        try {
            int likes = jaimeService.getCount(post.getIdPost());
            int comms = commentaireService.countByPost(post.getIdPost());
            Label ll = new Label("J'aime: " + likes); ll.getStyleClass().add("stats-label");
            Label lc = new Label("Commentaires: " + comms); lc.getStyleClass().add("stats-label");
            statsRow.getChildren().addAll(ll, lc);
        } catch (SQLException ex) { ex.printStackTrace(); }

        Separator sep1 = new Separator();

        // ACTIONS
        HBox actionsRow = new HBox(0);
        actionsRow.setAlignment(Pos.CENTER);
        actionsRow.setPadding(new Insets(2, 0, 2, 0));
        Button btnLike = makeActionBtn("J'aime");
        Button btnComm = makeActionBtn("Commenter");
        try {
            if (jaimeService.hasLiked(session.getCurrentUserId(), post.getIdPost()))
                btnLike.setStyle("-fx-background-color:transparent;-fx-text-fill:#1877f2;-fx-font-weight:bold;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:8 12;-fx-background-radius:6;-fx-border-color:transparent;");
        } catch (SQLException ex) { ex.printStackTrace(); }
        btnLike.setOnAction(ev -> { try { jaimeService.toggle(session.getCurrentUserId(), post.getIdPost()); loadFeed(); } catch (SQLException ex) { ex.printStackTrace(); } });
        btnComm.setOnAction(ev -> showToast("Commenter non implemente"));
        actionsRow.getChildren().addAll(btnLike, btnComm);
        if (canEdit) {
            Button btnEdit = makeActionBtn("Modifier");
            Button btnDel  = makeDangerBtn("Supprimer");
            btnEdit.setOnAction(ev -> startEditPost(post, card));
            btnDel.setOnAction(ev -> { try { posteService.delete(post); loadFeed(); } catch (SQLException ex) { ex.printStackTrace(); } });
            actionsRow.getChildren().addAll(btnEdit, btnDel);
        }

        // ── Translate button ──────────────────────────────────────────────────
        Button btnTranslate = makeActionBtn("Traduire");
        btnTranslate.setOnAction(ev -> togglePostTranslation(post, contentArea, btnTranslate));
        actionsRow.getChildren().add(btnTranslate);

        // ── Repost button (only on original posts, not on reposts) ─────────
        if (!post.isRepost()) {
            Button btnRepost = makeActionBtn("Repartager");
            try {
                int repostCount = posteService.getRepostCount(post.getIdPost());
                if (repostCount > 0)
                    btnRepost.setText("Repartager (" + repostCount + ")");
                if (posteService.hasReposted(post.getIdPost(), session.getCurrentUserId())) {
                    btnRepost.setStyle("-fx-background-color:transparent;-fx-text-fill:#1877f2;"
                        + "-fx-font-weight:bold;-fx-font-size:14px;-fx-cursor:hand;"
                        + "-fx-padding:8 12;-fx-background-radius:6;-fx-border-color:transparent;");
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
            btnRepost.setOnAction(ev -> handleRepost(post));
            actionsRow.getChildren().add(btnRepost);
        }

        Separator sep2 = new Separator();
        VBox commentSection = buildCommentSection(post);
        card.getChildren().addAll(header, contentArea, statsRow, sep1, actionsRow, sep2, commentSection);
        return card;
    }

    private VBox buildPostContentArea(Poste post) {
        VBox area = new VBox(4);
        area.setPadding(new Insets(0, 16, 10, 16));

        // ── Repost banner: "Repartage de @username" ───────────
        if (post.isRepost()) {
            String originalAuthor = resolveUserName(0); // fallback
            try {
                Poste original = posteService.getById(post.getOriginalPostId());
                if (original != null)
                    originalAuthor = resolveUserName(original.getIdUtilisateur());
            } catch (Exception ex) { /* skip */ }

            HBox repostBanner = new HBox(6);
            repostBanner.setAlignment(Pos.CENTER_LEFT);
            repostBanner.setPadding(new Insets(4, 0, 6, 0));
            Label icon = new Label("↩");
            icon.setStyle("-fx-font-size:13px;-fx-text-fill:#65676b;");
            Label lbl = new Label("Repartage de @" + originalAuthor);
            lbl.setStyle("-fx-font-size:12px;-fx-text-fill:#65676b;-fx-font-style:italic;");
            repostBanner.getChildren().addAll(icon, lbl);
            area.getChildren().add(repostBanner);
        }

        if (post.getTitre() != null && !post.getTitre().isEmpty()) {
            Label l = new Label(post.getTitre()); l.getStyleClass().add("post-title"); l.setWrapText(true); area.getChildren().add(l);
        }
        if (post.getContenu() != null && !post.getContenu().isEmpty()) {
            Label l = new Label(post.getContenu()); l.getStyleClass().add("post-content"); l.setWrapText(true); area.getChildren().add(l);
        }
        if (post.getImage() != null && !post.getImage().isEmpty()) {
            try {
                java.io.File imgFile = new java.io.File(post.getImage());
                if (imgFile.exists()) {
                    Image img = new Image(imgFile.toURI().toString());
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(588); iv.setPreserveRatio(true); iv.setSmooth(true);
                    double ratio = img.getHeight() / img.getWidth();
                    javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(588, 588 * ratio);
                    clip.setArcWidth(12); clip.setArcHeight(12);
                    iv.setClip(clip);
                    area.getChildren().add(iv);
                }
            } catch (Exception ex) { /* skip */ }
        }
        return area;
    }

    private void startEditPost(Poste post, VBox card) {
        if (card.getChildren().size() < 2) return;
        VBox editArea = new VBox(8);
        editArea.setPadding(new Insets(8, 16, 12, 16));
        TextField tfT = new TextField(post.getTitre() != null ? post.getTitre() : "");
        tfT.getStyleClass().add("create-post-field"); tfT.setPromptText("Titre...");
        TextField tfC = new TextField(post.getContenu() != null ? post.getContenu() : "");
        tfC.getStyleClass().add("create-post-field"); tfC.setPromptText("Contenu...");
        HBox btns = new HBox(8); btns.setAlignment(Pos.CENTER_RIGHT);
        Button btnSave = makeSaveBtn("Sauvegarder");
        Button btnCancel = makeCancelBtn("Annuler");
        btnSave.setOnAction(ev -> {
            String nt = tfT.getText().trim(), nc = tfC.getText().trim();
            if (nt.isEmpty() && nc.isEmpty()) { showToast("Titre ou contenu requis."); return; }
            post.setTitre(nt); post.setContenu(nc);
            try { posteService.update(post); showToast("Post modifie !"); loadFeed(); }
            catch (SQLException ex) { ex.printStackTrace(); showToast("Erreur modification."); }
        });
        btnCancel.setOnAction(ev -> {
            int idx = card.getChildren().indexOf(editArea);
            if (idx >= 0) card.getChildren().set(idx, buildPostContentArea(post));
        });
        btns.getChildren().addAll(btnCancel, btnSave);
        editArea.getChildren().addAll(tfT, tfC, btns);
        card.getChildren().set(1, editArea);
        tfT.requestFocus(); tfT.selectAll();
    }

    // ── COMMENT SECTION ─────────────────────────────────────────────────────
    private VBox buildCommentSection(Poste post) {
        VBox section = new VBox(0);
        section.getStyleClass().add("comment-section");
        try {
            for (Commentaire c : commentaireService.getByPost(post.getIdPost()))
                section.getChildren().add(buildCommentRow(c, section, false));
        } catch (SQLException ex) { ex.printStackTrace(); }
        section.getChildren().add(buildCommentInputRow(post.getIdPost(), null, section));
        return section;
    }

    private HBox buildCommentInputRow(int postId, Integer parentId, VBox section) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(parentId == null ? new Insets(10,12,10,12) : new Insets(6,12,6,56));
        StackPane av = buildAvatar(session.getCurrentUserName(), true);
        TextField tf = new TextField();
        tf.setPromptText(parentId == null ? "Ecrire un commentaire..." : "Ecrire une reponse...");
        tf.getStyleClass().add("comment-field");
        HBox.setHgrow(tf, Priority.ALWAYS);
        Button btnSend = new Button("Envoyer");
        btnSend.setStyle("-fx-background-color:transparent;-fx-text-fill:#1877f2;-fx-font-weight:bold;-fx-font-size:13px;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:6 10;");
        Runnable send = () -> {
            String txt = tf.getText().trim();
            if (!txt.isEmpty()) {
                // ── AI Moderation check ──────────────────────────────────────────────────
                ModerationService.ModerationResult mod =
                    ModerationService.analyze(txt, session.getCurrentUserId());
                if (!mod.allowed) {
                    showModerationAlert(mod);
                    return;
                }
                try {
                    Commentaire c = new Commentaire(txt, session.getCurrentUserId(), postId);
                    c.setParentId(parentId);
                    commentaireService.add(c);
                    tf.clear(); loadFeed();
                } catch (SQLException ex) { ex.printStackTrace(); }
            }
        };
        tf.setOnAction(ev -> send.run());
        btnSend.setOnAction(ev -> send.run());
        row.getChildren().addAll(av, tf, btnSend);
        return row;
    }

    // ── COMMENT ROW ──────────────────────────────────────────────────────────
    private VBox buildCommentRow(Commentaire c, VBox section, boolean isReply) {
        VBox wrapper = new VBox(0);

        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(isReply ? new Insets(6,12,6,56) : new Insets(8,12,8,12));
        row.setStyle(isReply
            ? "-fx-background-color:#f7f8fa;-fx-border-color:#e4e6eb;-fx-border-width:0 0 1 0;"
            : "-fx-background-color:white;-fx-border-color:#e4e6eb;-fx-border-width:0 0 1 0;");

        String authorName  = resolveUserName(c.getIdUtilisateur());
        Role authorRole    = resolveUserRole(c.getIdUtilisateur());
        int    avatarSize  = isReply ? 28 : 36;
        StackPane avatar = new StackPane();
        avatar.setMinSize(avatarSize,avatarSize); avatar.setPrefSize(avatarSize,avatarSize); avatar.setMaxSize(avatarSize,avatarSize);
        avatar.setStyle("-fx-background-color:" + roleColor(authorRole) + ";-fx-background-radius:50;");
        Label avLbl = new Label(authorName.substring(0,1).toUpperCase());
        avLbl.setStyle("-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:" + (isReply?11:13) + "px;");
        avatar.getChildren().add(avLbl);

        VBox textBox = new VBox(2);
        HBox.setHgrow(textBox, Priority.ALWAYS);
        // Author name + badge on same line
        HBox nameRow = new HBox(6);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label lblName = new Label(authorName + "  (" + (authorRole!=null?authorRole:"?") + ")");
        lblName.setStyle("-fx-font-weight:bold;-fx-font-size:" + (isReply?12:13) + "px;-fx-text-fill:#050505;");
        String commentBadge = BadgeService.getBadgeDisplay(c.getIdUtilisateur());
        Label lblCommentBadge = new Label(commentBadge);
        lblCommentBadge.setStyle("-fx-font-size:10px;-fx-font-weight:bold;"
            + "-fx-padding:1 6;-fx-background-radius:10;-fx-text-fill:white;"
            + "-fx-background-color:" + badgeColor(BadgeService.getBadge(c.getIdUtilisateur())) + ";");
        nameRow.getChildren().addAll(lblName, lblCommentBadge);
        Label lblText = new Label(c.getContenu());
        lblText.setStyle("-fx-font-size:" + (isReply?12:13) + "px;-fx-text-fill:#050505;");
        lblText.setWrapText(true);
        textBox.getChildren().addAll(nameRow, lblText);

        VBox rightBox = new VBox(4);
        rightBox.setAlignment(Pos.TOP_RIGHT);
        String timeStr = c.getDateCreation()!=null ? formatRelativeTime(c.getDateCreation().getTime()) : "A l'instant";
        Label lblTime = new Label(timeStr);
        lblTime.setStyle("-fx-font-size:11px;-fx-text-fill:#8a8d91;");
        rightBox.getChildren().add(lblTime);

        HBox actionBtns = new HBox(6);
        actionBtns.setAlignment(Pos.CENTER_RIGHT);

        if (!isReply) {
            Button btnReply = new Button("Repondre");
            btnReply.setStyle("-fx-background-color:transparent;-fx-text-fill:#1877f2;-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 4;");
            btnReply.setOnAction(ev -> toggleReplyInput(c, wrapper, section));
            actionBtns.getChildren().add(btnReply);
        }

        // Translate button — always visible on every comment
        Button btnTranslateC = new Button("Traduire");
        btnTranslateC.setStyle("-fx-background-color:transparent;-fx-text-fill:#1877f2;"
            + "-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;"
            + "-fx-border-color:transparent;-fx-padding:2 4;");
        btnTranslateC.setOnAction(ev -> toggleCommentTranslation(c, textBox, btnTranslateC));
        actionBtns.getChildren().add(btnTranslateC);

        if (session.canModify(c.getIdUtilisateur())) {
            Button btnModify = new Button("Modifier");
            btnModify.setStyle("-fx-background-color:transparent;-fx-text-fill:#050505;-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 4;");
            Button btnDelete = new Button("Supprimer");
            btnDelete.setStyle("-fx-background-color:transparent;-fx-text-fill:#e74c3c;-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;-fx-border-color:transparent;-fx-padding:2 4;");
            btnModify.setOnAction(ev -> startEditComment(c, row, section, wrapper, isReply));
            btnDelete.setOnAction(ev -> { try { commentaireService.delete(c); loadFeed(); } catch (SQLException ex) { ex.printStackTrace(); } });
            actionBtns.getChildren().addAll(btnModify, btnDelete);
        }

        if (!actionBtns.getChildren().isEmpty()) rightBox.getChildren().add(actionBtns);
        row.getChildren().addAll(avatar, textBox, rightBox);
        wrapper.getChildren().add(row);

        if (!isReply) {
            try {
                for (Commentaire reply : commentaireService.getReplies(c.getIdCommentaire()))
                    wrapper.getChildren().add(buildCommentRow(reply, section, true));
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
        return wrapper;
    }

    private void toggleReplyInput(Commentaire parent, VBox wrapper, VBox section) {
        String tag = "reply-input-" + parent.getIdCommentaire();
        boolean removed = wrapper.getChildren().removeIf(node -> tag.equals(node.getUserData()));
        if (!removed) {
            HBox replyInput = buildCommentInputRow(parent.getIdPost(), parent.getIdCommentaire(), section);
            replyInput.setUserData(tag);
            wrapper.getChildren().add(replyInput);
            replyInput.getChildren().stream()
                .filter(n -> n instanceof TextField).findFirst()
                .ifPresent(n -> ((TextField) n).requestFocus());
        }
    }

    private void startEditComment(Commentaire c, HBox row, VBox section, VBox wrapper, boolean isReply) {
        VBox editBox = new VBox(6);
        HBox.setHgrow(editBox, Priority.ALWAYS);
        String authorName = resolveUserName(c.getIdUtilisateur());
        Role authorRole   = resolveUserRole(c.getIdUtilisateur());
        Label nameClone = new Label(authorName + "  (" + (authorRole!=null?authorRole.name():"?") + ")");
        nameClone.setStyle("-fx-font-weight:bold;-fx-font-size:13px;-fx-text-fill:#050505;");
        TextField tfEdit = new TextField(c.getContenu());
        tfEdit.getStyleClass().add("comment-field");
        tfEdit.setPromptText("Modifier le commentaire...");
        HBox editBtns = new HBox(8); editBtns.setAlignment(Pos.CENTER_LEFT);
        Button btnSave   = makeSaveBtn("Sauvegarder");
        Button btnCancel = makeCancelBtn("Annuler");
        btnSave.setStyle(btnSave.getStyle()     + "-fx-font-size:12px;-fx-padding:4 12;");
        btnCancel.setStyle(btnCancel.getStyle() + "-fx-font-size:12px;-fx-padding:4 12;");
        btnSave.setOnAction(ev -> {
            String newText = tfEdit.getText().trim();
            if (newText.isEmpty()) { showToast("Commentaire vide."); return; }
            c.setContenu(newText);
            try { commentaireService.update(c); showToast("Commentaire modifie !"); loadFeed(); }
            catch (SQLException ex) { ex.printStackTrace(); showToast("Erreur modification."); }
        });
        btnCancel.setOnAction(ev -> {
            int idx = wrapper.getChildren().indexOf(row);
            if (idx >= 0) wrapper.getChildren().set(idx, buildCommentRow(c, section, isReply).getChildren().get(0));
        });
        editBtns.getChildren().addAll(btnSave, btnCancel);
        editBox.getChildren().addAll(nameClone, tfEdit, editBtns);
        row.getChildren().set(1, editBox);
        if (row.getChildren().size() > 2) row.getChildren().remove(2);
        tfEdit.requestFocus(); tfEdit.selectAll();
    }

    // ── HELPERS ─────────────────────────────────────────────────────────────
    private User getUserById(int userId) {
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                userCache.put(userId, user);
                return user;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return DEMO_USERS.stream().filter(u -> u.getIdUtilisateur() == userId).findFirst().orElse(null);
    }

    private String resolveUserName(int userId) {
        User user = getUserById(userId);
        if (user != null && user.getNomUtilisateur() != null) {
            return user.getNomUtilisateur();
        }
        return "Utilisateur #" + userId;
    }

    private Role resolveUserRole(int userId) {
        User user = getUserById(userId);
        if (user != null) {
            return user.getRole();
        }
        return null;
    }
    private String roleColor(Role role) {
        if (role==null) return "#95a5a6";
        switch(role) { 
            case ADMIN: return "#e74c3c"; 
            case ENSEIGNANT:
            case INSTRUCTEUR: return "#f39c12"; 
            default: return "#27ae60"; 
        }
    }
    private String badgeColor(Badge badge) {
        if (badge==null) return "#95a5a6";
        switch(badge) { case DEBUTANT: return "#22c55e"; case ACTIF: return "#3b82f6"; case PRO: return "#ef4444"; default: return "#6b7280"; }
    }
    private Button makeActionBtn(String text) {
        Button btn = new Button(text); btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS); btn.getStyleClass().add("btn-action"); return btn;
    }
    private Button makeDangerBtn(String text) {
        Button btn = new Button(text); btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);
        final String base  = "-fx-background-color:transparent;-fx-text-fill:#e74c3c;-fx-font-weight:bold;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:8 12;-fx-background-radius:6;-fx-border-color:transparent;";
        final String hover = "-fx-background-color:#fdecea;-fx-text-fill:#c0392b;-fx-font-weight:bold;-fx-font-size:14px;-fx-cursor:hand;-fx-padding:8 12;-fx-background-radius:6;-fx-border-color:transparent;";
        btn.setStyle(base); btn.setOnMouseEntered(ev->btn.setStyle(hover)); btn.setOnMouseExited(ev->btn.setStyle(base)); return btn;
    }
    private Button makeSaveBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:#1877f2;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:6 16;-fx-cursor:hand;-fx-border-color:transparent;");
        return btn;
    }
    private Button makeCancelBtn(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color:transparent;-fx-text-fill:#65676b;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:6 16;-fx-cursor:hand;-fx-border-color:transparent;");
        return btn;
    }
    private StackPane buildAvatar(String name, boolean small) {
        StackPane sp = new StackPane(); int size = small?36:40;
        sp.setMinSize(size,size); sp.setPrefSize(size,size); sp.setMaxSize(size,size);
        sp.getStyleClass().add("avatar");
        Label l = new Label(name.isEmpty()?"?":name.substring(0,1).toUpperCase());
        l.getStyleClass().add("avatar-label"); if(small) l.setStyle("-fx-font-size:13px;");
        sp.getChildren().add(l); return sp;
    }
    private String formatRelativeTime(long millis) {
        long diff=System.currentTimeMillis()-millis, secs=diff/1000, mins=secs/60, hours=mins/60, days=hours/24;
        if(days>0) return "il y a "+days+"j"; if(hours>0) return "il y a "+hours+"h";
        if(mins>0) return "il y a "+mins+"m"; return "il y a "+secs+"s";
    }
    private void showToast(String msg) {
        lblToast.setText(msg); lblToast.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.seconds(3), lblToast);
        ft.setFromValue(1.0); ft.setToValue(0.0); ft.setOnFinished(e->lblToast.setVisible(false)); ft.play();
    }

    // ── Moderation alert — styled to match the existing UI ──────────────────
    private void showModerationAlert(ModerationService.ModerationResult mod) {
        // Build warning message based on user's violation count
        String warningLine = mod.warnings >= 3
            ? "\n\nAttention : votre compte est sous surveillance apres " + mod.warnings + " violations."
            : (mod.warnings > 0 ? "\n\nAvertissement " + mod.warnings + " enregistre sur votre compte." : "");

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Publication refusee");
        alert.setHeaderText("Contenu inapproprie detecte");
        alert.setContentText(
            "Publication refusee : contenu inapproprie detecte.\n\n"
            + "Raison : " + mod.reason
            + warningLine
        );

        // Style the dialog to match Skillora's blue theme
        alert.getDialogPane().setStyle(
            "-fx-background-color:white;"
            + "-fx-border-color:#1877f2;"
            + "-fx-border-width:2;"
            + "-fx-border-radius:8;"
            + "-fx-background-radius:8;"
        );

        // Show admin stats in console for monitoring
        if (session.isAdmin()) {
            System.out.println("[ADMIN MODERATION] " + ModerationService.getAdminStats());
        }

        alert.showAndWait();
    }

    /**
     * Admin-only: show moderation statistics in a dialog.
     */
    public void showAdminModerationStats() {
        if (!session.isAdmin()) return;
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Statistiques de moderation");
        info.setHeaderText("Tableau de bord — Moderation IA");
        info.setContentText(
            ModerationService.getAdminStats()
            + "\n\nSeuil de toxicite : 70%"
            + "\nMoteur : Google Perspective API + filtres locaux"
        );
        info.showAndWait();
    }

    /**
     * Admin-only: show image moderation statistics in a dialog.
     */
    public void showAdminImageModerationStats() {
        if (!session.isAdmin()) return;
        int blocked = ImageModerationService.getTotalBlockedImages();
        String topUsers = ImageModerationService.getTopUnsafeUsers(5);
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Statistiques — Moderation Images");
        info.setHeaderText("Tableau de bord — Images bloquees");
        info.setContentText(
            "Images bloquees au total : " + blocked
            + "\n\nTop utilisateurs avec violations :\n" + topUsers
            + "\nSeuil : 70% | Moteur : Sightengine API"
        );
        info.showAndWait();
    }

    // Image moderation alert — styled to match existing Skillora UI
    private void showImageModerationAlert(ImageModerationService.ImageModerationResult mod) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Image refusee");
        alert.setHeaderText("Image inappropriee detectee");
        StringBuilder msg = new StringBuilder();
        msg.append("Publication refusee : image inappropriee detectee.\n\n");
        msg.append("Raison : ").append(mod.reason).append("\n\n");
        msg.append("Details :\n");
        if (mod.nudityScore > 0)
            msg.append(String.format("  - Nudite/NSFW : %.0f%%\n", mod.nudityScore * 100));
        if (mod.violenceScore > 0)
            msg.append(String.format("  - Violence    : %.0f%%\n", mod.violenceScore * 100));
        if (mod.weaponDetected)
            msg.append("  - Arme detectee\n");
        if (mod.spamDetected)
            msg.append("  - Spam/watermark detecte\n");
        alert.setContentText(msg.toString());
        alert.getDialogPane().setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:#1877f2;" +
            "-fx-border-width:2;" +
            "-fx-border-radius:8;" +
            "-fx-background-radius:8;"
        );
        System.out.println("[ImageModeration] Blocked: " + mod);
        alert.showAndWait();
    }

    // ══════════════════════════════════════════════════════════
    //  TRANSLATION — POST
    // ══════════════════════════════════════════════════════════

    /**
     * Toggles translation panel below a post's content area.
     * First click: shows language picker + translated text.
     * Second click: hides the translation panel.
     */
    private void togglePostTranslation(Poste post, VBox contentArea, Button btnTranslate) {
        final String TAG = "translation-panel-post-" + post.getIdPost();

        // If panel already open, remove it (toggle off)
        boolean removed = contentArea.getChildren().removeIf(n -> TAG.equals(n.getUserData()));
        if (removed) {
            btnTranslate.setText("Traduire");
            return;
        }

        // Build the translation panel
        VBox panel = buildTranslationPanel(
            post.getTitre() + " " + post.getContenu(),
            btnTranslate,
            TAG
        );
        contentArea.getChildren().add(panel);
        btnTranslate.setText("Masquer traduction");
    }

    // ══════════════════════════════════════════════════════════
    //  TRANSLATION — COMMENT
    // ══════════════════════════════════════════════════════════

    private void toggleCommentTranslation(Commentaire comment, VBox textBox, Button btnTranslate) {
        final String TAG = "translation-panel-comment-" + comment.getIdCommentaire();
        boolean removed = textBox.getChildren().removeIf(n -> TAG.equals(n.getUserData()));
        if (removed) {
            btnTranslate.setText("Traduire");
            return;
        }
        VBox panel = buildTranslationPanel(comment.getContenu(), btnTranslate, TAG);
        textBox.getChildren().add(panel);
        btnTranslate.setText("Masquer traduction");
    }

    // ══════════════════════════════════════════════════════════
    //  TRANSLATION — SHARED UI HELPER
    // ══════════════════════════════════════════════════════════

    private VBox buildTranslationPanel(String originalText, Button btnTranslate, String tag) {
        VBox panel = new VBox(8);
        panel.setUserData(tag);
        panel.setStyle("-fx-padding:12 0 0 0;");

        // Language picker
        HBox langRow = new HBox(8);
        langRow.setAlignment(Pos.CENTER_LEFT);

        Label lblTo = new Label("Traduire en :");
        lblTo.setStyle("-fx-text-fill:#65676b;-fx-font-size:12px;-fx-font-weight:bold;");

        ComboBox<String> langCombo = new ComboBox<>();
        langCombo.getItems().addAll("Français (fr)", "English (en)", "العربية (ar)");
        langCombo.setValue("Français (fr)");
        langCombo.setStyle("-fx-background-radius:6;-fx-border-radius:6;-fx-padding:4 8;");

        Label lblResult = new Label();
        lblResult.setWrapText(true);
        lblResult.setStyle("-fx-background-color:#f7f8fa;-fx-padding:10 12;-fx-background-radius:8;-fx-border-color:#e4e6eb;-fx-border-width:1;-fx-text-fill:#050505;");

        // Translate button (per-panel)
        Button btnDoTranslate = new Button("Traduire");
        btnDoTranslate.setStyle("-fx-background-color:#1877f2;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:6;-fx-padding:6 14;-fx-cursor:hand;-fx-border-color:transparent;");

        btnDoTranslate.setOnAction(ev -> {
            String selected = langCombo.getValue();
            String targetLang = "fr";
            if (selected != null) {
                if (selected.contains("en")) targetLang = "en";
                else if (selected.contains("ar")) targetLang = "ar";
            }
            String translated = TranslationService.translateText(originalText, "auto", targetLang);
            lblResult.setText(translated);
        });

        langRow.getChildren().addAll(lblTo, langCombo, btnDoTranslate);
        panel.getChildren().addAll(langRow, lblResult);
        return panel;
    }

    // ── Repost handler ──────────────────────────────────────────────
    private void handleRepost(Poste original) {
        try {
            Poste repost = posteService.repost(original.getIdPost(), session.getCurrentUserId());
            if (repost != null) {
                showToast("Repartage reussi !");
                loadFeed();
            } else {
                showToast("Vous avez deja repartage ce post.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showToast("Erreur lors du repartage.");
        }
    }
}
