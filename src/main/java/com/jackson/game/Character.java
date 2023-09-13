package com.jackson.game;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Character extends ImageView {

    private int xPos;
    private int yPos;

    private SimpleBooleanProperty isModelFacingRight;

    private Rectangle feetCollision;
    private Rectangle headCollision;
    private Rectangle bodyCollision;



    public Character() {
        setImage(new Image("file:src/main/resources/images/playerIdle.png"));
        setPreserveRatio(true);
        setFitWidth(32);
        setX(486); //Half Screen size (512) - Character Width (48) + Some Value(22)

        initFeetCollision();
        initBodyCollision();

        xProperty().addListener((observable, oldValue, newValue) -> {

        });

        yProperty().addListener((observable, oldValue, newValue) -> {

        });

        this.isModelFacingRight = new SimpleBooleanProperty(true);
        this.isModelFacingRight.addListener((observable, oldValue, newValue) -> {
            setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);
        });
    }

    // TODO: 09/09/2023 add listeners to x and y location to update world

    private void initFeetCollision() {
        this.feetCollision = new Rectangle(25, 6);
        this.feetCollision.xProperty().bind(xProperty().add(4));
        this.feetCollision.yProperty().bind(yProperty().add(42));
        this.feetCollision.setVisible(false);
    }

    private void initBodyCollision() {
        this.bodyCollision = new Rectangle(35, 45);
        this.bodyCollision.xProperty().bind(xProperty().subtract(2));
        this.bodyCollision.yProperty().bind(yProperty());
        this.bodyCollision.setVisible(false);
    }

    public void swapMovingImage() {
        if(getImage().getUrl().contains("1")) {
            setImage(new Image("file:src/main/resources/images/playerRun2.png"));
            return;
        }
        setImage(new Image("file:src/main/resources/images/playerRun1.png"));

    }

    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/playerIdle.png"));
    }

    public int getXPos() {
        return xPos;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public double getWidth() {
        return 48;
    }

    public List<Rectangle> getCollisions() {
        return List.of(this.feetCollision, this.bodyCollision);
    }

    public Rectangle getFeetCollision() {
        return this.feetCollision;
    }

    public Rectangle getBodyCollision() {
        return this.bodyCollision;
    }

    public void setIsModelFacingRight(boolean isModelFacingRight) {
        this.isModelFacingRight.set(isModelFacingRight);
    }
}
