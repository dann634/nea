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

    public void calculateYProperties(boolean isWPressed) {
        boolean isCharacterTouchingFloor = camera.isEntityTouchingBlock(character.getFeetCollision()); //Is character touching floor
        if(isCharacterTouchingFloor && !isWPressed && jumpAcceleration >= 0) { //Not jumping and on floor
            return;
        }
        if(jumpAcceleration < 0) { //In Mid air jumping
            jumpAcceleration += 0.15;
            if(jumpVelocity < 3 && jumpVelocity > -3) {
                jumpVelocity += jumpAcceleration;
            }
            if(camera.isEntityTouchingBlock(character.getHeadCollision())) { //Get head collision
                jumpAcceleration = 0;
                jumpVelocity = 0;
            } else {
                doYOffsetStuff((int) -jumpVelocity, false);
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
        doYOffsetStuff(-4, true);

    }

    private boolean doYOffsetStuff(int offset, boolean isCharacterMovingDown) {
        boolean condition = isCharacterMovingDown ? camera.getyOffset() < -32 : camera.getyOffset() > 32; // FIXME: 22/10/2023 problem
        if (condition) {

            int newYPos = isCharacterMovingDown ? 1 : -1;
            int newYOffset = isCharacterMovingDown ? 32 : -32;

            camera.drawHorizontalLine(!isCharacterMovingDown);
            camera.deleteHorizontal(isCharacterMovingDown);
            character.addYPos(newYPos);
            camera.addYOffset(newYOffset);
        }


        camera.translateBlocksByY(offset);
        return condition;
    }



    public void calculateXProperties(boolean isAPressed, boolean isDPressed, Player player) {

        if(!isAPressed && !isDPressed) { //For optimization
            return;
        }

//        walkingEffects.play();

        boolean canMoveLeft = !camera.isEntityTouchingBlock(character.getLeftCollision());
        boolean canMoveRight = !camera.isEntityTouchingBlock(character.getRightCollision());

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


        boolean condition = isCharacterMovingLeft ? camera.getxOffset() > 32 : camera.getxOffset() < -32;

        if (condition) {
            player.addAgilityXP(1);

            //Get block x at edge and add one
            //Will break if blocks is not ordered
            int xLocalOffset = isCharacterMovingLeft ? -RENDER_WIDTH : RENDER_WIDTH;
            int newXPos = isCharacterMovingLeft ? -1 : 1;
            int newXOffset = isCharacterMovingLeft ? -32 : 32;

            camera.addLine(camera.getVerticalLine(xLocalOffset), isCharacterMovingLeft); // FIXME: 30/10/2023 optimise this shit
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
        boolean canMove = !camera.isEntityTouchingBlock(needsToMoveRight ? zombie.getRightCollision() : zombie.getLeftCollision());

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
        boolean isZombieTouchingFloor = camera.isEntityTouchingBlock(zombie.getFeetCollision());
        if(isZombieTouchingFloor && zombie.getJumpAcceleration() >= 0 && !zombie.isNeedsToJump()) {
            //Not jumping and on floor
            return;
        }

        if(zombie.getJumpAcceleration() < 0) { //In Mid air jumping
            zombie.addJumpAcceleration(0.15); //To add the parabola
            if(zombie.getJumpVelocity() < 3 && zombie.getJumpVelocity() > -3) {
                //If smaller than max jump velocity (add more)
                zombie.addJumpVelocity(zombie.getJumpAcceleration());
            }
            if(camera.isEntityTouchingBlock(zombie.getHeadCollision())) {
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
        zombie.addTranslateY(3); //Falling
    }






}
