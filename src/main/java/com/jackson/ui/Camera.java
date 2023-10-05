package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class Camera {

    public static final int RENDER_WIDTH = 18;
    public static final int RENDER_HEIGHT = 11;
    private Character character;
    private String[][] map;
    private AnchorPane root;

    private List<List<Block>> blocks;

    private int xOffset;
    private int yOffset;

    public Camera(Character character, String[][] map, AnchorPane root, List<List<Block>> blocks) {
        this.character = character;
        this.map = map;
        this.root = root;
        this.blocks = blocks;
        this.xOffset = 0;
        this.yOffset = 0;
    }

    // TODO: 05/10/2023 Gets vertical line and adds to scene and 2d array
    public void drawVerticalLine(int xLocalOffset) {
        int nextXIndex = this.character.getXPos() + xLocalOffset; //Gets index of line to be loaded
        int blockIndex = 0;
        List<Block> line = new ArrayList<>();
        for (int i = this.character.getYPos() - RENDER_HEIGHT; i < this.character.getYPos() + RENDER_HEIGHT; i++) { //top of screen to bottom
            Block block = new Block(map[nextXIndex][i], nextXIndex, i); //takes a string for block type and X pos and Y pos

            block.setTranslateX(512 + (xLocalOffset * 32) + this.xOffset);
            block.setTranslateY((blockIndex - 1) * 32 + this.yOffset);
            line.add(block);
            this.root.getChildren().add(block);

            blockIndex++;
        }

        //trying to add line into correct place
//        int index = 0;
//        for(List<Block> blockList : this.blocks) {
//            if(blockList.isEmpty()) { //gatekeeping
//                return;
//            }
//            int indexXPos = blockList.get(0).getXPos();
//            int lineXPos = line.get(0).getXPos();
//
//            if(indexXPos == lineXPos - 1) {
//                this.blocks.add(index + 1, line);
//                this.root.getChildren().addAll(line);
//                return;
//            }
//
//            index++;
//
//        }

        // TODO: 05/10/2023 adding line to correct side of 2d arraylist
        if (xLocalOffset == RENDER_WIDTH || xLocalOffset == -RENDER_WIDTH) {
            this.blocks.add((xLocalOffset < 0) ? 0 : this.blocks.size() - 1, line);
            return;
        }
        this.blocks.add(line); //for initialising the world (xLocalOffset wont be either)
    }

    // TODO: 05/10/2023 this is where the bug happens
    //2d array isnt ordered correctly
    public void deleteVertical(boolean isLeft) {
        int index = isLeft ? 0 : this.blocks.size() - 1;
        this.root.getChildren().removeAll(this.blocks.get(index));
        this.blocks.remove(index);
    }


    public void drawHorizontalLine(int nextYIndex) {
        int blockIndex = 0;
        List<Block> line = new ArrayList<>();

        int nextYOffset = this.character.getYPos() + nextYIndex;

        for (int i = character.getXPos() - RENDER_WIDTH; i < character.getXPos() + RENDER_WIDTH; i++) {
            Block block = new Block(map[i][nextYOffset], i, nextYOffset);


            block.setTranslateX((blockIndex - 1) * 32 + this.xOffset);
            block.setTranslateY(((nextYOffset) * 64) + this.yOffset);
            line.add(block);
            root.getChildren().add(block);
            blockIndex++;
        }
        for (int i = 0; i < line.size(); i++) { //only works down
            this.blocks.get(i).add(line.get(i));
        }
    }



    public void deleteHorizontal(int yOffset) { //It wouldnt delete a clear line (as invisible imageviews didnt exist?)

        if(this.blocks.isEmpty() || this.blocks.get(0).isEmpty()) {
            return;
        }

        for(List<Block> blockList : this.blocks) {
            Block removeBlock = new Block("0", -1, -1);
            for(Block block : blockList) {
                if(block.getYPos() == this.character.getYPos() + yOffset) {
                    removeBlock = block;
                }
            }
            blockList.remove(removeBlock);
        }



    }


    public void translateBlocksByX(int offset) {
        this.xOffset += offset;
        for (List<Block> blocks : this.blocks) {
            for (Block block : blocks) {
                block.setTranslateX(block.getTranslateX() + offset);
            }
        }
    }

    public void translateBlocksByY(int offset) {
        this.yOffset = this.yOffset + offset;
        for (List<Block> blocks : this.blocks) {
            for (Block block : blocks) {
                block.setTranslateY(block.getTranslateY() + offset); // FIXME: 26/09/2023 maybe blocks added after translation
            }
        }
    }

    public void initWorld() {
        for (int i = -RENDER_WIDTH; i < RENDER_WIDTH; i++) { //Init world
            drawVerticalLine(i);
        }
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }


    public void addXOffset(int value) {
        this.xOffset += value;
    }

    public void addYOffset(int value) {
        this.yOffset += value;
    }
}
