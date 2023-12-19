package com.jackson.game.characters;

import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class Boss extends Zombie {

    private final int HEALTH = 1000;
    private final int DAMAGE = 30;
    private ImageView leftArm;
    private ImageView rightArm;
    public Boss() {
        super();
        health.set(HEALTH);
        setImage(new Image("file:src/main/resources/images/boss_body.png"));
        setTranslateX(0);
        setTranslateY(0);



        setFitWidth(66);
        initArms();
        changeCollisions();
    }

    @Override
    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/boss_body.png"));
    }

    @Override
    public void swapMovingImage() {

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
        int shoulderOffset = 30;

        Image arm = new Image("file:src/main/resources/images/boss_arm.png");

        leftArm = new ImageView(arm);
        leftArm.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        leftArm.translateXProperty().bind(translateXProperty().subtract(35));
        leftArm.translateYProperty().bind(translateYProperty().add(shoulderOffset).add(15));
        leftArm.setRotate(-45);

        rightArm = new ImageView(arm);
        rightArm.translateXProperty().bind(translateXProperty().add(40));
        rightArm.translateYProperty().bind(translateYProperty().add(shoulderOffset).add(15));
        rightArm.setRotate(45);
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
}
