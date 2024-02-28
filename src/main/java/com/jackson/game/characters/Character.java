package com.jackson.game.characters;

import com.jackson.game.items.Entity;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.List;

public abstract class Character extends ImageView {

    protected final SimpleBooleanProperty isModelFacingRight;
    protected final SimpleDoubleProperty health;
    protected Rectangle feetCollision;
    protected Rectangle headCollision;
    protected Rectangle leftCollision;
    protected Rectangle rightCollision;
    protected int BASE_ATTACK_DAMAGE;

    public Character() {
        setPreserveRatio(true);
        setFitWidth(32);
        health = new SimpleDoubleProperty(100);
        isModelFacingRight = new SimpleBooleanProperty(true);

        initFeetCollision();
        initBodyCollision();
        initHeadCollision();
        setIdleImage();


        isModelFacingRight.addListener((observable, oldValue, newValue) -> setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT));
        BASE_ATTACK_DAMAGE = 30;
    }

    //Create head collision and bind to player
    private void initHeadCollision() {
        headCollision = new Rectangle(25, 6); //Width of 25, Height of 6
        headCollision.xProperty().bind(xProperty().add(3)); //Binds x to player
        headCollision.yProperty().bind(yProperty().subtract(3)); //Binds y to player
        headCollision.setVisible(false); //Hides the rectangle
    }

    //Create feet collision and bind to player
    private void initFeetCollision() {
        feetCollision = new Rectangle(25, 6);
        feetCollision.xProperty().bind(xProperty().add(3));
        feetCollision.yProperty().bind(yProperty().add(42));
        feetCollision.setVisible(false);
    }

    //Create left and right collisions
    private void initBodyCollision() {
        leftCollision = getBodyCollision(0);
        rightCollision = getBodyCollision(30);
    }

    //Create vertical collision and bind according to parameter
    private Rectangle getBodyCollision(int offset) {
        Rectangle rectangle = new Rectangle(5, 45);
        rectangle.xProperty().bind(xProperty().add(offset));
        rectangle.yProperty().bind(yProperty());
        rectangle.setVisible(false);
        return rectangle;
    }

    //Set health from integer
    public void setHealth(int health) {
        this.health.set(health);
    }

    //Set health from double
    public void setHealth(double value) {
        health.set(value);
    }

    //Get a list of all collisions
    public List<Rectangle> getCollisions() {
        return List.of(feetCollision, leftCollision, rightCollision, headCollision);
    }

    //Collision getters
    public Rectangle getFeetCollision() {
        return feetCollision;
    }

    public Rectangle getLeftCollision() {
        return leftCollision;
    }

    public Rectangle getRightCollision() {
        return rightCollision;
    }

    public Rectangle getHeadCollision() {
        return headCollision;
    }

    //get health property
    public SimpleDoubleProperty healthProperty() {
        return health;
    }

    //set idle animation according to inherited class name
    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/" + getClass().getSimpleName() + "Idle.png"));
    }

    //Swap moving image for walking animation according to inherited class name
    public void swapMovingImage() {
        if (getImage().getUrl().contains("1")) {
            setImage(new Image("file:src/main/resources/images/" + getClass().getSimpleName() + "Run2.png"));
            return;
        }
        setImage(new Image("file:src/main/resources/images/" + getClass().getSimpleName() + "Run1.png"));
    }

    //Children must implement this method
    public abstract void attack(Entity item);

    //Makes character take damage and returns true if dead
    public boolean takeDamage(double amount) {
        health.set(health.get() - amount);
        //Die
        return health.get() <= 0;
    }

    //Attack damage getter
    public double getAttackDamage() {
        return BASE_ATTACK_DAMAGE;
    }

}
