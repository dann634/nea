package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.List;

public class Camera {

    public static final int RENDER_WIDTH = 18;
    public static final int RENDER_HEIGHT = 10;
    private Character character;
    private String[][] map;
    private AnchorPane root;

    private List<List<Block>> blocks;

    private int xOffset;
    private int yOffset;
    private int currentYTranslation;

    public Camera(Character character, String[][] map, AnchorPane root, List<List<Block>> blocks) {
        this.character = character;
        this.map = map;
        this.root = root;
        this.blocks = blocks;
        this.xOffset = 0;
        this.yOffset = 0;
        this.currentYTranslation = 0;
    }

    public void drawVerticalLine(int xLocalOffset) {
        int nextXIndex = this.character.getXPos() + xLocalOffset;
        int blockIndex = 0;
        List<Block> line = new ArrayList<>();
        for (int i = this.character.getYPos() - RENDER_HEIGHT; i < this.character.getYPos() + RENDER_HEIGHT; i++) {
            Block block = new Block(map[nextXIndex][i], nextXIndex, i);
            if(nextXIndex == 516) {
                System.out.println();
            }
            block.setTranslateX(512 + (xLocalOffset * 32) + this.xOffset);
            block.setTranslateY((blockIndex - 1) * 32 + this.yOffset);
            line.add(block);
            root.getChildren().add(block);
            blockIndex++;
        }
        if (xLocalOffset == RENDER_WIDTH || xLocalOffset == -RENDER_WIDTH) { //Maybe breaking eveeyrhing
            if(!this.blocks.isEmpty() && this.blocks.get((xLocalOffset < 0) ? 0 : this.blocks.size() - 1).get(0).getXPos() == nextXIndex) {
                return;
            }
            this.blocks.add((xLocalOffset < 0) ? 0 : this.blocks.size() - 1, line);
            return;
        }

        if(!this.blocks.isEmpty() && this.blocks.get((xLocalOffset < 0) ? 0 : this.blocks.size() - 1).get(0).getXPos() == nextXIndex) {
            return;
        }
        this.blocks.add(line);
    }

    public void printBlocks() {
        for(List<Block> blocks1 : this.blocks) {
            System.out.println(blocks1.get(0).getXPos());
        }
        System.out.println("---");
    }

    public void drawHorizontalLine(int yLocalOffset) {
        int nextIndex = this.character.getYPos() + yLocalOffset;
        int blockIndex = 0;
        List<Block> line = new ArrayList<>();
        for (int i = character.getXPos() - RENDER_WIDTH; i < character.getXPos() + RENDER_WIDTH; i++) {
            Block block = new Block(map[i][nextIndex], i, nextIndex);


            block.setTranslateX((blockIndex - 1) * 32 + this.xOffset);
            block.setTranslateY((((yLocalOffset) * 32)*2) + this.yOffset); // FIXME: 25/09/2023 wrong y
            line.add(block);
            root.getChildren().add(block);
            blockIndex++;
        }
        for (int i = 0; i < line.size(); i++) { //only works down
            this.blocks.get(i).add(line.get(i));
        }
    }

    public void deleteVertical(boolean isLeft) {
        System.out.println("----");
        System.out.println("Before");
        this.blocks.forEach(n -> {
            System.out.println(n.get(0).getXPos());
        });

        int index = isLeft ? 0 : this.blocks.size() - 1;
        this.root.getChildren().removeAll(this.blocks.get(index));
        this.blocks.remove(index);

        System.out.println("After");
        this.blocks.forEach(n -> {
            System.out.println(n.get(0).getXPos());
        });
    }

    public void deleteHorizontal(boolean isDown) {

        if(this.blocks.get(0).isEmpty()) {
            return;
        }

        int index = isDown ? 0 : this.blocks.get(0).size() - 1 ;
        for(List<Block> blocks : this.blocks) {
            this.root.getChildren().remove(blocks.get(index));
            blocks.remove(index);
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
