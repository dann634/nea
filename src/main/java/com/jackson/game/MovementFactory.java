package com.jackson.game;

import com.jackson.ui.Camera;
import com.jackson.ui.GameController;

import static com.jackson.ui.Camera.RENDER_HEIGHT;
import static com.jackson.ui.Camera.RENDER_WIDTH;
import static java.lang.Math.abs;

public class MovementFactory {

    private static final double FPS = 60;
    private GameController gameController;
    private Character character;

    private double jumpVelocity;
    private double jumpAcceleration;

    private Camera camera;

    private double oldX;

    public MovementFactory(Character character, GameController gameController, Camera camera) { //Start new thread for each character (Maybe change later)
        this.gameController = gameController;
        this.character = character;
        this.camera = camera;
    }

    public boolean calculateYProperties(boolean isWPressed) {
        boolean isCharacterTouchingFloor = this.gameController.isEntityTouchingGround(this.character);
        boolean isCharacterMovingDown = true;
        if(isCharacterTouchingFloor && !isWPressed && this.jumpAcceleration >= 0) { //Not jumping and on floor
            return false;
        }
        if(this.jumpAcceleration < 0) { //In Mid air jumping
            isCharacterMovingDown = false;
            this.jumpAcceleration += 0.15;
            if(this.jumpVelocity < 3 && this.jumpVelocity > -3) {
                this.jumpVelocity += this.jumpAcceleration;
            }
//            doYOffsetStuff((int) -this.jumpVelocity, false);
            return false;
        }

        if(this.jumpAcceleration > 0) { //To fix floating point math
            this.jumpAcceleration = 0;
        }

        if(isCharacterTouchingFloor && isWPressed) { //Start jump
            this.jumpAcceleration = -2.5;
            return false;
        }

//        System.out.println("gravity");
        return doYOffsetStuff(-3, true);



    }

    private boolean doYOffsetStuff(int offset, boolean isCharacterMovingDown) { // FIXME: 26/09/2023 something here breaks everything
        boolean condition = isCharacterMovingDown ? this.camera.getyOffset() < -32 : this.camera.getyOffset() > 32;
        System.out.println(condition);
        System.out.println(this.camera.getyOffset());
        if (condition) {
            int yLocalOffset = isCharacterMovingDown ? RENDER_HEIGHT : -(RENDER_HEIGHT) ;
            int newYPos = isCharacterMovingDown ? 1 : -1;
            int newYOffset = isCharacterMovingDown ? 32 : -32;

            this.camera.drawHorizontalLine(yLocalOffset); // FIXME: 24/09/2023 will probably break
            this.camera.deleteHorizontal(isCharacterMovingDown);
            this.character.addYPos(newYPos);
            this.camera.addYOffset(newYOffset);
        }
        this.camera.translateBlocksByY(offset); // FIXME: 26/09/2023 this method :((
        return condition;
    }

    public boolean calculateXProperties(boolean isAPressed, boolean isDPressed) {

        if(!isAPressed && !isDPressed) { //For optimization
            return false;
        }

        boolean canMoveLeft = !gameController.isEntityTouchingSide(character.getLeftCollision());
        boolean canMoveRight = !gameController.isEntityTouchingSide(character.getRightCollision());

        int offset = 0;
        boolean isCharacterMovingLeft = false;
        if (isAPressed != isDPressed) {
            if(canMoveRight && isDPressed) {
                offset = -6;
            }
            if(isAPressed && canMoveLeft) {
                offset = 6;
                isCharacterMovingLeft = true;
            }
        }
        this.camera.translateBlocksByX(offset);
        boolean condition = isCharacterMovingLeft ? this.camera.getxOffset() > 32 : this.camera.getxOffset() < -32;
        if (condition) {
            int xLocalOffset = isCharacterMovingLeft ? -RENDER_WIDTH - 1 : RENDER_WIDTH;
            int newXPos = isCharacterMovingLeft ? -1 : 1;
            int newXOffset = isCharacterMovingLeft ? -32 : 32;

            this.camera.drawVerticalLine(xLocalOffset);
            this.camera.deleteVertical(!isCharacterMovingLeft);
            this.character.addXPos(newXPos);
            this.camera.addXOffset(newXOffset);
        }

        if(abs(oldX - this.character.getX()) > 30) {
            this.character.swapMovingImage();
            oldX = this.character.getX();
        } else if(oldX == this.character.getX()) {
            this.character.setIdleImage();
        }
        return condition;
    }


}
