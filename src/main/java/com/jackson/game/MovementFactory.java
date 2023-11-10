package com.jackson.game;

import com.jackson.game.characters.Player;
import com.jackson.game.characters.Zombie;
import com.jackson.game.items.ItemStack;
import com.jackson.ui.Camera;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.NodeOrientation;
import javafx.util.Duration;

import java.util.List;

import static com.jackson.ui.Camera.RENDER_WIDTH;
import static java.lang.Math.abs;
import static java.lang.Math.divideExact;

public class MovementFactory {

    private final Player character;

    private double jumpVelocity;
    private double jumpAcceleration;

    private final Camera camera;

    private double oldX;

    public MovementFactory(Player character, Camera camera) {
        this.character = character;
        this.camera = camera;
    }

    public void calculateYProperties(boolean isWPressed) {
        boolean isCharacterTouchingFloor = this.camera.isEntityTouchingGround(this.character);
        if(isCharacterTouchingFloor && !isWPressed && this.jumpAcceleration >= 0) { //Not jumping and on floor
            return;
        }
        if(this.jumpAcceleration < 0) { //In Mid air jumping
            this.jumpAcceleration += 0.15;
            if(this.jumpVelocity < 3 && this.jumpVelocity > -3) {
                this.jumpVelocity += this.jumpAcceleration;
            }
            doYOffsetStuff((int) -this.jumpVelocity, false);
            return;
        }

        if(this.jumpAcceleration > 0) { //To fix floating point math
            this.jumpAcceleration = 0;
        }

        if(isCharacterTouchingFloor && isWPressed) { //Start jump
            this.jumpAcceleration = -2.5;
            return;
        }
        doYOffsetStuff(-4, true);

    }

    private boolean doYOffsetStuff(int offset, boolean isCharacterMovingDown) {
        boolean condition = isCharacterMovingDown ? this.camera.getyOffset() < -32 : this.camera.getyOffset() > 32; // FIXME: 22/10/2023 problem
        if (condition) {

            int newYPos = isCharacterMovingDown ? 1 : -1;
            int newYOffset = isCharacterMovingDown ? 32 : -32;

            this.camera.drawHorizontalLine(!isCharacterMovingDown);
            this.camera.deleteHorizontal(isCharacterMovingDown);
            this.character.addYPos(newYPos);
            this.camera.addYOffset(newYOffset);
        }


        this.camera.translateBlocksByY(offset);
        return condition;
    }



    public void calculateXProperties(boolean isAPressed, boolean isDPressed) {

        if(!isAPressed && !isDPressed) { //For optimization
            return;
        }
        boolean canMoveLeft = !this.camera.isEntityTouchingSide(character.getLeftCollision());
        boolean canMoveRight = !this.camera.isEntityTouchingSide(character.getRightCollision());

        int offset = 0;
        boolean isCharacterMovingLeft = false;
        if (isAPressed != isDPressed) {
            if(canMoveRight && isDPressed) {
                offset = -4;
            }
            if(isAPressed && canMoveLeft) {
                offset = 4;
                isCharacterMovingLeft = true;
            }
        }


        this.camera.translateBlocksByX(offset);

        boolean condition = isCharacterMovingLeft ? this.camera.getxOffset() > 32 : this.camera.getxOffset() < -32;

        if (condition) {

            //Get block x at edge and add one
            //Will break if blocks is not ordered
            int xLocalOffset = isCharacterMovingLeft ? -RENDER_WIDTH : RENDER_WIDTH;
            int newXPos = isCharacterMovingLeft ? -1 : 1;
            int newXOffset = isCharacterMovingLeft ? -32 : 32;

            this.camera.addLine(this.camera.getVerticalLine(xLocalOffset), isCharacterMovingLeft); // FIXME: 30/10/2023 optimise this shit
            this.camera.deleteVertical(!isCharacterMovingLeft); //Deletes line on opposite side
            this.character.addXPos(newXPos); //Updates x pos of character
            this.camera.addXOffset(newXOffset); //Resets camera offset
        }

        //for walking animation (change this later)
        if(abs(this.oldX - this.camera.getxOffset()) > 31) {
            this.character.swapMovingImage();
            this.oldX = this.character.getX();
        } else if(oldX == this.character.getX()) {
            this.character.setIdleImage();
        }
    }

