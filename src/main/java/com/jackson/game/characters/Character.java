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

    private void initHeadCollision() {
        headCollision = new Rectangle(25, 6); //Width of 25, Height of 6
        headCollision.xProperty().bind(xProperty().add(3)); //Binds x to player
        headCollision.yProperty().bind(yProperty().subtract(3)); //Binds y to player
        headCollision.setVisible(false); //Hides the rectangle
    }

    private void initFeetCollision() {
        feetCollision = new Rectangle(25, 6);
        feetCollision.xProperty().bind(xProperty().add(3));
        feetCollision.yProperty().bind(yProperty().add(42));
        feetCollision.setVisible(false);
    }

    private void initBodyCollision() {
        leftCollision = getBodyCollision(0);
        rightCollision = getBodyCollision(30);
    }





    private Rectangle getBodyCollision(int offset) {
        Rectangle rectangle = new Rectangle(5, 45);
        rectangle.xProperty().bind(xProperty().add(offset));
        rectangle.yProperty().bind(yProperty());
        rectangle.setVisible(false);
        return rectangle;
    }

    public void setHealth(int health) {
        this.health.set(health);
    }

    public void setHealth(double value) {
        health.set(value);
    }

    public List<Rectangle> getCollisions() {
        return List.of(feetCollision, leftCollision, rightCollision, headCollision);
    }

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

    public SimpleDoubleProperty healthProperty() {
        return health;
    }

    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/" + getClass().getSimpleName() + "Idle.png"));
    }

    public void swapMovingImage() {
        if(getImage().getUrl().contains("1")) {
            setImage(new Image("file:src/main/resources/images/" + getClass().getSimpleName() + "Run2.png"));
            return;
        }
        setImage(new Image("file:src/main/resources/images/" + getClass().getSimpleName() + "Run1.png"));
    }



    public abstract void attack(Entity item);

    public boolean takeDamage(double amount) { //Returns true if dead
        health.set(health.get() - amount);
        //Die
        return health.get() <= 0;
    }


    public boolean isIsModelFacingRight() {
        return isModelFacingRight.get();
    }

    public double getAttackDamage() {
        return BASE_ATTACK_DAMAGE;
    }

}
