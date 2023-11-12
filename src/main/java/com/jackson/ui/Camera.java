package com.jackson.ui;

import com.jackson.game.characters.Character;
import com.jackson.game.characters.Zombie;
import com.jackson.game.items.Block;
import com.jackson.game.characters.Player;
import com.jackson.game.items.Entity;
import com.jackson.game.items.ItemStack;
import com.jackson.ui.hud.Inventory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Camera {

    public static final int RENDER_WIDTH = 18;
    public static final int RENDER_HEIGHT = 10;
    private final Player character;
    private final String[][] map;
    private final AnchorPane root;
    private boolean blockJustBroken;
    private final List<List<Block>> blocks;
    private final List<ItemStack> droppedBlocks;
    private final List<Zombie> zombies;
    private final GameController gameController;
    private final Inventory inventory;
    private Block blockUnderCursor;
    private int xOffset;
    private int yOffset;

    public Camera(Player character, String[][] map, AnchorPane root, GameController gameController, Inventory inventory, List<Zombie> zombies) {
        this.character = character;
        this.zombies = zombies;
        this.map = map;
        this.root = root;
        this.blocks = new ArrayList<>();
        this.xOffset = 0;
        this.yOffset = 0;
        this.droppedBlocks = new ArrayList<>();
        this.blockJustBroken = false;
        this.gameController = gameController;
        this.inventory = inventory;
    }

    public List<Block> getVerticalLine(int xLocalOffset) {
        int nextXIndex = this.character.getXPos() + xLocalOffset; //Gets index of line to be loaded
        int blockIndex = 0;
        List<Block> line = new ArrayList<>();
        for (int i = this.character.getYPos() - RENDER_HEIGHT; i < this.character.getYPos() + RENDER_HEIGHT; i++) { //top of screen to bottom
            Block block = new Block(GameController.lookupTable.get(map[nextXIndex][i]), nextXIndex, i, this, this.inventory); //takes a string for block type and X pos and Y pos
            block.setPos(512 + (xLocalOffset * 32) + this.xOffset,
                    (blockIndex - 1) * 32 + this.yOffset);
            line.add(block);
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

        List<Block> newBlocks = new ArrayList<>();
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
            String key;
            if(newIndex >= 300) {
                key = "3"; //Just bedrock below
            }  else {
                key = this.map[xIndex][newIndex];
            }
            Block block = new Block(GameController.lookupTable.get(key), xIndex, newIndex, this, this.inventory);
            block.setPos(line.get(0).getX(), yTranslate);
            line.add((isUp) ? 0 : line.size(), block);
            newBlocks.add(block);
        }
        this.root.getChildren().addAll(newBlocks);

    }

    public void deleteHorizontal(boolean isUp) { //It wouldn't delete a clear line (as invisible imageviews didn't exist?)

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
                block.addPos(offset, 0);
            }
        }
        for(ItemStack itemStack : this.droppedBlocks) {
            itemStack.addPos(offset, 0);
        }

        for(Zombie zombie : this.zombies) {
            zombie.addTranslateX(offset);
        }

    }

    public void translateBlocksByY(int offset) {
        this.yOffset = this.yOffset + offset;
        for (List<Block> blocks : this.blocks) {
            for (Block block : blocks) {
                block.addPos(0, offset);
            }
        }
        for(ItemStack itemStack : this.droppedBlocks) {
            itemStack.addPos(0, offset);
        }

        for(Zombie zombie : this.zombies) {
            zombie.addTranslateY(offset);
        }
    }

    public void initWorld() { // FIXME: 30/10/2023 issue on creating -> two lines are overlapping
        for (int i = -RENDER_WIDTH; i < RENDER_WIDTH; i++) { //Init world
            addLine(getVerticalLine(i));
        }
    }

    public void addLine(List<Block> line) {
        this.root.getChildren().addAll(line);
        this.blocks.add(line);
    }

    public void addLine(List<Block> line, boolean isLeft) {
        this.root.getChildren().addAll(line);
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

    //For breaking blocks
    public void removeBlock(Block remBlock) { //Remove block and replaces with air block
        ItemStack itemStack = new ItemStack(remBlock.getItemName(), remBlock.getTranslateX(), remBlock.getTranslateY());
        itemStack.addStackValue(1);
        int[] index = new int[2];
        Block newBlock = new Block("air", -1, -1, this, this.inventory);
        for(int j = 0; j < this.blocks.size(); j++) {
            List<Block> line = this.blocks.get(j);
            for(int i = 0; i < line.size(); i++) {
                Block block = line.get(i);
                if(block == remBlock) {
                    index[0] = j;
                    index[1] = i;
                    newBlock = new Block("air", remBlock.getXPos(), remBlock.getYPos(), this, this.inventory); //Copy
                    newBlock.setPos(remBlock.getTranslateX(), remBlock.getTranslateY());
                    break;
                }
            }
        }
        itemStack.setPos(remBlock.getTranslateX(), remBlock.getTranslateY());
        itemStack.randomRotation();
        this.droppedBlocks.add(itemStack); //Adds block to dropped blocks list
        this.root.getChildren().remove(remBlock); //Removes old block
        this.root.getChildren().addAll(itemStack, newBlock); //Adds air block in place to pane
        this.blocks.get(index[0]).set(index[1], newBlock); //Adds air block to blocks
        this.blockJustBroken = true; //Sets flag to true so blocks fall
        this.map[remBlock.getXPos()][remBlock.getYPos()] = "0"; //Update map to an air block
    }

    //For Placing Blocks
    public void placeBlock(Block block) { // TODO: 04/11/2023 doesnt update map file

        int[] index = new int[2];
        for (int i = 0; i < this.blocks.size(); i++) {
            for (int j = 0; j < this.blocks.get(i).size(); j++) {
                if(blocks.get(i).get(j) == block) {
                    index = new int[]{i, j};
                }
            }
        }
        Block placedBlock = new Block(inventory.getSelectedItemStack().getItemName() ,block.getXPos(), block.getYPos(), this, this.inventory);
        System.out.println(GameController.lookupTable.get(placedBlock.getItemName()));
        this.map[placedBlock.getXPos()][placedBlock.getYPos()] = GameController.lookupTable.get(placedBlock.getItemName());
        placedBlock.setTranslateX(block.getTranslateX());
        placedBlock.setTranslateY(block.getTranslateY());
        this.inventory.useBlockFromSelectedSlot();
        this.root.getChildren().remove(block);
        this.root.getChildren().add(placedBlock);
        this.blocks.get(index[0]).set(index[1], placedBlock);
    }

    //For character movement
    public boolean isEntityTouchingBlock(Rectangle collision) { //Can be optimised
        return !getBlockTouchingSide(collision).isEmpty();
    }

    //For collisions
    // TODO: 10/11/2023 could maybe optimise using xPos and yPos
    public boolean isEntityTouchingSide(Rectangle collision) {
        List<Block> blocks = getBlockTouchingSide(collision);
        blocks.removeIf(n -> n.getItemName().equals("air"));
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
        blocks.removeIf(n -> n.getItemName().equals("air"));
        return blocks;
    }

    //For Dropped blocks
    public double getBlockHeightUnderBlock(ItemStack itemStack) {

        for (int i = 0; i < this.blocks.size() - 1; i++) {
            if(this.blocks.get(i).get(0).getTranslateX() <= itemStack.getX() &&
            this.blocks.get(i+1).get(0).getTranslateX() > itemStack.getX()) { //If on same column
                for (int j = 0; j < this.blocks.get(i).size() - 1; j++) {
                    Block b = this.blocks.get(i).get(j);
                    if(b.getTranslateY() > itemStack.getY() && !b.getItemName().equals("air")) {
                        return b.getTranslateY();
                    }
                }
            }
        }
        return -1;
    }

    public double getBlockHeightUnderBlock(double mouseX, double mouseY) {
        for (int i = 0; i < this.blocks.size() - 1; i++) {
            if(this.blocks.get(i).get(0).getTranslateX() < mouseX
                    && this.blocks.get(i+1).get(0).getTranslateX() > mouseX) { //Is right column
                for (int j = 0; j < this.blocks.get(i).size() - 1; j++) {

                    if(mouseY > this.blocks.get(i).get(j).getTranslateY() && mouseY < this.blocks.get(i).get(j+1).getTranslateY()) {
                        return this.blocks.get(i).get(j+1).getTranslateY();
                    }
                }
            }
        }
        return -1;

    }

    public List<ItemStack> getDroppedBlocks() {
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
        Iterator<ItemStack> droppedBlockIterator = this.droppedBlocks.listIterator();
        while(droppedBlockIterator.hasNext()) {
            ItemStack itemStack = droppedBlockIterator.next();
            if(itemStack.getX() < -200 || itemStack.getX() > 1224
                    || itemStack.getY() < -100 || itemStack.getY() > 644) {
                this.root.getChildren().remove(itemStack);
                droppedBlockIterator.remove();
            }
        }

        //Zombie Despawn
        Iterator<Zombie> zombieIterator = this.zombies.listIterator();
        while(zombieIterator.hasNext()) {
            Zombie zombie = zombieIterator.next();
            if(zombie.getTranslateX() < - 200 || zombie.getTranslateX() > 1224 || zombie.getTranslateY() < -200 || zombie.getTranslateY() > 764) {
                this.root.getChildren().removeAll(zombie.getNodes());
                zombieIterator.remove();
                System.out.println("removed");
            }
        }
    }

    public void checkBlockPickup() {
        Iterator<ItemStack> droppedBlocksIterator = this.droppedBlocks.listIterator();
        while(droppedBlocksIterator.hasNext()) {
            ItemStack itemStack = droppedBlocksIterator.next();
            if(this.character.intersects(itemStack.getBoundsInParent())) {
                //Touching
                if(!this.gameController.getInventory().addItem(itemStack)) {
                    continue;
                }
                this.root.getChildren().remove(itemStack);
                droppedBlocksIterator.remove();
            }
        }
    }

    public void createDroppedBlock(ItemStack itemStack, double x, double y) {
        itemStack.setPos(x, y);
        this.droppedBlocks.add(itemStack);
        this.root.getChildren().add(itemStack);
    }

    public void checkBlockBorder() {
        //If walk out of range

    }

    public void setBlockUnderCursor(Block blockUnderCursor) {
        this.blockUnderCursor = blockUnderCursor;
    }
}
