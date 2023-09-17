package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import com.jackson.io.TextIO;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.security.spec.RSAOtherPrimeInfo;
import java.util.ArrayList;
import java.util.List;

public class Camera {

    public static final int RENDER_WIDTH = 17;
    public static final int RENDER_HEIGHT = 9;
    private String[][] map;

    public Camera() {
//        this.map = TextIO.readMapFile(true);
    }

    public void draw(Character character, GameController gameController, String[][] map) {

        int mapCol = character.getXPos() - RENDER_WIDTH;
        int mapRow = character.getYPos() - RENDER_HEIGHT;
        while(mapCol < character.getXPos() + RENDER_WIDTH && mapRow < character.getYPos() + RENDER_HEIGHT) {
            int mapX = mapCol * 32;
            int mapY = mapRow * 32;
            double screenPosX = mapX - (character.getXPos() * 32) + character.getX();
            double screenPosY = mapY - (character.getYPos() * 32) + character.getY();

            if(screenPosX + 32 > 0 &&
            screenPosY + 32 > 0 &&
            screenPosX < 1024 &&
            screenPosY < 544) {
                //Draw
                Block block = new Block(map[mapCol][mapRow], mapCol, mapRow);
                block.setX(screenPosX);
                block.setY(screenPosY);
                gameController.drawBlock(block);
            }
            mapCol++;
            if(mapCol == character.getXPos() + RENDER_WIDTH) {
                mapCol = character.getXPos() - RENDER_WIDTH;
                mapRow++;
            }

        }

    }





}
