package com.jackson.ui.hud;

import com.jackson.game.items.Entity;
import com.jackson.game.items.Item;
import com.jackson.game.items.ItemStack;
import com.jackson.io.TextIO;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class CraftingMenu extends BorderPane {
    private final ScrollPane leftMenu;
    private final HBox bottomMenu;
    private final VBox centerMenu;
    private final Label selectedItem;
    private VBox recipeVbox;
    private Button craftButton;
    private final List<String> fileData;
    private final Inventory inventory;

    public CraftingMenu(Inventory inventory) {
        this.leftMenu = new ScrollPane();
        this.inventory = inventory;
        this.centerMenu = new VBox();
        this.bottomMenu = new HBox();
        this.selectedItem = new Label("Crafting");
        fileData = TextIO.readFile("src/main/resources/settings/recipes.txt");

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
        leftMenu.setMaxHeight(350); //Change as needed

        setLeft(leftMenu);

        //Add new item
        for(String line : fileData) {
            String[] splitLine = line.split(" ");
            if(splitLine[0].equals("axe") || splitLine[0].equals("sword") || splitLine[0].equals("pickaxe") || splitLine[0].equals("shovel")) {
                vBox.getChildren().addAll(getItemOption("wood_" + splitLine[0]));
                vBox.getChildren().addAll(getItemOption("stone_" + splitLine[0]));
                vBox.getChildren().addAll(getItemOption("metal_" + splitLine[0]));
                continue;
            }
            vBox.getChildren().add(getItemOption(line.split(" ")[0]));
        }

    }

    private HBox getItemOption(String itemName) {
        HBox hBox = new HBox();
        hBox.setOnMouseClicked(e -> updateCenterMenu(itemName));
        hBox.getStyleClass().add("craftingHbox");

        Entity icon = new Entity(itemName);

        Label label = new Label(itemName);
        label.setStyle("-fx-text-fill: white;" +
                "-fx-font-size: 14;" +
                "-fx-min-width: 50");

        hBox.getChildren().addAll(icon, label);

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
        centerMenu.getChildren().addAll(selectedItem, recipeVbox);

    }

    private void setBottomMenu() {
        setBottom(bottomMenu);
        bottomMenu.setMinWidth(800);
        bottomMenu.setMinHeight(75);
        bottomMenu.setStyle("-fx-background-color: grey;" +
                "-fx-padding: 10;" +
                "-fx-alignment: center-right;" +
                "-fx-border-width: 1;" +
                "-fx-border-style:  solid none none none;");


        craftButton = new Button("Craft");
        craftButton.setPrefWidth(100);
        craftButton.setPrefHeight(30);
        craftButton.setDisable(true);
        craftButton.setOnAction(e -> {
            List<ItemStack> recipe = getCraftingRecipe(selectedItem.getText());
            if(!inventory.canCraft(recipe)) {
                return;
            }
            inventory.craft(selectedItem.getText(), recipe);
            craftButton.setDisable(!inventory.canCraft(getCraftingRecipe(selectedItem.getText()))); //Disables if it cannot craft anymore
        });

        Pane pusherPane = new Pane();
        HBox.setHgrow(pusherPane, Priority.ALWAYS);
        bottomMenu.getChildren().addAll(craftButton);

    }

    public void toggleShown(Timeline gameTimeline, boolean isSingleplayer) {
        if(getTranslateX() == 2000) {
            setTranslateX((1024 / 2) - (800 / 2));
            if(isSingleplayer) gameTimeline.pause();
            setNodeDisable(false);
            return;
        }
        setTranslateX(2000);
        setNodeDisable(true);
        if(isSingleplayer) gameTimeline.play();
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
        craftButton.setDisable(!inventory.canCraft(getCraftingRecipe(itemName)));
    }

    private AnchorPane getRecipeTree(String item) {
        AnchorPane pane = new AnchorPane();

        pane.getChildren().add(getRecipeIcon(item));
        HBox recipeHbox = new HBox(40);
        recipeHbox.setPrefWidth(530);
        recipeHbox.setAlignment(Pos.CENTER);
        recipeHbox.setTranslateY(120);
        pane.getChildren().add(recipeHbox);

        //Get item recipe
        List<ItemStack> recipe = getCraftingRecipe(item);
        Pane startPush = new Pane();
        HBox.setHgrow(startPush, Priority.ALWAYS);
        recipeHbox.getChildren().add(startPush);

        for (ItemStack itemStack : recipe) {
            Pane pusher = new Pane();
            HBox.setHgrow(pusher, Priority.ALWAYS);

            itemStack.setOnMouseClicked(e -> updateCenterMenu(itemStack.getItemName()));
            itemStack.setMouseTransparent(false);
            itemStack.setSize(32);
            recipeHbox.getChildren().addAll(itemStack, pusher);
        }


        return pane;
    }

    private Pane getRecipeIcon(String item) {
        Pane pane = new Pane();

        ImageView imageView = new ImageView(new Image("file:src/main/resources/images/" + item + ".png"));
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(64);
        imageView.setFitWidth(64);

        if(item.equals("bullet")) {
            ItemStack itemStack = new ItemStack(new Item("bullet"));
            itemStack.addStackValue(20);
            itemStack.setSize(64);
            pane.getChildren().add(itemStack);
        } else {
            pane.getChildren().add(imageView);
        }

        pane.setTranslateX(233);
        return pane;
    }


    private List<ItemStack> getCraftingRecipe(String item) { // FIXME: 19/12/2023 isnt given stack size
        String itemType;
        if(item.contains("_")) {
            itemType = item.split("_")[1];
        } else {
            itemType = item;
        }

        List<ItemStack> recipe = new ArrayList<>();

        for(String line : fileData) {
            String[] splitLine = line.split(" ");
            if(itemType.equals(splitLine[0])) {
                for (int i = 1; i < splitLine.length; i++) {
                    ItemStack craftingItem;
                    if(splitLine[i].contains("ingot")) {
                        craftingItem = new ItemStack(new Item(item.split("_")[0]));
                    } else {
                        craftingItem = new ItemStack(new Item(splitLine[i].substring(1)));
                    }
                    craftingItem.addStackValue(Integer.parseInt(String.valueOf(splitLine[i].charAt(0))));
                    recipe.add(craftingItem);
                }
            }
        }
        return recipe;
    }




}
