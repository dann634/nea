package com.jackson.game.characters;

import com.jackson.game.items.Entity;
import com.jackson.io.TextIO;
import com.jackson.ui.Camera;
import javafx.animation.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.TimeoutException;

public class Player extends Character {

    private int xPos;
    private int yPos;
    private Label displayNameLabel;
    private final double ATTACK_RANGE = 300;
    private final double INTERACT_RANGE = 600;
    private SimpleIntegerProperty agilityLevel;
    private SimpleIntegerProperty strengthLevel;
    private SimpleIntegerProperty defenceLevel;
    private SimpleDoubleProperty agilityXP;
    private SimpleDoubleProperty strengthXP;
    private SimpleDoubleProperty defenceXP;

    public Player() {
        super();
        setX(484); //Half Screen size (512) - Character Width (48) + Some Value(22)
        setY(180);

        initDisplayNameLabel();

        this.agilityLevel = new SimpleIntegerProperty(1);
        this.strengthLevel = new SimpleIntegerProperty(1);
        this.defenceLevel = new SimpleIntegerProperty(1);

        this.agilityXP = new SimpleDoubleProperty(0);
        this.strengthXP = new SimpleDoubleProperty(0);
        this.defenceXP = new SimpleDoubleProperty(0);
    }

    public void moveHand(double x, double y) {
        this.handImageView.setVisible(true);
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

    public boolean isWithinAttackRange(double x, double y) {
        return getDistance(x, y) < ATTACK_RANGE;
    }

    public boolean isWithinInteractRange(double x, double y) {
        return getDistance(x, y) < INTERACT_RANGE;
    }

    private double getDistance(double x, double y) {
        double[] distance = getXYDistance(x, y);
        return Math.hypot(distance[0], distance[1]);
    }

    private double[] getXYDistance(double x, double y) {
        double xDifference;
        if(x < this.getX()) {
            xDifference = this.getX() - x;
        } else {
            xDifference = x - this.getX();
        }

        double yDifference;
        if(y < this.getY()) {
            yDifference = this.getY() - y;
        } else {
            yDifference = y - this.getY();
        }
        return new double[]{xDifference, yDifference};
    }

    @Override
    public void attack(Entity item) {
        if(item != null && item.isUsable()) {
            this.attackTranslate.play();
        }
    }

    public void addStrengthXP(double amount) {
        this.strengthXP.set(this.strengthXP.get() + amount);
        if(this.strengthXP.get() > Math.sqrt(0.25 * (this.strengthLevel.get() + 1))) { //Is total xp more than required for next level
            this.strengthLevel.set(this.strengthLevel.get() + 1); //Level up
        }
    }


    public SimpleIntegerProperty agilityLevelProperty() {
        return agilityLevel;
    }

    public SimpleIntegerProperty strengthLevelProperty() {
        return strengthLevel;
    }


    public SimpleIntegerProperty defenceLevelProperty() {
        return defenceLevel;
    }



    public SimpleDoubleProperty agilityXPProperty() {
        return agilityXP;
    }



    public SimpleDoubleProperty strengthXPProperty() {
        return strengthXP;
    }



    public SimpleDoubleProperty defenceXPProperty() {
        return defenceXP;
    }

}
