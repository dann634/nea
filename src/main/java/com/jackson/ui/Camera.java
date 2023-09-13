package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import javafx.animation.Timeline;

import java.util.List;

public class Camera {

    private final int RENDER_WIDTH = 17;
    private final int RENDER_HEIGHT = 9;

    public Block[][] getRenderBlocks(String[][] map, Character character) { //Render 34 wide and 19 high
        //Get blocks around character
        Block[][] renderArray = new Block[RENDER_WIDTH * 2][RENDER_HEIGHT * 2];
        int blockXIndex = 0;
        int blockYIndex = 0;

        for (int i = character.getXPos() - RENDER_WIDTH; i < character.getXPos() + RENDER_WIDTH; i++) {
            blockYIndex = 0;
            for (int j = character.getYPos() - RENDER_HEIGHT; j < character.getYPos() + RENDER_HEIGHT; j++) {
                renderArray[blockXIndex][blockYIndex] = new Block(map[i][j], i, j);
                blockYIndex++;
            }
            blockXIndex++;
        }


        return renderArray;
    }

    public void checkForEdgeOfScreen(Character character, GameController gameController) {
        if(character.getX() < 100 || character.getX() > 924) {
            List<Block> blocksTouchingPlayer = gameController.getBlocksTouchingPlayer(character);
            //Just take first
            character.setXPos(blocksTouchingPlayer.get(0).getXPos());
            character.setYPos(blocksTouchingPlayer.get(0).getYPos());
            System.out.printf("%s %s \n", character.getXPos(), character.getYPos());


        }

    }

    public Timeline getPanningTimeline() {

    }

}
