package com.jackson.network.connections;

import com.jackson.game.characters.PlayerInterface;
import com.jackson.io.TextIO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PseudoPlayer implements PlayerInterface {
    private ImageView imageView;
    private int xPos;
    private int yPos;
    private int xOffset;
    private int yOffset;
    private final Image image1;
    private final Image image2;
    private Label displayNameLabel;
    private final String displayName;


    public PseudoPlayer(String displayName) {
        this.displayName = displayName;
        image1 = new Image("file:src/main/resources/images/playerRun1.png");
        image2 = new Image("file:src/main/resources/images/playerRun2.png");
        imageView = new ImageView(image1);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(32);
        initDisplayNameLabel();

    }

    private void initDisplayNameLabel() {
        displayNameLabel = new Label(TextIO.readFile("src/main/resources/settings/settings.txt").get(0));
        displayNameLabel.translateXProperty().bind(this.imageView.xProperty().subtract(displayNameLabel.getWidth() / 2));
        displayNameLabel.translateYProperty().bind(this.imageView.yProperty().subtract(15));
        displayNameLabel.setStyle("-fx-font-weight: bold");
    }

    public void translateX(int value) {
        this.imageView.setTranslateX(this.imageView.getTranslateX() + value);
        if(value > 0) {
            this.imageView.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        } else {
            this.imageView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        }
    }

    public void translateY(int value) {
        this.imageView.setTranslateY(this.imageView.getTranslateY() + value);
    }


    @Override
    public void setXPos(int value) {
        xPos = value;
    }

    @Override
    public void setYPos(int value) {
        yPos = value;
    }

    @Override
    public int getXPos() {
        return xPos;
    }

    @Override
    public int getYPos() {
        return yPos;
    }

    public int getxOffset() {
        return xOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public String getDisplayName() {
        return displayName;
    }
}
