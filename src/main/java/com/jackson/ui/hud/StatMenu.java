package com.jackson.ui.hud;

import com.jackson.game.characters.Player;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Locale;

public class StatMenu extends VBox {

    private HBox strengthHBox;
    private HBox agilityHBox;
    private HBox defenceHBox;
    private final TranslateTransition translate;
    private boolean isVisible;

    public StatMenu(Player player) {
        this.strengthHBox = createHBox("Strength", player.strengthLevelProperty(), player.strengthXPProperty());
        this.agilityHBox = createHBox("Agility", player.agilityLevelProperty(), player.agilityXPProperty());
        this.defenceHBox = createHBox("Defence", player.defenceLevelProperty(), player.defenceXPProperty());
        this.isVisible = false;
        Label title = new Label("Stats");
        title.getStyleClass().add("title");

        getChildren().addAll(title, this.strengthHBox, this.agilityHBox, this.defenceHBox);
        setId("statMenu");

        this.translate = new TranslateTransition();
        this.translate.setNode(this);
        this.translate.setRate(1);
        this.translate.setInterpolator(Interpolator.EASE_BOTH);
        this.translate.setOnFinished(e -> isVisible = !isVisible);
    }

    private HBox createHBox(String statName, SimpleIntegerProperty stat, SimpleIntegerProperty currentXP) {
        Label statNameLabel = new Label(statName + ":");
        statNameLabel.getStyleClass().add("statNumbers");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label currentLevel = new Label("1");
        currentLevel.getStyleClass().add("statNumbers");
        currentLevel.textProperty().bind(stat.asString());

        Label nextLevel = new Label("2");
        nextLevel.getStyleClass().add("statNumbers");
        nextLevel.textProperty().bind(stat.add(1).asString());

        ProgressBar xpBar = new ProgressBar();
        xpBar.getStyleClass().add("xpBar");
        xpBar.progressProperty().bind(currentXP);
        currentXP.set(50);

        HBox hBox = new HBox();
        hBox.getChildren().addAll(statNameLabel, spacer, currentLevel, xpBar, nextLevel);
        hBox.getStyleClass().add("stat");
        return hBox;
    }

    public void toggleShown() {
        this.translate.setToX(this.isVisible ? 1324 : 714);
        this.translate.play();
    }
}
