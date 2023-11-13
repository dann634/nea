package com.jackson.game.characters;

import com.jackson.game.items.Block;
import com.jackson.game.items.Entity;
import com.jackson.io.TextIO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;
import java.util.List;

public class Character extends ImageView {

    protected SimpleBooleanProperty isModelFacingRight;
    protected SimpleDoubleProperty health;
    protected Rectangle feetCollision;
    protected Rectangle headCollision;
    protected Rectangle leftCollision;
    protected Rectangle rightCollision;
    protected ImageView handImageView;
    private int[] currentItemOffsets;

    public Character() {
        setPreserveRatio(true);
        setFitWidth(32);
        this.health = new SimpleDoubleProperty(100);
        this.isModelFacingRight = new SimpleBooleanProperty(true);
        this.currentItemOffsets = new int[]{0, 0, 0};


        initFeetCollision();
        initBodyCollision();
        initHandRectangle();
        initHeadCollision();
        setIdleImage();


        this.isModelFacingRight.addListener((observable, oldValue, newValue) -> {
            setNodeOrientation((newValue) ? NodeOrientation.LEFT_TO_RIGHT : NodeOrientation.RIGHT_TO_LEFT);

            this.handImageView.setTranslateX(newValue ? this.currentItemOffsets[1] : this.currentItemOffsets[0]);
            this.handImageView.setNodeOrientation((newValue) ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
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
        this.handImageView.xProperty().bind(this.xProperty());
        this.handImageView.setScaleY(0.3);
        this.handImageView.setScaleX(0.3);
        this.handImageView.setTranslateX(this.currentItemOffsets[1]);
        this.handImageView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
    }

    public void updateBlockInHand(String itemName) {

        this.handImageView.setImage(new Image("file:src/main/resources/images/" + itemName + ".png"));
        this.handImageView.setVisible(true);
        //Offsets

        this.currentItemOffsets = getOffsets(itemName);
        System.out.println(Arrays.toString(this.currentItemOffsets));
        this.handImageView.setTranslateY(this.currentItemOffsets[2]);
        this.handImageView.setTranslateX(this.isModelFacingRight.get() ? this.currentItemOffsets[1] : this.currentItemOffsets[0]);
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

    private int[] getOffsets(String itemName) {
        List<String> file = TextIO.readFile("src/main/resources/settings/offsets.txt");
        for(String str : file) {
            if(str.contains(itemName)) {
                String[] splitLine = str.split(" ");
                try {
                    return new int[]{Integer.parseInt(splitLine[1]), Integer.parseInt(splitLine[2]), Integer.parseInt(splitLine[3])};
                } catch (NumberFormatException e) {
                    System.err.println("Error: Offsets failed to convert");
                }
            }
        }
        return new int[]{0, 0, 0};
    }



}
