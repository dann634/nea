package com.jackson.game.items;

import com.jackson.ui.Camera;
import com.jackson.ui.GameController;
import com.jackson.ui.hud.Inventory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static com.jackson.ui.GameController.lookupTable;

public class Block extends Entity {

    private int xPos;
    private int yPos;
    private Timeline breakingTimeline; //Maybe change to a transition
    private  Camera camera;
    private  Inventory inventory;
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

        this.isBreakable = true;
        if(this.itemName.equals("air") || this.itemName.equals("bedrock")) {
            this.isBreakable = false;
        }

        rotateProperty().addListener((observableValue, number, t1) -> {
            System.out.println(t1.intValue());
        });

        getStyleClass().add("block");

        this.xPos = xPos;
        this.yPos = yPos;

        setSize(32);
    }

    private void initButtonPresses() {

        setOnMouseEntered(e -> {
            toFront();
        });

        setOnMousePressed(e -> {

            System.out.println(xPos + "," + yPos);

            if(this.itemName.equals("air") && this.inventory.getSelectedItemStack() != null
                    && this.inventory.getItemStackOnCursor() == null && !this.inventory.getSelectedItemStack().isUsable()) {
                //Place block
                try {
                    this.camera.placeBlock(this, inventory.getSelectedItemStackName(), true);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            if(getOpacity() == 0 || !this.isBreakable) {
                return;
            }

            //Calculate Break Speed
            double waitTime = 200; //Default break time
            if(inventory.getSelectedItemStack() != null) {
                String itemInHand = inventory.getSelectedItemStackName();

                if((itemInHand.contains("axe") && !itemInHand.contains("pickaxe") && itemName.equals("wood")) ||
                        (itemInHand.contains("pickaxe") && itemName.equals("stone")) ||
                        (itemInHand.contains("shovel") && (itemName.equals("dirt") || itemName.equals("grass")))) {

                    if(itemInHand.contains("metal_")) {
                        waitTime *= 0.4;
                    } else if(itemInHand.contains("stone_") ) {
                        waitTime *= 0.6;
                    } else if(itemInHand.contains("wood_") ) {
                        waitTime *= 0.8;
                    }
                }

            }



            //Break block
            this.breakingTimeline = new Timeline();
            this.breakingTimeline.setCycleCount(4);
            KeyFrame breakingFrame = new KeyFrame(Duration.millis(50), m -> { //50
                setOpacity(getOpacity() - 0.25);
            });
            KeyFrame waitFrame = new KeyFrame(Duration.millis(waitTime)); //200
            this.breakingTimeline.getKeyFrames().addAll(breakingFrame, waitFrame);
            this.breakingTimeline.play();
        });

        setOnMouseReleased(e -> {
            if(!this.isBreakable) {
                return;
            }
            this.breakingTimeline.stop();
            if(getOpacity() != 0) { //Not finished breaking so reset
                setOpacity(1);
            } else {
                try {
                    drop();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public int getXPos() {
        return xPos;
    }
    public int getYPos() {
        return yPos;
    }


    private void drop() throws IOException {
        int blockHeight = 16;
        this.imageView.setFitHeight(blockHeight);
        this.imageView.setFitWidth(blockHeight);
        this.imageView.setRotate(new Random().nextDouble(360) + 1);
        this.setOpacity(1);
        this.camera.removeBlock(this, true);
    }


}
