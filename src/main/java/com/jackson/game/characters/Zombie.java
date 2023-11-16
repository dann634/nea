package com.jackson.game.characters;

import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;

import java.util.List;

public class Zombie extends Character {

    /*
    Have chance to spawn pack
    use normal distribution again
    change mean on difficulty
     */

    public static final int SPEED = 1;
    private int xCounter;
    private double jumpAcceleration;
    private double jumpVelocity;
    private boolean needsToJump;
    private int attackCooldown; //Measured in cycles of main game loop
    private final ProgressBar healthBar;

    public Zombie() {
        super();
        this.jumpAcceleration = 0;
        this.jumpVelocity = 0;
        this.needsToJump = false;
        this.xCounter = 30;
        this.attackCooldown = 100;
        this.healthBar = initHealthBar();

        this.feetCollision.xProperty().bind(translateXProperty().add(4));

    }

    @Override
    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/zombieRun1.png"));
        rebindCollisions();
    }

    private ProgressBar initHealthBar() {
        ProgressBar progressBar = new ProgressBar(1);
        progressBar.setPrefWidth(34);
        progressBar.setPrefHeight(8);
        progressBar.translateXProperty().bind(this.translateXProperty().subtract(2));
        progressBar.translateYProperty().bind(this.translateYProperty().subtract(10));
        progressBar.progressProperty().bind(health.divide(100));
        return progressBar;
    }

    private void rebindCollisions() {
        //Using translate instead because its more optimised
        this.leftCollision.xProperty().bind(this.translateXProperty().subtract(1));
        this.leftCollision.yProperty().bind((this.translateYProperty()));
        this.rightCollision.xProperty().bind(this.translateXProperty().add(29));
        this.rightCollision.yProperty().bind((this.translateYProperty()));
        this.feetCollision.xProperty().bind(this.translateXProperty());
        this.feetCollision.yProperty().bind((this.translateYProperty().add(43)));
        this.headCollision.xProperty().bind(this.translateXProperty().add(3));
        this.headCollision.yProperty().bind(this.translateYProperty().add(-3));
    }

    public void addTranslateX(int value) {
        this.setTranslateX(this.getTranslateX() + value);
        this.xCounter -= 1;
        if(xCounter <= 0) {
            this.xCounter = 30;
            this.swapMovingImage();
        }
    }

    public void addTranslateY(double value) {
        this.setTranslateY(this.getTranslateY() + value);
    }

    public List<Node> getNodes() { //More Optimised
        return List.of(this, this.leftCollision, this.rightCollision, this.feetCollision, this.healthBar, this.headCollision);
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
