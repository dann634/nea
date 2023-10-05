package com.jackson.game;

import com.jackson.io.TextIO;
import com.jackson.ui.Camera;
import com.jackson.ui.GameController;

import static com.jackson.ui.Camera.RENDER_HEIGHT;
import static com.jackson.ui.Camera.RENDER_WIDTH;
import static java.lang.Math.abs;

public class MovementFactory {

    private GameController gameController;
    private Character character;

    private double jumpVelocity;
    private double jumpAcceleration;

    private Camera camera;

    private double oldX;

    public MovementFactory(Character character, GameController gameController, Camera camera) {
        this.gameController = gameController;
        this.character = character;
        this.camera = camera;
    }

    public boolean calculateYProperties(boolean isWPressed) {
        boolean isCharacterTouchingFloor = this.gameController.isEntityTouchingGround(this.character);
        if(isCharacterTouchingFloor && !isWPressed && this.jumpAcceleration >= 0) { //Not jumping and on floor
            return false;
        }
        if(this.jumpAcceleration < 0) { //In Mid air jumping
            this.jumpAcceleration += 0.15;
            if(this.jumpVelocity < 3 && this.jumpVelocity > -3) {
                this.jumpVelocity += this.jumpAcceleration;
            }
            doYOffsetStuff((int) -this.jumpVelocity, false); // FIXME: 27/09/2023 jumping deletes top layer
            return false;
        }

        if(this.jumpAcceleration > 0) { //To fix floating point math
            this.jumpAcceleration = 0;
        }

        if(isCharacterTouchingFloor && isWPressed) { //Start jump
            this.jumpAcceleration = -2.5;
            return false;
        }

        return doYOffsetStuff(-3, true);



    }

    private boolean doYOffsetStuff(int offset, boolean isCharacterMovingDown) {
        boolean condition = isCharacterMovingDown ? this.camera.getyOffset() < -32 : this.camera.getyOffset() > 0;
        if (condition) {

            int yOffset = isCharacterMovingDown ? RENDER_HEIGHT : -RENDER_HEIGHT;
            int newYPos = isCharacterMovingDown ? 1 : -1;
            int newYOffset = isCharacterMovingDown ? 32 : -32;

            this.camera.drawHorizontalLine(yOffset);
            this.camera.deleteHorizontal(yOffset);
            this.character.addYPos(newYPos);
            this.camera.addYOffset(newYOffset);
        }


        this.camera.translateBlocksByY(offset);
        return condition;
    }


    // TODO: 05/10/2023 this happens every frame

    public void calculateXProperties(boolean isAPressed, boolean isDPressed) {

        if(!isAPressed && !isDPressed) { //For optimization
            return;
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

        // TODO: 05/10/2023 If character has moved more than 32 (block width) render new line
        if (condition) {

            //Get block x at edge and add one
            //Will break if blocks is not ordered
            int xLocalOffset = isCharacterMovingLeft ? -RENDER_WIDTH : RENDER_WIDTH;
            int newXPos = isCharacterMovingLeft ? -1 : 1;
            int newXOffset = isCharacterMovingLeft ? -32 : 32;

            this.camera.drawVerticalLine(xLocalOffset); //Renders new line
            this.camera.deleteVertical(!isCharacterMovingLeft); //Deletes line on opposite side
            this.character.addXPos(newXPos); //Updates x pos of character
            this.camera.addXOffset(newXOffset); //Resets camera offset
        }

        //for walking animation (dont worry)
        if(abs(this.oldX - this.camera.getxOffset()) > 32) {
            this.character.swapMovingImage();
            this.oldX = this.character.getX();
        } else if(oldX == this.character.getX()) {
            this.character.setIdleImage();
        }
    }




}
