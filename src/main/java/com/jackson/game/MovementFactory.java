package com.jackson.game;

import com.jackson.ui.GameController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.abs;

public class MovementFactory {

    private static final double FPS = 60;
    private GameController gameController;
    private Character character;

    private SimpleBooleanProperty isAPressed;
    private SimpleBooleanProperty isDPressed;
    private SimpleBooleanProperty isWPressed;
    private double jumpVelocity;
    private double jumpAcceleration;

    private double oldX;

    public MovementFactory(Character character, GameController gameController) { //Start new thread for each character (Maybe change later)
        this.gameController = gameController;
        this.character = character;

        this.isAPressed = new SimpleBooleanProperty(false);
        this.isDPressed = new SimpleBooleanProperty(false);
        this.isWPressed = new SimpleBooleanProperty(false);


    }

    public Timeline getMovementTimeline() {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame keyFrame = new KeyFrame(Duration.millis(1000/FPS), (e -> {
            calculateXProperties();
            calculateYProperties();
        }));
        timeline.getKeyFrames().add(keyFrame);
        return timeline;
    }




    private void calculateYProperties() {
        boolean isCharacterTouchingFloor = this.gameController.isEntityTouchingGround(this.character);

        System.out.println(isCharacterTouchingFloor);
        if(isCharacterTouchingFloor && !this.isWPressed.get() && this.jumpAcceleration >= 0) { //Not jumping and on floor
            return;
        }
        if(this.jumpAcceleration < 0) { //In Mid air jumping
            this.jumpAcceleration += 0.2;
            if(this.jumpVelocity < 3 && this.jumpVelocity > -3) {
                this.jumpVelocity += this.jumpAcceleration;
            }
            this.character.setY(this.character.getY() + this.jumpVelocity);
            return;
        }

        if(this.jumpAcceleration > 0) { //To fix floating point math
            this.jumpAcceleration = 0;
        }

        if(isCharacterTouchingFloor && this.isWPressed.get()) { //Start jump
            this.jumpAcceleration = -2.5;
            return;
        }
        this.character.setY(this.character.getY() + 3);



    }

    private void calculateXProperties() {

        if(gameController.isEntityTouchingSide(character)) {
            return;
        }

        if (this.isAPressed.get() != this.isDPressed.get()) {
            AtomicInteger offset = new AtomicInteger(3);
            if(this.isAPressed.get()) {
                offset.set(-3);
            }
            character.setX(character.getX() + offset.get());
        }
        if(abs(oldX - this.character.getX()) > 30) {
            this.character.swapMovingImage();
            oldX = this.character.getX();
        } else if(oldX == this.character.getX()) {
            this.character.setIdleImage();
        }
    }




    private void checkForEdgeOfScreen() {
//        if(character.getX() < 100 || character.getX() > 924) {
//            List<Block> blockTouchingPlayer = gameController.getBlockTouchingPlayer(character);
//            if(blockTouchingPlayer != null) {
//                character.setXPos(blockTouchingPlayer.getXPos());
//                character.setYPos(blockTouchingPlayer.getYPos());
//            }
//            Platform.runLater(() -> gameController.drawWorld());
//        }

    }

    public void setIsAPressed(boolean isAPressed) {
        this.isAPressed.set(isAPressed);
    }

    public void setIsDPressed(boolean isDPressed) {
        this.isDPressed.set(isDPressed);
    }

    public void setIsWPressed(boolean isWPressed) {
        this.isWPressed.set(isWPressed);
    }
}
