package com.jackson.network.connections;

import com.jackson.game.characters.Player;
import com.jackson.game.characters.PlayerInterface;
import com.jackson.io.TextIO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PseudoPlayer implements PlayerInterface {
    private ImageView imageView;
    private int xPos;
    private int yPos;
    private int xOffset;
    private int yOffset;
    private final Image image1;
    private final Image image2;
    private Label displayNameLabel;
    private final String displayName;
    private int oldX;
    private boolean isOnScreen;


    public PseudoPlayer(String displayName) {
        this.displayName = displayName;
        image1 = new Image("file:src/main/resources/images/playerRun1.png");
        image2 = new Image("file:src/main/resources/images/playerRun2.png");
        imageView = new ImageView(image1);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(32);
        initDisplayNameLabel();
        oldX = 16;
        isOnScreen = false;
    }

    private void initDisplayNameLabel() {
        displayNameLabel = new Label(this.displayName);
        displayNameLabel.translateXProperty().bind(this.imageView.translateXProperty().subtract((displayNameLabel.getWidth() / 2) + 32));
        displayNameLabel.translateYProperty().bind(this.imageView.translateYProperty().subtract(20));
        displayNameLabel.setStyle("-fx-font-weight: bold;" +
                "-fx-min-width: 86;" +
                "-fx-text-alignment: center;" +
                "-fx-alignment: center;");
    }

    public void translateX(int value) { //This is being run as the legs are moving (person is not being translated with it)
        // FIXME: 05/01/2024 TRANSLATE X IS CHANGING BUT IMAGEVIEW ISNT MOVING
        this.imageView.setTranslateX(this.imageView.getTranslateX() + value);
        System.out.println(imageView.getTranslateX());
        if(value > 0) {
            this.imageView.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        } else {
            this.imageView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }

        //Walking Animation
        oldX -= Math.abs(value);
        if(oldX <= 0) {
            if(this.imageView.getImage() == image1) {
                this.imageView.setImage(image2);
            } else {
                this.imageView.setImage(image1);
            }
            oldX = 16;
        }
    }

    public void translateY(int value) {
        this.imageView.setTranslateY(this.imageView.getTranslateY() + value);
    }

    public Label getDisplayNameLabel() {
        return displayNameLabel;
    }

    @Override
    public void setXPos(int value) {
        xPos = value;
    }

    @Override
    public void setYPos(int value) {
        yPos = value;
    }

    @Override
    public int getXPos() {
        return xPos;
    }

    @Override
    public int getYPos() {
        return yPos;
    }

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

    public boolean isOnScreen() {
        return isOnScreen;
    }

    public void setOnScreen(boolean onScreen) {
        isOnScreen = onScreen;
    }
}
