package controllers;

import javafx.application.Platform;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import entities.User;
import entities.UserPreferences;
import entities.Message;
import services.ServiceUser;
import services.ServiceUserPreferences;
import services.ServiceMessage;
import services.CloudinaryService;
import utils.BadgeUtils;
import com.skillora.events.controllers.ReservationController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.net.URL;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class MainController implements Initializable {

    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @FXML private StackPane rootPane;
    @FXML private BorderPane mainShell;
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
    @FXML private Button navTodo;
    @FXML private Button navCalendar;
    @FXML private Button navObjectives;
    @FXML private Button navProductivity;
    @FXML private Button navShop;
    @FXML private Button navEvent;
    @FXML private Button navReservation;
    @FXML private Button navCommunity;
    @FXML private Button navProfile;
    @FXML private Button navAdmin;
    @FXML private Button navAdminCourses;
    @FXML private Button navAdminShop;
    @FXML private Button navAdminEvents;
    @FXML private Button navAdminReservations;

    @FXML private Button navQuizChoose;
    @FXML private Button navQuizManage;
    @FXML private Button navQuizQuestions;
    @FXML private Button navQuizAnswers;
    @FXML private Button navQuizHistory;

    @FXML private Button navSettings;
    @FXML private Button navLogout;

    @FXML private Label  lblHome;
    @FXML private Label  lblCourses;
    @FXML private Label  lblQuizzes;

    @FXML private Label  lblLeaderboard;
    @FXML private Label  lblTodo;
    @FXML private Label  lblCalendar;
    @FXML private Label  lblObjectives;
    @FXML private Label  lblProductivity;
    @FXML private Label  lblShop;
    @FXML private Label  lblEvent;
    @FXML private Label  lblReservation;
    @FXML private Label  lblCommunity;
    @FXML private Label  lblProfile;
    @FXML private Label  lblAdmin;
    @FXML private Label  lblAdminCourses;
    @FXML private Label  lblAdminShop;
    @FXML private Label  lblAdminEvents;
    @FXML private Label  lblAdminReservations;
  @FXML private Label  lblQuizChoose;
    @FXML private Label  lblQuizManage;
    @FXML private Label  lblQuizQuestions;
    @FXML private Label  lblQuizAnswers;
    @FXML private Label  lblQuizHistory;

    @FXML private Label  lblSettings;
    @FXML private Label  lblLogout;

    @FXML private ScrollPane contentScroll;
    @FXML private VBox   pageHome;
    @FXML private VBox pageCourses;
    @FXML private VBox pageQuizzes;


    @FXML private VBox quizSubmenu;
    @FXML private StackPane quizContentPane;

    @FXML private VBox pageLeaderboard;
    @FXML private VBox   leaderboardContainer;
    @FXML private ComboBox<String> sortMetricCombo;
    @FXML private ComboBox<String> countryFilterCombo;
    @FXML private VBox   pageTodo;
    @FXML private VBox   pageCalendar;
    @FXML private VBox   pageObjectives;
    @FXML private VBox   pageProductivity;
    @FXML private VBox   pageShop;
    @FXML private VBox   pageEvent;
    @FXML private VBox   pageReservation;
    @FXML private VBox   pageCommunity;
    private boolean todoPageLoaded;
    private boolean calendarPageLoaded;
    private boolean objectivesPageLoaded;
    private boolean productivityPageLoaded;
    private boolean shopPageLoaded;
    private boolean eventsPageLoaded;
    private boolean reservationsPageLoaded;

    @FXML private VBox   pageProfile;
    @FXML private VBox   pageAdmin;
    @FXML private VBox   pageSettings;

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


    @FXML private TextField profileFirstNameField;
    @FXML private TextField profileLastNameField;
    @FXML private TextField profileUsernameField;
    @FXML private TextField profileEmailField;
    @FXML private ComboBox<String> profileCountryCombo;

    @FXML private VBox profileFriendsContainer;
    @FXML private TextField adminSearchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private VBox   userTableBody;
    @FXML private CheckBox darkModeToggle;

    private boolean sidebarCollapsed = false;
    private boolean dropdownOpen     = false;

    private boolean quizMenuExpanded = false;

    private UserPreferences prefs;
    private User currentUser;
    private final services.ServiceRelation serviceRelation = new services.ServiceRelation();
    
    @FXML private VBox pagePublicProfile;
    @FXML private VBox publicProfileContainer;

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
    private List<User> allUsers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        allNavBtns = new java.util.ArrayList<>();
        addIfNotNull(allNavBtns, navHome, "navHome");
        addIfNotNull(allNavBtns, navCourses, "navCourses");
        addIfNotNull(allNavBtns, navQuizzes, "navQuizzes");

        addIfNotNull(allNavBtns, navLeaderboard, "navLeaderboard");
        addIfNotNull(allNavBtns, navTodo, "navTodo");
        addIfNotNull(allNavBtns, navCalendar, "navCalendar");
        addIfNotNull(allNavBtns, navObjectives, "navObjectives");
        addIfNotNull(allNavBtns, navProductivity, "navProductivity");
        addIfNotNull(allNavBtns, navShop, "navShop");
        addIfNotNull(allNavBtns, navEvent, "navEvent");
        addIfNotNull(allNavBtns, navReservation, "navReservation");
        addIfNotNull(allNavBtns, navCommunity, "navCommunity");
        addIfNotNull(allNavBtns, navAdmin, "navAdmin");
        addIfNotNull(allNavBtns, navAdminCourses, "navAdminCourses");
        addIfNotNull(allNavBtns, navAdminShop, "navAdminShop");
        addIfNotNull(allNavBtns, navAdminEvents, "navAdminEvents");
        addIfNotNull(allNavBtns, navAdminReservations, "navAdminReservations");

        addIfNotNull(allNavBtns, navQuizChoose, "navQuizChoose");
        addIfNotNull(allNavBtns, navQuizManage, "navQuizManage");
        addIfNotNull(allNavBtns, navQuizQuestions, "navQuizQuestions");
        addIfNotNull(allNavBtns, navQuizAnswers, "navQuizAnswers");
        addIfNotNull(allNavBtns, navQuizHistory, "navQuizHistory");
        addIfNotNull(allNavBtns, navLeaderboard, "navLeaderboard");
        addIfNotNull(allNavBtns, navAdmin, "navAdmin");

        addIfNotNull(allNavBtns, navSettings, "navSettings");
        addIfNotNull(allNavBtns, navLogout, "navLogout");

        allNavLabels = new java.util.ArrayList<>();
        addIfNotNull(allNavLabels, lblHome, "lblHome");
        addIfNotNull(allNavLabels, lblCourses, "lblCourses");
        addIfNotNull(allNavLabels, lblQuizzes, "lblQuizzes");

        addIfNotNull(allNavLabels, lblLeaderboard, "lblLeaderboard");
        addIfNotNull(allNavLabels, lblTodo, "lblTodo");
        addIfNotNull(allNavLabels, lblCalendar, "lblCalendar");
        addIfNotNull(allNavLabels, lblObjectives, "lblObjectives");
        addIfNotNull(allNavLabels, lblProductivity, "lblProductivity");
        addIfNotNull(allNavLabels, lblShop, "lblShop");
        addIfNotNull(allNavLabels, lblEvent, "lblEvent");
        addIfNotNull(allNavLabels, lblReservation, "lblReservation");
        addIfNotNull(allNavLabels, lblCommunity, "lblCommunity");
        addIfNotNull(allNavLabels, lblAdmin, "lblAdmin");
        addIfNotNull(allNavLabels, lblAdminCourses, "lblAdminCourses");
        addIfNotNull(allNavLabels, lblAdminShop, "lblAdminShop");
        addIfNotNull(allNavLabels, lblAdminEvents, "lblAdminEvents");
        addIfNotNull(allNavLabels, lblAdminReservations, "lblAdminReservations");

        addIfNotNull(allNavLabels, lblQuizChoose, "lblQuizChoose");
        addIfNotNull(allNavLabels, lblQuizManage, "lblQuizManage");
        addIfNotNull(allNavLabels, lblQuizQuestions, "lblQuizQuestions");
        addIfNotNull(allNavLabels, lblQuizAnswers, "lblQuizAnswers");
        addIfNotNull(allNavLabels, lblQuizHistory, "lblQuizHistory");
        addIfNotNull(allNavLabels, lblLeaderboard, "lblLeaderboard");
        addIfNotNull(allNavLabels, lblAdmin, "lblAdmin");

        addIfNotNull(allNavLabels, lblSettings, "lblSettings");
        addIfNotNull(allNavLabels, lblLogout, "lblLogout");
        allPages = new java.util.ArrayList<>();
        addIfNotNull(allPages, pageHome, "pageHome");
        addIfNotNull(allPages, pageCourses, "pageCourses");
        addIfNotNull(allPages, pageQuizzes, "pageQuizzes");
        addIfNotNull(allPages, pageLeaderboard, "pageLeaderboard");
        addIfNotNull(allPages, pageTodo, "pageTodo");
        addIfNotNull(allPages, pageCalendar, "pageCalendar");
        addIfNotNull(allPages, pageObjectives, "pageObjectives");
        addIfNotNull(allPages, pageProductivity, "pageProductivity");

        addIfNotNull(allPages, pageShop, "pageShop");
        addIfNotNull(allPages, pageEvent, "pageEvent");
        addIfNotNull(allPages, pageReservation, "pageReservation");
        addIfNotNull(allPages, pageCommunity, "pageCommunity");

        addIfNotNull(allPages, pageProfile, "pageProfile");
        addIfNotNull(allPages, pageAdmin, "pageAdmin");
        addIfNotNull(allPages, pageSettings, "pageSettings");
        addIfNotNull(allPages, pagePublicProfile, "pagePublicProfile");

        setupLeaderboardFilters();
        setGreeting();
        populateFilters();
        loadUserTable();
        setupSearch();

        loadProfileCountries();

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
        boolean isInstructor = isInstructorRole(p.getUserRole());
        updateRoleAwareNavigation(isAdmin, isInstructor);

        SessionManager.setRole(isAdmin
                ? SessionManager.Role.ADMIN
                : isInstructor ? SessionManager.Role.INSTRUCTOR : SessionManager.Role.USER);
        updateQuizSubmenuForRole();

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
        setupRoundedShellClip();

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

    private void updateRoleAwareNavigation(boolean isAdmin, boolean isInstructor) {
        if (topbarLogoText != null) {
            topbarLogoText.setText(isAdmin
                    ? "Admin Dashboard"
                    : "Player Dashboard");
        }

        if (lblShop != null) {
            lblShop.setText("Shop");
        }

        setVisibleManaged(navShop, !isAdmin);
        setVisibleManaged(navAdminShop, isAdmin);
        setVisibleManaged(navAdminEvents, isAdmin);
        setVisibleManaged(navAdminReservations, isAdmin);
    }

    private boolean isInstructorRole(String role) {
        if (role == null) {
            return false;
        }
        String normalizedRole = role.trim();
        return "INSTRUCTEUR".equalsIgnoreCase(normalizedRole)
                || "INSTRUCTOR".equalsIgnoreCase(normalizedRole)
                || "ENSEIGNANT".equalsIgnoreCase(normalizedRole)
                || "TEACHER".equalsIgnoreCase(normalizedRole);
    }

    private void setVisibleManaged(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
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

    private void setupRoundedShellClip() {
        if (mainShell == null) return;

        Rectangle clip = new Rectangle();
        clip.setArcWidth(48);
        clip.setArcHeight(48);
        mainShell.setClip(clip);

        mainShell.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            clip.setWidth(newBounds.getWidth());
            clip.setHeight(newBounds.getHeight());
        });
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
            if (currentUser != null) {
                utils.Session.setUser(currentUser);
                utils.SessionManager.setCurrentUser(currentUser);
            }
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

                updateQuizSubmenuVisibility();

            } else {
                sidebar.setPadding(new javafx.geometry.Insets(24, 16, 24, 16));
                if (sidebarHeader != null) sidebarHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                for (Button b : allNavBtns) b.setAlignment(javafx.geometry.Pos.BASELINE_LEFT);

                updateQuizSubmenuVisibility();

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
    @FXML private void onNavCourses()  { 
        loadViewIntoPage("/ui/cours-list.fxml", pageCourses);
        navigateTo(pageCourses,  navCourses);  
    }

    public <T> T loadViewIntoPage(String fxmlPath, VBox page) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            page.getChildren().clear();
            page.getChildren().add(view);
            VBox.setVgrow(view, Priority.ALWAYS);
            return loader.getController();
        } catch (Exception e) {
            System.err.println("Error loading view " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @FXML private void onNavQuizzes()  {
        navigateTo(pageQuizzes,  navQuizzes);
        if (SessionManager.isQuizManager()) {
            quizMenuExpanded = !quizMenuExpanded;
            updateQuizSubmenuVisibility();
            loadDefaultQuizTaskForRole();
        } else {
            quizMenuExpanded = false;
            updateQuizSubmenuVisibility();
            openQuizTask("UserQuizSelection.fxml", navQuizzes, false);
        }
    }

    @FXML private void onNavLeaderboard()  { 
        navigateTo(pageLeaderboard,  navLeaderboard);
        loadLeaderboard();
    }
    @FXML private void onNavTodo()        { navigateTo(pageTodo, navTodo); ensureTodoPageLoaded(); }
    @FXML private void onNavCalendar()    { navigateTo(pageCalendar, navCalendar); ensureCalendarPageLoaded(); }
    @FXML private void onNavObjectives()  { navigateTo(pageObjectives, navObjectives); ensureObjectivesPageLoaded(); }
    @FXML private void onNavProductivity(){ navigateTo(pageProductivity, navProductivity); ensureProductivityPageLoaded(); }
    @FXML private void onNavShop()      { navigateTo(pageShop,      navShop); ensureShopPageLoaded();      }
    @FXML private void onNavEvent()     { navigateTo(pageEvent,     navEvent); ensureEventsPageLoaded();     }
    @FXML private void onNavReservation() {
        reservationsPageLoaded = false;
        navigateTo(pageReservation, navReservation);
        ensureReservationsPageLoaded();
    }
    @FXML private void onNavCommunity() { navigateTo(pageCommunity, navCommunity); }
    @FXML private void onGoProfile()   { 
        navigateTo(pageProfile,  navProfile); 
        closeDropdown(); 
        loadProfileFriends(); 
        populateProfileFields();
    }
    @FXML private void onNavAdmin()    { navigateTo(pageAdmin,    navAdmin);    }
    @FXML private void onNavAdminCourses() { 
        loadViewIntoPage("/ui/cours-list.fxml", pageCourses);
        navigateTo(pageCourses, navAdminCourses); 
    }
    @FXML private void onNavAdminShop()    { navigateTo(pageShop,    navAdminShop); ensureShopPageLoaded();    }
    @FXML private void onNavAdminEvents()  { navigateTo(pageEvent,   navAdminEvents); ensureEventsPageLoaded();  }
    @FXML private void onNavAdminReservations() { navigateTo(pageReservation, navAdminReservations); ensureReservationsPageLoaded(); }
    @FXML private void onNavSettings() { navigateTo(pageSettings, navSettings); closeDropdown(); }

    private void ensureTodoPageLoaded() {
        if (!todoPageLoaded) {
            todoPageLoaded = loadEmbeddedPage(pageTodo, "/fxml/todo/TodoView.fxml", "todo");
        }
    }

    private void ensureCalendarPageLoaded() {
        if (!calendarPageLoaded) {
            calendarPageLoaded = loadEmbeddedPage(pageCalendar, "/fxml/todo/CalendarView.fxml", "calendar");
        }
    }

    private void ensureObjectivesPageLoaded() {
        if (!objectivesPageLoaded) {
            objectivesPageLoaded = loadEmbeddedPage(pageObjectives, "/fxml/todo/ObjectivesView.fxml", "objectives");
        }
    }

    private void ensureProductivityPageLoaded() {
        if (!productivityPageLoaded) {
            productivityPageLoaded = loadEmbeddedPage(pageProductivity, "/fxml/todo/ProductivityDashboard.fxml", "productivity");
        }
    }

    private void ensureShopPageLoaded() {
        syncShopSession();
        if (!shopPageLoaded) {
            shopPageLoaded = loadEmbeddedPage(pageShop, "/fxml/shop/ShopShell.fxml", "shop");
        }
    }

    private void syncShopSession() {
        com.skillora.shop.entities.User shopUser = new com.skillora.shop.entities.User();
        String firstName = currentUser != null ? currentUser.getPrenom() : null;
        String lastName = currentUser != null ? currentUser.getNom() : null;
        String username = currentUser != null ? currentUser.getNomUtilisateur() : null;
        String email = currentUser != null ? currentUser.getEmail() : null;
        String role = currentUser != null ? currentUser.getRole() : null;

        if ((firstName == null || firstName.isBlank()) && prefs != null && prefs.getUserName() != null) {
            firstName = prefs.getUserName();
        }
        if ((email == null || email.isBlank()) && prefs != null) {
            email = prefs.getUserEmail();
        }
        if ((role == null || role.isBlank()) && prefs != null) {
            role = prefs.getUserRole();
        }

        shopUser.setIdUtilisateur(currentUser != null ? currentUser.getIdUtilisateur() : 0);
        shopUser.setPrenom(firstName != null && !firstName.isBlank() ? firstName : "Skillora");
        shopUser.setNom(lastName != null ? lastName : "");
        shopUser.setNomUtilisateur(username != null ? username : shopUser.getPrenom());
        shopUser.setEmail(email);
        shopUser.setRole(toShopRole(role));
        com.skillora.shop.Session.setUser(shopUser);
    }

    private com.skillora.shop.entities.Role toShopRole(String role) {
        if (role == null) {
            return com.skillora.shop.entities.Role.ETUDIANT;
        }
        String normalizedRole = role.trim().toUpperCase();
        if ("INSTRUCTOR".equals(normalizedRole) || "ENSEIGNANT".equals(normalizedRole) || "TEACHER".equals(normalizedRole)) {
            return com.skillora.shop.entities.Role.INSTRUCTEUR;
        }
        try {
            return com.skillora.shop.entities.Role.valueOf(normalizedRole);
        } catch (IllegalArgumentException e) {
            return com.skillora.shop.entities.Role.ETUDIANT;
        }
    }

    private void ensureEventsPageLoaded() {
        if (!eventsPageLoaded) {
            if (usesStudentEventFlow()) {
                eventsPageLoaded = loadEmbeddedPage(pageEvent, "/com/skillora/views/ReservationInterface.fxml", "events",
                        controller -> {
                            if (controller instanceof ReservationController) {
                                ((ReservationController) controller).showAvailableEventsOnly();
                            }
                        });
            } else {
                eventsPageLoaded = loadEmbeddedPage(pageEvent, "/com/skillora/views/EvenementInterface.fxml", "events");
            }
        }
    }

    private void ensureReservationsPageLoaded() {
        if (!reservationsPageLoaded) {
            reservationsPageLoaded = loadEmbeddedPage(pageReservation, "/com/skillora/views/ReservationInterface.fxml", "reservations",
                    controller -> {
                        if (controller instanceof ReservationController && usesStudentEventFlow()) {
                            ((ReservationController) controller).showMyReservationsOnly();
                        }
                    });
        }
    }

    private boolean usesStudentEventFlow() {
        String role = currentUser != null ? currentUser.getRole() : null;
        if ((role == null || role.isBlank()) && prefs != null) {
            role = prefs.getUserRole();
        }
        return !"ADMIN".equalsIgnoreCase(role);
    }

    private boolean loadEmbeddedPage(Pane container, String resourcePath, String label) {
        return loadEmbeddedPage(container, resourcePath, label, null);
    }

    private boolean loadEmbeddedPage(Pane container, String resourcePath, String label, Consumer<Object> controllerConfigurer) {
        if (container == null) {
            return false;
        }

        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                throw new IllegalStateException("missing resource " + resourcePath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            if (controllerConfigurer != null) {
                controllerConfigurer.accept(loader.getController());
            }
            container.getChildren().setAll(view);

            if (view instanceof Region) {
                Region region = (Region) view;
                region.setMaxWidth(Double.MAX_VALUE);
                region.setMaxHeight(Double.MAX_VALUE);
                region.prefWidthProperty().bind(container.widthProperty());
            }

            VBox.setVgrow(view, Priority.ALWAYS);
            return true;
        } catch (Exception e) {
            container.getChildren().setAll(new Label("Could not load " + label + ": " + e.getMessage()));
            System.err.println("[FXML] Cannot load " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void onSaveProfile() {
        if (currentUser == null) return;
        
        currentUser.setPrenom(profileFirstNameField.getText().trim());
        currentUser.setNom(profileLastNameField.getText().trim());
        currentUser.setNomUtilisateur(profileUsernameField.getText().trim());
        
        String fullCountry = profileCountryCombo.getValue();
        if (fullCountry != null && fullCountry.contains(" ")) {
            currentUser.setPays(fullCountry.substring(0, fullCountry.lastIndexOf(" ")).trim());
        } else {
            currentUser.setPays(fullCountry);
        }
        
        try {
            serviceUser.update(currentUser);
            utils.Session.setUser(currentUser);
            
            // Update other UI parts
            if (prefs != null) {
                prefs.setUserName(formatUserName(currentUser));
                applyPreferences(prefs);
            }
            
            showStyledAlert(Alert.AlertType.INFORMATION, "Profile Updated", "Your profile has been updated successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Update Failed", "Could not update profile: " + e.getMessage());
        }
    }
    
    @FXML
    private void onResetProfile() {
        populateProfileFields();
    }
    
    private void populateProfileFields() {
        if (currentUser == null) return;
        
        profileFirstNameField.setText(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
        profileLastNameField.setText(currentUser.getNom() != null ? currentUser.getNom() : "");
        profileUsernameField.setText(currentUser.getNomUtilisateur() != null ? currentUser.getNomUtilisateur() : "");
        profileEmailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        
        if (profileCountryCombo.getItems().isEmpty()) {
            loadProfileCountries();
        } else {
            String userCountry = currentUser.getPays();
            if (userCountry != null) {
                String match = profileCountryCombo.getItems().stream()
                        .filter(c -> c.startsWith(userCountry))
                        .findFirst()
                        .orElse(userCountry);
                profileCountryCombo.setValue(match);
            }
        }
    }
    
    private void loadProfileCountries() {
        if (profileCountryCombo == null) return;
        utils.CountryService.getInstance().getAllCountryNames().thenAccept(countries -> {
            javafx.application.Platform.runLater(() -> {
                profileCountryCombo.getItems().setAll(countries);
                if (currentUser != null && currentUser.getPays() != null) {
                    String userCountry = currentUser.getPays();
                    String match = countries.stream()
                            .filter(c -> c.startsWith(userCountry))
                            .findFirst()
                            .orElse(userCountry);
                    profileCountryCombo.setValue(match);
                }
            });
        });
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


    @FXML private void onQuizChoose()    { openQuizTask("UserQuizSelection.fxml", navQuizChoose, false); }
    @FXML private void onQuizManage()    { openQuizTask("QuizManagement.fxml", navQuizManage, true); }
    @FXML private void onQuizQuestions() { openQuizTask("QuestionsManagement.fxml", navQuizQuestions, true); }
    @FXML private void onQuizAnswers()   { openQuizTask("AnswersManagement.fxml", navQuizAnswers, true); }
    @FXML private void onQuizHistory()   { openQuizTask("QuizHistory.fxml", navQuizHistory, true); }

    private void loadDefaultQuizTaskForRole() {
        if (quizContentPane == null) return;
        if (quizContentPane.getChildren().isEmpty()) {
            if (SessionManager.isQuizManager()) {
                openQuizTask("QuizManagement.fxml", navQuizManage, true);
            } else {
                openQuizTask("UserQuizSelection.fxml", navQuizChoose, false);
            }
        }
    }

    private void openQuizTask(String fxml, Button activeQuizButton, boolean requireAdmin) {
        if (requireAdmin && !SessionManager.isQuizManager()) {
            openQuizTask("UserQuizSelection.fxml", navQuizChoose, false);
            return;
        }
        if (quizContentPane == null) return;

        if (AppNavigator.isQuizInProgress() && !"PasserQuiz.fxml".equals(fxml)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Quiz en cours");
            alert.setHeaderText(null);
            alert.setContentText("Vous ne pouvez pas quitter le quiz pendant une tentative. Terminez le quiz avant de changer d'ecran.");
            alert.showAndWait();
            return;
        }

        try {
            AppNavigator.setContentPane(quizContentPane);
            String quizStylesheet = getClass().getResource("/style.css").toExternalForm();
            if (!quizContentPane.getStylesheets().contains(quizStylesheet)) {
                quizContentPane.getStylesheets().add(quizStylesheet);
            }
            Parent quizRoot = FXMLLoader.load(getClass().getResource("/" + fxml));
            quizContentPane.getChildren().setAll(quizRoot);
            activateQuizSubButton(activeQuizButton);
        } catch (Exception e) {
            System.err.println("[Quiz] Cannot load MOUAYED view " + fxml + ": " + e.getMessage());
            Label errorLabel = new Label("Impossible de charger Gestion des Quiz.");
            errorLabel.getStyleClass().add("page-greeting");
            quizContentPane.getChildren().setAll(errorLabel);
        }
    }

    private void activateQuizSubButton(Button activeQuizButton) {
        for (Button btn : java.util.Arrays.asList(navQuizChoose, navQuizManage, navQuizQuestions, navQuizAnswers, navQuizHistory)) {
            if (btn != null) btn.getStyleClass().remove("nav-active");
        }
        if (activeQuizButton != null && activeQuizButton != navQuizzes && !activeQuizButton.getStyleClass().contains("nav-active")) {
            activeQuizButton.getStyleClass().add("nav-active");
        }
    }

    private void updateQuizSubmenuForRole() {
        boolean isQuizManager = SessionManager.isQuizManager();
        if (lblQuizzes != null) {
            lblQuizzes.setText(isQuizManager ? "Gestion de Quiz" : "Passer un quiz");
        }
        if (navQuizChoose != null) {
            navQuizChoose.setVisible(false);
            navQuizChoose.setManaged(false);
        }
        for (Button btn : java.util.Arrays.asList(navQuizManage, navQuizQuestions, navQuizAnswers, navQuizHistory)) {
            if (btn != null) {
                btn.setVisible(isQuizManager);
                btn.setManaged(isQuizManager);
            }
        }
        if (!isQuizManager) {
            quizMenuExpanded = false;
        }
        activateQuizSubButton(null);
        updateQuizSubmenuVisibility();
    }

    private void updateQuizSubmenuVisibility() {
        if (quizSubmenu == null) return;
        boolean show = SessionManager.isQuizManager() && quizMenuExpanded && !sidebarCollapsed;
        quizSubmenu.setVisible(show);
        quizSubmenu.setManaged(show);
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


        if (targetPage != pageQuizzes) {
            quizMenuExpanded = false;
            updateQuizSubmenuVisibility();
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
        loadProfileFriends();

        populateProfileFields();


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
            
            // Upload to Cloudinary instead of Base64
            String cloudinaryUrl = CloudinaryService.getInstance().uploadImage(imageBytes, "profile_pictures");

            serviceUser.updateProfilePhotoByEmail(prefs.getUserEmail(), cloudinaryUrl);

            if (currentUser == null) {
                currentUser = new User();
                currentUser.setEmail(prefs.getUserEmail());
            }
            
            currentUser.setPhotoProfil(cloudinaryUrl);
            applyCurrentAvatar();
            showStyledAlert(Alert.AlertType.INFORMATION, "Success", "Profile picture updated and uploaded to cloud.");
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
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Edit User");
        dialogStage.initOwner(topbar.getScene().getWindow());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setResizable(false);

        VBox mainVBox = new VBox(20);
        mainVBox.setPadding(new javafx.geometry.Insets(30));
        mainVBox.setStyle("-fx-background-color: #FFFFFF;");

        Label titleLabel = new Label("Edit User: " + user.getEmail());
        titleLabel.setStyle("-fx-font-family: Georgia; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");

        TextField firstNameField = new TextField(user.getPrenom() != null ? user.getPrenom() : "");
        firstNameField.getStyleClass().add("dialog-text-field");
        firstNameField.setPromptText("First Name");
        
        TextField lastNameField = new TextField(user.getNom() != null ? user.getNom() : "");
        lastNameField.getStyleClass().add("dialog-text-field");
        lastNameField.setPromptText("Last Name");
        
        TextField usernameField = new TextField(user.getNomUtilisateur() != null ? user.getNomUtilisateur() : "");
        usernameField.getStyleClass().add("dialog-text-field");
        usernameField.setPromptText("Username");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getStyleClass().add("dialog-combo-box");
        roleCombo.getItems().addAll("ETUDIANT", "ADMIN");
        roleCombo.setValue(user.getRole());
        roleCombo.setMaxWidth(Double.MAX_VALUE);

        CheckBox activeCheck = new CheckBox("Active Account");
        activeCheck.setSelected(user.isEstActif());
        activeCheck.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("dialog-cancel-btn");
        cancelBtn.setOnAction(e -> dialogStage.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.getStyleClass().add("dialog-ok-btn");
        saveBtn.setOnAction(e -> {
            user.setPrenom(firstNameField.getText().trim());
            user.setNom(lastNameField.getText().trim());
            user.setNomUtilisateur(usernameField.getText().trim());
            user.setRole(roleCombo.getValue());
            user.setEstActif(activeCheck.isSelected());

            try {
                serviceUser.update(user);
                loadUserTable();
                dialogStage.close();
                showStyledAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                showStyledAlert(Alert.AlertType.ERROR, "Error", "Failed to update user: " + ex.getMessage());
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        mainVBox.getChildren().addAll(titleLabel, firstNameField, lastNameField, usernameField, roleCombo, activeCheck, buttonBox);

        Scene scene = new Scene(mainVBox);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.show();
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
        

        // Populate countries with flags
        utils.CountryService.getInstance().getAllCountryNames().thenAccept(countries -> {
            javafx.application.Platform.runLater(() -> {
                countryFilterCombo.getItems().addAll(countries);
            });
        });

        // Populate countries from users later

    }

    private void loadLeaderboard() {
        if (leaderboardContainer == null) return;
        try {
            List<User> users = serviceUser.getAll();

          
                // Since selectedCountry now has a flag (e.g. "Morocco 🇲🇦"), 
                // we check if the user's country is the prefix

            
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

        hero.setStyle("-fx-background-color: linear-gradient(to bottom right, #2563eb, #3b82f6);");
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

        initL.setStyle("-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: #2563eb;");

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

        styleSocialBtn(addBtn, "+  Add Friend", "#ffffff", "#2563eb");

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
        sg.add(buildStatTile("🎓", "Certificates", String.valueOf(user.getCertificatesCount()), "#10b981"), 0, 1);

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

                styleSocialBtn(addBtn, "+  Add Friend", "#2563eb", "#ffffff");

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
                loadRightFriends();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        
        MenuItem blockItem = new MenuItem("Block User");
        blockItem.setOnAction(e -> {
            try {
                serviceRelation.blockUser(currentUser.getId(), u.getId());
                loadRightFriends();
            } catch (Exception ex) { ex.printStackTrace(); }
        });
        
        menu.getItems().addAll(msgItem, profileItem, new SeparatorMenuItem(), removeItem, blockItem);
        return menu;
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

    private void loadProfileFriends() {
        if (profileFriendsContainer == null || currentUser == null) return;
        
        try {
            List<Integer> friendIds = serviceRelation.getFriendsIds(currentUser.getId());
            profileFriendsContainer.getChildren().clear();
            
            if (friendIds.isEmpty()) {
                Label empty = new Label("No connections yet. Find players in the Leaderboard!");
                empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px; -fx-padding: 10;");
                profileFriendsContainer.getChildren().add(empty);
                return;
            }
            
            for (int id : friendIds) {
                User u = serviceUser.getById(id);
                if (u == null) continue;
                profileFriendsContainer.getChildren().add(buildFriendProfileCard(u));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private HBox buildFriendProfileCard(User u) {
        HBox card = new HBox(16);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(12, 16, 12, 16));
        card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-cursor: hand;");
        
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-cursor: hand;"));
        card.setOnMouseClicked(e -> showPublicProfile(u));

        StackPane avStack = new StackPane();
        Circle av = new Circle(20);
        av.setStyle("-fx-fill: #e2e8f0;");
        Label initLbl = new Label("?");
        initLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        setAvatar(av, initLbl, u);
        avStack.getChildren().addAll(av, initLbl);

        Circle statusBadge = new Circle(5);
        statusBadge.setStyle("-fx-fill: " + (u.isEstEnLigne() ? "#22c55e" : "#cbd5e1") + "; -fx-stroke: white; -fx-stroke-width: 2;");
        StackPane.setAlignment(statusBadge, javafx.geometry.Pos.BOTTOM_RIGHT);
        avStack.getChildren().add(statusBadge);

        VBox info = new VBox(2);
        Label name = new Label(formatUserName(u));
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #1e293b;");
        Label rank = new Label(getRankName(u.getRankedPoints()).toUpperCase());
        rank.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #94a3b8;");
        info.getChildren().addAll(name, rank);

        card.getChildren().addAll(avStack, info);
        return card;
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
        String photo = u.getPhotoProfil();
        
        if (photo != null && !photo.isEmpty()) {
            try {
                if (photo.startsWith("http")) {
                    // It's a Cloudinary URL
                    Image img = new Image(photo, true); // background loading
                    
                    img.progressProperty().addListener((obs, old, progress) -> {
                        if (progress.doubleValue() == 1.0 && !img.isError()) {
                            Platform.runLater(() -> {
                                circle.setFill(new ImagePattern(img));
                                if (initial != null) {
                                    initial.setVisible(false);
                                    initial.setManaged(false);
                                }
                            });
                        }
                    });
                    
                    img.errorProperty().addListener((obs, old, hasError) -> {
                        if (hasError) {
                            Platform.runLater(() -> applyDefaultAvatarStyle(circle, initial, u));
                        }
                    });
                } else {
                    // It's Base64
                    byte[] b = Base64.getDecoder().decode(photo);
                    Image img = new Image(new ByteArrayInputStream(b));
                    circle.setFill(new ImagePattern(img));
                    if (initial != null) {
                        initial.setVisible(false);
                        initial.setManaged(false);
                    }
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
