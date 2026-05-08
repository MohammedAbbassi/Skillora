package utils;

import entities.User;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.util.Duration;

public class BadgeUtils {

    public static void buildAchievements(User user, FlowPane pane) {
        pane.getChildren().clear();
        
        // Category 1: Onboarding (Common)
        pane.getChildren().add(buildBadgeCard("First Boot", "Welcome to the system.", "common", "1.png"));
        pane.getChildren().add(buildBadgeCard("Hello World", "Your first step into a larger world.", "common", "2.png"));
        if (user.getXpPoints() > 500) pane.getChildren().add(buildBadgeCard("Explorer", "You've seen the core modules.", "common", "3.png"));
        pane.getChildren().add(buildBadgeCard("Connected", "Synchronized across networks.", "common", "4.png"));
        
        // Category 2: Consistency (Progressive)
        if (user.getStreakDays() >= 3)  pane.getChildren().add(buildBadgeCard("Warm Start", "3-day streak initialized.", "common", "5.png"));
        if (user.getStreakDays() >= 7)  pane.getChildren().add(buildBadgeCard("On Fire", "7-day streak! You're glowing.", "common", "6.png"));
        if (user.getStreakDays() >= 30) pane.getChildren().add(buildBadgeCard("Unstoppable", "30 days of pure focus.", "rare", "7.png"));
        if (user.getStreakDays() >= 7)  pane.getChildren().add(buildBadgeCard("No Days Off", "Activity daily for a week.", "common", "8.png"));
        
        // Category 3: Skill (Rare)
        if (user.getXpPoints() >= 1000) pane.getChildren().add(buildBadgeCard("Specialist", "Deep mastery in one field.", "rare", "9.png"));
        if (user.getXpPoints() >= 3000) pane.getChildren().add(buildBadgeCard("Multi-Threaded", "Parallel learning active.", "rare", "10.png"));
        
        // Category 5: Projects (Epic)
        if (user.getQuizzesDone() >= 5) pane.getChildren().add(buildBadgeCard("Builder", "First project constructed.", "epic", "11.png"));
        if (user.getCertificatesCount() >= 1) pane.getChildren().add(buildBadgeCard("Deployer", "Live and operational.", "epic", "12.png"));
        if (user.getXpPoints() >= 10000) pane.getChildren().add(buildBadgeCard("Engineer", "Intricate system construction.", "epic", null));
        if (user.getQuizzesDone() >= 20) pane.getChildren().add(buildBadgeCard("System Designer", "Layered architecture mastery.", "epic", null));
        
        // Rank-based (Legendary)
        if (user.getRankedPoints() >= 2000) pane.getChildren().add(buildBadgeCard("Dynamic Mastery", "Unstable red plasma core.", "legendary", null));
        if (user.getRankedPoints() >= 4000) pane.getChildren().add(buildBadgeCard("Protocol Authority", "Intense red lightning node.", "legendary", null));
        if (user.getRankedPoints() >= 7000) pane.getChildren().add(buildBadgeCard("Architect Mind", "Reality-distorting black pyramid.", "legendary", null));
        if (user.getRankedPoints() >= 9000) pane.getChildren().add(buildBadgeCard("Root Access", "The ultimate singularity.", "legendary", null));
    }

    public static VBox buildBadgeCard(String name, String desc, String rarity, String imgName) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("badge-card", "badge-glow-" + rarity);
        card.setAlignment(Pos.CENTER);
        
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("badge-icon-container");
        
        if (imgName != null) {
            try {
                Image img = new Image(BadgeUtils.class.getResourceAsStream("/badges/" + imgName));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(60);
                iv.setFitHeight(60);
                iv.setPreserveRatio(true);
                iconContainer.getChildren().add(iv);
            } catch (Exception e) {
                Label placeholder = new Label(getBadgeSymbol(name));
                placeholder.setStyle("-fx-font-size: 28px;");
                iconContainer.getChildren().add(placeholder);
            }
        } else {
            Label symbol = new Label(getBadgeSymbol(name));
            symbol.setStyle("-fx-font-size: 32px;");
            iconContainer.getChildren().add(symbol);
        }
        
        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("badge-name");
        
        Label rarityLbl = new Label(rarity.toUpperCase());
        rarityLbl.getStyleClass().addAll("badge-rarity", "rarity-" + rarity);
        
        card.getChildren().addAll(iconContainer, nameLbl, rarityLbl);
        
        Tooltip tt = new Tooltip(name + "\n" + desc + "\nRarity: " + rarity.toUpperCase());
        tt.setShowDelay(Duration.millis(200));
        Tooltip.install(card, tt);

        card.setOnMouseClicked(e -> showBadgeDetail(name, desc, rarity, imgName, card.getScene().getWindow()));
        
        return card;
    }

    private static void showBadgeDetail(String name, String desc, String rarity, String imgName, javafx.stage.Window owner) {
        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.initStyle(javafx.stage.StageStyle.TRANSPARENT);
        popup.initOwner(owner);
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.getStyleClass().addAll("badge-popup", "badge-glow-" + rarity);
        content.setPadding(new javafx.geometry.Insets(30));
        content.setPrefSize(350, 450);

        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("badge-popup-icon-container");
        
        if (imgName != null) {
            try {
                Image img = new Image(BadgeUtils.class.getResourceAsStream("/badges/" + imgName));
                ImageView iv = new ImageView(img);
                iv.setFitWidth(120); iv.setFitHeight(120);
                iv.setPreserveRatio(true);
                iconContainer.getChildren().add(iv);
            } catch (Exception e) {}
        } else {
            Label symbol = new Label(getBadgeSymbol(name));
            symbol.setStyle("-fx-font-size: 64px;");
            iconContainer.getChildren().add(symbol);
        }

        Label nameLbl = new Label(name);
        nameLbl.getStyleClass().add("badge-popup-name");

        Label rarityLbl = new Label(rarity.toUpperCase());
        rarityLbl.getStyleClass().addAll("badge-rarity", "rarity-" + rarity);
        rarityLbl.setStyle("-fx-font-size: 14px; -fx-padding: 6 15;");

        Label descLbl = new Label(desc);
        descLbl.getStyleClass().add("badge-popup-desc");
        descLbl.setWrapText(true);
        descLbl.setAlignment(Pos.CENTER);

        Button closeBtn = new Button("CLOSE");
        closeBtn.getStyleClass().add("badge-popup-close");
        closeBtn.setOnAction(e -> popup.close());

        content.getChildren().addAll(iconContainer, nameLbl, rarityLbl, descLbl, closeBtn);

        Scene scene = new Scene(content);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        popup.setScene(scene);

        // Dismiss on click outside
        content.focusedProperty().addListener((obs, old, val) -> {
            if (!val) popup.close();
        });
        
        popup.focusedProperty().addListener((obs, old, val) -> {
            if (!val) popup.close();
        });

        popup.show();
        content.requestFocus();
    }

    private static String getBadgeSymbol(String name) {
        if (name.contains("Root")) return "#";
        if (name.contains("Engineer")) return "⚙";
        if (name.contains("Designer")) return "△";
        if (name.contains("Mastery")) return "~>";
        if (name.contains("Authority")) return "::";
        return "🏆";
    }
}
