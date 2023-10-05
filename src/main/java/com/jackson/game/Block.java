package com.jackson.game;

import com.jackson.ui.GameController;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class Block extends ImageView {

    private int xPos;
    private int yPos;

    public Block(String key, int xPos, int yPos) {
        String dir = "file:src/main/resources/images/";
        dir += switch (key) {
            case "0" -> "air1.png";
            case "1" -> "dirt.png";
            case "2" -> "grass.png";
            case "3" -> "bedrock.png";
            default -> "";
        };
        setImage(new Image(dir));

//        if(key.equals("0")) {
//            setOpacity(1);
//        }

        this.xPos = xPos;
        this.yPos = yPos;

        setOnMouseClicked(e -> {
            System.out.printf("%s %s", this.xPos, this.yPos);
            System.out.println();
        });


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
