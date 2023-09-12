package com.jackson.game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Character extends ImageView {

    private int xPos;
    private int yPos;

    private Rectangle feetCollision;
    private Rectangle headCollision;
    private Rectangle leftCollision;
    private Rectangle rightCollision;



    public Character() {
        setImage(new Image("file:src/main/resources/images/playerIdle.png"));
        setPreserveRatio(true);
        setFitWidth(32);
        setX(486); //Half Screen size (512) - Character Width (48) + Some Value(22)

        initFeetCollision();


    }

    // TODO: 09/09/2023 add listeners to x and y location to update world

    private void initFeetCollision() {
        this.feetCollision = new Rectangle(24, 6);
        this.feetCollision.xProperty().bind(xProperty().add(6));
        this.feetCollision.yProperty().bind(yProperty().add(48));
        this.feetCollision.setVisible(false);
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
        return List.of(this.feetCollision);
    }

    public Rectangle getFeetCollision() {
        return this.feetCollision;
    }
}
