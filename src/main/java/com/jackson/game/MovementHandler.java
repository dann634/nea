package com.jackson.game;

import com.jackson.game.characters.Boss;
import com.jackson.game.characters.Player;
import com.jackson.game.characters.Zombie;
import com.jackson.game.items.ItemStack;
import com.jackson.network.connections.Client;
import com.jackson.ui.Camera;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.NodeOrientation;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.List;

import static com.jackson.ui.Camera.RENDER_WIDTH;
import static java.lang.Math.abs;

public class MovementHandler {

    private final int MAX_Y_SPEED = 3;
    private final Player character;
    private final Camera camera;
    private final Client client;
    private double jumpVelocity;
    private double jumpAcceleration;
    private double oldX;

    public MovementHandler(Player character, Camera camera, Client client) {
        this.character = character;
        this.camera = camera;
        this.client = client;
    }

    /*
    Manages the players Y position
    Decides if player needs to go up or down
    Or if on floor stay still
    If jumping manages parabolic arc
     */
    public void calculateYProperties(boolean isWPressed) throws IOException {
        //Is character touching floor
        boolean isCharacterTouchingFloor = camera.isEntityTouchingBlock(
                character.getFeetCollision(), true);

        if (isCharacterTouchingFloor && !isWPressed && jumpAcceleration >= 0) { //Not jumping and on floor
            return;
        }

        if (jumpAcceleration < 0) { //In Mid air jumping
            jumpAcceleration += 0.15;
            if (jumpVelocity < MAX_Y_SPEED && jumpVelocity > -MAX_Y_SPEED) { //Max Speed
                jumpVelocity += jumpAcceleration;
            }
            if (camera.isEntityTouchingBlock(character.getHeadCollision(), true)) {
                //Is head touching block
                jumpAcceleration = 0;
                jumpVelocity = 0;
            } else {
                //Moving upward
                updateYOffset((int) -jumpVelocity, false);
            }
            return;
        }

        if (jumpAcceleration > 0) { //To fix floating point math
            jumpAcceleration = 0;
        }

        if (isCharacterTouchingFloor && isWPressed) { //Start jump
            jumpAcceleration = -2.5;
            return;
        }
        updateYOffset(-4, true); //Gravity
    }

    /*
    If new line of blocks is needed
    Update position values
    Add new line of blocks
    delete old line on the other side
     */
    private void updateYOffset(int offset, boolean isCharacterMovingDown) throws IOException {
        camera.translateBlocksByY(offset); //Move world by a value
        boolean condition = isCharacterMovingDown ? camera.getyOffset() < -32 : camera.getyOffset() > 32;
        if (!condition) return;

        //If y offset has exceeded the height of the block

        int newYPos = isCharacterMovingDown ? 1 : -1;
        int newYOffset = isCharacterMovingDown ? 32 : -32;

        camera.drawHorizontalLine(!isCharacterMovingDown); //Add new line
        camera.deleteHorizontal(isCharacterMovingDown); //Delete opposite line
        character.addYPos(newYPos); //Update y position of player
        camera.addYOffset(newYOffset); //Reset offset of blocks

    }

    /*
    Manages X position of player
    Decides if the player can move left or right or if blocked
    If new line of blocks is required they are drawn to the screen
    Old blocks are removed
    Position values are updated
    */
    public void calculateXProperties(boolean isAPressed, boolean isDPressed, Player player) throws IOException {

        if (isAPressed == isDPressed) { //For optimization
            character.setIdleImage();
            return;
        }

        Rectangle collision = isAPressed ? character.getLeftCollision() : character.getRightCollision();
        if (camera.isEntityTouchingBlock(collision, true)) return;

        camera.translateBlocksByX(isAPressed ? 4 : -4);

        //Condition Changes based on direction
        boolean condition = isAPressed ? camera.getxOffset() > 32 : camera.getxOffset() < -32;
        if (condition) {
            player.addAgilityXP(1); //Gain xp from moving

            //Get block x at edge and add one
            int xLocalOffset = isAPressed ? -RENDER_WIDTH : RENDER_WIDTH;
            int newXPos = isAPressed ? -1 : 1;
            int newXOffset = isAPressed ? -32 : 32;

            //Load new line into world
            camera.addLine(camera.getVerticalLine(xLocalOffset), isAPressed);
            camera.deleteVertical(!isAPressed); //Deletes line on opposite side
            character.addXPos(newXPos); //Updates x pos of character
            camera.addXOffset(newXOffset); //Resets camera offset
        }

        //for walking animation
        oldX -= 4;
        if (oldX < 0) {
            character.swapMovingImage();
            oldX = 8;
        }
    }

