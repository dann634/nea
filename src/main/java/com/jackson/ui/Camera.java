package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.Iterator;
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

    public void drawVerticalLine(int xLocalOffset) {
        int nextXIndex = this.character.getXPos() + xLocalOffset;
        int blockIndex = 0;
        List<Block> line = new ArrayList<>();
        for (int i = this.character.getYPos() - RENDER_HEIGHT; i < this.character.getYPos() + RENDER_HEIGHT; i++) {
            Block block = new Block(map[nextXIndex][i], nextXIndex, i);

            block.setTranslateX(512 + (xLocalOffset * 32) + this.xOffset);
            block.setTranslateY((blockIndex - 1) * 32 + this.yOffset);
            line.add(block);
            root.getChildren().add(block);
            blockIndex++;
        }
        if (xLocalOffset == RENDER_WIDTH || xLocalOffset == -RENDER_WIDTH) {
            this.blocks.add((xLocalOffset < 0) ? 0 : this.blocks.size() - 1, line);
            return;
        }
        this.blocks.add(line);

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

    public void deleteVertical(boolean isLeft) { // FIXME: 28/09/2023 probkem i think
        int index = isLeft ? 0 : this.blocks.size() - 1;
        this.root.getChildren().removeAll(this.blocks.get(index));
        this.blocks.remove(index);
    }

    public void deleteHorizontal(boolean isDown) { //It wouldnt delete a clear line (as invisible imageviews didnt exist?)

        if(this.blocks.isEmpty() || this.blocks.get(0).isEmpty()) {
            return;
        }

        for(List<Block> blockList : this.blocks) {
            int index = isDown ? blockList.size() - 1 : 0;
            this.root.getChildren().remove(blockList.get(index));
            blockList.remove(index);
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
