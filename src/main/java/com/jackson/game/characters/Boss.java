package com.jackson.game.characters;

import com.jackson.game.Difficulty;
import com.jackson.game.items.Entity;
import com.jackson.ui.Camera;
import javafx.animation.*;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Boss extends Zombie {

    private final int HEALTH = 1000;
    private final int DAMAGE = 30;
    private ImageView leftArm;
    private ImageView rightArm;
    private boolean canJump;
    private final Camera camera;
    private final Player player;
    private final Image bossImage1 = new Image("file:src/main/resources/images/boss_body1.png");
    private final Image bossImage2 = new Image("file:src/main/resources/images/boss_body2.png");

    public Boss(Camera camera, Player player, double spawnX, double spawnY, Difficulty difficulty) {
        super(difficulty); //Initialises fields in zombie class
        //Initialises fields
        this.camera = camera;
        this.player = player;

        //Sets size, location, health
        setTranslateX(spawnX);
        setTranslateY(spawnY);
        setFitWidth(66);
        setImage(bossImage1);
        health.set(HEALTH);

        this.canJump = true; //Used during jump ability
        attackCooldown.setDuration(Duration.seconds(5)); //5 Second cooldown between attacks

        initArms(); //Sets up the arms and their offsets
        changeCollisions(); //Updates collisions for larger model
    }

    @Override
    public void setIdleImage() {
        setImage(bossImage1);
    }

    @Override
    public void attack(Entity item) {
        super.attack(item); //Plays the attack cooldown
        //50% chance for either attack
        if(new Random().nextBoolean()) {
            jumpAttack();
        } else {
            rockAttack();
        }
    }

    @Override
    public void swapMovingImage() {
        //Swaps image to create the walking animation
        if(getImage() == bossImage1) {
            setImage(bossImage2);
        } else {
            setImage(bossImage1);
        }
    }

    @Override
    public List<Node> getNodes() {
        //Gets all previous nodes from zombie class and adds arms
        List<Node> nodes = super.getNodes();
        nodes.add(leftArm);
        nodes.add(rightArm);
        return nodes;
    }

    @Override
    public double JUMPING_POWER() {
        return 1.5;
    } //Boss can jump higher than regular zombies

    private void changeCollisions() {
        int collisionHeight = 85;
        int topOffset = 5;
        int collisionLength = 55;

        //Feet
        feetCollision.yProperty().bind(translateYProperty().add(92));
        feetCollision.setWidth(collisionLength);

        //Head
        headCollision.setWidth(collisionLength);
        healthBar.setPrefWidth(collisionLength);
        healthBar.translateXProperty().bind(translateXProperty().add(4));
        healthBar.translateYProperty().bind(translateYProperty().subtract(15));
        healthBar.progressProperty().bind(healthProperty().divide(HEALTH));

        //Left collision
        leftCollision.setHeight(collisionHeight);
        leftCollision.yProperty().bind(translateYProperty().add(topOffset));

        //Right collision
        rightCollision.xProperty().bind(translateXProperty().add(62));
        rightCollision.yProperty().bind(translateYProperty().add(topOffset));
        rightCollision.setHeight(collisionHeight);
    }

    private void initArms() {
        Image arm = new Image("file:src/main/resources/images/boss_arm.png");

        leftArm = new ImageView(arm);
        leftArm.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        leftArm.translateXProperty().bind(translateXProperty().subtract(35));
        leftArm.setRotate(-45);

        rightArm = new ImageView(arm);
        rightArm.translateXProperty().bind(translateXProperty().add(40));
        rightArm.setRotate(45);

        setArms(15);

    }



    private void jumpAttack() {
        canJump = false; //Stops normal gravity
        //Point arms to sky
        leftArm.setRotate(90);
        rightArm.setRotate(-90);
        setArms(-30); //Changed y offset

        int animationLength = 1;
        //Jump from floor to offscreen
        TranslateTransition jumpAnimation = new TranslateTransition();
        jumpAnimation.setNode(this);
        jumpAnimation.setDuration(Duration.seconds(animationLength));
        jumpAnimation.setToY(-150);
        jumpAnimation.setOnFinished(e -> {
            //Move to player location
            setTranslateX(484);
            setRotate(180);
            //Flip over for dive
            leftArm.setRotate(-90);
            rightArm.setRotate(90);
            setArms(45);
            canJump = true;
        });

        PauseTransition resetAnimation = new PauseTransition();
        resetAnimation.setDelay(Duration.seconds(animationLength));
        resetAnimation.setDuration(Duration.seconds(2));
        resetAnimation.setOnFinished(e -> {
            //Reset rotation
            setRotate(0);
            leftArm.setRotate(-45);
            rightArm.setRotate(45);
            setArms(15);
            //Make crater on impact point
            try {
                camera.makeCrater(this.getTranslateX() + 33, this.getTranslateY(), 5, 2);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            //Player takes damage
            if(player.intersects(getBoundsInParent())) {
                player.takeDamage(DAMAGE);
            }
        });
        //Play animations
        jumpAnimation.play();
        resetAnimation.play();
    }

    private void setArms(int yOffset) {
        int shoulderOffset = 30;
        leftArm.translateYProperty().bind(translateYProperty().add(shoulderOffset + yOffset));
        rightArm.translateYProperty().bind(translateYProperty().add(shoulderOffset + yOffset));
    }

    private void rockAttack() {
        camera.moveRock(getTranslateX() + 10, getTranslateY() + 20, getTranslateX() + (isModelFacingRight.get() ? 30 : -30), -100);
        camera.setIsRainingRocks(true);
    }



    public boolean isCanJump() {
        return canJump;
    }


}
