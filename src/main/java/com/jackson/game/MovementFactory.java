package com.jackson.game;

import com.jackson.ui.Camera;
import com.jackson.ui.GameController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

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

    public Timeline getMovementTimeline(Camera camera, String[][] map) {
        Timeline timeline = new Timeline(); //Initialise Timeline object
        timeline.setCycleCount(Animation.INDEFINITE); //So timeline is infinite

        //Keyframe takes fps
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200), (e -> {
            gameController.clearWorld();
            calculateXProperties(); //Updates X Values
            calculateYProperties(); //Updates Y Values
            camera.draw(this.character, this.gameController, map);
        }));
        timeline.getKeyFrames().add(keyFrame); //Adds keyframe to timeline
        return timeline;
    }




    private void calculateYProperties() {
        boolean isCharacterTouchingFloor = this.gameController.isEntityTouchingGround(this.character);

        if(isCharacterTouchingFloor && !this.isWPressed.get() && this.jumpAcceleration >= 0) { //Not jumping and on floor
            return;
        }
        if(this.jumpAcceleration < 0) { //In Mid air jumping
            this.jumpAcceleration += 0.15;
            if(this.jumpVelocity < 3 && this.jumpVelocity > -3) {
                this.jumpVelocity += this.jumpAcceleration;
            }
            this.character.setYPos((int) (this.character.getYPos() + this.jumpVelocity));
            return;
        }

        if(this.jumpAcceleration > 0) { //To fix floating point math
            this.jumpAcceleration = 0;
        }

        if(isCharacterTouchingFloor && this.isWPressed.get()) { //Start jump
            this.jumpAcceleration = -2.5;
            return;
        }

        this.character.setYPos(this.character.getYPos() + 3);

    }

    private void calculateXProperties() {

        boolean canMoveLeft = !gameController.isEntityTouchingSide(character.getLeftCollision());
        boolean canMoveRight = !gameController.isEntityTouchingSide(character.getRightCollision());

        int offset = 0;
        if (this.isAPressed.get() != this.isDPressed.get()) {
            if(canMoveRight && this.isDPressed.get()) {
                offset = 3;
            }
            if(this.isAPressed.get() && canMoveLeft) {
                offset = -3;
            }

            this.character.setXPos(this.character.getXPos() + offset);
        }
        if(abs(oldX - this.character.getX()) > 30) {
            this.character.swapMovingImage();
            oldX = this.character.getX();
        } else if(oldX == this.character.getX()) {
            this.character.setIdleImage();
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
