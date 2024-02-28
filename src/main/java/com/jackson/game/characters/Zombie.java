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
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Zombie extends Character {

    public static final int SPEED = 1;
    protected final PauseTransition attackCooldown;
    protected final ProgressBar healthBar;
    protected final Difficulty difficulty;
    private int xCounter;
    private double jumpAcceleration;
    private double jumpVelocity;
    private boolean needsToJump;
    //Multiplayer
    private int id;
    private boolean isClientResponsible;

    public Zombie(Difficulty difficulty) {
        super();
        //Movement Values
        this.jumpAcceleration = 0;
        this.jumpVelocity = 0;
        this.needsToJump = false;
        this.xCounter = 30;
        this.difficulty = difficulty;
        this.isClientResponsible = true;

        //Attack Cooldown
        this.attackCooldown = new PauseTransition(Duration.seconds(2));

        //Health Bar
        this.healthBar = initHealthBar();
        //Update Health for Difficulty
        switch (difficulty) {
            case EASY -> health.set(100);
            case MEDIUM -> health.set(120);
            case HARD -> health.set(150);
        }

        //Attack Damage
        BASE_ATTACK_DAMAGE = switch (difficulty) {
            case EASY -> 3;
            case MEDIUM -> 5;
            case HARD -> 8;
        };
        rebindCollisions();
    }

    //Overrides method as zombies don't have an idle image
    @Override
    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/zombieRun1.png"));
    }

    //create health bar and bind to zombie
    private ProgressBar initHealthBar() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(34);
        progressBar.setPrefHeight(8);
        progressBar.translateXProperty().bind(this.translateXProperty().subtract(2));
        progressBar.translateYProperty().bind(this.translateYProperty().subtract(10));
        int normalise = switch(difficulty) {
            case EASY -> 100;
            case MEDIUM -> 120;
            case HARD -> 150;
        };
        progressBar.progressProperty().bind(health.divide(normalise));
        return progressBar;
    }

    //Updates the offsets for the collisions and rebinds it
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

    //Updates the x position of the zombie on screen
    public void addTranslateX(int value) {
        this.setTranslateX(this.getTranslateX() + value);
        //counter to change the image for walking animation
        this.xCounter -= 1;
        if (xCounter <= 0) {
            this.xCounter = 30;
            this.swapMovingImage();
        }
    }


    //Plays attack cooldown so they can't attack multiple times a second
    @Override
    public void attack(Entity item) {
        attackCooldown.play();
    }

    //Update the y position of the zombie on the screen
    public void addTranslateY(double value) {
        this.setTranslateY(this.getTranslateY() + value);
    }

    //Gets list of all nodes required for the zombie
    public List<Node> getNodes() {
        return new ArrayList<>(List.of(this, this.leftCollision, this.rightCollision, this.feetCollision, this.healthBar, this.headCollision));
    }

    //Movement getters and setters
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

    public double JUMPING_POWER() {
        return 1;
    }


    //Attack getter
    public boolean canAttack() {
        return attackCooldown.getStatus() == Animation.Status.STOPPED;
    }

    /*
    Gets attack damage value
    Returns base attack times a multiplier
     */
    public double getAttack() {
        Random rand = new Random();
        //Multiplier between 1 and 2
        double multiplier = rand.nextDouble() + 1;
        return BASE_ATTACK_DAMAGE * multiplier;
    }

    //Multiplayer getters and setters
    public int getGameId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isClientResponsible() {
        return isClientResponsible;
    }

    public void setClientResponsible(boolean clientResponsible) {
        isClientResponsible = clientResponsible;
    }
}
