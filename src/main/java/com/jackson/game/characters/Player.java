package com.jackson.game.characters;

import com.jackson.io.TextIO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Player extends Character {

    private int xPos;
    private int yPos;
    private Label displayNameLabel;


    public Player() {
        super();
        setX(484); //Half Screen size (512) - Character Width (48) + Some Value(22)
        setY(180);

        initDisplayNameLabel();

    }

    private void initDisplayNameLabel() {
        this.displayNameLabel = new Label(TextIO.readFile("src/main/resources/settings/settings.txt").get(0));
        this.displayNameLabel.translateXProperty().bind(this.xProperty().subtract(this.displayNameLabel.getWidth() / 2));
        this.displayNameLabel.translateYProperty().bind(this.yProperty().subtract(15));
        this.displayNameLabel.setStyle("-fx-font-weight: bold");
        this.displayNameLabel.setVisible(false); // not for singleplayer
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

    public void addXPos(int value) {
        this.xPos += value;
    }

    public void addYPos(int value) {
        this.yPos += value;
    }

    public ImageView getHandRectangle() {
        return this.handImageView;
    }

    public Label getDisplayNameLabel() {
        return this.displayNameLabel;
    }

    public void setIsModelFacingRight(boolean isModelFacingRight) {
        this.isModelFacingRight.set(isModelFacingRight);
    }


}