    /*
    Makes dropped blocks fall to nearest solid block directly beneath it
    It finds the y screen position and goes to it using a timeline
     */
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
            for (int i = 0; i < droppedBlocks.size(); i++) {
                try {
                    if (targetYArr[i] >= droppedBlocks.get(i).getY()) {
                        droppedBlocks.get(i).addPos(0, 1);
                        canStop = false;
                    }
                } catch (IndexOutOfBoundsException ignored) {
                } //If blocks break too fast this happens
            }
            if (canStop) {
                fallingTimeline.stop();
            }
        });
        fallingTimeline.setCycleCount(Animation.INDEFINITE);
        fallingTimeline.getKeyFrames().add(fallingKeyFrame);
        fallingTimeline.play();
    }

    /*
    Iterates through all zombies
    Calls methods for x and y movement
     */
    public void calculateZombieMovement(List<Zombie> zombies) {
        try {
            for (Zombie zombie : zombies) {
                calculateZombieX(zombie);
                calculateZombieY(zombie);
            }
        } catch (ConcurrentModificationException | IOException ignored) {
        }
    }

    /*
    Updates the zombies x value
    Moves towards player and updates imageview orientation
    Checks zombies collisions and if it needs to jump
     */
    private void calculateZombieX(Zombie zombie) throws IOException {
        //which way should zombie move

        if (!zombie.isClientResponsible()) return;

        double difference = character.getX() - zombie.getTranslateX();
        boolean needsToMoveRight = difference > 0;
        zombie.setNodeOrientation(needsToMoveRight ? //Point image left or right
                NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);

        //Check collision
        boolean canMove = !camera.isEntityTouchingBlock(
                needsToMoveRight ? zombie.getRightCollision() : zombie.getLeftCollision(), false);

        //Move
        if (canMove) {
            zombie.addTranslateX((needsToMoveRight) ? Zombie.SPEED : -Zombie.SPEED);
            if (client != null) client.updateZombiePos
                    (zombie.getGameId(), (needsToMoveRight) ? Zombie.SPEED : -Zombie.SPEED, 0);
        }

        //Needs to go in a direction but a wall is blocking
        if (!canMove && zombie.getJumpAcceleration() >= 0) {
            //Jump to get over block
            zombie.setNeedsToJump(true);
        }
    }

    /*
    Updates zombies y position
    If on floor then stay there
    If jumping update the acceleration and velocity to arc
    When jump velocity is close to 0, set to 0
     */
    private void calculateZombieY(Zombie zombie) throws IOException {
        if (!zombie.isClientResponsible()) return;

        boolean isZombieTouchingFloor = camera.isEntityTouchingBlock(
                zombie.getFeetCollision(), false);
        if (isZombieTouchingFloor && zombie.getJumpAcceleration() >= 0 && !zombie.isNeedsToJump()) {
            //Not jumping and on floor
            return;
        }

        //For boss
        if (zombie instanceof Boss && !((Boss) zombie).isCanJump()) {
            return;
        }

        if (zombie.getJumpAcceleration() < 0) { //In Mid air jumping
            zombie.addJumpAcceleration(0.15); //To add the parabola
            if (zombie.getJumpVelocity() < 3 && zombie.getJumpVelocity() > -3) {
                //If smaller than max jump velocity (add more)
                zombie.addJumpVelocity(zombie.getJumpAcceleration());
            }
            if (camera.isEntityTouchingBlock(zombie.getHeadCollision(), false)) {
                //Hitting head on block above
                //Sets jump values so it falls
                zombie.setJumpAcceleration(0);
                zombie.setJumpVelocity(0);
            } else {
                //Keep climbing
                zombie.addTranslateY(zombie.getJumpVelocity());
                if (client != null) client.updateZombiePos(zombie.getGameId(), 0, zombie.getJumpVelocity());
            }
            return;
        }

        if (zombie.getJumpAcceleration() > 0) { //To stop falling
            zombie.setJumpAcceleration(0);
        }

        if (isZombieTouchingFloor && zombie.isNeedsToJump()) { //Start jump
            zombie.setJumpAcceleration(-2.5 * zombie.JUMPING_POWER());
            zombie.setNeedsToJump(false);
            return;

        }
        zombie.addTranslateY(3 * zombie.JUMPING_POWER()); //Falling
        if (client != null) client.updateZombiePos(zombie.getGameId(), 0, 3 * zombie.JUMPING_POWER());
    }
}
