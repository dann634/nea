package com.jackson.game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Block extends ImageView {

    private int xPos;
    private int yPos;

    public Block(String key, int xPos, int yPos) {
        String dir = "file:src/main/resources/images/";
        dir += switch (key) {
            case "0" -> "air.png";
            case "1" -> "dirt.png";
            case "2" -> "grass.png";
            case "3" -> "bedrock.png";
            default -> ""; // FIXME: 09/09/2023 May Cause NPE
        };
        setImage(new Image(dir));

        this.xPos = xPos;
        this.yPos = yPos;

        setPreserveRatio(true);
        setFitHeight(32);
        setFitWidth(32);

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
}
