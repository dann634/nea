package com.jackson.game;

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

public class Block extends VBox {

    private int xPos;
    private int yPos;
    private String blockName;

    private int toughness;

    private Timeline breakingTimeline;

    private  Camera camera;
    private  Inventory inventory;

    private boolean isDropped;

    private boolean isBreakable;
    private  ImageView imageView;



    public Block(String blockName, int xPos, int yPos, Camera camera, Inventory inventory) {
        initButtonPresses();
        initFields(blockName, xPos, yPos, camera, inventory);

    }

    private void initFields(String blockName, int xPos, int yPos, Camera camera, Inventory inventory) {
        this.blockName = blockName;
        this.imageView = new ImageView(new Image("file:src/main/resources/images/" + this.blockName + ".png"));
        this.camera = camera;
        this.inventory = inventory;
        this.isDropped = false;

        this.isBreakable = true;
        if(this.blockName.equals("air") || this.blockName.equals("bedrock")) {
            this.isBreakable = false;
        }

        getChildren().add(this.imageView);

        this.xPos = xPos;
        this.yPos = yPos;

        this.imageView.setPreserveRatio(true);
        this.imageView.setFitWidth(32);
        this.imageView.setFitHeight(32);
    }

    private void initButtonPresses() {
        setOnMouseEntered(e -> {
            if(this.isDropped) {
                return;
            }
            setStyle("-fx-border-width: 2;" +
                    "-fx-border-color: black;");
            toFront();

        });

        setOnMouseExited(e -> {
            setStyle("-fx-border-width: 0");
        });

        setOnMousePressed(e -> {

            if(this.blockName.equals("air") && this.inventory.getSelectedItemStack() != null) {
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

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public String getBlockName() {
        return this.blockName;
    }

    public void drop() {
        int blockHeight = 16;
        this.imageView.setFitHeight(blockHeight);
        this.imageView.setFitWidth(blockHeight);
        this.imageView.setRotate(new Random().nextDouble(360) + 1);
        this.setOpacity(1);
        this.camera.removeBlock(this);
        this.isDropped = true;
    }

    public void setDefault() {
        int blockHeight = 32;
        this.imageView.setFitHeight(blockHeight);
        this.imageView.setFitWidth(blockHeight);
        this.imageView.setRotate(0);
        this.isDropped = false;
    }


}
