package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Camera {

    public static final int RENDER_WIDTH = 18;
    public static final int RENDER_HEIGHT = 10;
    private final Character character;
    private final String[][] map;
    private final AnchorPane root;

    private final List<List<Block>> blocks;

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

    public List<Block> getVerticalLine(int xLocalOffset) {
        int nextXIndex = this.character.getXPos() + xLocalOffset; //Gets index of line to be loaded
        int blockIndex = 0;
        long totalTime = 0;
        List<Block> line = new ArrayList<>();
        for (int i = this.character.getYPos() - RENDER_HEIGHT; i < this.character.getYPos() + RENDER_HEIGHT; i++) { //top of screen to bottom
//            long oldTime = System.nanoTime();
            Block block = new Block(map[nextXIndex][i], nextXIndex, i); //takes a string for block type and X pos and Y pos

            block.setTranslateX(512 + (xLocalOffset * 32) + this.xOffset);
            block.setTranslateY((blockIndex - 1) * 32 + this.yOffset);
            line.add(block);
//            totalTime += (System.nanoTime() - oldTime);
//            long oldTime = System.nanoTime();
            this.root.getChildren().add(block);
//            System.out.println((System.nanoTime() - oldTime) / 1000);

            blockIndex++;
        }
//        System.out.println(totalTime / 1000000 + "ms");

        return line;
    }

    public void deleteVertical(boolean isLeft) {
        int index = isLeft ? 0 : this.blocks.size() - 1;
        this.root.getChildren().removeAll(this.blocks.get(index));
        this.blocks.remove(index);
    }


    public void drawHorizontalLine(boolean isUp) {

        for (List<Block> line : this.blocks) {
            if(line.isEmpty()) {
                continue;
            }
            int newIndex;
            double yTranslate;
            if(isUp) {
                newIndex = line.get(0).getYPos() - 1;
                yTranslate = line.get(0).getTranslateY() - 32;
            } else {
                newIndex = line.get(line.size() - 1).getYPos() + 1;
                yTranslate = line.get(line.size() - 1).getTranslateY() + 32;
            }
            int xIndex = line.get(0).getXPos();
            Block block = new Block(this.map[xIndex][newIndex], xIndex, newIndex);
            block.setTranslateX(line.get(0).getTranslateX());
            block.setTranslateY(yTranslate);
            line.add((isUp) ? 0 : line.size(), block);
            this.root.getChildren().add(block);
        }

    }


    public void deleteHorizontal(boolean isUp) { //It wouldnt delete a clear line (as invisible imageviews didnt exist?)

        if (this.blocks.isEmpty() || this.blocks.get(0).isEmpty()) {
            return;
        }

        for (List<Block> blockList : this.blocks) {
            Block block = blockList.get(isUp ? 0 : blockList.size() - 1);
            blockList.remove(block);
            this.root.getChildren().remove(block);
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
            addLine(getVerticalLine(i));
        }
    }

    public void addLine(List<Block> line) {
        this.blocks.add(line);
    }

    public void addLine(List<Block> line, boolean isLeft) {
        if (isLeft) {
            this.blocks.add(0, line);
            return;
        }
        this.blocks.add(line);

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
