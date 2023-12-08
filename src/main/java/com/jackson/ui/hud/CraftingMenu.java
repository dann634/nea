package com.jackson.ui.hud;

import com.jackson.game.items.Entity;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;

public class CraftingMenu extends BorderPane {
    private final ScrollPane leftMenu;
    private final HBox bottomMenu;
    private final VBox centerMenu;
    private final Label selectedItem;
    private VBox recipeVbox;
    private Button craftButton;
    private final HashMap<String, CraftingItem> craftingDirectory;

    public CraftingMenu() {
        this.leftMenu = new ScrollPane();
        this.centerMenu = new VBox();
        this.bottomMenu = new HBox();
        this.selectedItem = new Label("Crafting");

        this.craftingDirectory = new HashMap<>();
        setUpCraftingDirectory();

        setTranslateX(2000);
        setTranslateY((544 / 2) - (425 / 2));

        setNodeDisable(true);

        setLeftMenu();
        setCenterMenu();
        setBottomMenu();

    }


    private void setLeftMenu() {
        int width = 250;
        VBox vBox = new VBox();
        vBox.setMinWidth(width-18);
        vBox.setMinHeight(350);
        vBox.setStyle("-fx-background-color: rgba(0,0,0,.65);" +
                "-fx-border-color: black;" +
                "-fx-spacing: 5");

        leftMenu.setMinWidth(width);
        leftMenu.setContent(vBox);
        leftMenu.setDisable(true);
        leftMenu.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftMenu.setMinHeight(350); //Change as needed
        setLeft(leftMenu);

        //Add new item
        vBox.getChildren().addAll(getItemOption("wood_sword"));
        vBox.getChildren().add(getItemOption("dirt"));
        vBox.getChildren().add(getItemOption("grass"));
        vBox.getChildren().add(getItemOption("stone"));
        vBox.getChildren().add(getItemOption("wood"));
    }

    private HBox getItemOption(String itemName) {
        HBox hBox = new HBox();
        hBox.setOnMouseClicked(e -> {
            updateCenterMenu(itemName);
        });
        hBox.getStyleClass().add("craftingHbox");

        Entity icon = new Entity(itemName);
        itemName.replace("_", " ");

        Label label = new Label(itemName);
        label.setStyle("-fx-text-fill: white;" +
                "-fx-font-size: 14;" +
                "-fx-min-width: 50");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button quickCraft = new Button("Craft");
        quickCraft.getStyleClass().add("craftButton");

        hBox.getChildren().addAll(icon, label, spacer, quickCraft);

        return hBox;
    }

    private void setCenterMenu() {
        centerMenu.setMinWidth(450);
        centerMenu.setMinHeight(350);
        centerMenu.setStyle("-fx-background-color: grey;" +
                "-fx-alignment: top-center;" +
                "-fx-padding: 8;" +
                "-fx-spacing: 25;");
        setCenter(centerMenu);

        selectedItem.setStyle("-fx-font-size: 32;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-padding: 5;");

        //Embedded Vbox
        recipeVbox = new VBox();
//        recipeVbox.setStyle("-fx-border-width: 1;" +
//                "-fx-border-style: solid;");
        centerMenu.getChildren().addAll(selectedItem, recipeVbox);

    }

    private void setBottomMenu() {
        setBottom(bottomMenu);
        bottomMenu.setMinWidth(800);
        bottomMenu.setMinHeight(75);
        bottomMenu.setStyle("-fx-background-color: blue;" +
                "-fx-padding: 10");

        Button closeButton = new Button("Close");
//        closeButton.setOnAction(e -> setTranslateX(2000));
        craftButton = new Button("Craft");
        craftButton.setDisable(true);

        Pane pusherPane = new Pane();
        HBox.setHgrow(pusherPane, Priority.ALWAYS);
        bottomMenu.getChildren().addAll(closeButton, pusherPane, craftButton);

    }

    public void toggleShown(Timeline gameTimeline) {
        if(getTranslateX() == 2000) {
            setTranslateX((1024 / 2) - (800 / 2));
            gameTimeline.pause();
            setNodeDisable(false);
            return;
        }
        setTranslateX(2000);
        setNodeDisable(true);
        gameTimeline.play();
    }

    private void setNodeDisable(boolean disable) {
        //Fixes crafting buttons disabling attacking
        leftMenu.setDisable(disable);
        bottomMenu.setDisable(disable);
    }

    private void updateCenterMenu(String itemName) {
        selectedItem.setText(itemName);
        craftButton.setDisable(false);
        recipeVbox.getChildren().clear();
        recipeVbox.getChildren().add(getRecipeTree(itemName));
    }

    private AnchorPane getRecipeTree(String item) {
        AnchorPane pane = new AnchorPane();

        pane.getChildren().add(getRecipeIcon(item, (550 / 2) - 24, 0));

        return pane;
    }

    private Pane getRecipeIcon(String item, int x, int y) {
        Pane pane = new Pane();

        ImageView imageView = new ImageView(new Image("file:src/main/resources/images/" + item + ".png"));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(32);
        imageView.setFitWidth(32);

        pane.getChildren().add(imageView);
        pane.setTranslateX(x);

        return pane;
    }

    private class CraftingItem {
        private String item;
        private int amount;

        public CraftingItem(String item, int amount) {
            this.item = item;
            this.amount = amount;
        }

        public String getItem() {
            return item;
        }

        public int getAmount() {
            return amount;
        }
    }

    private void setUpCraftingDirectory() {

    }

}
