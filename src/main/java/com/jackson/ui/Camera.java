package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;

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
                if((character.getYPos() + RENDER_HEIGHT) - (character.getYPos() - RENDER_HEIGHT) > 18) {
                    System.out.println("AHHH");
                }
                renderArray[blockXIndex][blockYIndex] = new Block(map[i][j], i, j);
                blockYIndex++;
            }
            blockXIndex++;
        }


        return renderArray;
    }

}
