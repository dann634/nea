package com.jackson.ui.hud;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

import java.util.Locale;

public class HealthBar {

    private HBox root;
    private ProgressBar healthBar;
    private Label healthLabel;

    public HealthBar(SimpleDoubleProperty healthProperty) {
        this.root = new HBox(10);
        this.healthBar = new ProgressBar(1);
        this.healthLabel = new Label("");

        this.healthBar.progressProperty().bind(healthProperty.divide(100));
        this.healthLabel.textProperty().bind(Bindings.format(new Locale("en", "uk"), "%.0f", healthProperty));

        this.root.setStyle("-fx-translate-x: 714;" +
                "-fx-translate-y: 10;" +
                "-fx-alignment: center;" +
                "-fx-background-color: rgba(0,0,0,.65);" +
                "-fx-padding: 5;" +
                "-fx-background-radius: 8;" +
                "-fx-min-width: 300;" +
                "-fx-max-width: 300;");
        this.healthLabel.setStyle("-fx-font-weight: bold;" +
                "-fx-font-size: 20;" +
                "-fx-text-fill: white;" +
                "-fx-min-width: 30;" +
                "-fx-alignment: center");
        this.healthBar.setStyle("-fx-accent: red;" +
                "-fx-min-width: 230;" +
                "-fx-min-height: 30;");

        this.root.getChildren().addAll(healthLabel, healthBar);
    }

    public HBox getHealthHud() {
        return this.root;
    }
}
