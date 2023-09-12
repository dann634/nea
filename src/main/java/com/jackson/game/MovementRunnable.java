package com.jackson.game;

import com.jackson.ui.GameController;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.ImageView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MovementRunnable implements Runnable {

    private static final double FPS = 60;
    private GameController gameController;
    private Character character;

    private SimpleBooleanProperty isAPressed;
    private SimpleBooleanProperty isDPressed;
    private SimpleBooleanProperty isWPressed;
    private double jumpVelocity;
    private double jumpAcceleration;

    public MovementRunnable(Character character, GameController gameController) { //Start new thread for each character (Maybe change later)
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

    @Override
    public void run() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                calculateXProperties();
                calculateYProperties();
//                checkForEdgeOfScreen(); // TODO: 12/09/2023 Move to character x and y change listener

            }
        }, 0, (int) (1000/FPS));

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

        Platform.runLater(() -> this.character.setY(this.character.getY() + 3)); //Gravity



    }

    private void calculateXProperties() {
        if (this.isAPressed.get() != this.isDPressed.get()) {
            AtomicInteger offset = new AtomicInteger(3);
            if(this.isAPressed.get()) {
                offset.set(-3);
            }
            Platform.runLater(() -> character.setX(character.getX() + offset.get())); //Only works for left side
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
