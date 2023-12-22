package com.jackson.ui;

import com.jackson.game.ProceduralGenerator;
import com.jackson.game.characters.Character;
import com.jackson.game.characters.Zombie;
import com.jackson.game.items.Block;
import com.jackson.game.characters.Player;
import com.jackson.game.items.Entity;
import com.jackson.game.items.Item;
import com.jackson.game.items.ItemStack;
import com.jackson.ui.hud.Inventory;
import javafx.animation.Animation;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Camera {

    public static final int RENDER_WIDTH = 18;
    public static final int RENDER_HEIGHT = 10;
    private final Player character;
    private final String[][] map;
    private final AnchorPane root;
    private boolean blockJustBroken;
    private List<List<Block>> blocks;
    private final List<ItemStack> droppedBlocks;
    private final List<Zombie> zombies;
    private final Inventory inventory;
    private int xOffset;
    private int yOffset;
    private Random rand;
    private final List<String> backgroundBlocks; //Blocks the player can walk through
    private final SimpleBooleanProperty isBloodMoonActive;

    // TODO: 19/12/2023 LOAD IMAGES ONCE IN CONSTRUCTOR

    public Camera(Player character, String[][] map, AnchorPane root, Inventory inventory, List<Zombie> zombies, SimpleBooleanProperty isBloodMoonActive) {
        this.rand = new Random();
        this.character = character;
        this.zombies = zombies;
        this.map = map;
        this.root = root;
        xOffset = 0;
        yOffset = 0;
        this.isBloodMoonActive = isBloodMoonActive;
        droppedBlocks = new ArrayList<>();
        blockJustBroken = false;
        this.inventory = inventory;
        backgroundBlocks = new ArrayList<>(List.of("air", "wood", "leaves"));
        checkAttackIntersect();

//        spawnItem("wood_sword", 1, 500, 200);
//        spawnItem("rifle", 1, 500, 200);
//        spawnItem("sniper", 1, 500, 200);
//        spawnItem("pistol", 1, 500, 200);
//        spawnItem("metal_sword", 1, 500, 200);
//        spawnItem("metal_pickaxe", 1, 500, 200);
//        spawnItem("metal_axe", 1, 500, 200);
//        spawnItem("metal_shovel", 1, 500, 200);
//        spawnItem("metal", 10, 500, 200);


    }

    public List<Block> getVerticalLine(int xLocalOffset) {
        int nextXIndex = character.getXPos() + xLocalOffset; //Gets index of line to be loaded
        int blockIndex = 0;
        List<Block> line = new ArrayList<>();
        for (int i = character.getYPos() - RENDER_HEIGHT; i < character.getYPos() + RENDER_HEIGHT; i++) { //top of screen to bottom
            if(nextXIndex < 0) {
                nextXIndex = 299;
            } else if (nextXIndex > 999) {
                nextXIndex = 0;
            }
            Block block = new Block(GameController.lookupTable.get(map[nextXIndex][i]), nextXIndex, i, this, inventory); //takes a string for block type and X pos and Y pos
            block.setPos(512 + (xLocalOffset * 32) + xOffset,
                    (blockIndex - 1) * 32 + yOffset);
            line.add(block);
            blockIndex++;
        }
        return line;
    }

    public void deleteVertical(boolean isLeft) {
        int index = isLeft ? 0 : blocks.size() - 1;
        root.getChildren().removeAll(blocks.get(index));
        blocks.remove(index);
    }

    public void drawHorizontalLine(boolean isUp) {

        List<Block> newBlocks = new ArrayList<>();
        for (List<Block> line : blocks) {
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
                try {
                    key = map[xIndex][newIndex];
                } catch (IndexOutOfBoundsException e) {
                    key = "0";
                }
            }
            Block block = new Block(GameController.lookupTable.get(key), xIndex, newIndex, this, inventory);
            block.setPos(line.get(0).getX(), yTranslate);
            line.add((isUp) ? 0 : line.size(), block);
            newBlocks.add(block);
        }
        root.getChildren().addAll(newBlocks);

    }


    public void deleteHorizontal(boolean isUp) { //It wouldn't delete a clear line (as invisible imageviews didn't exist?)

        if (blocks.isEmpty() || blocks.get(0).isEmpty()) {
            return;
        }

        for (List<Block> blockList : blocks) {
            Block block = blockList.get(isUp ? 0 : blockList.size() - 1);
            blockList.remove(block);
            root.getChildren().remove(block);
        }
    }

    public void translateBlocksByX(int offset) {
        xOffset += offset;
        for (List<Block> blocks : blocks) {
            for (Block block : blocks) {
                block.addPos(offset, 0);
            }
        }
        for(ItemStack itemStack : droppedBlocks) {
            itemStack.addPos(offset, 0);
        }

        for(Zombie zombie : zombies) {
            zombie.addTranslateX(offset);
        }

    }

    public void translateBlocksByY(int offset) {
        yOffset = yOffset + offset;
        for (List<Block> blocks : blocks) {
            for (Block block : blocks) {
                block.addPos(0, offset);
            }
        }
        for(ItemStack itemStack : droppedBlocks) {
            itemStack.addPos(0, offset);
        }

        for(Zombie zombie : zombies) {
            zombie.addTranslateY(offset);
        }
    }

    public void initWorld() { // FIXME: 30/10/2023 issue on creating -> two lines are overlapping
        List<List<Block>> blocks = new ArrayList<>();
        for (int i = -RENDER_WIDTH; i < RENDER_WIDTH; i++) { //Init world
            blocks.add(getVerticalLine(i));
        }
        this.blocks = blocks;
        List<Block> nodes = new ArrayList<>();
        for(List<Block> nodeList : blocks) {
            nodes.addAll(nodeList);
        }
        root.getChildren().addAll(nodes);
    }

    public void addLine(List<Block> line, boolean isLeft) {
        root.getChildren().addAll(line);
        if (isLeft) {
            blocks.add(0, line);
            return;
        }
        blocks.add(line);

    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }


    public void addXOffset(int value) {
        xOffset += value;
        cleanupEntities();
    }

    public void addYOffset(int value) {
        yOffset += value;
        cleanupEntities();
    }

    public void spawnItem(String itemName, int amount, double x, double y) {
        // TODO: 19/11/2023 get from database in future
        Entity item;
        if(GameController.lookupTable.containsKey(itemName)) {
            item = new Block(itemName, character.getXPos(), character.getYPos(), this, inventory);
        } else {
            item = new Item(itemName, character.getTranslateX(), character.getTranslateY());
        }
        item.setTranslateX(x);
        item.setTranslateY(y);

        ItemStack itemStack = new ItemStack(item);
        itemStack.randomRotation();
        itemStack.addStackValue(amount);
        droppedBlocks.add(itemStack);
        root.getChildren().add(itemStack);
    }

    //For breaking blocks
    public void removeBlock(Block remBlock) { //Remove block and replaces with air block
        int[] index = new int[2];
        Block newBlock = new Block("air", -1, -1, this, inventory);
        for(int j = 0; j < blocks.size(); j++) {
            List<Block> line = blocks.get(j);
            for(int i = 0; i < line.size(); i++) {
                Block block = line.get(i);
                if(block == remBlock) {
                    index[0] = j;
                    index[1] = i;
                    newBlock = new Block("air", remBlock.getXPos(), remBlock.getYPos(), this, inventory); //Copy
                    newBlock.setPos(remBlock.getTranslateX(), remBlock.getTranslateY());
                    break;
                }
            }
        }
        spawnItem(remBlock.getItemName(), 1, remBlock.getTranslateX(), remBlock.getTranslateY());
        root.getChildren().remove(remBlock); //Removes old block
        root.getChildren().addAll(newBlock); //Adds air block in place to pane
        blocks.get(index[0]).set(index[1], newBlock); //Adds air block to blocks
        blockJustBroken = true; //Sets flag to true so blocks fall
        map[remBlock.getXPos()][remBlock.getYPos()] = "0"; //Update map to an air block
    }

    //For Placing Blocks
    public void placeBlock(Block block) { // TODO: 04/11/2023 doesnt update map file

        int[] index = new int[2];
        for (int i = 0; i < blocks.size(); i++) {
            for (int j = 0; j < blocks.get(i).size(); j++) {
                if(blocks.get(i).get(j) == block) {
                    index = new int[]{i, j};
                }
            }
        }
        Block placedBlock = new Block(inventory.getSelectedItemStack().getItemName() ,block.getXPos(), block.getYPos(), this, inventory);
        map[placedBlock.getXPos()][placedBlock.getYPos()] = GameController.lookupTable.get(placedBlock.getItemName());
        placedBlock.setTranslateX(block.getTranslateX());
        placedBlock.setTranslateY(block.getTranslateY());
        inventory.useBlockFromSelectedSlot();
        root.getChildren().remove(block);
        root.getChildren().add(placedBlock);
        blocks.get(index[0]).set(index[1], placedBlock);

        character.updateBlockInHand(inventory.getSelectedItemStack());
    }

    public void checkAttackIntersect() {
        character.getAttackTranslate().currentTimeProperty().addListener((observableValue, duration, t1) -> {
            if(t1.equals(Duration.millis(400))) {
                List<Zombie> deadZombies = new ArrayList<>();
                List<Node> zombieNodes = new ArrayList<>();
                for(Zombie zombie : zombies) {
                    if(character.getHandRectangle().intersects(zombie.getTranslateX() + 24, zombie.getTranslateY(), 48, 72)) { // FIXME: 21/11/2023 could make this bound more precise
                        //Zombie touching weapon
                        if(zombie.takeDamage((int)character.getAttackDamage())) { //Returns true if dead
                            deadZombies.add(zombie);
                            zombieNodes.addAll(zombie.getNodes());
                            spawnZombieDrop();
                        }
                    }
                }
                root.getChildren().removeAll(zombieNodes);
                zombies.removeAll(deadZombies);
            }
        });

        character.getShootingPause().statusProperty().addListener((observableValue, status, t1) -> {
            if(t1 == Animation.Status.RUNNING) {
                List<Zombie> deadZombies = new ArrayList<>();
                List<Node> zombieNodes = new ArrayList<>();
                for(Zombie zombie : zombies) {
                    if(character.getAimingLine().intersects(zombie.getTranslateX() + 24, zombie.getTranslateY(), 48, 72)) {
                        if(zombie.takeDamage((int) character.getAttackDamage())) {
                            deadZombies.add(zombie);
                            zombieNodes.addAll(zombie.getNodes());
                            spawnZombieDrop();
                        }
                    }
                }
                root.getChildren().removeAll(zombieNodes);
                zombies.removeAll(deadZombies);
            }
        });
    }

    private void spawnZombieDrop() {
        character.addStrengthXP(5);

        //Random Chance to start blood moon
        if(rand.nextDouble() < 0.01) {
            isBloodMoonActive.set(true);
        }

    }

    //For collisions
    // TODO: 10/11/2023 could maybe optimise using xPos and yPos
    public boolean isEntityTouchingBlock(Rectangle collision) {
        List<Block> blocks = new ArrayList<>();
        for(List<Block> blockArr : this.blocks) { //Loops through all blocks on screen
            for(Block block : blockArr) {
                if(collision.intersects(block.getBoundsInParent()) &&
                        !backgroundBlocks.contains(block.getItemName())) {
                    //If rectangle is touching block its added to list
                    //If player cannot pass through the block
                    blocks.add(block);
                }
            }
        }
        return !blocks.isEmpty();
    }

    //For Dropped blocks
    public double getBlockHeightUnderBlock(ItemStack itemStack) {

        for (int i = 0; i < blocks.size() - 1; i++) {
            if(blocks.get(i).get(0).getTranslateX() <= itemStack.getX() &&
            blocks.get(i+1).get(0).getTranslateX() > itemStack.getX()) { //If on same column
                for (int j = 0; j < blocks.get(i).size() - 1; j++) {
                    Block b = blocks.get(i).get(j);
                    if(b.getTranslateY() > itemStack.getY() && !b.getItemName().equals("air")) {
                        return b.getTranslateY();
                    }
                }
            }
        }
        return -1;
    }

    public List<ItemStack> getDroppedBlocks() {
        return droppedBlocks;
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
        Iterator<ItemStack> droppedBlockIterator = droppedBlocks.listIterator();
        while(droppedBlockIterator.hasNext()) {
            ItemStack itemStack = droppedBlockIterator.next();
            if(itemStack.getX() < -200 || itemStack.getX() > 1224
                    || itemStack.getY() < -100 || itemStack.getY() > 644) {
                root.getChildren().remove(itemStack);
                droppedBlockIterator.remove();
            }
        }

        //Zombie Despawn
        Iterator<Zombie> zombieIterator = zombies.listIterator();
        while(zombieIterator.hasNext()) {
            Zombie zombie = zombieIterator.next();
            if(zombie.getTranslateX() < - 200 || zombie.getTranslateX() > 1224 || zombie.getTranslateY() < -200 || zombie.getTranslateY() > 764) {
                root.getChildren().removeAll(zombie.getNodes());
                zombieIterator.remove();
            }
        }
    }

    public void checkBlockPickup() {
        Iterator<ItemStack> droppedBlocksIterator = droppedBlocks.listIterator();
        while(droppedBlocksIterator.hasNext()) {
            ItemStack itemStack = droppedBlocksIterator.next();
            if(character.intersects(itemStack.getBoundsInParent())) {
                //Touching
                if(!inventory.addItem(itemStack)) {
                    continue;
                }
                root.getChildren().remove(itemStack);
                droppedBlocksIterator.remove();

                //Check hand
                character.updateBlockInHand(inventory.getSelectedItemStack());
            }
        }
    }

    public void createDroppedBlock(ItemStack itemStack, double x, double y) {
        itemStack.setPos(x, y);
        droppedBlocks.add(itemStack);
        root.getChildren().add(itemStack);
        character.updateBlockInHand(itemStack);
    }

    public void checkBlockBorder() {
        //If walk out of range

    }

    public void respawn() {
        character.setHealth(100);
        sendToSpawn();

        List<Node> nodes = new ArrayList<>();
        zombies.forEach(n -> nodes.addAll(n.getNodes()));
        root.getChildren().removeAll(nodes);
        zombies.clear();

    }

    private int findStartingY(String[][] map) {
        for (int i = 0; i < ProceduralGenerator.getHeight(); i++) {
            if(map[500][i].equals("2")) {
                return i;
            }
        }
        return -1;
    }




    public double getBlockTranslateY(int xPos) {
        for(Block block : blocks.get(xPos)) {
            if(!backgroundBlocks.contains(block.getItemName())) {
                return block.getTranslateY();
            }
        }
        return 0;
    }

    public void sendToSpawn() {
        character.setXPos(500);
        character.setYPos(findStartingY(map));

        //Reset World
        if(blocks != null) {
            blocks.forEach(n -> {
                root.getChildren().removeAll(n);
            });
        }
        initWorld();
    }

    public String[][] getMap() {
        return map;
    }

    public List<String> getPlayerData() {
        List<String> data = new ArrayList<>();
        data.add(String.valueOf(character.getXPos()));
        data.add(String.valueOf(character.getYPos()));
        data.add(String.valueOf(xOffset));
        data.add(String.valueOf(yOffset));

        data.add(character.getStrengthLevel() + " " + character.getStrengthXP());
        data.add(character.getAgilityLevel() + " " + character.getAgilityXP());
        data.add(character.getDefenceLevel() + " " + character.getDefenceXP());

        //Inventory
        ItemStack[][] inv = inventory.getItemArray();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                if(inv[j][i] == null) {
                    data.add("null");
                }
                else {
                    data.add(inv[j][i].getItemName() + " " + inv[j][i].getStackSize());
                }
            }
        }
        return data;
    }

}
