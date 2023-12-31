package com.jackson.game.characters;

import com.jackson.game.Difficulty;
import com.jackson.game.items.Entity;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Zombie extends Character {

    /*
    Have chance to spawn pack
    use normal distribution again
    change mean on difficulty
     */

    public static final int SPEED = 1;
    private int xCounter;
    protected final PauseTransition attackCooldown;
    private double jumpAcceleration;
    private double jumpVelocity;
    private boolean needsToJump;
    protected final ProgressBar healthBar;
    protected Difficulty difficulty;

    public Zombie(Difficulty difficulty) {
        super();
        //Movement Values
        this.jumpAcceleration = 0;
        this.jumpVelocity = 0;
        this.needsToJump = false;
        this.xCounter = 30;
        this.difficulty = difficulty;

        //Attack Cooldown
        this.attackCooldown = new PauseTransition();
        this.attackCooldown.setDuration(Duration.seconds(2));

        //Health Bar
        this.healthBar = initHealthBar();
        //Update Health for Difficulty
        switch(difficulty) {
            case MEDIUM -> health.set(120);
            case HARD -> health.set(150);
        }
        rebindCollisions();
    }

    @Override
    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/zombieRun1.png"));
    }

    private ProgressBar initHealthBar() {
        ProgressBar progressBar = new ProgressBar();
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
        this.feetCollision.xProperty().bind(this.translateXProperty().add(4));
        this.feetCollision.yProperty().bind((this.translateYProperty().add(43)));
        this.headCollision.xProperty().bind(this.translateXProperty().add(3));
        this.headCollision.yProperty().bind(this.translateYProperty().add(-3));
    }

    public void addTranslateX(int value) {
        this.setTranslateX(this.getTranslateX() + value);
        //counter to change the image for walking animation
        this.xCounter -= 1;
        if(xCounter <= 0) {
            this.xCounter = 30;
            this.swapMovingImage();
        }
    }



    @Override
    public void attack(Entity item) {
        attackCooldown.play();
    }

    public void addTranslateY(double value) {
        this.setTranslateY(this.getTranslateY() + value);
    }

    public List<Node> getNodes() { //More Optimised
        return new ArrayList<>(List.of(this, this.leftCollision, this.rightCollision, this.feetCollision, this.healthBar, this.headCollision));
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

    public boolean canAttack() {
        return attackCooldown.getStatus() == Animation.Status.STOPPED;
    }

    public double JUMPING_POWER() {
        return 1;
    }

    public double getAttack() {
        Random rand = new Random();
        //Multiplier between 1 and 2
        double multiplier = rand.nextDouble() + 1;
        switch (difficulty) {
            case MEDIUM -> {
                return 5 * multiplier;
            }
            case HARD -> {
                return 8 * multiplier;
            }
        }
        //Easy
        return 3 * multiplier;
    }
}
