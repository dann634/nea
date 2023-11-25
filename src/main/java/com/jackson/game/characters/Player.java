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

    private final double LEVEL_CONSTANT = 1.1;

    private SimpleIntegerProperty agilityLevel;
    private SimpleIntegerProperty strengthLevel;
    private SimpleIntegerProperty defenceLevel;
    private SimpleIntegerProperty agilityXP;
    private final SimpleIntegerProperty strengthXP;
    private SimpleIntegerProperty defenceXP;

    public Player() {
        super();
        setX(484); //Half Screen size (512) - Character Width (48) + Some Value(22)
        setY(180);

        initDisplayNameLabel();

        agilityLevel = new SimpleIntegerProperty(1);
        strengthLevel = new SimpleIntegerProperty(1);
        defenceLevel = new SimpleIntegerProperty(1);

        agilityXP = new SimpleIntegerProperty(0);
        strengthXP = new SimpleIntegerProperty(0);
        defenceXP = new SimpleIntegerProperty(0);
    }

    private void initDisplayNameLabel() {
        displayNameLabel = new Label(TextIO.readFile("src/main/resources/settings/settings.txt").get(0));
        displayNameLabel.translateXProperty().bind(xProperty().subtract(displayNameLabel.getWidth() / 2));
        displayNameLabel.translateYProperty().bind(yProperty().subtract(15));
        displayNameLabel.setStyle("-fx-font-weight: bold");
        displayNameLabel.setVisible(false); // not for singleplayer
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
        xPos += value;
    }

    public void addYPos(int value) {
        yPos += value;
    }

    public ImageView getHandRectangle() {
        return handImageView;
    }

    public Label getDisplayNameLabel() {
        return displayNameLabel;
    }

    public void setIsModelFacingRight(boolean isModelFacingRight) {
        this.isModelFacingRight.set(isModelFacingRight);
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
        if(x < getX()) {
            xDifference = getX() - x;
        } else {
            xDifference = x - getX();
        }

        double yDifference;
        if(y < getY()) {
            yDifference = getY() - y;
        } else {
            yDifference = y - getY();
        }
        return new double[]{xDifference, yDifference};
    }

    @Override
    public void attack(Entity item) {
        if(item != null && !item.isUsable()) {
            return;
        }
        attackTranslate.play();
    }

    @Override
    public double getAttackDamage() {
        return super.getAttackDamage() + strengthLevel.get();
    }

    public void addStrengthXP(int amount) {
        strengthXP.set(strengthXP.get() + amount);
        if((50 * Math.pow(strengthLevel.get(), 1.5) + 1 < strengthXP.get())) {
            strengthLevel.set(strengthLevel.get() + 1);
        }
    }

    public void addAgilityXP(int amount) {
        agilityXP.set(agilityXP.get() + amount);
        if((50 * Math.pow(agilityLevel.get(), 1.5) + 1 < agilityXP.get())) {
            agilityLevel.set(agilityLevel.get() + 1);
        }
    }

    public int getAgilityLevel() {
        return agilityLevel.get();
    }

    public SimpleIntegerProperty agilityLevelProperty() {
        return agilityLevel;
    }

    public int getStrengthLevel() {
        return strengthLevel.get();
    }

    public SimpleIntegerProperty strengthLevelProperty() {
        return strengthLevel;
    }


    public int getDefenceLevel() {
        return defenceLevel.get();
    }

    public SimpleIntegerProperty defenceLevelProperty() {
        return defenceLevel;
    }



    public int getAgilityXP() {
        return agilityXP.get();
    }

    public SimpleIntegerProperty agilityXPProperty() {
        return agilityXP;
    }

    public int getStrengthXP() {
        return strengthXP.get();
    }

    public SimpleIntegerProperty strengthXPProperty() {
        return strengthXP;
    }



    public int getDefenceXP() {
        return defenceXP.get();
    }

    public SimpleIntegerProperty defenceXPProperty() {
        return defenceXP;
    }

}
