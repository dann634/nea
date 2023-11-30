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
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Player extends Character {

    private int xPos;
    private int yPos;
    private Label displayNameLabel;
    private final SimpleIntegerProperty agilityLevel;
    private final SimpleIntegerProperty strengthLevel;
    private final SimpleIntegerProperty defenceLevel;
    private final SimpleIntegerProperty agilityXP;
    private final SimpleIntegerProperty strengthXP;
    private final SimpleIntegerProperty defenceXP;

    public Player() {
        super();
        setX(484); //Half Screen size (512) - Character Width (48) + Some Value(22)
        setY(180);

        initDisplayNameLabel();

        agilityLevel = new SimpleIntegerProperty(50);
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
        if((50 * Math.pow(strengthLevel.get(), 1.5) < strengthXP.get())) {
            strengthLevel.set(strengthLevel.get() + 1);
        }
    }
    public void addAgilityXP(int amount) {
        agilityXP.set(agilityXP.get() + amount);
        if((50 * Math.pow(agilityLevel.get(), 1.5) < agilityXP.get())) {
            agilityLevel.set(agilityLevel.get() + 1);
        }
    }
    public void addDefenceXP(int amount) {
        defenceXP.set(defenceXP.get() + amount);
        if((50 * Math.pow(defenceLevel.get(), 1.5)) < defenceXP.get()) {
            defenceLevel.set(defenceLevel.get() + 1);
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
    public SimpleIntegerProperty agilityXPProperty() {
        return agilityXP;
    }
    public SimpleIntegerProperty strengthXPProperty() {
        return strengthXP;
    }
    public SimpleIntegerProperty defenceXPProperty() {
        return defenceXP;
    }

    @Override
    public boolean takeDamage(double amount) {
        if(new Random().nextDouble(100) < defenceLevel.get() * 0.5) {
            System.out.println("dodge");
            return false; //Dodge
        }
        addDefenceXP(10);
        return super.takeDamage(amount);
    }
}
