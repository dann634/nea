package com.jackson.game;

import com.jackson.ui.Camera;
import com.jackson.ui.GameController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class Block extends VBox {

    private int xPos;
    private int yPos;
    private String blockName;

    private int toughness;

    private Timeline breakingTimeline;

    private final Camera camera;

    private final ImageView imageView;
    public Block(String key, int xPos, int yPos, Camera camera) {
        String dir = "file:src/main/resources/images/";
        this.blockName = switch (key) {
            case "0" -> "air";
            case "1" -> "dirt";
            case "2" -> "grass";
            case "3" -> "bedrock";
            default -> "";
        };
        dir += this.blockName + ".png";
        this.imageView = new ImageView(new Image(dir));
        this.camera = camera;

        getChildren().add(this.imageView);

        this.xPos = xPos;
        this.yPos = yPos;

        setOnMouseEntered(e -> {
            setStyle("-fx-border-width: 2;" +
                    "-fx-border-color: black;");
            toFront();

        });

        setOnMouseExited(e -> {
            setStyle("-fx-border-width: 0");
        });

        setOnMousePressed(e -> {
            if(getOpacity() == 0 || this.blockName.equals("air")) {
                return;
            }
            this.breakingTimeline = new Timeline();
            this.breakingTimeline.setCycleCount(4);
            KeyFrame breakingFrame = new KeyFrame(Duration.millis(50), m -> {
               setOpacity(getOpacity() - 0.25);
            });
            KeyFrame waitFrame = new KeyFrame(Duration.millis(200));
            this.breakingTimeline.getKeyFrames().addAll(breakingFrame, waitFrame);
            this.breakingTimeline.play();
        });

        setOnMouseReleased(e -> {
            if(blockName.equals("air")) {
                return;
            }
            this.breakingTimeline.stop();
            if(getOpacity() != 0) { //Not finished breaking so reset
                setOpacity(1);
            } else {
                drop();
            }
        });

        this.imageView.setPreserveRatio(true);
        this.imageView.setFitWidth(32);
        this.imageView.setFitHeight(32);


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
    }
}