    public void calculateDroppedBlockGravity() {
        this.camera.setBlockJustBroken(false);
        List<ItemStack> droppedBlocks = this.camera.getDroppedBlocks();
        double[] targetYArr = new double[droppedBlocks.size()];
        for (int i = 0; i < droppedBlocks.size(); i++) {
            targetYArr[i] = this.camera.getBlockHeightUnderBlock(droppedBlocks.get(i)) - 16;
        }


        Timeline fallingTimeline = new Timeline();
        KeyFrame fallingKeyFrame = new KeyFrame(Duration.ONE, e -> {
            boolean canStop = true;
            for(int i = 0; i < droppedBlocks.size(); i++) {
                try {
                    if(targetYArr[i] >= droppedBlocks.get(i).getY()) {
                        droppedBlocks.get(i).addPos(0, 1);
                        canStop = false;
                    }
                } catch (IndexOutOfBoundsException ignored) {} //If blocks break too fast this happens
            }
            if(canStop) {
                fallingTimeline.stop();
            }
        });
        fallingTimeline.setCycleCount(Animation.INDEFINITE);
        fallingTimeline.getKeyFrames().add(fallingKeyFrame);
        fallingTimeline.play();
    }

    public void calculateZombieMovement(List<Zombie> zombies) {

        for(Zombie zombie : zombies) {
            calculateZombieX(zombie);
            calculateZombieY(zombie);
        }

    }

    private void calculateZombieX(Zombie zombie) { // TODO: 10/11/2023 ADD XOFFSET
        //make zombie face player
        double difference = this.character.getX() - zombie.getTranslateX();
        boolean needsToMoveRight = difference > 0;
        zombie.setNodeOrientation(needsToMoveRight ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);


        //Check collision
        boolean canMoveLeft = !this.camera.isEntityTouchingSide(zombie.getLeftCollision());
        boolean canMoveRight = !this.camera.isEntityTouchingSide(zombie.getRightCollision());
        if(!canMoveRight && !canMoveLeft) { //Stuck
            return;
        }

        //Move left or right
        if((needsToMoveRight && canMoveRight) || (!needsToMoveRight && canMoveLeft)) {
            zombie.addTranslateX((needsToMoveRight) ? Zombie.SPEED : -Zombie.SPEED);
        }

        if((needsToMoveRight && !canMoveRight)
        || (!needsToMoveRight && !canMoveLeft) && zombie.getJumpAcceleration() >= 0) {
            //Jump to get over block
            zombie.setNeedsToJump(true);
        }


    }

    private void calculateZombieY(Zombie zombie) {
        boolean isZombieTouchingFloor = this.camera.isEntityTouchingGround(zombie);
        if(isZombieTouchingFloor && zombie.getJumpAcceleration() >= 0 && !zombie.isNeedsToJump()) { //Not jumping and on floor
            return;
        }

        if(zombie.getJumpAcceleration() < 0) { //In Mid air jumping
            zombie.addJumpAcceleration(0.15);
            if(zombie.getJumpVelocity() < 3 && zombie.getJumpVelocity() > -3) {
                zombie.addJumpVelocity(zombie.getJumpAcceleration());
            }
            zombie.addTranslateY(zombie.getJumpVelocity());
            return;
        }

        if(zombie.getJumpAcceleration() > 0) { //To fix floating point math
            zombie.setJumpAcceleration(0);
        }

        System.out.println(isZombieTouchingFloor);
        if(isZombieTouchingFloor && zombie.isNeedsToJump()) { //Start jump
            zombie.setJumpAcceleration(-2.5);
            zombie.setNeedsToJump(false);
            return;

        }
        zombie.addTranslateY(3); //Falling
    }






}
