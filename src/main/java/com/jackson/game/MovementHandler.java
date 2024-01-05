package com.jackson.game;

import com.jackson.game.characters.Boss;
import com.jackson.game.characters.Player;
import com.jackson.game.characters.Zombie;
import com.jackson.game.items.ItemStack;
import com.jackson.ui.Camera;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.NodeOrientation;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.List;

import static com.jackson.ui.Camera.RENDER_WIDTH;
import static java.lang.Math.abs;
import static java.lang.Math.divideExact;

public class MovementHandler {

    private final Player character;
    private double jumpVelocity;
    private double jumpAcceleration;
    private final Camera camera;
    private double oldX;

    public MovementHandler(Player character, Camera camera) {
        this.character = character;
        this.camera = camera;
    }

    public void calculateYProperties(boolean isWPressed) throws IOException {
        boolean isCharacterTouchingFloor = camera.isEntityTouchingBlock(character.getFeetCollision(), true); //Is character touching floor
        if(isCharacterTouchingFloor && !isWPressed && jumpAcceleration >= 0) { //Not jumping and on floor
            return;
        }
        if(jumpAcceleration < 0) { //In Mid air jumping
            jumpAcceleration += 0.15;
            if(jumpVelocity < 3 && jumpVelocity > -3) {
                jumpVelocity += jumpAcceleration;
            }
            if(camera.isEntityTouchingBlock(character.getHeadCollision(), true)) { //Get head collision
                jumpAcceleration = 0;
                jumpVelocity = 0;
            } else {
                updateYOffset((int) -jumpVelocity, false);
            }
            return;
        }

        if(jumpAcceleration > 0) { //To fix floating point math
            jumpAcceleration = 0;
        }

        if(isCharacterTouchingFloor && isWPressed) { //Start jump
            jumpAcceleration = -2.5;
            return;
        }
        updateYOffset(-4, true);

    }

    private void updateYOffset(int offset, boolean isCharacterMovingDown) throws IOException {
        camera.translateBlocksByY(offset); //Move world by a value
        boolean condition = isCharacterMovingDown ? camera.getyOffset() < -32 : camera.getyOffset() > 32;
        if(!condition) return;

        //If y offset has exceeded the height of the block

        int newYPos = isCharacterMovingDown ? 1 : -1;
        int newYOffset = isCharacterMovingDown ? 32 : -32;

        camera.drawHorizontalLine(!isCharacterMovingDown); //Add new line
        camera.deleteHorizontal(isCharacterMovingDown); //Delete opposite line
        character.addYPos(newYPos); //Update y position of player
        camera.addYOffset(newYOffset); //Reset offset of blocks

    }



    public void calculateXProperties(boolean isAPressed, boolean isDPressed, Player player) throws IOException {

        if(!isAPressed && !isDPressed) { //For optimization
            return;
        }

//        walkingEffects.play();

        boolean canMoveLeft = !camera.isEntityTouchingBlock(character.getLeftCollision(), true);
        boolean canMoveRight = !camera.isEntityTouchingBlock(character.getRightCollision(), true);

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
        } else {
            return; //Pressing A and D at the same time
        }

        if((isAPressed && !canMoveLeft) || (isDPressed && !canMoveRight)) {
            return; //Trying to move but stuck
        }


        camera.translateBlocksByX(offset);


        //Condition Changes based on direction
        boolean condition = isCharacterMovingLeft ? camera.getxOffset() > 32 : camera.getxOffset() < -32;
        if (condition) {
            player.addAgilityXP(1); //Gain xp from moving

            //Get block x at edge and add one
            int xLocalOffset = isCharacterMovingLeft ? -RENDER_WIDTH : RENDER_WIDTH;
            int newXPos = isCharacterMovingLeft ? -1 : 1;
            int newXOffset = isCharacterMovingLeft ? -32 : 32;

            //Load new line into world
            camera.addLine(camera.getVerticalLine(xLocalOffset), isCharacterMovingLeft);
            camera.deleteVertical(!isCharacterMovingLeft); //Deletes line on opposite side
            character.addXPos(newXPos); //Updates x pos of character
            camera.addXOffset(newXOffset); //Resets camera offset
        }

        //for walking animation (change this later)
        if(abs(oldX - camera.getxOffset()) > 31) {
            character.swapMovingImage();
            oldX = character.getX();
        } else if(oldX == character.getX()) {
            character.setIdleImage();
        }
    }

    public void calculateDroppedBlockGravity() {
        camera.setBlockJustBroken(false);
        List<ItemStack> droppedBlocks = camera.getDroppedBlocks();
        double[] targetYArr = new double[droppedBlocks.size()];
        for (int i = 0; i < droppedBlocks.size(); i++) {
            targetYArr[i] = camera.getBlockHeightUnderBlock(droppedBlocks.get(i)) - 16;
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

        try {
            for(Zombie zombie : zombies) {
                calculateZombieX(zombie);
                calculateZombieY(zombie);
            }
        } catch (ConcurrentModificationException ignored) {}

    }

    private void calculateZombieX(Zombie zombie) {
        //which way should zombie move
        double difference = character.getX() - zombie.getTranslateX();
        boolean needsToMoveRight = difference > 0;
        zombie.setNodeOrientation(needsToMoveRight ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);

        //Check collision
        boolean canMove = !camera.isEntityTouchingBlock(needsToMoveRight ? zombie.getRightCollision() : zombie.getLeftCollision(), false);

        //Move
        if(canMove) {
            zombie.addTranslateX((needsToMoveRight) ? Zombie.SPEED : -Zombie.SPEED);
        }

        //Needs to go in a direction but a wall is blocking
        if(!canMove && zombie.getJumpAcceleration() >= 0) {
            //Jump to get over block
            zombie.setNeedsToJump(true);
        }
    }

    private void calculateZombieY(Zombie zombie) {
        boolean isZombieTouchingFloor = camera.isEntityTouchingBlock(zombie.getFeetCollision(), false);
        if(isZombieTouchingFloor && zombie.getJumpAcceleration() >= 0 && !zombie.isNeedsToJump()) {
            //Not jumping and on floor
            return;
        }

        //For boss
        if(zombie instanceof Boss && !((Boss) zombie).isCanJump()) {
            return;
        }

        if(zombie.getJumpAcceleration() < 0) { //In Mid air jumping
            zombie.addJumpAcceleration(0.15); //To add the parabola
            if(zombie.getJumpVelocity() < 3 && zombie.getJumpVelocity() > -3) {
                //If smaller than max jump velocity (add more)
                zombie.addJumpVelocity(zombie.getJumpAcceleration());
            }
            if(camera.isEntityTouchingBlock(zombie.getHeadCollision(), false)) {
                //Hitting head on block above
                //Sets jump values so it falls
                zombie.setJumpAcceleration(0);
                zombie.setJumpVelocity(0);
            } else {
                //Keep climbing
                zombie.addTranslateY(zombie.getJumpVelocity());
            }
            return;
        }

        if(zombie.getJumpAcceleration() > 0) { //To stop falling
            zombie.setJumpAcceleration(0);
        }


        if(isZombieTouchingFloor && zombie.isNeedsToJump()) { //Start jump and maybe acceleration == 0
            zombie.setJumpAcceleration(-2.5 * zombie.JUMPING_POWER());
            zombie.setNeedsToJump(false);
            return;

        }
        zombie.addTranslateY(3 * zombie.JUMPING_POWER()); //Falling
    }






}
