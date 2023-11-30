package com.jackson.game.items;

import com.jackson.ui.Camera;
import com.jackson.ui.GameController;
import com.jackson.ui.hud.Inventory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.jackson.ui.GameController.lookupTable;

public class Block extends Entity {

    private int xPos;
    private int yPos;
    private int toughness;
    private Timeline breakingTimeline; //Maybe change to a transition
    private  Camera camera;
    private  Inventory inventory;
    private boolean isDropped;
    private boolean isBreakable;


    public Block(String blockName, int xPos, int yPos, Camera camera, Inventory inventory) {
        super(blockName);
        initButtonPresses();
        initFields(xPos, yPos, camera, inventory);
        this.isUsable = false;
    }

    private void initFields(int xPos, int yPos, Camera camera, Inventory inventory) {
        this.camera = camera;
        this.inventory = inventory;
        this.isDropped = false;

        this.isBreakable = true;
        if(this.itemName.equals("air") || this.itemName.equals("bedrock")) {
            this.isBreakable = false;
        }

        getStyleClass().add("block");

        this.xPos = xPos;
        this.yPos = yPos;

        setSize(32);
    }

    private void initButtonPresses() {
        setOnMouseEntered(e -> {
            if(this.isDropped) {
                return;
            }
            toFront();

        });


        setOnMousePressed(e -> {

            if(this.itemName.equals("air") && this.inventory.getSelectedItemStack() != null
                    && this.inventory.getItemStackOnCursor() == null && !this.inventory.getSelectedItemStack().isUsable()) {
                //Place block
                this.camera.placeBlock(this);
                return;
            }

            if(getOpacity() == 0 || !this.isBreakable || this.isDropped) {
                return;
            }

            //Break block
            this.breakingTimeline = new Timeline();
            this.breakingTimeline.setCycleCount(4);
            KeyFrame breakingFrame = new KeyFrame(Duration.millis(50), m -> { //50
                setOpacity(getOpacity() - 0.25);
            });
            KeyFrame waitFrame = new KeyFrame(Duration.millis(200)); //200
            this.breakingTimeline.getKeyFrames().addAll(breakingFrame, waitFrame);
            this.breakingTimeline.play();
        });

        setOnMouseReleased(e -> {
            if(!this.isBreakable || this.isDropped) {
                return;
            }
            this.breakingTimeline.stop();
            if(getOpacity() != 0) { //Not finished breaking so reset
                setOpacity(1);
            } else {
                drop();
            }
        });
    }

    public int getXPos() {
        return xPos;
    }
    public int getYPos() {
        return yPos;
    }


    private void drop() {
        int blockHeight = 16;
        this.imageView.setFitHeight(blockHeight);
        this.imageView.setFitWidth(blockHeight);
        this.imageView.setRotate(new Random().nextDouble(360) + 1);
        this.setOpacity(1);
        this.camera.removeBlock(this);
        this.isDropped = true;
    }


}
