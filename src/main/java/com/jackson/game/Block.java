package com.jackson.game;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Block extends ImageView {

    public Block(String key) {
        String dir = "file:src/main/resources/images/";
        dir += switch (key) {
            case "0" -> "air.png";
            case "1" -> "dirt.png";
            case "2" -> "grass.png";
            case "3" -> "bedrock.png";
            default -> ""; // FIXME: 09/09/2023 May Cause NPE#
        };
        setImage(new Image(dir));

        setPreserveRatio(true);
        setFitHeight(32);
        setFitWidth(32);

    }

}
