package com.jackson.game.characters;

import com.jackson.game.items.Entity;
import com.jackson.ui.Camera;
import javafx.animation.*;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class Boss extends Zombie {

    private final int HEALTH = 1000;
    private final int DAMAGE = 30;
    private ImageView leftArm;
    private ImageView rightArm;
    private boolean canMove;
    private boolean canJump;
    private final Camera camera;

    private final Image bossImage1 = new Image("file:src/main/resources/images/boss_body1.png");
    private final Image bossImage2 = new Image("file:src/main/resources/images/boss_body2.png");

    public Boss(Camera camera) {
        super();
        this.camera = camera;
        health.set(HEALTH);
        setImage(bossImage1);
        setTranslateX(400);
        setTranslateY(200);
        this.canMove = true;
        this.canJump = true;

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
        rockAttack();
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
        rightArm.setRotate(45);

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

        canJump = false;
        //Point arms to sky
        leftArm.setRotate(90);
        rightArm.setRotate(-90);
        setArms(-30); //Changed y offset

        int animationValue = 1;
        //Start jump
        TranslateTransition jumpAnimation = new TranslateTransition();
        jumpAnimation.setNode(this);
        jumpAnimation.setDuration(Duration.seconds(animationValue));
        jumpAnimation.setToY(-150); //-200

        jumpAnimation.setOnFinished(e -> {
            setTranslateX(484);
            setRotate(180);
            leftArm.setRotate(-90);
            rightArm.setRotate(90);
            setArms(45);
            canJump = true;
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
            camera.makeCrater(this.getTranslateX() + 33, 4, 2); // TODO: 22/12/2023 fix this
        });
        resetAnimation.play();

        //Make crater
        // TODO: 22/12/2023 need to find block that intersects

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


    public ImageView getLeftArm() {
        return leftArm;
    }

    public ImageView getRightArm() {
        return rightArm;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public boolean isCanJump() {
        return canJump;
    }


}
