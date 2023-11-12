package com.jackson.game.characters;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Character extends ImageView {

    protected SimpleBooleanProperty isModelFacingRight;
    protected SimpleDoubleProperty health;
    protected Rectangle feetCollision;
    protected Rectangle headCollision;
    protected Rectangle leftCollision;
    protected Rectangle rightCollision;
    protected ImageView handImageView;

    public Character() {
        setPreserveRatio(true);
        setFitWidth(32);
        this.health = new SimpleDoubleProperty(100);
        this.isModelFacingRight = new SimpleBooleanProperty(true);

        initFeetCollision();
        initBodyCollision();
        initHandRectangle();
        initHeadCollision();
        setIdleImage();

        this.isModelFacingRight.addListener((observable, oldValue, newValue) -> {
            setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);
            updateHandPosition(newValue ? 9 : -25, newValue);
        });

    }

    private void initHeadCollision() {
        this.headCollision = new Rectangle(25, 6);
        this.headCollision.xProperty().bind(xProperty().add(3));
        this.headCollision.yProperty().bind(yProperty().subtract(3));
        this.headCollision.setVisible(false);
    }

    private void initFeetCollision() {
        this.feetCollision = new Rectangle(25, 6);
        this.feetCollision.xProperty().bind(xProperty().add(3));
        this.feetCollision.yProperty().bind(yProperty().add(42));
        this.feetCollision.setVisible(false);
    }

    private void initBodyCollision() {
        this.leftCollision = getBodyCollision(0);
        this.rightCollision = getBodyCollision(30);
    }

    protected void initHandRectangle() {
        this.handImageView = new ImageView();
        this.handImageView.yProperty().bind(this.yProperty().add(5));
        this.handImageView.setScaleY(0.3);
        this.handImageView.setScaleX(0.3);
        updateHandPosition(9, true);
    }

    protected void updateHandPosition(int xOffset, boolean isFacingRight) {
        this.handImageView.xProperty().bind(this.xProperty().add(xOffset));
        this.handImageView.setNodeOrientation(isFacingRight ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
    }

    public void updateBlockInHand(String blockName) {
        this.handImageView.setImage(new Image("file:src/main/resources/images/" + blockName + ".png"));
        if(blockName.equals("fist")) {
            this.handImageView.setVisible(false);
            return;
        }
        this.handImageView.setVisible(true);
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
        if(this.health.get() + value > 100 || this.health.get() + value < 0) {
            return;
        }
        this.health.set(this.health.get() + value);
    }


    public List<Rectangle> getCollisions() {
        return List.of(this.feetCollision, this.leftCollision, this.rightCollision, this.headCollision);
    }

    public Rectangle getFeetCollision() {
        return this.feetCollision;
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


}
