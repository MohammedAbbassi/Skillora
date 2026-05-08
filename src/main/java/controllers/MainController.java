package controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.effect.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import entities.User;
import entities.UserPreferences;
import entities.Message;
import entities.Quiz;
import entities.Question;
import entities.Reponse;
import services.ServiceUser;
import services.ServiceUserPreferences;
import services.ServiceMessage;
import services.QuizService;
import services.QuestionService;
import services.ReponseService;
import utils.BadgeUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.net.URL;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class MainController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private HBox   topbar;
    @FXML private Label  topbarLogoText;
    @FXML private Label  topbarUserName;
    @FXML private Label  topbarUserRole;
    @FXML private Label  avatarInitial;
    @FXML private Circle avatarCircle;
    @FXML private HBox   profileTrigger;
    @FXML private Label  profileChevron;
    @FXML private VBox   profileDropdown;
    @FXML private Label  ddUserName;
    @FXML private Label  ddUserEmail;
    @FXML private Button notifBtn;
    @FXML private Label  notifBadge;
    @FXML private Label levelLabel;
    @FXML private Label xpLabel;
    @FXML private VBox   sidebar;
    @FXML private HBox   sidebarHeader;
    @FXML private StackPane sidebarIcon;
    @FXML private Label  sidebarLevelLabel;
    @FXML private Button collapseBtn;
    @FXML private Label  collapseIcon;
    @FXML private VBox   battlePassWidget;
    @FXML private VBox   adminSection;

    @FXML private Button navHome;
    @FXML private Button navCourses;
    @FXML private Button navQuizzes;
    @FXML private Button navLeaderboard;
    @FXML private Button navProgress;
    @FXML private Button navProfile;
    @FXML private Button navAdmin;
    @FXML private Button navSettings;
    @FXML private Button navLogout;

    @FXML private Label  lblHome;
    @FXML private Label  lblCourses;
    @FXML private Label  lblQuizzes;
    @FXML private Label  lblLeaderboard;
    @FXML private Label  lblProgress;
    @FXML private Label  lblProfile;
    @FXML private Label  lblAdmin;
    @FXML private Label  lblSettings;
    @FXML private Label  lblLogout;

    @FXML private ScrollPane contentScroll;
    @FXML private VBox   pageHome;
    @FXML private VBox   pageCourses;
    @FXML private VBox   pageQuizzes;
    @FXML private VBox   quizListContainer;
    @FXML private TextField quizSearchField;
    @FXML private ComboBox<String> quizNiveauFilter;
    @FXML private ComboBox<Quiz.Matiere> quizMatiereFilter;
    @FXML private VBox   pageLeaderboard;
    @FXML private VBox   leaderboardContainer;
    @FXML private ComboBox<String> sortMetricCombo;
    @FXML private ComboBox<String> countryFilterCombo;
    @FXML private VBox   pageProgress;
    @FXML private VBox   pageProfile;
    @FXML private VBox   pageAdmin;
    @FXML private VBox   pageSettings;

    // Progress page charts & KPIs
    @FXML private AreaChart<String, Number> xpAreaChart;
    @FXML private BarChart<String, Number>  activityBarChart;
    @FXML private Label kpiStreak;
    @FXML private Label kpiXP;
    @FXML private Label kpiCourses;
    @FXML private Label kpiQuizzes;

    @FXML private Label       greetingLabel;
    @FXML private Label       streakCount;
    @FXML private ProgressBar prog1;
    @FXML private ProgressBar prog2;
    @FXML private ProgressBar profileProg;
    @FXML private Label       profileXpLabel;
    @FXML private Label       sidebarLevelLabelProfile;



    @FXML private Label  profileAvatarInitial;
    @FXML private Circle profilePhotoCircle;
    @FXML private Label  profileHeroName;
    @FXML private Label  profileHeroEmail;
    @FXML private FlowPane profileAchievementsFlow;
    @FXML private Label  profileBadgeCount;
    @FXML private Label  profileRankBadge;
    @FXML private Label  profileRoleBadge;
    @FXML private Label  statStreak;
    @FXML private Label  statXP;

    @FXML private TextField adminSearchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private VBox   userTableBody;
    @FXML private CheckBox darkModeToggle;

    private boolean sidebarCollapsed = false;
    private boolean dropdownOpen     = false;
    private UserPreferences prefs;
    private User currentUser;
    private final services.ServiceRelation serviceRelation = new services.ServiceRelation();
    
    @FXML private VBox pagePublicProfile;
    @FXML private VBox publicProfileContainer;
    @FXML private VBox pageFriends;
    @FXML private VBox friendsContainer;
    @FXML private VBox activityFeedContainer;
    @FXML private TextField friendSearchField;
    @FXML private Button navFriends;
    @FXML private Label lblFriends;
    @FXML private Button navCommunity;
    @FXML private Label lblCommunity;
    @FXML private VBox pageCommunity;
    @FXML private VBox communityFeedContainer;
    @FXML private javafx.scene.control.TextField communityPostTitle;
    @FXML private javafx.scene.control.TextArea communityPostContent;
    @FXML private Button navShop;
    @FXML private Button navOrders;
    @FXML private Button navAdminProducts;
    @FXML private Label lblShop;
    @FXML private Label lblOrders;
    @FXML private Label lblAdminProducts;
    @FXML private VBox pageShop;
    @FXML private VBox pageOrders;
    @FXML private VBox pageAdminProducts;

    private static final double SIDEBAR_FULL      = 230;
    private static final double SIDEBAR_COLLAPSED = 80;

    private List<Button> allNavBtns;
    private List<Label>  allNavLabels;
    private List<VBox> allPages;

    private final ServiceMessage serviceMessage = new ServiceMessage();
    private Timeline pollingTimeline;

    @FXML private VBox rightSidebar;
    @FXML private Button rightCollapseBtn;
    @FXML private Label rightCollapseIcon;
    @FXML private Label rightSidebarTitle;
    @FXML private VBox rightFriendsContainer;
    @FXML private VBox chatBox;
    @FXML private Label chatHeader;
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;

    private boolean rightSidebarCollapsed = false;
    private User currentChatUser = null;
    private static final double RIGHT_SIDEBAR_FULL = 250;
    private static final double RIGHT_SIDEBAR_COLLAPSED = 64;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceUserPreferences servicePrefs = new ServiceUserPreferences();
    private final QuizService quizService = new QuizService();
    private final QuestionService questionService = new QuestionService();
    private final ReponseService reponseService = new ReponseService();
    private final CommunityController communityController = new CommunityController();
    private List<User> allUsers;
    private List<Quiz> allQuizzes;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        allNavBtns = new java.util.ArrayList<>();
        addIfNotNull(allNavBtns, navHome, "navHome");
        addIfNotNull(allNavBtns, navCourses, "navCourses");
        addIfNotNull(allNavBtns, navQuizzes, "navQuizzes");
        addIfNotNull(allNavBtns, navLeaderboard, "navLeaderboard");
        addIfNotNull(allNavBtns, navProgress, "navProgress");
        addIfNotNull(allNavBtns, navProfile, "navProfile");
        addIfNotNull(allNavBtns, navAdmin, "navAdmin");
        addIfNotNull(allNavBtns, navSettings, "navSettings");
        addIfNotNull(allNavBtns, navLogout, "navLogout");
        addIfNotNull(allNavBtns, navFriends, "navFriends");
        addIfNotNull(allNavBtns, navCommunity, "navCommunity");
        addIfNotNull(allNavBtns, navShop, "navShop");
        addIfNotNull(allNavBtns, navOrders, "navOrders");
        addIfNotNull(allNavBtns, navAdminProducts, "navAdminProducts");

        allNavLabels = new java.util.ArrayList<>();
        addIfNotNull(allNavLabels, lblHome, "lblHome");
        addIfNotNull(allNavLabels, lblCourses, "lblCourses");
        addIfNotNull(allNavLabels, lblQuizzes, "lblQuizzes");
        addIfNotNull(allNavLabels, lblLeaderboard, "lblLeaderboard");
        addIfNotNull(allNavLabels, lblProgress, "lblProgress");
        addIfNotNull(allNavLabels, lblProfile, "lblProfile");
        addIfNotNull(allNavLabels, lblAdmin, "lblAdmin");
        addIfNotNull(allNavLabels, lblSettings, "lblSettings");
        addIfNotNull(allNavLabels, lblLogout, "lblLogout");
        addIfNotNull(allNavLabels, lblFriends, "lblFriends");
        addIfNotNull(allNavLabels, lblCommunity, "lblCommunity");
        addIfNotNull(allNavLabels, lblShop, "lblShop");
        addIfNotNull(allNavLabels, lblOrders, "lblOrders");
        addIfNotNull(allNavLabels, lblAdminProducts, "lblAdminProducts");

        allPages = new java.util.ArrayList<>();
        addIfNotNull(allPages, pageHome, "pageHome");
        addIfNotNull(allPages, pageCourses, "pageCourses");
        addIfNotNull(allPages, pageQuizzes, "pageQuizzes");
        addIfNotNull(allPages, pageLeaderboard, "pageLeaderboard");
        addIfNotNull(allPages, pageProgress, "pageProgress");
        addIfNotNull(allPages, pageProfile, "pageProfile");
        addIfNotNull(allPages, pageAdmin, "pageAdmin");
        addIfNotNull(allPages, pageSettings, "pageSettings");
        addIfNotNull(allPages, pagePublicProfile, "pagePublicProfile");
        addIfNotNull(allPages, pageFriends, "pageFriends");
        addIfNotNull(allPages, pageCommunity, "pageCommunity");
        addIfNotNull(allPages, pageShop, "pageShop");
        addIfNotNull(allPages, pageOrders, "pageOrders");
        addIfNotNull(allPages, pageAdminProducts, "pageAdminProducts");

        if (friendSearchField != null) {
            friendSearchField.textProperty().addListener((obs, old, val) -> loadFriends());
        }

        communityController.init(communityFeedContainer, communityPostTitle, communityPostContent);

        setupLeaderboardFilters();
        setupQuizFilters();
        setGreeting();
        populateFilters();
        loadUserTable();
        loadFriends();
        setupSearch();
        setupQuizSearch();
        try {
            initProgressCharts();
        } catch (Exception e) {
            System.err.println("[Charts] Init error: " + e.getMessage());
        }

        topbar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnMouseClicked(e -> {
                    if (!profileTrigger.getBoundsInParent().contains(
                            profileTrigger.sceneToLocal(e.getSceneX(), e.getSceneY()))
                        && !profileDropdown.getBoundsInParent().contains(
                            profileDropdown.sceneToLocal(e.getSceneX(), e.getSceneY()))) {
                        closeDropdown();
                    }
                });
            }
        });
    }

    private void initProgressCharts() {
        // XP Area Chart (weekly data)
        XYChart.Series<String, Number> xpSeries = new XYChart.Series<>();
        xpSeries.setName("XP");
        xpSeries.getData().add(new XYChart.Data<>("Week 1", 120));
        xpSeries.getData().add(new XYChart.Data<>("Week 2", 310));
        xpSeries.getData().add(new XYChart.Data<>("Week 3", 580));
        xpSeries.getData().add(new XYChart.Data<>("Week 4", 950));
        if (xpAreaChart != null) {
            xpAreaChart.getData().add(xpSeries);
            xpAreaChart.setCreateSymbols(true);
        }

        // Activity Bar Chart (daily lessons)
        XYChart.Series<String, Number> actSeries = new XYChart.Series<>();
        actSeries.setName("Lessons");
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[]    vals = {  3,     5,     2,     7,     4,     6,     1   };
        for (int i = 0; i < days.length; i++) {
            actSeries.getData().add(new XYChart.Data<>(days[i], vals[i]));
        }
        if (activityBarChart != null) {
            activityBarChart.getData().add(actSeries);
        }
    }

    public void applyPreferences(UserPreferences p) {
        if (p == null) return;
        this.prefs = p;
        
        if (p.getUserName() != null) {
            if (topbarUserName != null) topbarUserName.setText(p.getUserName());
            if (profileHeroName != null) profileHeroName.setText(p.getUserName());
            if (ddUserName != null) ddUserName.setText(p.getUserName());
            
            String initial = p.getUserName().substring(0, 1).toUpperCase();
            if (avatarInitial != null) avatarInitial.setText(initial);
            if (profileAvatarInitial != null) profileAvatarInitial.setText(initial);
        }
        
        if (topbarUserRole != null) topbarUserRole.setText(p.getUserRole());
        if (profileHeroEmail != null) profileHeroEmail.setText(p.getUserEmail());
        if (profileRoleBadge != null) profileRoleBadge.setText(p.getUserRole());
        
        if (statXP != null) statXP.setText(String.format("%,d", p.getXpPoints()));
        
        // Calculate Level (1000 XP per level)
        int xp = p.getXpPoints();
        int level = (xp / 1000) + 1;
        int currentXp = xp % 1000;
        double progress = currentXp / 1000.0;
        
        if (levelLabel != null) levelLabel.setText("LEVEL " + level);
        if (sidebarLevelLabel != null) sidebarLevelLabel.setText("QUEST LEVEL: " + level);
        if (sidebarLevelLabelProfile != null) sidebarLevelLabelProfile.setText("LEVEL " + level);
        if (xpLabel != null) xpLabel.setText(currentXp + " / 1000 XP");
        if (profileXpLabel != null) profileXpLabel.setText(currentXp + " / 1000 XP to next level");
        if (prog1 != null) prog1.setProgress(progress);
        if (profileProg != null) profileProg.setProgress(progress);
        if (streakCount != null) streakCount.setText(p.getStreakDays() + " Days");
        if (statStreak != null) statStreak.setText(String.valueOf(p.getStreakDays()));

        setGreeting();

        boolean isAdmin = "ADMIN".equalsIgnoreCase(p.getUserRole());
        if (adminSection != null) {
            adminSection.setVisible(isAdmin);
            adminSection.setManaged(isAdmin);
        }

        loadCurrentUserAndAvatar();
        
        // Force status to online on load
        if (currentUser != null) {
            try { serviceUser.setOnline(currentUser.getId()); } catch(Exception ignored){}
        }
        
        loadRightFriends();
        applyAccessibilityCss(p);
        setupWindowDragging();

        if (pollingTimeline == null) {
            pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> pollData()));
            pollingTimeline.setCycleCount(Animation.INDEFINITE);
            pollingTimeline.play();
        }

        if (rootPane != null) {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                        if (newWin instanceof Stage) {
                            ((Stage) newWin).setOnCloseRequest(e -> {
                                if (currentUser != null) {
                                    try { serviceUser.setOffline(currentUser.getId()); } catch(Exception ex){}
                                }
                            });
                        }
                    });
                }
            });
        }

        if (p.isReduceAnimations()) {
        }
    }

    private void applyAccessibilityCss(UserPreferences p) {
        contentScroll.setStyle("-fx-background: " + p.getBackgroundColor() + ";");
        pageHome.setStyle("-fx-background-color: " + p.getBackgroundColor() + ";");
        pageProfile.setStyle("-fx-background-color: " + p.getBackgroundColor() + ";");
        pageAdmin.setStyle("-fx-background-color: " + p.getBackgroundColor() + ";");

        String textStyle = "-fx-text-fill: " + p.getTextColor() + ";";
        greetingLabel.setStyle(textStyle);
    }

    private void setGreeting() {
        if (greetingLabel == null) return;
        int hour = LocalTime.now().getHour();
        String tod = hour < 12 ? "Good morning" : hour < 18 ? "Good afternoon" : "Good evening";
        String name = prefs != null ? prefs.getUserName() : "there";
        greetingLabel.setText(tod + ", " + name + " 👋");
    }

    private void loadCurrentUserAndAvatar() {
        if (prefs == null || prefs.getUserEmail() == null || prefs.getUserEmail().isBlank()) {
            applyDefaultAvatar();
            return;
        }
        try {
            currentUser = serviceUser.findByEmail(prefs.getUserEmail());
            applyCurrentAvatar();
            if (profileAchievementsFlow != null) {
                BadgeUtils.buildAchievements(currentUser, profileAchievementsFlow);
                profileBadgeCount.setText(profileAchievementsFlow.getChildren().size() + " / 32 Badges");
            }
        } catch (Exception e) {
            System.err.println("Could not load user avatar: " + e.getMessage());
            applyDefaultAvatar();
        }
    }

    private void applyCurrentAvatar() {
        if (currentUser == null) return;
        setAvatar(avatarCircle, avatarInitial, currentUser);
        setAvatar(profilePhotoCircle, profileAvatarInitial, currentUser);
    }

    private void applyDefaultAvatar() {
        if (currentUser == null) return;
        applyDefaultAvatarStyle(avatarCircle, avatarInitial, currentUser);
        applyDefaultAvatarStyle(profilePhotoCircle, profileAvatarInitial, currentUser);
    }

    @FXML
    private void onToggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;

        double targetW = sidebarCollapsed ? SIDEBAR_COLLAPSED : SIDEBAR_FULL;
        double fromW   = sidebarCollapsed ? SIDEBAR_FULL : SIDEBAR_COLLAPSED;

        if (prefs != null && prefs.isReduceAnimations()) {
            sidebar.setPrefWidth(targetW);
            sidebar.setMinWidth(targetW);
            updateCollapseState();
        } else {
            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(sidebar.prefWidthProperty(), fromW, Interpolator.EASE_BOTH),
                    new KeyValue(sidebar.minWidthProperty(),  fromW, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(280),
                    new KeyValue(sidebar.prefWidthProperty(), targetW, Interpolator.EASE_BOTH),
                    new KeyValue(sidebar.minWidthProperty(),  targetW, Interpolator.EASE_BOTH))
            );
            tl.setOnFinished(e -> updateCollapseState());
            tl.play();
        }
    }

    private void updateCollapseState() {
        if (collapseIcon != null) {
            collapseIcon.setText(sidebarCollapsed ? "›" : "‹");
        }
        
        for (Label l : allNavLabels) {
            l.setVisible(!sidebarCollapsed);
            l.setManaged(!sidebarCollapsed);
        }
        
        if (topbarLogoText != null) {
            topbarLogoText.setVisible(!sidebarCollapsed);
            topbarLogoText.setManaged(!sidebarCollapsed);
        }
        
        if (sidebarLevelLabel != null) {
            sidebarLevelLabel.setVisible(!sidebarCollapsed);
            sidebarLevelLabel.setManaged(!sidebarCollapsed);
        }
        
        if (sidebarIcon != null) {
            sidebarIcon.setVisible(!sidebarCollapsed);
            sidebarIcon.setManaged(!sidebarCollapsed);
        }
        
        if (battlePassWidget != null) {
            battlePassWidget.setVisible(!sidebarCollapsed);
            battlePassWidget.setManaged(!sidebarCollapsed);
        }
        
        if (sidebar != null) {
            if (sidebarCollapsed) {
                sidebar.setPadding(new javafx.geometry.Insets(24, 0, 24, 0));
                if (sidebarHeader != null) sidebarHeader.setAlignment(javafx.geometry.Pos.CENTER);
                for (Button b : allNavBtns) b.setAlignment(javafx.geometry.Pos.CENTER);
            } else {
                sidebar.setPadding(new javafx.geometry.Insets(24, 16, 24, 16));
                if (sidebarHeader != null) sidebarHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                for (Button b : allNavBtns) b.setAlignment(javafx.geometry.Pos.BASELINE_LEFT);
            }
        }
    }

    @FXML
    private void onToggleRightSidebar() {
        if (rightSidebar == null) return;
        rightSidebarCollapsed = !rightSidebarCollapsed;

        double targetW = rightSidebarCollapsed ? RIGHT_SIDEBAR_COLLAPSED : RIGHT_SIDEBAR_FULL;
        double fromW   = rightSidebarCollapsed ? RIGHT_SIDEBAR_FULL : RIGHT_SIDEBAR_COLLAPSED;

        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(rightSidebar.prefWidthProperty(), fromW, Interpolator.EASE_BOTH),
                new KeyValue(rightSidebar.minWidthProperty(),  fromW, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(280),
                new KeyValue(rightSidebar.prefWidthProperty(), targetW, Interpolator.EASE_BOTH),
                new KeyValue(rightSidebar.minWidthProperty(),  targetW, Interpolator.EASE_BOTH))
        );
        tl.setOnFinished(e -> updateRightCollapseState());
        tl.play();

        if (rightCollapseIcon != null) {
            rightCollapseIcon.setText(rightSidebarCollapsed ? "‹" : "›");
        }
    }

    private void updateRightCollapseState() {
        if (rightSidebarTitle != null) {
            rightSidebarTitle.setVisible(!rightSidebarCollapsed);
            rightSidebarTitle.setManaged(!rightSidebarCollapsed);
        }
        if (rightCollapseIcon != null) {
            rightCollapseIcon.setText(rightSidebarCollapsed ? "‹" : "›");
        }
        
        if (rightSidebar != null) {
            if (rightSidebarCollapsed) {
                rightSidebar.setPadding(new javafx.geometry.Insets(24, 0, 24, 0));
            } else {
                rightSidebar.setPadding(new javafx.geometry.Insets(24, 16, 24, 16));
            }
        }
        
        loadRightFriends();
    }



    @FXML private void onNavHome()     { navigateTo(pageHome,     navHome);     }
    @FXML private void onNavCourses()  { navigateTo(pageCourses,  navCourses);  }
    @FXML private void onNavQuizzes()  { navigateTo(pageQuizzes,  navQuizzes); loadQuizList(); }
    @FXML private void onNavLeaderboard()  { 
        navigateTo(pageLeaderboard,  navLeaderboard);
        loadLeaderboard();
    }
    @FXML private void onNavProgress() { navigateTo(pageProgress, navProgress); }
    @FXML private void onGoProfile()   { navigateTo(pageProfile,  navProfile); closeDropdown(); }
    @FXML private void onNavAdmin()    { navigateTo(pageAdmin,    navAdmin);    }
    @FXML private void onNavSettings() { navigateTo(pageSettings, navSettings); closeDropdown(); }

    @FXML private void onNavCommunity() {
        navigateTo(pageCommunity, navCommunity);
        communityController.loadFeed();
    }

    @FXML private void onCommunityRefresh() {
        communityController.loadFeed();
    }

    @FXML private void onSubmitCommunityPost() {
        communityController.onSubmitPost();
    }

    // ──────────────────────────────────────────────

    private void setupQuizFilters() {
        if (quizNiveauFilter != null) {
            quizNiveauFilter.getItems().addAll("All Levels", "DEBUTANT", "INTERMEDIAIRE", "AVANCE", "EXPERT");
            quizNiveauFilter.setValue("All Levels");
        }
        if (quizMatiereFilter != null) {
            quizMatiereFilter.getItems().addAll(Quiz.Matiere.values());
        }
    }

    private void setupQuizSearch() {
        if (quizSearchField != null) {
            quizSearchField.textProperty().addListener((obs, old, val) -> loadQuizList());
        }
        if (quizNiveauFilter != null) {
            quizNiveauFilter.valueProperty().addListener((obs, old, val) -> loadQuizList());
        }
        if (quizMatiereFilter != null) {
            quizMatiereFilter.valueProperty().addListener((obs, old, val) -> loadQuizList());
        }
    }

    @FXML
    private void onQuizRefresh() {
        loadQuizList();
    }

    private void loadQuizList() {
        if (quizListContainer == null) return;
        try {
            String search = quizSearchField != null ? quizSearchField.getText() : null;
            String niveauFiltre = null;
            if (quizNiveauFilter != null && quizNiveauFilter.getValue() != null) {
                String v = quizNiveauFilter.getValue();
                if (!"All Levels".equalsIgnoreCase(v)) {
                    niveauFiltre = v;
                }
            }
            String matiere = null;
            if (quizMatiereFilter != null && quizMatiereFilter.getValue() != null) {
                matiere = quizMatiereFilter.getValue().name();
            }

            allQuizzes = quizService.rechercher(search, null, matiere, niveauFiltre);
            quizListContainer.getChildren().clear();
            for (Quiz quiz : allQuizzes) {
                quizListContainer.getChildren().add(buildQuizCard(quiz));
            }
            if (allQuizzes.isEmpty()) {
                Label empty = new Label("No quizzes found. Click + Add Quiz to create one.");
                empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-padding: 40;");
                quizListContainer.getChildren().add(empty);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox buildQuizCard(Quiz quiz) {
        HBox card = new HBox(16);
        card.getStyleClass().add("table-row");
        card.setPadding(new javafx.geometry.Insets(14, 20, 14, 20));
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox info = new VBox(4);
        Label title = new Label(quiz.getTitre() != null ? quiz.getTitre() : "");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        Label meta = new Label(
            (quiz.getMatiereValue() != null ? quiz.getMatiereValue() : "N/A") +
            "  ·  " + (quiz.getNiveau() != null ? quiz.getNiveau() : "N/A") +
            (quiz.getCreateurNom() != null && !quiz.getCreateurNom().isEmpty() ? "  ·  by " + quiz.getCreateurNom() : "")
        );
        meta.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label desc = new Label(quiz.getDescription() != null ? quiz.getDescription().trim() : "");
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        if (!desc.getText().isEmpty()) info.getChildren().addAll(title, meta, desc);
        else info.getChildren().addAll(title, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button questionsBtn = new Button("Questions");
        questionsBtn.getStyleClass().add("row-action-btn");
        questionsBtn.setOnAction(e -> showManageQuestionsDialog(quiz));

        Button takeBtn = new Button("Take Quiz");
        takeBtn.getStyleClass().add("primary-btn");
        takeBtn.setStyle("-fx-padding: 6 16; -fx-font-size: 12px;");
        takeBtn.setOnAction(e -> showTakeQuizDialog(quiz));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("row-action-btn");
        editBtn.setOnAction(e -> showAddQuizDialog(quiz));

        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().addAll("row-action-btn", "row-delete-btn");
        deleteBtn.setOnAction(e -> deleteQuiz(quiz));

        HBox actions = new HBox(6, takeBtn, questionsBtn, editBtn, deleteBtn);
        card.getChildren().addAll(info, spacer, actions);
        return card;
    }

    @FXML
    private void onAddQuiz() {
        showAddQuizDialog(null);
    }

    private void showAddQuizDialog(Quiz existing) {
        Stage dialog = new Stage();
        dialog.setTitle(existing == null ? "Add Quiz" : "Edit Quiz");
        dialog.initOwner(topbar.getScene().getWindow());
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(30));
        root.setStyle("-fx-background-color: #FFFFFF;");

        Label titleLabel = new Label(existing == null ? "Create New Quiz" : "Edit Quiz");
        titleLabel.setStyle("-fx-font-family: Georgia; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        TextField titreField = new TextField();
        titreField.getStyleClass().add("dialog-text-field");
        titreField.setPromptText("Quiz Title");
        if (existing != null) titreField.setText(existing.getTitre());

        TextArea descArea = new TextArea();
        descArea.getStyleClass().add("dialog-text-field");
        descArea.setPromptText("Description");
        descArea.setPrefHeight(80);
        if (existing != null) descArea.setText(existing.getDescription());

        ComboBox<String> niveauCombo = new ComboBox<>();
        niveauCombo.getStyleClass().add("dialog-combo-box");
        niveauCombo.getItems().addAll("DEBUTANT", "INTERMEDIAIRE", "AVANCE", "EXPERT");
        niveauCombo.setValue(existing != null ? existing.getNiveau() : "DEBUTANT");

        ComboBox<Quiz.Matiere> matiereCombo = new ComboBox<>();
        matiereCombo.getStyleClass().add("dialog-combo-box");
        matiereCombo.getItems().addAll(Quiz.Matiere.values());
        if (existing != null) matiereCombo.setValue(existing.getMatiere());
        else matiereCombo.setValue(Quiz.Matiere.JAVA);

        HBox buttons = new HBox(12);
        buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("dialog-cancel-btn");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button(existing == null ? "Create" : "Save");
        saveBtn.getStyleClass().add("dialog-ok-btn");
        saveBtn.setOnAction(e -> {
            if (titreField.getText().trim().isEmpty()) {
                showStyledAlert(javafx.scene.control.Alert.AlertType.WARNING, "Warning", "Title is required.");
                return;
            }
            try {
                Quiz q = existing != null ? existing : new Quiz();
                q.setTitre(titreField.getText().trim());
                q.setDescription(descArea.getText().trim());
                q.setNiveau(niveauCombo.getValue());
                q.setMatiere(matiereCombo.getValue());
                if (existing == null) {
                    q.setIdCreateur(currentUser != null ? currentUser.getId() : 0);
                    quizService.add(q);
                } else {
                    quizService.update(q);
                }
                dialog.close();
                loadQuizList();
            } catch (Exception ex) {
                ex.printStackTrace();
                showStyledAlert(javafx.scene.control.Alert.AlertType.ERROR, "Error", "Failed to save quiz: " + ex.getMessage());
            }
        });

        buttons.getChildren().addAll(cancelBtn, saveBtn);

        VBox form = new VBox(12);
        form.getChildren().addAll(
            createField("Title", titreField),
            createField("Description", descArea),
            createField("Level", niveauCombo),
            createField("Subject", matiereCombo)
        );

        root.getChildren().addAll(titleLabel, form, buttons);
        Scene scene = new Scene(root, 440, 480);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void deleteQuiz(Quiz quiz) {
        boolean confirmed = showStyledConfirm("Delete Quiz",
            "Delete quiz \"" + quiz.getTitre() + "\"? This will also delete all questions and answers.");
        if (confirmed) {
            try {
                List<Question> questions = questionService.getByQuiz(quiz.getId());
                for (Question q : questions) {
                    List<Reponse> reponses = reponseService.getByQuestion(q.getId());
                    for (Reponse r : reponses) reponseService.delete(r);
                    questionService.delete(q);
                }
                quizService.delete(quiz);
                loadQuizList();
            } catch (Exception e) {
                e.printStackTrace();
                showStyledAlert(javafx.scene.control.Alert.AlertType.ERROR, "Error", "Could not delete quiz.");
            }
        }
    }

    // ── Consolidated Question & Answer Management (single window) ──────────

    private void showManageQuestionsDialog(Quiz quiz) {
        Stage dialog = new Stage();
        dialog.setTitle("Questions: " + quiz.getTitre());
        dialog.initOwner(topbar.getScene().getWindow());
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new javafx.geometry.Insets(24));
        root.setStyle("-fx-background-color: #FFFFFF;");

        StackPane views = new StackPane();

        // ── VIEW 1: Question List ──────────────────────────────────────────
        VBox questionListView = new VBox(16);
        Label qlTitle = new Label("Questions for: " + quiz.getTitre());
        qlTitle.setStyle("-fx-font-family: Georgia; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        HBox qlTopBar = new HBox(12);
        qlTopBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Button qlAddBtn = new Button("+ Add Question");
        qlAddBtn.getStyleClass().add("dialog-ok-btn");
        Button qlCloseBtn = new Button("Close");
        qlCloseBtn.getStyleClass().add("dialog-cancel-btn");
        qlCloseBtn.setOnAction(e -> dialog.close());
        qlTopBar.getChildren().addAll(qlAddBtn, qlCloseBtn);

        ScrollPane qlScroll = new ScrollPane();
        qlScroll.setFitToWidth(true);
        qlScroll.setPrefHeight(400);
        VBox questionsList = new VBox(8);
        qlScroll.setContent(questionsList);

        questionListView.getChildren().addAll(qlTitle, qlTopBar, qlScroll);

        // ── VIEW 2: Question Form ──────────────────────────────────────────
        VBox questionFormView = new VBox(16);
        Label qfTitle = new Label("New Question");
        qfTitle.setStyle("-fx-font-family: Georgia; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        TextArea qfEnonce = new TextArea();
        qfEnonce.getStyleClass().add("dialog-text-field");
        qfEnonce.setPromptText("Question text...");
        qfEnonce.setPrefHeight(80);

        ComboBox<Question.TypeQuestion> qfType = new ComboBox<>();
        qfType.getStyleClass().add("dialog-combo-box");
        qfType.getItems().addAll(Question.TypeQuestion.QCU, Question.TypeQuestion.QCM);
        qfType.setValue(Question.TypeQuestion.QCU);

        TextField qfPoint = new TextField();
        qfPoint.getStyleClass().add("dialog-text-field");
        qfPoint.setPromptText("Points (default: 1)");

        HBox qfButtons = new HBox(12);
        qfButtons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button qfBackBtn = new Button("← Back");
        qfBackBtn.getStyleClass().add("dialog-cancel-btn");

        Button qfSaveBtn = new Button("Save & Add Answers");
        qfSaveBtn.getStyleClass().add("dialog-ok-btn");

        qfButtons.getChildren().addAll(qfBackBtn, qfSaveBtn);

        VBox qfForm = new VBox(12);
        qfForm.getChildren().addAll(
            createField("Question", qfEnonce),
            createField("Type", qfType),
            createField("Points", qfPoint)
        );
        questionFormView.getChildren().addAll(qfTitle, qfForm, qfButtons);

        // ── VIEW 3: Answer Management ──────────────────────────────────────
        VBox answerView = new VBox(16);
        Label avBackTitle = new Label("Answers");
        avBackTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        Label avQuestionLabel = new Label();
        avQuestionLabel.setWrapText(true);
        avQuestionLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

        HBox avTopBar = new HBox(12);
        avTopBar.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button avBackBtn = new Button("← Back to Questions");
        avBackBtn.getStyleClass().add("dialog-cancel-btn");

        Button avAddBtn = new Button("+ Add Answer");
        avAddBtn.getStyleClass().add("dialog-ok-btn");

        avTopBar.getChildren().addAll(avBackBtn, avAddBtn);

        ScrollPane avScroll = new ScrollPane();
        avScroll.setFitToWidth(true);
        avScroll.setPrefHeight(240);
        VBox answersList = new VBox(8);
        avScroll.setContent(answersList);

        // Inline add answer form (hidden by default)
        HBox avInlineForm = new HBox(8);
        avInlineForm.setVisible(false);
        avInlineForm.setManaged(false);
        avInlineForm.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField avNewText = new TextField();
        avNewText.getStyleClass().add("dialog-text-field");
        avNewText.setPromptText("Answer text...");
        HBox.setHgrow(avNewText, Priority.ALWAYS);

        CheckBox avNewCorrect = new CheckBox("Correct");
        avNewCorrect.setStyle("-fx-font-size: 12px; -fx-font-weight: 600;");

        Button avNewAddBtn = new Button("Add");
        avNewAddBtn.getStyleClass().add("dialog-ok-btn");
        avNewAddBtn.setStyle("-fx-padding: 6 14; -fx-font-size: 12px;");

        Button avNewCancelBtn = new Button("Cancel");
        avNewCancelBtn.getStyleClass().add("dialog-cancel-btn");
        avNewCancelBtn.setStyle("-fx-padding: 6 14; -fx-font-size: 12px;");
        avNewCancelBtn.setOnAction(e -> {
            avInlineForm.setVisible(false);
            avInlineForm.setManaged(false);
            avNewText.clear();
            avNewCorrect.setSelected(false);
        });

        avInlineForm.getChildren().addAll(avNewText, avNewCorrect, avNewAddBtn, avNewCancelBtn);
        answerView.getChildren().addAll(avBackTitle, avQuestionLabel, avTopBar, avScroll, avInlineForm);

        // ── State ──────────────────────────────────────────────────────────
        final Question[] editingQuestion = {null};
        final Question[] currentAnswerQuestion = {null};

        Runnable[] refreshAnswersList = new Runnable[1];
        Runnable[] loadQuestions = new Runnable[1];

        refreshAnswersList[0] = () -> {
            if (currentAnswerQuestion[0] == null) return;
            answersList.getChildren().clear();
            try {
                List<Reponse> reponses = reponseService.getByQuestion(currentAnswerQuestion[0].getId());
                if (reponses.isEmpty()) {
                    Label empty = new Label("No answers yet. Use the form above to add one.");
                    empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 20;");
                    answersList.getChildren().add(empty);
                } else {
                    for (Reponse r : reponses) {
                        HBox row = new HBox(12);
                        row.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
                        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        row.setStyle("-fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0;");

                        Label badge = new Label(r.isCorrecte() ? "CORRECT" : "WRONG");
                        badge.setStyle(r.isCorrecte()
                            ? "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-font-size: 10px; -fx-font-weight: 800; -fx-padding: 2 8; -fx-background-radius: 4;"
                            : "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-size: 10px; -fx-font-weight: 800; -fx-padding: 2 8; -fx-background-radius: 4;");
                        badge.setMinWidth(70);
                        badge.setAlignment(javafx.geometry.Pos.CENTER);

                        Label txt = new Label(r.getTexte());
                        txt.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155;");

                        Region sp = new Region();
                        HBox.setHgrow(sp, Priority.ALWAYS);

                        CheckBox tog = new CheckBox();
                        tog.setSelected(r.isCorrecte());
                        tog.setOnAction(ev -> {
                            r.setCorrecte(tog.isSelected());
                            try { reponseService.update(r); refreshAnswersList[0].run(); } catch (Exception ex) {}
                        });

                        Button del = new Button("✕");
                        del.getStyleClass().addAll("row-action-btn", "row-delete-btn");
                        del.setOnAction(ev -> {
                            try { reponseService.delete(r); refreshAnswersList[0].run(); } catch (Exception ex) {}
                        });

                        row.getChildren().addAll(badge, txt, sp, tog, del);
                        answersList.getChildren().add(row);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        };

        loadQuestions[0] = () -> {
            questionsList.getChildren().clear();
            try {
                List<Question> questions = questionService.getByQuiz(quiz.getId());
                if (questions.isEmpty()) {
                    Label empty = new Label("No questions yet. Click + Add Question to add one.");
                    empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-padding: 20;");
                    questionsList.getChildren().add(empty);
                } else {
                    for (Question q : questions) {
                        HBox card = new HBox(12);
                        card.setPadding(new javafx.geometry.Insets(10, 16, 10, 16));
                        card.setStyle("-fx-border-color: transparent transparent #f1f5f9 transparent; -fx-border-width: 0 0 1 0;");
                        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                        Label typeBadge = new Label(q.getTypeQuestionValue());
                        typeBadge.setStyle("-fx-background-color: #ede9fe; -fx-text-fill: #7c3aed; -fx-font-size: 11px; -fx-font-weight: 800; -fx-padding: 2 8; -fx-background-radius: 4;");
                        typeBadge.setMinWidth(48);
                        typeBadge.setAlignment(javafx.geometry.Pos.CENTER);

                        VBox info = new VBox(2);
                        Label en = new Label(q.getEnonce());
                        en.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1e293b;");
                        Label meta = new Label("Point: " + q.getPoint());
                        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
                        info.getChildren().addAll(en, meta);

                        Region sp = new Region();
                        HBox.setHgrow(sp, Priority.ALWAYS);

                        Button ansBtn = new Button("Answers");
                        ansBtn.getStyleClass().add("row-action-btn");
                        ansBtn.setOnAction(ev -> {
                            currentAnswerQuestion[0] = q;
                            avQuestionLabel.setText(q.getEnonce());
                            refreshAnswersList[0].run();
                            views.getChildren().setAll(answerView);
                        });

                        Button editBtn = new Button("Edit");
                        editBtn.getStyleClass().add("row-action-btn");
                        editBtn.setOnAction(ev -> {
                            editingQuestion[0] = q;
                            qfTitle.setText("Edit Question");
                            qfEnonce.setText(q.getEnonce());
                            qfType.setValue(q.getTypeQuestion());
                            qfPoint.setText(String.valueOf(q.getPoint()));
                            qfSaveBtn.setText("Save");
                            qfBackBtn.setVisible(true);
                            qfBackBtn.setManaged(true);
                            views.getChildren().setAll(questionFormView);
                        });

                        Button delBtn = new Button("✕");
                        delBtn.getStyleClass().addAll("row-action-btn", "row-delete-btn");
                        delBtn.setOnAction(ev -> {
                            try {
                                List<Reponse> reps = reponseService.getByQuestion(q.getId());
                                for (Reponse r : reps) reponseService.delete(r);
                                questionService.delete(q);
                                loadQuestions[0].run();
                            } catch (Exception ex) { ex.printStackTrace(); }
                        });

                        card.getChildren().addAll(typeBadge, info, sp, ansBtn, editBtn, delBtn);
                        questionsList.getChildren().add(card);
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        };

        // ── Navigation wiring ──────────────────────────────────────────────
        Runnable showQuestionForm = () -> {
            editingQuestion[0] = null;
            qfTitle.setText("New Question");
            qfEnonce.clear();
            qfType.setValue(Question.TypeQuestion.QCU);
            qfPoint.clear();
            qfSaveBtn.setText("Save & Add Answers");
            qfBackBtn.setVisible(true);
            qfBackBtn.setManaged(true);
            views.getChildren().setAll(questionFormView);
        };

        qlAddBtn.setOnAction(e -> showQuestionForm.run());

        qfBackBtn.setOnAction(e -> {
            views.getChildren().setAll(questionListView);
            loadQuestions[0].run();
        });

        qfSaveBtn.setOnAction(e -> {
            String enonce = qfEnonce.getText().trim();
            if (enonce.isEmpty()) {
                showStyledAlert(javafx.scene.control.Alert.AlertType.WARNING, "Warning", "Question text is required.");
                return;
            }
            try {
                Question q = editingQuestion[0] != null ? editingQuestion[0] : new Question();
                q.setQuizId(quiz.getId());
                q.setEnonce(enonce);
                q.setTypeQuestion(qfType.getValue());
                q.setPoint(qfPoint.getText().trim().isEmpty() ? 1 : Integer.parseInt(qfPoint.getText().trim()));
                q.setEstActive(true);
                q.setIdUtilisateur(currentUser != null ? currentUser.getId() : 0);

                if (editingQuestion[0] == null) {
                    questionService.add(q);
                } else {
                    questionService.update(q);
                    editingQuestion[0] = null;
                }

                // Navigate to answers for new questions
                if (qfSaveBtn.getText().equals("Save & Add Answers")) {
                    currentAnswerQuestion[0] = q;
                    avQuestionLabel.setText(q.getEnonce());
                    refreshAnswersList[0].run();
                    views.getChildren().setAll(answerView);
                } else {
                    views.getChildren().setAll(questionListView);
                    loadQuestions[0].run();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showStyledAlert(javafx.scene.control.Alert.AlertType.ERROR, "Error", "Failed to save question: " + ex.getMessage());
            }
        });

        avBackBtn.setOnAction(e -> {
            views.getChildren().setAll(questionListView);
            loadQuestions[0].run();
        });

        avAddBtn.setOnAction(e -> {
            avInlineForm.setVisible(true);
            avInlineForm.setManaged(true);
            avNewText.requestFocus();
        });

        avNewAddBtn.setOnAction(e -> {
            if (avNewText.getText().trim().isEmpty()) {
                showStyledAlert(javafx.scene.control.Alert.AlertType.WARNING, "Warning", "Answer text is required.");
                return;
            }
            if (currentAnswerQuestion[0] == null) return;
            try {
                if (avNewCorrect.isSelected() && currentAnswerQuestion[0].getTypeQuestion() == Question.TypeQuestion.QCU) {
                    List<Reponse> existingCorrect = reponseService.getCorrectByQuestion(currentAnswerQuestion[0].getId());
                    if (!existingCorrect.isEmpty()) {
                        showStyledAlert(javafx.scene.control.Alert.AlertType.WARNING, "QCU Rule",
                            "QCU questions can only have one correct answer.");
                        return;
                    }
                }
                Reponse r = new Reponse(currentAnswerQuestion[0].getId(), avNewText.getText().trim(), avNewCorrect.isSelected());
                reponseService.add(r);
                avNewText.clear();
                avNewCorrect.setSelected(false);
                avInlineForm.setVisible(false);
                avInlineForm.setManaged(false);
                refreshAnswersList[0].run();
            } catch (Exception ex) {
                ex.printStackTrace();
                showStyledAlert(javafx.scene.control.Alert.AlertType.ERROR, "Error", "Failed to save answer.");
            }
        });

        views.getChildren().setAll(questionListView);
        root.getChildren().add(views);
        loadQuestions[0].run();

        Scene scene = new Scene(root, 640, 540);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ── Take Quiz Dialog ────────────────────────────────────────────────────

    private void showTakeQuizDialog(Quiz quiz) {
        try {
            List<Question> questions = questionService.getByQuiz(quiz.getId());
            if (questions.isEmpty()) {
                showStyledAlert(javafx.scene.control.Alert.AlertType.INFORMATION, "Empty Quiz",
                    "This quiz has no questions yet. Add some questions first.");
                return;
            }

            Stage dialog = new Stage();
            dialog.setTitle("Quiz: " + quiz.getTitre());
            dialog.initOwner(topbar.getScene().getWindow());
            dialog.setResizable(false);

            VBox root = new VBox(20);
            root.setPadding(new javafx.geometry.Insets(28));
            root.setStyle("-fx-background-color: #FFFFFF;");

            // Header
            HBox header = new HBox(16);
            header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label progressLbl = new Label("Question 1 / " + questions.size());
            progressLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #64748b;");
            Label scoreLbl = new Label("Score: 0 / 0");
            scoreLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #7c3aed;");
            Region hdrSpacer = new Region();
            HBox.setHgrow(hdrSpacer, Priority.ALWAYS);
            Button exitBtn = new Button("✕");
            exitBtn.getStyleClass().addAll("row-action-btn", "row-delete-btn");
            exitBtn.setOnAction(e -> dialog.close());
            header.getChildren().addAll(progressLbl, scoreLbl, hdrSpacer, exitBtn);

            // Question text
            Label questionLbl = new Label();
            questionLbl.setWrapText(true);
            questionLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");

            // Options container
            VBox optionsBox = new VBox(10);
            optionsBox.setPadding(new javafx.geometry.Insets(8, 0, 8, 0));

            // Next button
            Button nextBtn = new Button("Next →");
            nextBtn.getStyleClass().add("dialog-ok-btn");
            nextBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 28;");

            root.getChildren().addAll(header, questionLbl, optionsBox, nextBtn);

            // State
            ToggleGroup qcuGroup = new ToggleGroup();
            int[] currentIdx = {0};
            int[] correctCount = {0};
            int[] earnedPoints = {0};

            Runnable loadQuestion = () -> {
                try {
                    optionsBox.getChildren().clear();
                    if (currentIdx[0] >= questions.size()) {
                        showQuizResult(dialog, quiz, correctCount[0], questions.size(), earnedPoints[0]);
                        return;
                    }
                    Question q = questions.get(currentIdx[0]);
                    progressLbl.setText("Question " + (currentIdx[0] + 1) + " / " + questions.size());
                    scoreLbl.setText("Score: " + correctCount[0] + " / " + questions.size());
                    questionLbl.setText(q.getEnonce());

                    List<Reponse> reponses = reponseService.getByQuestion(q.getId());
                    if (q.getTypeQuestion() == Question.TypeQuestion.QCU) {
                        qcuGroup.getToggles().clear();
                        for (Reponse r : reponses) {
                            RadioButton rb = new RadioButton(r.getTexte());
                            rb.setUserData(r);
                            rb.setToggleGroup(qcuGroup);
                            rb.setStyle("-fx-font-size: 14px; -fx-padding: 6 0;");
                            optionsBox.getChildren().add(rb);
                        }
                    } else {
                        for (Reponse r : reponses) {
                            CheckBox cb = new CheckBox(r.getTexte());
                            cb.setUserData(r);
                            cb.setStyle("-fx-font-size: 14px; -fx-padding: 6 0;");
                            optionsBox.getChildren().add(cb);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            nextBtn.setOnAction(e -> {
                if (currentIdx[0] >= questions.size()) return;
                Question q = questions.get(currentIdx[0]);
                boolean correct = false;

                if (q.getTypeQuestion() == Question.TypeQuestion.QCU) {
                    Toggle selected = qcuGroup.getSelectedToggle();
                    correct = selected != null && ((Reponse) selected.getUserData()).isCorrecte();
                } else {
                    try {
                        List<Reponse> correctReponses = reponseService.getCorrectByQuestion(q.getId());
                        java.util.Set<Integer> correctIds = new java.util.HashSet<>();
                        for (Reponse cr : correctReponses) correctIds.add(cr.getId());

                        java.util.Set<Integer> selectedIds = new java.util.HashSet<>();
                        for (javafx.scene.Node node : optionsBox.getChildren()) {
                            if (node instanceof CheckBox && ((CheckBox) node).isSelected()) {
                                selectedIds.add(((Reponse) ((CheckBox) node).getUserData()).getId());
                            }
                        }
                        correct = !correctIds.isEmpty() && selectedIds.equals(correctIds);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (correct) {
                    correctCount[0]++;
                    earnedPoints[0] += Math.max(1, q.getPoint());
                }
                currentIdx[0]++;
                loadQuestion.run();
            });

            loadQuestion.run();

            Scene scene = new Scene(root, 560, 500);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            dialog.setScene(scene);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showQuizResult(Stage dialog, Quiz quiz, int correct, int total, int points) {
        dialog.close();
        Stage resultStage = new Stage();
        resultStage.setTitle("Quiz Complete!");
        resultStage.initOwner(topbar.getScene().getWindow());
        resultStage.setResizable(false);

        VBox root = new VBox(24);
        root.setPadding(new javafx.geometry.Insets(40));
        root.setStyle("-fx-background-color: #FFFFFF;");
        root.setAlignment(javafx.geometry.Pos.CENTER);

        Label resultTitle = new Label(correct == total ? "🎉 Perfect Score! 🎉" : "Quiz Complete!");
        resultTitle.setStyle("-fx-font-family: Georgia; -fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " +
            (correct == total ? "#16a34a" : "#1A1A2E") + ";");

        Label scoreLabel = new Label(correct + " / " + total + " correct  (" + points + " pts)");
        scoreLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #475569;");

        double pct = total > 0 ? (double) correct / total * 100 : 0;
        Label pctLabel = new Label(String.format("%.0f%%", pct));
        pctLabel.setStyle("-fx-font-size: 42px; -fx-font-weight: 900; -fx-text-fill: #7c3aed;");

        Button doneBtn = new Button("Done");
        doneBtn.getStyleClass().add("dialog-ok-btn");
        doneBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 40;");
        doneBtn.setOnAction(e -> resultStage.close());

        root.getChildren().addAll(resultTitle, scoreLabel, pctLabel, doneBtn);
        Scene scene = new Scene(root, 400, 340);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        resultStage.setScene(scene);
        resultStage.showAndWait();
    }

    @FXML
    private void onOpenPreferences() {
        Stage stage = (Stage) topbar.getScene().getWindow();
        PreferencesController.showPreferencesDialog(stage, null);
    }

    @FXML
    private void onToggleDarkMode() {
        if (darkModeToggle.isSelected()) {
            if (!rootPane.getStyleClass().contains("dark-theme")) {
                rootPane.getStyleClass().add("dark-theme");
            }
        } else {
            rootPane.getStyleClass().remove("dark-theme");
        }
    }

    private void navigateTo(VBox targetPage, Button activeBtn) {
        for (VBox page : allPages) {
            if (page != null) {
                page.setVisible(false);
                page.setManaged(false);
            }
        }

        for (Button btn : allNavBtns) {
            if (btn != null) {
                btn.getStyleClass().remove("nav-active");
            }
        }

        if (targetPage != null) {
            targetPage.setVisible(true);
            targetPage.setManaged(true);
        }

        if (activeBtn != null) {
            activeBtn.getStyleClass().add("nav-active");
        }

        contentScroll.setVvalue(0);

        if (prefs == null || !prefs.isReduceAnimations()) {
            targetPage.setOpacity(0);
            targetPage.setTranslateY(10);
            FadeTransition ft = new FadeTransition(Duration.millis(220), targetPage);
            ft.setToValue(1);
            TranslateTransition tt = new TranslateTransition(Duration.millis(220), targetPage);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            new ParallelTransition(ft, tt).play();
        } else {
            targetPage.setOpacity(1);
            targetPage.setTranslateY(0);
        }
    }

    @FXML
    private void onProfileClick() {
        navigateTo(pageProfile, null);
    }

    private void closeDropdown() {
        dropdownOpen = false;
        profileDropdown.setVisible(false);
        profileDropdown.setManaged(false);
        profileChevron.setText("⌄");
    }

    @FXML
    private void onLogout() {
        try {
            if (currentUser != null) {
                serviceUser.setOffline(currentUser.getId());
            }
            if (pollingTimeline != null) {
                pollingTimeline.stop();
            }
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/Login.fxml"));
            Parent root = loader.load();

            LoginController lc = loader.getController();
            Stage stage = (Stage) topbar.getScene().getWindow();
            lc.setPrimaryStage(stage);

            Scene scene = new Scene(root, 960, 660);
            scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm());
            stage.setTitle("Skillora — Welcome");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onChangeProfilePhoto() {
        if (prefs == null || prefs.getUserEmail() == null || prefs.getUserEmail().isBlank()) {
            showStyledAlert(Alert.AlertType.WARNING, "Profile", "No user session found.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Picture");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        File selectedFile = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            // Optimization: Resize image before saving
            BufferedImage originalImage = ImageIO.read(selectedFile);
            if (originalImage == null) {
                showStyledAlert(Alert.AlertType.ERROR, "Error", "Selected file is not a valid image.");
                return;
            }

            BufferedImage resizedImage = resizeImage(originalImage, 400, 400);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            String base64Photo = Base64.getEncoder().encodeToString(imageBytes);
            serviceUser.updateProfilePhotoByEmail(prefs.getUserEmail(), base64Photo);

            if (currentUser == null) {
                currentUser = new User();
            }
            currentUser.setPhotoProfil(base64Photo);
            applyCurrentAvatar();
            showStyledAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture updated.");
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Error", "Could not update picture: " + e.getMessage());
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Maintain aspect ratio
        double ratio = Math.min((double) targetWidth / width, (double) targetHeight / height);
        if (ratio >= 1.0) return originalImage; // No need to upscale
        
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        
        // High quality scaling
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();
        
        return resizedImage;
    }

    @FXML private void onResume()  { System.out.println("Resuming course..."); }

    private void populateFilters() {
        if (roleFilter != null) {
            roleFilter.getItems().addAll("All Roles", "ETUDIANT", "ADMIN");
            roleFilter.setValue("All Roles");
        }
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All Status", "Active", "Inactive");
            statusFilter.setValue("All Status");
        }
    }

    private void loadUserTable() {
        if (userTableBody == null) return;
        try {
            allUsers = serviceUser.getAll();
            userTableBody.getChildren().clear();
            for (User user : allUsers) {
                userTableBody.getChildren().add(buildTableRow(user));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox buildTableRow(User user) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");

        String displayName = formatUserName(user);
        String initial = displayName.substring(0, 1).toUpperCase();

        HBox nameCell = new HBox(10);
        nameCell.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        nameCell.setPrefWidth(220);
        StackPane av = new StackPane();
        Circle c = new Circle(16);
        c.getStyleClass().add("row-avatar-circle");
        Label init = new Label(initial);
        init.getStyleClass().add("row-avatar-init");
        av.getChildren().addAll(c, init);
        Label nameLbl = new Label(displayName);
        nameLbl.getStyleClass().add("row-name");
        nameCell.getChildren().addAll(av, nameLbl);

        Label emailLbl = new Label(user.getEmail());
        emailLbl.getStyleClass().add("row-email");
        emailLbl.setPrefWidth(220);

        Label roleLbl = new Label(user.getRole());
        roleLbl.getStyleClass().addAll("row-badge",
            "ADMIN".equalsIgnoreCase(user.getRole()) ? "badge-admin" : "badge-learner");
        roleLbl.setPrefWidth(110);

        Label rankLbl = new Label("Bronze");
        rankLbl.getStyleClass().add("row-rank");
        rankLbl.setPrefWidth(100);

        String status = user.isEstActif() ? "Active" : "Inactive";
        Label statusLbl = new Label(status);
        statusLbl.getStyleClass().addAll("row-status",
            user.isEstActif() ? "status-active" : "status-inactive");
        statusLbl.setPrefWidth(100);

        HBox actions = new HBox(6);
        actions.setPrefWidth(110);
        actions.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Button editBtn   = new Button("Edit");
        Button deleteBtn = new Button("✕");
        editBtn.getStyleClass().add("row-action-btn");
        deleteBtn.getStyleClass().addAll("row-action-btn", "row-delete-btn");
        editBtn.setOnAction(e -> onEditUser(user));
        deleteBtn.setOnAction(e -> onDeleteUser(user));
        actions.getChildren().addAll(editBtn, deleteBtn);

        row.getChildren().addAll(nameCell, emailLbl, roleLbl, rankLbl, statusLbl, actions);
        HBox.setMargin(nameCell, new javafx.geometry.Insets(0, 0, 0, 16));
        row.setPadding(new javafx.geometry.Insets(12, 16, 12, 0));
        return row;
    }

    private String formatUserName(User user) {
        String name = user.getPrenom();
        if (name == null || name.isEmpty()) {
            name = user.getNomUtilisateur();
        }
        if (name == null || name.isEmpty()) {
            name = user.getEmail().split("@")[0];
        }
        return name;
    }

    private void setupSearch() {
        if (adminSearchField == null) return;
        adminSearchField.textProperty().addListener((obs, old, val) -> filterUserTable(val));
        if (roleFilter != null) {
            roleFilter.valueProperty().addListener((obs, old, val) -> filterUserTable(adminSearchField.getText()));
        }
        if (statusFilter != null) {
            statusFilter.valueProperty().addListener((obs, old, val) -> filterUserTable(adminSearchField.getText()));
        }
    }

    private void filterUserTable(String search) {
        if (userTableBody == null) return;
        userTableBody.getChildren().clear();
        String searchLower = (search != null ? search : "").toLowerCase();
        String roleFilterVal = roleFilter != null ? roleFilter.getValue() : "All Roles";
        String statusFilterVal = statusFilter != null ? statusFilter.getValue() : "All Status";

        for (User user : allUsers) {
            boolean matchesSearch = searchLower.isEmpty() ||
                formatUserName(user).toLowerCase().contains(searchLower) ||
                user.getEmail().toLowerCase().contains(searchLower);

            boolean matchesRole = roleFilterVal.equals("All Roles") ||
                user.getRole().equalsIgnoreCase(roleFilterVal);

            boolean matchesStatus = statusFilterVal.equals("All Status") ||
                (statusFilterVal.equals("Active") && user.isEstActif()) ||
                (statusFilterVal.equals("Inactive") && !user.isEstActif());

            if (matchesSearch && matchesRole && matchesStatus) {
                userTableBody.getChildren().add(buildTableRow(user));
            }
        }
    }

    private void onEditUser(User user) {
        System.out.println("Edit user: " + formatUserName(user));
    }

    private void onDeleteUser(User user) {
        boolean confirmed = showStyledConfirm("Delete User",
            "Are you sure you want to delete " + formatUserName(user) + "?");
        if (confirmed) {
            try {
                serviceUser.delete(user);
                loadUserTable();
            } catch (Exception e) {
                e.printStackTrace();
                showStyledAlert(javafx.scene.control.Alert.AlertType.ERROR, "Error", "Could not delete user.");
            }
        }
    }

    @FXML
    private void onAddUser() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Add New User");
        dialogStage.initOwner(topbar.getScene().getWindow());
        dialogStage.setResizable(false);

        VBox mainVBox = new VBox(20);
        mainVBox.setPadding(new javafx.geometry.Insets(30));
        mainVBox.setStyle("-fx-background-color: #FFFFFF;");

        Label titleLabel = new Label("Create New User");
        titleLabel.setStyle("-fx-font-family: Georgia; -fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        TextField firstNameField = new TextField();
        firstNameField.getStyleClass().add("dialog-text-field");
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.getStyleClass().add("dialog-text-field");
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("dialog-text-field");
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.getStyleClass().add("dialog-text-field");
        passwordField.setPromptText("Password");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getStyleClass().add("dialog-combo-box");
        roleCombo.getItems().addAll("ETUDIANT", "ADMIN");
        roleCombo.setValue("ETUDIANT");

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("dialog-cancel-btn");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button addBtn = new Button("Add User");
        addBtn.getStyleClass().add("dialog-ok-btn");
        addBtn.setOnAction(e -> {
            User newUser = new User();
            newUser.setPrenom(firstNameField.getText().trim());
            newUser.setNom(lastNameField.getText().trim());
            newUser.setEmail(emailField.getText().trim());
            newUser.setMotDePasse(passwordField.getText());
            newUser.setNomUtilisateur(emailField.getText().contains("@") ? emailField.getText().split("@")[0] : emailField.getText());
            newUser.setRole(roleCombo.getValue());
            newUser.setEstActif(true);

            if (newUser.getEmail().isEmpty() || newUser.getMotDePasse().isEmpty()) {
                showStyledAlert(Alert.AlertType.WARNING, "Warning", "Email and password are required.");
                return;
            }
            try {
                if (serviceUser.emailExists(newUser.getEmail())) {
                    showStyledAlert(Alert.AlertType.WARNING, "Warning", "Email already exists.");
                    return;
                }
                serviceUser.add(newUser);
                loadUserTable();
                dialogStage.close();
                showStyledAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                showStyledAlert(Alert.AlertType.ERROR, "Error", "Failed to create user: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, addBtn);

        VBox formBox = new VBox(12);
        formBox.getChildren().addAll(
            createField("First Name", firstNameField),
            createField("Last Name", lastNameField),
            createField("Email", emailField),
            createField("Password", passwordField),
            createField("Role", roleCombo)
        );

        mainVBox.getChildren().addAll(titleLabel, formBox, buttonBox);

        Scene scene = new Scene(mainVBox, 420, 520);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private void showStyledAlert(javafx.scene.control.Alert.AlertType type, String title, String message) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initOwner(topbar != null ? topbar.getScene().getWindow() : null);
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(30));
        root.setStyle("-fx-background-color: #FFFFFF;");
        root.setAlignment(javafx.geometry.Pos.CENTER);

        String icon = type == javafx.scene.control.Alert.AlertType.ERROR ? "\u274C" :
                      type == javafx.scene.control.Alert.AlertType.WARNING ? "\u26A0\uFE0F" : "\u2705";
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 40px;");

        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569; -fx-text-alignment: center;");
        msg.setMaxWidth(320);

        Button okBtn = new Button("OK");
        okBtn.getStyleClass().add("dialog-ok-btn");
        okBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(iconLbl, msg, okBtn);
        Scene scene = new Scene(root, 360, 220);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private boolean showStyledConfirm(String title, String message) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initOwner(topbar != null ? topbar.getScene().getWindow() : null);
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new javafx.geometry.Insets(30));
        root.setStyle("-fx-background-color: #FFFFFF;");
        root.setAlignment(javafx.geometry.Pos.CENTER);

        Label msg = new Label(message);
        msg.setWrapText(true);
        msg.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569; -fx-text-alignment: center;");
        msg.setMaxWidth(320);

        HBox buttons = new HBox(12);
        buttons.setAlignment(javafx.geometry.Pos.CENTER);

        final boolean[] result = {false};

        Button yesBtn = new Button("Yes");
        yesBtn.getStyleClass().add("dialog-ok-btn");
        yesBtn.setOnAction(e -> { result[0] = true; stage.close(); });

        Button noBtn = new Button("No");
        noBtn.getStyleClass().add("dialog-cancel-btn");
        noBtn.setOnAction(e -> stage.close());

        buttons.getChildren().addAll(yesBtn, noBtn);
        root.getChildren().addAll(msg, buttons);
        Scene scene = new Scene(root, 360, 180);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
        return result[0];
    }

    private VBox createField(String label, Node field) {
        VBox box = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4A90E2;");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private void setupLeaderboardFilters() {
        if (sortMetricCombo == null || countryFilterCombo == null) return;
        
        sortMetricCombo.getItems().addAll("Ranked Points (RP)", "Experience (XP)");
        sortMetricCombo.setValue("Ranked Points (RP)");
        sortMetricCombo.setOnAction(e -> loadLeaderboard());
        
        countryFilterCombo.getItems().add("All Countries");
        countryFilterCombo.setValue("All Countries");
        countryFilterCombo.setOnAction(e -> loadLeaderboard());
        
        // Populate countries from users later
    }

    private void loadLeaderboard() {
        if (leaderboardContainer == null) return;
        try {
            List<User> users = serviceUser.getAll();
            
            // Populate country filter if empty (except "All Countries")
            if (countryFilterCombo.getItems().size() <= 1) {
                users.stream()
                     .map(User::getPays)
                     .filter(p -> p != null && !p.isEmpty())
                     .distinct()
                     .sorted()
                     .forEach(p -> countryFilterCombo.getItems().add(p));
            }
            
            // Filter by country
            String selectedCountry = countryFilterCombo.getValue();
            if (selectedCountry != null && !selectedCountry.equals("All Countries")) {
                users.removeIf(u -> !selectedCountry.equals(u.getPays()));
            }
            
            // Sort by metric
            String metric = sortMetricCombo.getValue();
            if ("Experience (XP)".equals(metric)) {
                users.sort((u1, u2) -> Integer.compare(u2.getXpPoints(), u1.getXpPoints()));
            } else {
                users.sort((u1, u2) -> Integer.compare(u2.getRankedPoints(), u1.getRankedPoints()));
            }
            
            leaderboardContainer.getChildren().clear();
            
            int rank = 1;
            for (User u : users) {
                leaderboardContainer.getChildren().add(buildLeaderboardRow(u, rank));
                rank++;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private HBox buildLeaderboardRow(User user, int rank) {
        HBox row = new HBox(16);
        row.getStyleClass().add("table-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new javafx.geometry.Insets(12, 20, 12, 20));
        row.setCursor(javafx.scene.Cursor.HAND);
        
        row.setOnMouseClicked(e -> showPublicProfile(user));
        
        Label rankLbl = new Label("#" + rank);
        rankLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #94a3b8;");
        rankLbl.setPrefWidth(40);
        
        if (rank == 1) rankLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #F59E0B;");
        else if (rank == 2) rankLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #94A3B8;");
        else if (rank == 3) rankLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #B45309;");
        
        StackPane avStack = new StackPane();
        Circle c = new Circle(20);
        c.getStyleClass().add("row-avatar-circle");
        Label initLbl = new Label("?");
        initLbl.getStyleClass().add("row-avatar-init");
        
        setAvatar(c, initLbl, user);
        avStack.getChildren().addAll(c, initLbl);
        
        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(formatUserName(user));
        nameLbl.getStyleClass().add("row-name");
        Label rankNameLbl = new Label(getRankName(user.getRankedPoints()));
        rankNameLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #4F46E5; -fx-font-weight: 800; -fx-text-transform: uppercase;");
        nameBox.getChildren().addAll(nameLbl, rankNameLbl);
        nameBox.setPrefWidth(200);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        VBox statsBox = new VBox(2);
        statsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label rpLbl = new Label(String.format("%,d RP", user.getRankedPoints()));
        rpLbl.setStyle("-fx-font-weight: 800; -fx-text-fill: #4F46E5; -fx-font-size: 14px;");
        Label xpLbl = new Label(String.format("%,d XP", user.getXpPoints()));
        xpLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-weight: 700;");
        statsBox.getChildren().addAll(rpLbl, xpLbl);
        
        ImageView rankImg = new ImageView();
        try {
            rankImg.setImage(new Image(getClass().getResourceAsStream(getRankImage(user.getRankedPoints()))));
            rankImg.setFitWidth(40);
            rankImg.setFitHeight(40);
        } catch (Exception ex) {}
        
        row.getChildren().addAll(rankLbl, avStack, nameBox, spacer, statsBox, rankImg);
        
        return row;
    }

    private String getRankImage(int rp) {
        int level = (rp / 1000) + 1;
        if (level >= 10) return "/images/root.png";
        switch (level) {
            case 1: return "/images/sandbox.png";
            case 2: return "/images/binary.png";
            case 3: return "/images/assembly.png";
            case 4: return "/images/kernel.png";
            case 5: return "/images/compiler.png";
            case 6: return "/images/static.png";
            case 7: return "/images/dynamic.png";
            case 8: return "/images/protocol.png";
            case 9: return "/images/architect.png";
            default: return "/images/sandbox.png";
        }
    }

    private String getRankName(int rp) {
        int level = (rp / 1000) + 1;
        if (level >= 10) return "Root";
        switch (level) {
            case 1: return "Sandbox";
            case 2: return "Binary";
            case 3: return "Assembly";
            case 4: return "Kernel";
            case 5: return "Compiler";
            case 6: return "Static";
            case 7: return "Dynamic";
            case 8: return "Protocol";
            case 9: return "Architect";
            default: return "Sandbox";
        }
    }
    
    private void showPublicProfile(User user) {
        if (publicProfileContainer == null) return;
        publicProfileContainer.getChildren().clear();
        navigateTo(pagePublicProfile, navLeaderboard);

        int rp = user.getRankedPoints();
        String accent = getRankAccent(rp);
        int nextCeil = ((rp / 1000) + 1) * 1000;
        double pct = (rp % 1000) / 1000.0;

        // ── HERO ──────────────────────────────────────────────────────────
        StackPane hero = new StackPane();
        hero.setPrefHeight(180);
        hero.setStyle("-fx-background-color: linear-gradient(to bottom right, #4F46E5, #7C3AED);");

        HBox heroContent = new HBox(28);
        heroContent.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        heroContent.setPadding(new javafx.geometry.Insets(0, 40, 20, 40));

        // Avatar
        StackPane avStack = new StackPane();
        Circle ring = new Circle(50); ring.setStyle("-fx-fill: " + accent + ";");
        ring.setEffect(new DropShadow(16, javafx.scene.paint.Color.web(accent, 0.5)));
        Circle av = new Circle(46); av.setStyle("-fx-fill: #ffffff;");
        Label initL = new Label(formatUserName(user).substring(0,1).toUpperCase());
        initL.setStyle("-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: #4F46E5;");
        if (user.getPhotoProfil() != null && !user.getPhotoProfil().isEmpty()) {
            try { byte[] b = Base64.getDecoder().decode(user.getPhotoProfil());
                av.setFill(new ImagePattern(new Image(new ByteArrayInputStream(b))));
                initL.setVisible(false);
            } catch (Exception ignored) {}
        }
        avStack.getChildren().addAll(ring, av, initL);

        // Name + rank pill
        VBox nameBlock = new VBox(6);
        nameBlock.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        Label nameLbl = new Label(formatUserName(user));
        nameLbl.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #ffffff;");
        Label rankPill = new Label("  " + getRankName(rp).toUpperCase() + "  ");
        rankPill.setStyle("-fx-background-color: " + accent + "22; -fx-text-fill: " + accent
                + "; -fx-font-weight: 800; -fx-font-size: 11px; -fx-background-radius: 20; -fx-padding: 4 0;");
        nameBlock.getChildren().addAll(nameLbl, rankPill);

        // Social buttons
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        Button addBtn = new Button(); addBtn.setPrefWidth(140);
        Button blockBtn = new Button(); blockBtn.setPrefWidth(110);
        styleSocialBtn(addBtn, "+  Add Friend", "#ffffff", "#4F46E5");
        styleSocialBtn(blockBtn, "Block", "transparent", "#ffffff");
        updateSocialButtons(user, addBtn, blockBtn);
        HBox socialRow = new HBox(10, sp2, addBtn, blockBtn);
        socialRow.setAlignment(javafx.geometry.Pos.BOTTOM_RIGHT);
        HBox.setHgrow(socialRow, Priority.ALWAYS);

        heroContent.getChildren().addAll(avStack, nameBlock, socialRow);
        hero.getChildren().add(heroContent);
        StackPane.setAlignment(heroContent, javafx.geometry.Pos.BOTTOM_LEFT);

        // ── PROGRESS BAR ──────────────────────────────────────────────────
        VBox progSection = new VBox(8);
        progSection.setPadding(new javafx.geometry.Insets(16, 40, 4, 40));
        progSection.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");
        HBox progHdr = new HBox();
        Label progTitle = new Label("RANK PROGRESS — " + getRankName(rp).toUpperCase());
        progTitle.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: #64748b;");
        Region pSp = new Region(); HBox.setHgrow(pSp, Priority.ALWAYS);
        Label progPct = new Label(String.format("%.0f%%  →  %s", pct * 100, getRankName(nextCeil)));
        progPct.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: " + accent + ";");
        progHdr.getChildren().addAll(progTitle, pSp, progPct);
        StackPane track = new StackPane();
        track.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        track.setPrefHeight(7);
        track.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 4;");
        Region fill = new Region();
        fill.setPrefHeight(7);
        fill.setStyle("-fx-background-color: linear-gradient(to right, " + accent + "99, " + accent + "); -fx-background-radius: 4;");
        fill.prefWidthProperty().bind(track.widthProperty().multiply(pct));
        fill.setEffect(new DropShadow(5, javafx.scene.paint.Color.web(accent, 0.5)));
        track.getChildren().add(fill);
        progSection.getChildren().addAll(progHdr, track);

        // ── BODY (two columns) ────────────────────────────────────────────
        HBox body = new HBox(20);
        body.setPadding(new javafx.geometry.Insets(20, 40, 40, 40));
        body.setStyle("-fx-background-color: #F8FAFC;");

        // LEFT: rank showcase + stats
        VBox leftCol = new VBox(16);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Rank showcase
        VBox rankCard = new VBox(12);
        rankCard.setAlignment(javafx.geometry.Pos.CENTER);
        rankCard.setPadding(new javafx.geometry.Insets(28));
        rankCard.setStyle("-fx-background-color: linear-gradient(to bottom, #ede9fe, #F8FAFC);"
                + "-fx-background-radius: 16; -fx-border-color: " + accent + "55;"
                + "-fx-border-radius: 16; -fx-border-width: 1;");
        rankCard.setEffect(new DropShadow(20, javafx.scene.paint.Color.web(accent, 0.12)));
        ImageView rImg = new ImageView();
        try { rImg.setImage(new Image(getClass().getResourceAsStream(getRankImage(rp))));
            rImg.setFitWidth(88); rImg.setFitHeight(88);
            rImg.setEffect(new DropShadow(14, javafx.scene.paint.Color.web(accent, 0.6)));
        } catch (Exception ignored) {}
        Label rName = new Label(getRankName(rp).toUpperCase());
        rName.setStyle("-fx-font-size: 22px; -fx-font-weight: 900; -fx-text-fill: " + accent + ";");
        rName.setEffect(new DropShadow(8, javafx.scene.paint.Color.web(accent, 0.4)));
        Label rDesc = new Label(getRankDescription(rp));
        rDesc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;"); rDesc.setWrapText(true);
        Label rRp = new Label(String.format("%,d / %,d RP", rp, nextCeil));
        rRp.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #64748b;"
                + "-fx-background-color: #ede9fe; -fx-background-radius: 20; -fx-padding: 6 14;");
        rankCard.getChildren().addAll(rImg, rName, rDesc, rRp);

        // Stat tiles grid
        GridPane sg = new GridPane();
        sg.setHgap(12); sg.setVgap(12);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setPercentWidth(50);
        sg.getColumnConstraints().addAll(col1, col2);
        sg.add(buildStatTile("⚡", "XP Points",    String.format("%,d", user.getXpPoints()), "#8b5cf6"), 0, 0);
        sg.add(buildStatTile("🔥", "Day Streak",   user.getStreakDays() + " days",         "#f97316"), 1, 0);
        sg.add(buildStatTile("🧠", "Quizzes Done", String.valueOf(user.getQuizzesDone()),  "#0ea5e9"), 0, 1);
        sg.add(buildStatTile("🎓", "Certificates", String.valueOf(user.getCertificatesCount()), "#10b981"), 1, 1);

        leftCol.getChildren().addAll(rankCard, sg);

        // RIGHT: skills + badges
        VBox rightCol = new VBox(16);
        rightCol.setPrefWidth(280); rightCol.setMinWidth(260);

        VBox skillCard = buildSectionCard("SKILL MATRIX");
        VBox skillInner = (VBox) skillCard.getUserData();
        skillInner.getChildren().addAll(
            buildSkillBar("Algorithms & DS",   0.85, "#0ea5e9"),
            buildSkillBar("System Design",      0.62, "#a855f7"),
            buildSkillBar("Database Mastery",   0.74, "#10b981"),
            buildSkillBar("Problem Solving",    0.91, "#f97316")
        );

        VBox achCard = buildSectionCard("ACHIEVEMENTS");
        FlowPane achFlow = new FlowPane(12, 12);
        BadgeUtils.buildAchievements(user, achFlow);
        ((VBox) achCard.getUserData()).getChildren().add(achFlow);

        rightCol.getChildren().addAll(skillCard, achCard);
        body.getChildren().addAll(leftCol, rightCol);

        // Assemble
        VBox page = new VBox(0, hero, progSection, body);
        publicProfileContainer.getChildren().add(page);
        VBox.setVgrow(page, Priority.ALWAYS);
    }

    private void updateSocialButtons(User target, Button addBtn, Button blockBtn) {
        if (currentUser == null || currentUser.getId() == target.getId()) {
            addBtn.setVisible(false); blockBtn.setVisible(false); return;
        }
        try {
            String status = serviceRelation.getRelationStatus(currentUser.getId(), target.getId());
            if ("FRIEND".equals(status)) {
                styleSocialBtn(addBtn, "✓ Friends", "#dcfce7", "#16a34a");
                addBtn.setOnAction(e -> handleRelationAction(target, "REMOVE", addBtn, blockBtn));
            } else {
                String ac = getRankAccent(target.getRankedPoints());
                styleSocialBtn(addBtn, "+  Add Friend", "#4F46E5", "#ffffff");
                addBtn.setOnAction(e -> handleRelationAction(target, "FRIEND", addBtn, blockBtn));
            }
            if ("BLOCKED".equals(status)) {
                styleSocialBtn(blockBtn, "Unblock", "#f1f5f9", "#64748b");
                blockBtn.setOnAction(e -> handleRelationAction(target, "REMOVE", addBtn, blockBtn));
            } else {
                styleSocialBtn(blockBtn, "Block", "transparent", "#ef4444");
                blockBtn.setOnAction(e -> handleRelationAction(target, "BLOCK", addBtn, blockBtn));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void handleRelationAction(User target, String action, Button addBtn, Button blockBtn) {
        try {
            if ("FRIEND".equals(action)) serviceRelation.addFriend(currentUser.getId(), target.getId());
            else if ("BLOCK".equals(action)) serviceRelation.blockUser(currentUser.getId(), target.getId());
            else serviceRelation.removeRelation(currentUser.getId(), target.getId());
            
            updateSocialButtons(target, addBtn, blockBtn);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private String getRankAccent(int rp) {
        int lvl = (rp / 1000) + 1;
        switch (Math.min(lvl, 10)) {
            case 1: return "#64748b"; case 2: return "#22c55e";
            case 3: return "#eab308"; case 4: return "#f97316";
            case 5: return "#0ea5e9"; case 6: return "#8b5cf6";
            case 7: return "#22d3ee"; case 8: return "#ec4899";
            case 9: return "#f59e0b"; default: return "#ef4444";
        }
    }

    private String getRankDescription(int rp) {
        int lvl = (rp / 1000) + 1;
        switch (Math.min(lvl, 10)) {
            case 1: return "Just getting started."; case 2: return "Reading machine language.";
            case 3: return "Low-level system explorer."; case 4: return "Deep in the OS core.";
            case 5: return "Translating logic to execution."; case 6: return "Mastering immutable states.";
            case 7: return "Adaptive runtime performance."; case 8: return "Fluent in protocols.";
            case 9: return "Designing systems from scratch."; default: return "Pinnacle of mastery.";
        }
    }

    private void styleSocialBtn(Button btn, String text, String bg, String fg) {
        btn.setText(text);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg
                + "; -fx-font-weight: 800; -fx-font-size: 12px; -fx-background-radius: 10;"
                + "-fx-border-color: " + fg + "55; -fx-border-radius: 10; -fx-border-width: 1; -fx-cursor: hand;");
    }

    private VBox buildSectionCard(String title) {
        VBox card = new VBox(12);
        card.setPadding(new javafx.geometry.Insets(18));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 14;"
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 14; -fx-border-width: 1;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        Label t = new Label(title);
        t.setStyle("-fx-font-size: 10px; -fx-font-weight: 800; -fx-text-fill: #64748b;");
        VBox inner = new VBox(10);
        card.getChildren().addAll(t, inner);
        card.setUserData(inner);
        return card;
    }

    private VBox buildStatTile(String icon, String label, String value, String color) {
        VBox tile = new VBox(5);
        tile.setPadding(new javafx.geometry.Insets(14));
        tile.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12;"
                + "-fx-border-color: " + color + "44; -fx-border-radius: 12; -fx-border-width: 1;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 1);");
        tile.setMaxWidth(Double.MAX_VALUE); GridPane.setHgrow(tile, Priority.ALWAYS);
        Label iconL = new Label(icon); iconL.setStyle("-fx-font-size: 18px;");
        Label valL  = new Label(value); valL.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: " + color + ";");
        Label lblL  = new Label(label); lblL.setStyle("-fx-font-size: 10px; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        tile.getChildren().addAll(iconL, valL, lblL);
        return tile;
    }

    private VBox buildSkillBar(String name, double prog, String color) {
        VBox row = new VBox(5);
        HBox hdr = new HBox();
        Label n = new Label(name); n.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #64748b;");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        Label p = new Label((int)(prog*100) + "%"); p.setStyle("-fx-font-size: 12px; -fx-font-weight: 800; -fx-text-fill: " + color + ";");
        hdr.getChildren().addAll(n, s, p);
        StackPane trk = new StackPane(); trk.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        trk.setPrefHeight(6); trk.setStyle("-fx-background-color: #e2e8f0; -fx-background-radius: 3;");
        Region f = new Region(); f.setPrefHeight(5);
        f.setStyle("-fx-background-color: linear-gradient(to right, " + color + "aa, " + color + "); -fx-background-radius: 3;");
        f.prefWidthProperty().bind(trk.widthProperty().multiply(prog));
        trk.getChildren().add(f); row.getChildren().addAll(hdr, trk);
        return row;
    }

    @FXML
    private void onNavFriends() {
        navigateTo(pageFriends, navFriends);
        loadFriends();
    }

    @FXML private void onNavShop()         { navigateTo(pageShop,         navShop);         }
    @FXML private void onNavOrders()       { navigateTo(pageOrders,       navOrders);       }
    @FXML private void onNavAdminProducts(){ navigateTo(pageAdminProducts, navAdminProducts); }

    private void loadFriends() {
        if (friendsContainer == null || currentUser == null) return;
        String search = (friendSearchField != null) ? friendSearchField.getText().toLowerCase() : "";
        
        try {
            List<Integer> friendIds = serviceRelation.getFriendsIds(currentUser.getId());
            VBox onlineContainer = new VBox(10);
            VBox offlineContainer = new VBox(10);
            
            for (int id : friendIds) {
                User u = serviceUser.getById(id);
                if (u == null) continue;
                
                String fullName = formatUserName(u).toLowerCase();
                if (!search.isEmpty() && !fullName.contains(search)) continue;
                
                if (u.isEstEnLigne()) {
                    onlineContainer.getChildren().add(buildFriendCard(u));
                } else {
                    offlineContainer.getChildren().add(buildFriendCard(u));
                }
            }
            
            javafx.application.Platform.runLater(() -> {
                friendsContainer.getChildren().clear();
                if (!onlineContainer.getChildren().isEmpty()) {
                    Label onlineLabel = new Label("🟢 Online - " + onlineContainer.getChildren().size());
                    onlineLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a; -fx-padding: 10 0 5 0;");
                    friendsContainer.getChildren().addAll(onlineLabel, onlineContainer);
                }
                
                if (!offlineContainer.getChildren().isEmpty()) {
                    Label offlineLabel = new Label("⚪ Offline - " + offlineContainer.getChildren().size());
                    offlineLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-padding: 10 0 5 0;");
                    friendsContainer.getChildren().addAll(offlineLabel, offlineContainer);
                }
                
                if (friendsContainer.getChildren().isEmpty()) {
                    Label empty = new Label(search.isEmpty() ? "No neural connections yet. Find players in the Leaderboard!" : "No connections match your search.");
                    empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 20;");
                    friendsContainer.getChildren().add(empty);
                }
            });
            
            loadActivityFeed(friendIds);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private HBox buildFriendCard(User u) {
        HBox card = new HBox(16);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(16, 24, 16, 24));
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 16; -fx-border-color: #e2e8f0; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 4); -fx-cursor: hand;");
        
        // Avatar
        StackPane avStack = new StackPane();
        Circle ring = new Circle(24);
        String accent = getRankAccent(u.getRankedPoints());
        ring.setStyle("-fx-fill: " + accent + "22; -fx-stroke: " + accent + "44; -fx-stroke-width: 1;");
        Circle av = new Circle(22);
        av.setStyle("-fx-fill: #f1f5f9;");
        Label initLbl = new Label("?");
        initLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        
        setAvatar(av, initLbl, u);
        avStack.getChildren().addAll(ring, av, initLbl);

        Circle statusBadge = new Circle(6);
        statusBadge.setStyle("-fx-fill: " + (u.isEstEnLigne() ? "#22c55e" : "#cbd5e1") + "; -fx-stroke: white; -fx-stroke-width: 2;");
        StackPane.setAlignment(statusBadge, javafx.geometry.Pos.BOTTOM_RIGHT);
        avStack.getChildren().add(statusBadge);

        VBox info = new VBox(2);
        Label name = new Label(formatUserName(u));
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #1e293b;");
        Label rank = new Label(getRankName(u.getRankedPoints()).toUpperCase() + " • " + u.getRankedPoints() + " RP");
        rank.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: " + accent + ";");
        info.getChildren().addAll(name, rank);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button viewBtn = new Button("View Profile");
        viewBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-font-weight: 700; -fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> showPublicProfile(u));
        
        card.getChildren().addAll(avStack, info, spacer, viewBtn);
        card.setOnMouseClicked(e -> { if (e.getClickCount() == 2) showPublicProfile(u); });
        
        card.setOnContextMenuRequested(e -> createFriendContextMenu(u).show(card, e.getScreenX(), e.getScreenY()));
        
        return card;
    }

    private ContextMenu createFriendContextMenu(User u) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("premium-context-menu");
        
        MenuItem msgItem = new MenuItem("Message");
        msgItem.setOnAction(e -> openChat(u));
        
        MenuItem profileItem = new MenuItem("View Profile");
        profileItem.setOnAction(e -> showPublicProfile(u));
        
        MenuItem removeItem = new MenuItem("Remove Connection");
        removeItem.setStyle("-fx-text-fill: #ef4444;");
        removeItem.setOnAction(e -> {
            try {
                serviceRelation.removeRelation(currentUser.getId(), u.getId());
                loadFriends();
                loadRightFriends();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        
        MenuItem blockItem = new MenuItem("Block User");
        blockItem.setOnAction(e -> {
            try {
                serviceRelation.blockUser(currentUser.getId(), u.getId());
                loadFriends();
                loadRightFriends();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        
        menu.getItems().addAll(msgItem, profileItem, new SeparatorMenuItem(), removeItem, blockItem);
        return menu;
    }

    private void loadActivityFeed(List<Integer> friendIds) {
        if (activityFeedContainer == null) return;
        activityFeedContainer.getChildren().clear();
        
        if (friendIds.isEmpty()) {
            Label empty = new Label("Add friends to see their recent achievements here.");
            empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-wrap-text: true;");
            activityFeedContainer.getChildren().add(empty);
            return;
        }

        // Mock activities (In a real app, this would come from a database table)
        try {
            for (int i = 0; i < Math.min(friendIds.size(), 5); i++) {
                User u = serviceUser.getById(friendIds.get(i));
                if (u == null) continue;
                
                VBox act = new VBox(4);
                act.setPadding(new javafx.geometry.Insets(10));
                act.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #f1f5f9; -fx-border-radius: 12;");
                
                HBox hdr = new HBox(8);
                hdr.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Circle tinyAv = new Circle(10);
                tinyAv.setStyle("-fx-fill: #e2e8f0;");
                Label n = new Label(u.getPrenom()); n.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #1e293b;");
                Label t = new Label("2h ago"); t.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
                Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
                hdr.getChildren().addAll(tinyAv, n, s, t);
                
                Label desc = new Label(getRandomActivity(u));
                desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
                
                act.getChildren().addAll(hdr, desc);
                activityFeedContainer.getChildren().add(act);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadRightFriends() {
        if (rightFriendsContainer == null || currentUser == null) return;
        
        try {
            List<Integer> friendIds = serviceRelation.getFriendsIds(currentUser.getId());
            VBox onlineContainer = new VBox(5);
            VBox offlineContainer = new VBox(5);
            
            for (int id : friendIds) {
                User u = serviceUser.getById(id);
                if (u == null) continue;
                
                if (u.isEstEnLigne()) {
                    onlineContainer.getChildren().add(buildRightFriendCard(u));
                } else {
                    offlineContainer.getChildren().add(buildRightFriendCard(u));
                }
            }
            
            javafx.application.Platform.runLater(() -> {
                rightFriendsContainer.getChildren().clear();
                if (!onlineContainer.getChildren().isEmpty()) {
                    Label onlineLabel = new Label("ONLINE");
                    onlineLabel.getStyleClass().addAll("sidebar-header-label", "online-status");
                    onlineLabel.setVisible(!rightSidebarCollapsed);
                    onlineLabel.setManaged(!rightSidebarCollapsed);
                    rightFriendsContainer.getChildren().addAll(onlineLabel, onlineContainer);
                }
                
                if (!offlineContainer.getChildren().isEmpty()) {
                    Label offlineLabel = new Label("OFFLINE");
                    offlineLabel.getStyleClass().addAll("sidebar-header-label", "offline-status");
                    offlineLabel.setVisible(!rightSidebarCollapsed);
                    offlineLabel.setManaged(!rightSidebarCollapsed);
                    rightFriendsContainer.getChildren().addAll(offlineLabel, offlineContainer);
                }
                
                if (rightFriendsContainer.getChildren().isEmpty()) {
                    Label empty = new Label("No connections found.");
                    empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-padding: 10;");
                    rightFriendsContainer.getChildren().add(empty);
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private HBox buildRightFriendCard(User u) {
        HBox card = new HBox(12);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(8));
        card.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-cursor: hand;");
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: transparent; -fx-background-radius: 8; -fx-cursor: hand;"));
        card.setOnMouseClicked(e -> openChat(u));

        // Avatar
        StackPane avStack = new StackPane();
        Circle av = new Circle(16);
        av.setStyle("-fx-fill: #e2e8f0;");
        Label initLbl = new Label("?");
        initLbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        
        setAvatar(av, initLbl, u);
        avStack.getChildren().addAll(av, initLbl);

        if (!u.isEstEnLigne()) {
            ColorAdjust desaturate = new ColorAdjust();
            desaturate.setSaturation(-0.8);
            avStack.setEffect(desaturate);
            card.setOpacity(0.7);
        }

        Circle statusBadge = new Circle(4);
        statusBadge.setStyle("-fx-fill: " + (u.isEstEnLigne() ? "#22c55e" : "#cbd5e1") + "; -fx-stroke: white; -fx-stroke-width: 1.5;");
        StackPane.setAlignment(statusBadge, javafx.geometry.Pos.BOTTOM_RIGHT);
        avStack.getChildren().add(statusBadge);

        VBox info = new VBox(2);
        info.setVisible(!rightSidebarCollapsed);
        info.setManaged(!rightSidebarCollapsed);
        Label name = new Label(formatUserName(u));
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label status = new Label(u.isEstEnLigne() ? "Online" : "Offline");
        status.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        info.getChildren().addAll(name, status);
        
        card.getChildren().addAll(avStack, info);
        card.setOnContextMenuRequested(e -> createFriendContextMenu(u).show(card, e.getScreenX(), e.getScreenY()));
        return card;
    }

    private void pollData() {
        if (currentUser == null) return;
        try {
            // Re-fetch current user from DB to get latest stats/status
            User updatedUser = serviceUser.getById(currentUser.getId());
            if (updatedUser != null) {
                currentUser = updatedUser;
            }

            // Refresh friends status
            loadRightFriends();
            if (pageFriends != null && pageFriends.isVisible()) {
                loadFriends();
            }

            List<Message> unread = serviceMessage.getUnreadMessages(currentUser.getId());
            
            javafx.application.Platform.runLater(() -> {
                if (notifBadge != null) {
                    int count = unread.size();
                    if (count > 0) {
                        notifBadge.setText(String.valueOf(count));
                        notifBadge.setVisible(true);
                        notifBadge.setManaged(true);
                        notifBtn.setStyle("-fx-text-fill: #ef4444;"); // Red bell icon
                    } else {
                        notifBadge.setVisible(false);
                        notifBadge.setManaged(false);
                        notifBtn.setStyle(""); 
                    }
                }
            });

            if (chatBox != null && chatBox.isVisible() && currentChatUser != null) {
                loadChatMessages();
            }
        } catch (Exception e) {}
    }

    @FXML
    private void onNotifClick() {
        if (currentUser == null) return;
        try {
            List<Message> unread = serviceMessage.getUnreadMessages(currentUser.getId());
            javafx.application.Platform.runLater(() -> {
                ContextMenu menu = new ContextMenu();
                if (unread.isEmpty()) {
                    MenuItem empty = new MenuItem("No new notifications");
                    empty.setDisable(true);
                    menu.getItems().add(empty);
                } else {
                    MenuItem markAll = new MenuItem("✓ Mark all as read");
                    markAll.setStyle("-fx-text-fill: #4F46E5; -fx-font-weight: 800;");
                    markAll.setOnAction(e -> {
                        try {
                            serviceMessage.markAllAsRead(currentUser.getId());
                            pollData();
                        } catch (Exception ex) { ex.printStackTrace(); }
                    });
                    menu.getItems().add(markAll);
                    menu.getItems().add(new SeparatorMenuItem());

                    for (Message m : unread) {
                        MenuItem item = new MenuItem("New message from " + m.getExpediteurNom() + "...");
                        item.setOnAction(e -> {
                            try {
                                User sender = serviceUser.getById(m.getExpediteurId());
                                if (sender != null) openChat(sender);
                            } catch(Exception ex) {}
                        });
                        menu.getItems().add(item);
                    }
                }
                menu.show(notifBtn, javafx.geometry.Side.BOTTOM, 0, 0);
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openChat(User u) {
        if (chatBox == null || chatHeader == null || chatMessages == null) return;
        currentChatUser = u;
        chatHeader.setText("Chat with " + formatUserName(u));
        loadChatMessages();
        
        chatBox.setVisible(true);
        chatBox.setManaged(true);
        if (rightSidebarCollapsed) {
            onToggleRightSidebar();
        }
    }

    private void loadChatMessages() {
        if (currentUser == null || currentChatUser == null) return;
        try {
            chatMessages.getChildren().clear();
            List<Message> msgs = serviceMessage.getMessagesEntre(currentUser.getId(), currentChatUser.getId());
            for (Message m : msgs) {
                if (m.getDestinataireId() == currentUser.getId() && !m.isEstLu()) {
                    serviceMessage.markAsRead(m.getId());
                }
                
                HBox msgRow = new HBox();
                Label msg = new Label(m.getContenu());
                msg.setWrapText(true);
                
                if (m.getExpediteurId() == currentUser.getId()) {
                    msgRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    msg.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 16 16 0 16; -fx-font-size: 12px;");
                } else {
                    msgRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    msg.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #1e293b; -fx-padding: 8 12; -fx-background-radius: 16 16 16 0; -fx-font-size: 12px;");
                }
                msgRow.getChildren().add(msg);
                chatMessages.getChildren().add(msgRow);
            }
            
            // Re-poll immediately to update bell status
            List<Message> unread = serviceMessage.getUnreadMessages(currentUser.getId());
            if (notifBadge != null) {
                int count = unread.size();
                notifBadge.setText(String.valueOf(count));
                notifBadge.setVisible(count > 0);
                notifBadge.setManaged(count > 0);
            }
            notifBtn.setStyle(unread.isEmpty() ? "" : "-fx-text-fill: #ef4444;");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onCloseChat() {
        if (chatBox != null) {
            chatBox.setVisible(false);
            chatBox.setManaged(false);
            currentChatUser = null;
        }
    }

    @FXML
    private void onSendChat() {
        if (chatInput == null || chatInput.getText().trim().isEmpty() || currentChatUser == null || currentUser == null) return;
        String text = chatInput.getText().trim();
        chatInput.clear();
        
        try {
            Message m = new Message(currentUser.getId(), currentChatUser.getId(), text);
            serviceMessage.envoyerMessage(m);
            loadChatMessages();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getRandomActivity(User u) {
        String[] activities = {
            "Reached " + getRankName(u.getRankedPoints()) + " rank!",
            "Completed a difficult quiz with 90% accuracy.",
            "Earned the 'On Fire' achievement!",
            "Just started a new course: Advanced Neural Networks.",
            "Surpassed a new personal best in daily XP!"
        };
        return activities[new java.util.Random().nextInt(activities.length)];
    }



    private void setupWindowDragging() {
        final double[] xOffset = {0};
        final double[] yOffset = {0};

        if (topbar != null) {
            topbar.setOnMousePressed(event -> {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            });
            topbar.setOnMouseDragged(event -> {
                Stage stage = (Stage) topbar.getScene().getWindow();
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
            });
        }
    }

    @FXML
    private void onCloseWindow() {
        if (currentUser != null) {
            try { serviceUser.setOffline(currentUser.getId()); } catch(Exception e){}
        }
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onMinimizeWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void onMaximizeWindow() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    private void setAvatar(Circle circle, Label initial, User u) {
        if (circle == null) return;
        if (u.getPhotoProfil() != null && !u.getPhotoProfil().isEmpty()) {
            try {
                byte[] b = Base64.getDecoder().decode(u.getPhotoProfil());
                circle.setFill(new ImagePattern(new Image(new ByteArrayInputStream(b))));
                if (initial != null) {
                    initial.setVisible(false);
                    initial.setManaged(false);
                }
            } catch (Exception e) {
                applyDefaultAvatarStyle(circle, initial, u);
            }
        } else {
            applyDefaultAvatarStyle(circle, initial, u);
        }
    }

    private void applyDefaultAvatarStyle(Circle circle, Label initial, User u) {
        circle.setFill(javafx.scene.paint.Color.web("#e2e8f0"));
        if (initial != null) {
            String firstChar = formatUserName(u).substring(0,1).toUpperCase();
            initial.setText(firstChar);
            initial.setVisible(true);
            initial.setManaged(true);
        }
    }

    private <T> void addIfNotNull(List<T> list, T item, String name) {
        if (item != null) {
            list.add(item);
        } else {
            System.err.println("[FXML] Warning: " + name + " is null. Check fx:id in FXML.");
        }
    }
}
