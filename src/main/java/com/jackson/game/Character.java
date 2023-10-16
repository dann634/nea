package com.jackson.game;

import com.jackson.io.TextIO;
import com.jackson.ui.Camera;
import com.jackson.ui.SettingsController;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class Character extends ImageView {

    private int xPos;
    private int yPos;

    private SimpleBooleanProperty isModelFacingRight;

    private SimpleDoubleProperty health;

    private Rectangle feetCollision;
    private Rectangle headCollision;
    private Rectangle leftCollision;
    private Rectangle rightCollision;
    private Label displayNameLabel;

    public Character() {
        setImage(new Image("file:src/main/resources/images/playerIdle.png"));
        setPreserveRatio(true);
        setFitWidth(32);
        setX(484); //Half Screen size (512) - Character Width (48) + Some Value(22)
        setY(180);

        initFeetCollision();
        initBodyCollision();
        initDisplayNameLabel();

        this.health = new SimpleDoubleProperty(100);


        this.isModelFacingRight = new SimpleBooleanProperty(true);
        this.isModelFacingRight.addListener((observable, oldValue, newValue) -> {
            setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);
        });
    }

    private void initDisplayNameLabel() {
        this.displayNameLabel = new Label(this.xPos + "," + this.yPos); //TextIO.readFile("src/main/resources/settings/settings.txt").get(0)
        this.displayNameLabel.translateXProperty().bind(this.xProperty().subtract(this.displayNameLabel.getWidth() / 2));
        this.displayNameLabel.translateYProperty().bind(this.yProperty().subtract(15));
        this.displayNameLabel.setStyle("-fx-font-weight: bold");
//        this.displayNameLabel.setVisible(false); // not for singleplayer
    }

    public void updateLabel() {
        this.displayNameLabel.setText(this.xPos + "," + this.yPos);
    }


    private void initFeetCollision() {
        this.feetCollision = new Rectangle(25, 6);
        this.feetCollision.xProperty().bind(xProperty().add(4));
        this.feetCollision.yProperty().bind(yProperty().add(42));
        this.feetCollision.setVisible(false);
    }

    private void initBodyCollision() {
        this.leftCollision = new Rectangle(5, 45);
        this.leftCollision.xProperty().bind(xProperty().subtract(1));
        this.leftCollision.yProperty().bind(yProperty());
        this.leftCollision.setVisible(false);

        this.rightCollision = new Rectangle(5, 45);
        this.rightCollision.xProperty().bind(xProperty().add(30));
        this.rightCollision.yProperty().bind(yProperty());
        this.rightCollision.setVisible(false);
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

    public void addXPos(int value) {
        this.xPos += value;
    }

    public void addYPos(int value) {
        this.yPos += value;
    }

    public double getWidth() {
        return 48;
    }

    public double getHeight() {
        return 72 * (32 / getWidth());
    }

    public List<Rectangle> getCollisions() {
        return List.of(this.feetCollision, this.leftCollision, this.rightCollision);
    }

    public Label getDisplayNameLabel() {
        return this.displayNameLabel;
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

    public double getHealth() {
        return health.get();
    }

    public SimpleDoubleProperty healthProperty() {
        return health;
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
}
