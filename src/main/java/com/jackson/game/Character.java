package com.jackson.game;

import com.jackson.ui.Camera;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Character extends ImageView {

    private int xPos;
    private int yPos;

    private SimpleBooleanProperty isModelFacingRight;

    private Rectangle feetCollision;
    private Rectangle headCollision;
    private Rectangle leftCollision;
    private Rectangle rightCollision;




    public Character() {
        setImage(new Image("file:src/main/resources/images/playerIdle.png"));
        setPreserveRatio(true);
        setFitWidth(32);
        setX(486); //Half Screen size (512) - Character Width (48) + Some Value(22)
        setY(272 - (getFitWidth() / 2));

        initFeetCollision();
        initBodyCollision();


        this.isModelFacingRight = new SimpleBooleanProperty(true);
        this.isModelFacingRight.addListener((observable, oldValue, newValue) -> {
            setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);
        });
    }

    // TODO: 09/09/2023 add listeners to x and y location to update world

    private void initFeetCollision() {
        this.feetCollision = new Rectangle(25, 6); //Creates rectangle with Width 25 / Height 6
        this.feetCollision.xProperty().bind(xProperty().add(4)); //Binds xProperty to character plus an offset
        this.feetCollision.yProperty().bind(yProperty().add(42)); //Binds yProperty to character plus an offset
        this.feetCollision.setVisible(false); //Sets invisible
    }

    private void initBodyCollision() {
        //Initialises both collisions
        this.leftCollision = initSideCollisionRectangle(-1);
        this.rightCollision = initSideCollisionRectangle(28);
    }

    private Rectangle initSideCollisionRectangle(int offset) {
        Rectangle rectangle = new Rectangle(5, 45); //Creates new rectangle with width 5 / height 45
        rectangle.xProperty().bind(xProperty().add(offset)); //Binds to xProperty plus offset
        rectangle.yProperty().bind(yProperty()); //Binds to yProperty
        rectangle.setVisible(true); //Sets invisible
        rectangle.setFill(Color.RED);
        return rectangle;
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
        return List.of(this.feetCollision, this.leftCollision, this.rightCollision);
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

    public void setIsModelFacingRight(boolean isModelFacingRight) {
        this.isModelFacingRight.set(isModelFacingRight);
    }
}
