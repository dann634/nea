package com.jackson.ui;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Inventory {

    private HBox hotBarHBox;
    private VBox[] hotBarSlots;

    private static final int HOTBAR_SIZE = 5;

    public Inventory() {
        initHotBar();
    }

    private void initHotBar() {
        this.hotBarHBox = new HBox(10);
        this.hotBarHBox.setStyle("-fx-padding: 10");

        this.hotBarSlots = new VBox[HOTBAR_SIZE];
        for (int i = 0; i < this.hotBarSlots.length ; i++) {
            VBox vBox = new VBox();
            vBox.setStyle("-fx-background-color: grey;" +
                    "-fx-border-radius: 6;" +
                    "-fx-background-radius: 8;" +
                    "-fx-min-width: 40;" +
                    "-fx-min-height: 40;" +
                    "-fx-border-color: black;" +
                    "-fx-border-width: 3;");
            this.hotBarSlots[i] = vBox;
            this.hotBarHBox.getChildren().add(vBox);
        }




    }

    public HBox getHotBarHBox() {
        return hotBarHBox;
    }
}
