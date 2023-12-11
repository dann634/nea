package com.jackson.game.characters;

import com.jackson.game.items.Entity;
import com.jackson.io.TextIO;
import com.jackson.ui.GameController;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;
import java.util.List;

public abstract class Character extends ImageView {

    protected SimpleBooleanProperty isModelFacingRight;
    protected SimpleDoubleProperty health;
    protected Rectangle feetCollision;
    protected Rectangle headCollision;
    protected Rectangle leftCollision;
    protected Rectangle rightCollision;
    protected ImageView handImageView;
    protected TranslateTransition attackTranslate;
    private int[] currentItemOffsets;
    private final int BASE_ATTACK_DAMAGE = 30;

    public Character() {
        setPreserveRatio(true);
        setFitWidth(32);
        health = new SimpleDoubleProperty(100);
        isModelFacingRight = new SimpleBooleanProperty(true);
        currentItemOffsets = new int[]{0, 0, 0};

        initFeetCollision();
        initBodyCollision();
        initHandRectangle();
        initHeadCollision();
        setIdleImage();


        isModelFacingRight.addListener((observable, oldValue, newValue) -> {
            setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);
            attackTranslate.stop(); //Fixes Attack and Turn Bug
            attackTranslate.setByX((newValue) ? 20 : -20);
            handImageView.setTranslateX(newValue ? currentItemOffsets[1] : currentItemOffsets[0]);
            handImageView.setNodeOrientation((newValue) ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
            handImageView.setRotate((newValue) ? 45 : -45);
        });

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

    protected void initHandRectangle() {
        handImageView = new ImageView();
        handImageView.yProperty().bind(yProperty().add(5));
        handImageView.xProperty().bind(xProperty());
        handImageView.setRotate(45);
        handImageView.setScaleY(0.3);
        handImageView.setScaleX(0.3);
        handImageView.setTranslateX(currentItemOffsets[1]);
        handImageView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        attackTranslate = new TranslateTransition();
        attackTranslate.setNode(handImageView);
        attackTranslate.setCycleCount(2);
        attackTranslate.setAutoReverse(true);
        attackTranslate.setRate(4);
        attackTranslate.setByX(20);

    }

    public void updateBlockInHand(Entity item) {
        String itemName;
        if(item == null) {
            itemName = "fist";
        } else {
            itemName = item.getItemName();
        }
        handImageView.setImage(new Image("file:src/main/resources/images/" + itemName + ".png"));
        handImageView.setVisible(true);
        //Offsets

        currentItemOffsets = getOffsets(itemName);
        handImageView.setTranslateY(currentItemOffsets[2]);
        handImageView.setTranslateX(isModelFacingRight.get() ? currentItemOffsets[1] : currentItemOffsets[0]);
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

    public void addHealth(int value) {
        if(health.get() + value > 100 || health.get() + value < 0) {
            return;
        }
        health.set(health.get() + value);
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

    private int[] getOffsets(String itemName) {

        if(itemName.contains("axe") || itemName.contains("sword") || itemName.contains("shovel")) {
            return new int[]{-62, -3, -25};
        }

        if(GameController.lookupTable.containsKey(itemName) || itemName.equals("fist")) {
            return new int[]{-25, 9, 0};
        }

        return new int[]{0, 0, 0};
    }

    public abstract void attack(Entity item);

    public boolean takeDamage(double amount) { //Returns true if dead
        health.set(health.get() - amount);
        //Die
        return health.get() <= 0;
    }

    public TranslateTransition getAttackTranslate() {
        return attackTranslate;
    }

    public boolean isIsModelFacingRight() {
        return isModelFacingRight.get();
    }

    public double getAttackDamage() {
        return BASE_ATTACK_DAMAGE;
    }

}
