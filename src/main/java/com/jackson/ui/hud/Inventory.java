package com.jackson.ui.hud;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private HBox hotBarHBox;
    private VBox[] hotBarSlots;
    private VBox[][] inventoryArr;
    private VBox wholeInventoryVbox;

    private List<HBox> hboxRows;


    private static final int HOTBAR_SIZE = 5;
    private static final int INVENTORY_SIZE = 4;
    private boolean isInventoryOpen;

    public Inventory() {
        initHotBar();
    }

    private void initHotBar() {

        this.isInventoryOpen = false;

        this.wholeInventoryVbox = new VBox(10); //Whole vbox
        this.wholeInventoryVbox.setStyle("-fx-padding: 10");

        this.hboxRows = new ArrayList<>(); //Array of all hbox rows

        this.inventoryArr = new VBox[HOTBAR_SIZE][INVENTORY_SIZE]; //Array of all vbox squares

        this.hotBarHBox = new HBox(3); //First row
        this.hotBarSlots = new VBox[HOTBAR_SIZE];
        this.hboxRows.add(this.hotBarHBox);

        for (int i = 0; i < HOTBAR_SIZE ; i++) { //Hotbar
            VBox vBox = getInventorySquare();
            this.hotBarSlots[i] = vBox;
            this.hotBarHBox.getChildren().add(vBox);
            this.inventoryArr[i][0] = vBox;
        }

        //Rest of inventory
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            HBox hBox = new HBox(3);
            this.hboxRows.add(hBox);
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                var vbox = getInventorySquare();
                hBox.getChildren().add(vbox);
                this.inventoryArr[j][i] = vbox;
            }
        }

        //Adds hbox to vbox
        for(HBox hBox : this.hboxRows) {
            this.wholeInventoryVbox.getChildren().add(hBox);
        }


        setHideInventory(true); //Should be hidden by default




    }

    private VBox getInventorySquare() {
        VBox vBox = new VBox();
        vBox.setStyle("-fx-background-color: rgba(0,0,0,.65);" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 8;" +
                "-fx-min-width: 40;" +
                "-fx-min-height: 40;" +
                "-fx-border-color: black;" +
                "-fx-border-width: 2;");

        vBox.setOnMouseClicked(e -> {
            if(this.isInventoryOpen) {
                setHideInventory(true);
            } else {
                setHideInventory(false);
            }
        });

        return vBox;
    }

    public void toggleInventory() {
        if(this.isInventoryOpen) {
            setHideInventory(true);
            return;
        }
        setHideInventory(false);
    }

    private void setHideInventory(boolean isHidden) {
        this.isInventoryOpen = !isHidden;
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            this.hboxRows.get(i).setVisible(!isHidden);
        }
    }

    public VBox getInventoryVbox() {
        return this.wholeInventoryVbox;
    }
}
