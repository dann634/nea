package com.jackson.network.connections;

import com.jackson.game.characters.PlayerInterface;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PseudoPlayer implements PlayerInterface {
    private final ImageView imageView;
    private final Image image1;
    private final Image image2;
    private final String displayName;
    private int xPos;
    private int yPos;
    private int xOffset;
    private int yOffset;
    private Label displayNameLabel;
    private int oldX;

    public PseudoPlayer(String displayName) {
        this.displayName = displayName;
        image1 = new Image("file:src/main/resources/images/playerRun1.png");
        image2 = new Image("file:src/main/resources/images/playerRun2.png");
        imageView = new ImageView(image1);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(32);
        initDisplayNameLabel();
        oldX = 16;
    }

    //Creates a display name label and binds it to the player
    private void initDisplayNameLabel() {
        displayNameLabel = new Label(this.displayName);
        displayNameLabel.translateXProperty().bind(this.imageView.translateXProperty().subtract((displayNameLabel.getWidth() / 2) + 32));
        displayNameLabel.translateYProperty().bind(this.imageView.translateYProperty().subtract(20));
        displayNameLabel.setStyle("-fx-font-weight: bold;" +
                "-fx-min-width: 86;" +
                "-fx-text-alignment: center;" +
                "-fx-alignment: center;");
    }

    //Moves the player and gives it a walking animation
    public void translateX(int value) {
        this.imageView.setTranslateX(this.imageView.getTranslateX() + value);
        this.imageView.setNodeOrientation((value > 0) ?
                NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);

        //Walking Animation
        oldX -= Math.abs(value);
        if (oldX <= 0) {
            if (this.imageView.getImage() == image1) {
                this.imageView.setImage(image2);
            } else {
                this.imageView.setImage(image1);
            }
            oldX = 16;
        }
    }

    //moves the player by a value in the y direction
    public void translateY(int value) {
        this.imageView.setTranslateY(this.imageView.getTranslateY() + value);
    }

    //display name getter
    public Label getDisplayNameLabel() {
        return displayNameLabel;
    }

    @Override
    public int getXPos() {
        return xPos;
    }

    //World setters and getters
    @Override
    public void setXPos(int value) {
        xPos = value;
    }

    @Override
    public int getYPos() {
        return yPos;
    }

    @Override
    public void setYPos(int value) {
        yPos = value;
    }

    //Screen getters and setters
    public int getxOffset() {
        return xOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public String getDisplayName() {
        return displayName;
    }

}
