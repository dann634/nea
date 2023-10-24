package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Camera {

    public static final int RENDER_WIDTH = 18;
    public static final int RENDER_HEIGHT = 10;
    private final Character character;
    private final String[][] map;
    private final AnchorPane root;
    private boolean blockJustBroken;
    private final List<List<Block>> blocks;
    private final List<Block> droppedBlocks;
    private GameController gameController;
    private int xOffset;
    private int yOffset;

    public Camera(Character character, String[][] map, AnchorPane root, GameController gameController) {
        this.character = character;
        this.map = map;
        this.root = root;
        this.blocks = new ArrayList<>();
        this.xOffset = 0;
        this.yOffset = 0;
        this.droppedBlocks = new ArrayList<>();
        this.blockJustBroken = false;
        this.gameController = gameController;
    }

    public List<Block> getVerticalLine(int xLocalOffset) {
        int nextXIndex = this.character.getXPos() + xLocalOffset; //Gets index of line to be loaded
        int blockIndex = 0;
        List<Block> line = new ArrayList<>();
        for (int i = this.character.getYPos() - RENDER_HEIGHT; i < this.character.getYPos() + RENDER_HEIGHT; i++) { //top of screen to bottom
            Block block = new Block(map[nextXIndex][i], nextXIndex, i, this); //takes a string for block type and X pos and Y pos

            block.setTranslateX(512 + (xLocalOffset * 32) + this.xOffset);
            block.setTranslateY((blockIndex - 1) * 32 + this.yOffset);
            line.add(block);
            this.root.getChildren().add(block);

            blockIndex++;
        }

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
            Block block = new Block(this.map[xIndex][newIndex], xIndex, newIndex, this);
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
        for(Block block : this.droppedBlocks) {
            block.setTranslateX(block.getTranslateX() + offset);
        }

    }

    public void translateBlocksByY(int offset) {
        this.yOffset = this.yOffset + offset;
        for (List<Block> blocks : this.blocks) {
            for (Block block : blocks) {
                block.setTranslateY(block.getTranslateY() + offset);
            }
        }
        for(Block block : this.droppedBlocks) {
            block.setTranslateY(block.getTranslateY() + offset);
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
        cleanupEntities();
    }

    public void addYOffset(int value) {
        this.yOffset += value;
        cleanupEntities();
    }

    public void removeBlock(Block remBlock) { //Remove block and replaces with air block
        int[] index = new int[2];
        Block newBlock = new Block("0", -1, -1, this);
        for(int j = 0; j < this.blocks.size(); j++) {
            List<Block> line = this.blocks.get(j);
            for(int i = 0; i < line.size(); i++) {
                Block block = line.get(i);
                if(block == remBlock) {
                    index[0] = j;
                    index[1] = i;
                    newBlock = new Block("0", remBlock.getXPos(), remBlock.getYPos(), this); //Copy
                    newBlock.setTranslateX(block.getTranslateX());
                    newBlock.setTranslateY(block.getTranslateY());
                    break;
                }
            }
        }
        this.droppedBlocks.add(remBlock); //Adds block to dropped blocks list
        this.root.getChildren().add(newBlock); //Adds air block in place to pane
        this.blocks.get(index[0]).set(index[1], newBlock); //Adds air block to blocks
        this.blockJustBroken = true; //Sets flag to true so blocks fall
        this.map[remBlock.getXPos()][remBlock.getYPos()] = "0"; //Update map to an air block
    }

    public boolean isEntityTouchingGround(Character character) { //Can be optimised
        List<Block> blocks = getBlocksTouchingPlayer(character);
        blocks.removeIf(n -> n.getBlockName().equals("air"));
        return !blocks.isEmpty();
    }

    public boolean isEntityTouchingSide(Rectangle collision) {
        List<Block> blocks = getBlockTouchingSide(collision);
        blocks.removeIf(n -> n.getBlockName().equals("air"));
        return !blocks.isEmpty();
    }

    public List<Block> getBlockTouchingSide(Rectangle collision) {
        List<Block> blocks = new ArrayList<>();
        for(List<Block> blockArr : this.blocks) {
            for(Block block : blockArr) {
                if(collision.intersects(block.getBoundsInParent())) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public List<Block> getBlocksTouchingPlayer(Character character) {
        List<Block> blocks = new ArrayList<>();
        for (List<Block> block : this.blocks)
            for (Block value : block) {
                if (character.getFeetCollision().intersects(value.getBoundsInParent())) {
                    blocks.add(value);
                }
            }

        return blocks;
    }

    public double getBlockHeightUnderBlock(Block block) {
        for (int i = 0; i < this.blocks.size() - 1; i++) {
            if(this.blocks.get(i).get(0).getTranslateX() == block.getTranslateX()) { //If on same column
                for (int j = 0; j < this.blocks.get(i).size() - 1; j++) {
                    Block b = this.blocks.get(i).get(j);
                    if(b.getTranslateY() > block.getTranslateY() && !b.getBlockName().equals("air")) {
                        return b.getTranslateY();
                    }
                }
            }
        }
        return -1;
    }

    public List<Block> getDroppedBlocks() {
        return this.droppedBlocks;
    }

    public boolean isBlockJustBroken() {
        return blockJustBroken;
    }

    public void setBlockJustBroken(boolean blockJustBroken) {
        this.blockJustBroken = blockJustBroken;
    }

    public void cleanupEntities() {
        //Despawn stuff off screen
        //If 200 pixels on X and 100 pixels on Y for despawn
        Iterator<Block> droppedBlockIterator = this.droppedBlocks.listIterator();
        while(droppedBlockIterator.hasNext()) {
            Block block = droppedBlockIterator.next();
            if(block.getTranslateX() < -200 || block.getTranslateX() > 1224
                    || block.getTranslateY() < -100 || block.getTranslateY() > 644) {
                this.root.getChildren().remove(block);
                droppedBlockIterator.remove();
            }
        }
    }
}
