package com.jackson.game.characters;

import com.jackson.game.items.Entity;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.List;

public class Boss extends Zombie {

    private final int HEALTH = 1000;
    private final int DAMAGE = 30;
    private ImageView leftArm;
    private ImageView rightArm;
    private boolean isDoingAttack;

    private final Image bossImage1 = new Image("file:src/main/resources/images/boss_body1.png");
    private final Image bossImage2 = new Image("file:src/main/resources/images/boss_body2.png");

    public Boss() {
        super();
        health.set(HEALTH);
        setImage(bossImage1);
        setTranslateX(400);
        setTranslateY(200);
        this.isDoingAttack = false;

        attackCooldown.setDuration(Duration.seconds(5));

        setFitWidth(66);
        initArms();
        changeCollisions();
        
    }

    @Override
    public void setIdleImage() {
        setImage(bossImage1);
    }

    @Override
    public void attack(Entity item) {
        super.attack(item);
        jumpAttack();
    }

    @Override
    public void swapMovingImage() {
        if(getImage() == bossImage1) {
            setImage(bossImage2);
        } else {
            setImage(bossImage1);
        }
    }

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

        setArms(15);

    }

    @Override
    public List<Node> getNodes() {
        List<Node> nodes = super.getNodes();
        nodes.add(leftArm);
        nodes.add(rightArm);
        return nodes;
    }

    @Override
    public double JUMPING_POWER() {
        return 1.5;
    }

    private void jumpAttack() {
        /*
        Jumps to sky (offscreen)
        Goes to player location
        Falls
         */
        isDoingAttack = true;
        //Point arms to sky
        leftArm.setRotate(90);
        rightArm.setRotate(-90);
        setArms(-30); //Changed y offset

        int animationValue = 1;
        //Start jump
        TranslateTransition jumpAnimation = new TranslateTransition();
        jumpAnimation.setNode(this);
        jumpAnimation.setDuration(Duration.seconds(animationValue));
        jumpAnimation.setToY(-200);

        jumpAnimation.setOnFinished(e -> {
            setTranslateX(484);
            setRotate(180);
            leftArm.setRotate(-90);
            rightArm.setRotate(90);
            setArms(45);
            isDoingAttack = false;
        });
        jumpAnimation.play();

        PauseTransition resetAnimation = new PauseTransition();
        resetAnimation.setDelay(Duration.seconds(animationValue));
        resetAnimation.setDuration(Duration.seconds(2));
        resetAnimation.setOnFinished(e -> {
            setRotate(0);
            leftArm.setRotate(-45);
            rightArm.setRotate(45);
            setArms(15);
        });
        resetAnimation.play();

        //Make crator

    }

    private void setArms(int yOffset) {
        int shoulderOffset = 30;
        leftArm.translateYProperty().bind(translateYProperty().add(shoulderOffset + yOffset));
        rightArm.translateYProperty().bind(translateYProperty().add(shoulderOffset + yOffset));
    }

    private void rockAttack() {

    }

    public boolean isDoingAttack() {
        return isDoingAttack;
    }

    public ImageView getLeftArm() {
        return leftArm;
    }

    public ImageView getRightArm() {
        return rightArm;
    }
}
