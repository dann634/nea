package com.jackson.game.characters;

import javafx.scene.Node;
import javafx.scene.image.Image;

import java.util.List;

public class Zombie extends Character {

    public static final int SPEED = 3;
    private double jumpAcceleration;
    private double jumpVelocity;
    private boolean needsToJump;

    public Zombie() {
        super();
        this.jumpAcceleration = 0;
        this.jumpVelocity = 0;
        this.needsToJump = false;
    }

    @Override
    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/zombieRun1.png"));
        rebindCollisions();
    }

    private void rebindCollisions() {
        //Using translate instead because its more optimised
        this.leftCollision.xProperty().bind(this.translateXProperty().subtract(1));
        this.leftCollision.yProperty().bind((this.translateYProperty()));
        this.rightCollision.xProperty().bind(this.translateXProperty().add(29));
        this.rightCollision.yProperty().bind((this.translateYProperty()));
        this.feetCollision.xProperty().bind(this.translateXProperty());
        this.feetCollision.yProperty().bind((this.translateYProperty().add(43)));
    }

    public void addTranslateX(int value) {
        this.setTranslateX(this.getTranslateX() + value);
    }

    public void addTranslateY(double value) {
        this.setTranslateY(this.getTranslateY() + value);
    }

    public List<Node> getNodes() { //More Optimised
        return List.of(this, this.leftCollision, this.rightCollision, this.feetCollision);
    }

    public double getJumpAcceleration() {
        return jumpAcceleration;
    }

    public void setJumpAcceleration(double jumpAcceleration) {
        this.jumpAcceleration = jumpAcceleration;
    }

    public void addJumpAcceleration(double value) {
        this.jumpAcceleration += value;
    }

    public void addJumpVelocity(double value) {
        this.jumpVelocity += value;
    }

    public double getJumpVelocity() {
        return jumpVelocity;
    }

    public void setJumpVelocity(double jumpVelocity) {
        this.jumpVelocity = jumpVelocity;
    }

    public boolean isNeedsToJump() {
        return needsToJump;
    }

    public void setNeedsToJump(boolean needsToJump) {
        this.needsToJump = needsToJump;
    }
}
