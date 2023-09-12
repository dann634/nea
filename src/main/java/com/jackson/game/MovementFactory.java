package com.jackson.game;

import com.jackson.ui.GameController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MovementFactory {

    private static final double FPS = 60;
    private GameController gameController;
    private Character character;

    private SimpleBooleanProperty isAPressed;
    private SimpleBooleanProperty isDPressed;
    private SimpleBooleanProperty isWPressed;
    private double jumpVelocity;
    private double jumpAcceleration;

    public MovementFactory(Character character, GameController gameController) { //Start new thread for each character (Maybe change later)
        //Look into making a virtual thread
        this.gameController = gameController;
        this.character = character;

        this.isAPressed = new SimpleBooleanProperty(false);
        this.isDPressed = new SimpleBooleanProperty(false);
        this.isWPressed = new SimpleBooleanProperty(false);

        this.isWPressed.or(this.isAPressed).or(this.isDPressed).addListener((observable, oldValue, newValue) -> {
            //If character approaches borders update world
        });

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
            this.jumpAcceleration = -3;
            return;
        }
        this.character.setY(this.character.getY() + 3);



    }

    private void calculateXProperties() {
        if (this.isAPressed.get() != this.isDPressed.get()) {
            AtomicInteger offset = new AtomicInteger(3);
            if(this.isAPressed.get()) {
                offset.set(-3);
            }
            character.setX(character.getX() + offset.get());
        }
    }

    private void checkForEdgeOfScreen() {
        if(character.getX() < 100 || character.getX() > 924) {
            Block blockTouchingPlayer = gameController.getBlockTouchingPlayer(character);
            if(blockTouchingPlayer != null) {
                character.setXPos(blockTouchingPlayer.getXPos());
                character.setYPos(blockTouchingPlayer.getYPos());
            }
            Platform.runLater(() -> gameController.drawWorld());
        }

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
