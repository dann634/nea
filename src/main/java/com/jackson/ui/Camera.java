package com.jackson.ui;

import com.jackson.game.ProceduralGenerator;
import com.jackson.game.characters.Boss;
import com.jackson.game.characters.Player;
import com.jackson.game.characters.Zombie;
import com.jackson.game.items.Block;
import com.jackson.game.items.Entity;
import com.jackson.game.items.Item;
import com.jackson.game.items.ItemStack;
import com.jackson.network.connections.Client;
import com.jackson.network.connections.PseudoPlayer;
import com.jackson.ui.hud.Inventory;
import javafx.animation.Animation;
import javafx.animation.PathTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Camera {

    public static final int RENDER_WIDTH = 18;
    public static final int RENDER_HEIGHT = 10;
    private final Player character;
    private final String[][] map;
    private final AnchorPane root;
    private boolean blockJustBroken;
    private List<List<Block>> blocks;
    private final List<ImageView> rocks;
    private final List<ItemStack> droppedBlocks;
    private final List<Zombie> zombies;
    private final List<PseudoPlayer> onlinePlayers;
    private final Inventory inventory;
    private int xOffset;
    private int yOffset;
    private final Random rand;
    private final List<String> backgroundBlocks; //Blocks the player can walk through
    private final SimpleBooleanProperty isBloodMoonActive;
    private final ImageView rock;
    private final Image rockImage = new Image("file:src/main/resources/images/rock.png");
    private final SimpleBooleanProperty isRainingRocks;
    private final SimpleIntegerProperty killCounter;
    private Client client;
    private final AudioPlayer bossMusic;
    private final GameController gameController;
    // TODO: 19/12/2023 LOAD IMAGES ONCE IN CONSTRUCTOR

    public Camera(Player character, String[][] map, AnchorPane root, Inventory inventory, List<Zombie> zombies,
                  SimpleBooleanProperty isBloodMoonActive, SimpleBooleanProperty isRainingRocks, SimpleIntegerProperty killCounter,
                  AudioPlayer bossMusic, GameController gameController) {
        this.gameController = gameController;
        this.rand = new Random();
        this.character = character;
        this.bossMusic = bossMusic;
        this.zombies = zombies;
        this.map = map;
        this.root = root;
        this.rock = initRock();
        xOffset = 0;
        yOffset = 0;
        this.onlinePlayers = new ArrayList<>(); //Won't be used in singleplayer
        this.isBloodMoonActive = isBloodMoonActive;
        this.killCounter = killCounter;
        droppedBlocks = new ArrayList<>();
        rocks = new ArrayList<>();
        blockJustBroken = false;
        this.inventory = inventory;
        backgroundBlocks = new ArrayList<>(List.of("air", "wood", "leaves"));
        checkAttackIntersect();

        this.isRainingRocks = isRainingRocks;
        isRainingRocks.addListener((observableValue, aBoolean, t1) -> {
            if(!this.isRainingRocks.get()) return;
            for (int i = 0; i < 6; i++) {
                spawnFallingRock();
            }
            isRainingRocks.set(false);
        });

        spawnItem("dirt", 99, 300, 200);


    }

    public List<Block> getVerticalLine(int xLocalOffset) {
        int nextXIndex = character.getXPos() + xLocalOffset; //Gets index of line to be loaded
        int blockIndex = 0; //To track position in line
        List<Block> line = new ArrayList<>();

        //top of screen to bottom
        for (int i = character.getYPos() - RENDER_HEIGHT; i < character.getYPos() + RENDER_HEIGHT; i++) {
            //world looping
            if(nextXIndex < 0) {
                //if on left side of world
                nextXIndex += 1000;
            } else if (nextXIndex > 999) {
                //if on right side of world
                nextXIndex -= 1000;
            }
            Block block = new Block(GameController.lookupTable.get(map[nextXIndex][i]), nextXIndex, i,
                    this, inventory);
            //Positions block on screen
            block.setPos(512 + (xLocalOffset * 32) + xOffset, (blockIndex - 1) * 32 + yOffset);
            line.add(block);
            blockIndex++;
        }
        return line;
    }

    public void deleteVertical(boolean isLeft) {
        //Find index and remove it from blocks and scenegraph
        int index = isLeft ? 0 : blocks.size() - 1;
        root.getChildren().removeAll(blocks.get(index));
        blocks.remove(index);
    }

    public void drawHorizontalLine(boolean isUp) {

        List<Block> newBlocks = new ArrayList<>();
        for (List<Block> line : blocks) {
            if(line.isEmpty()) continue; //If vertical line is empty

            int newIndex;
            double yTranslate;
            //Get new y index and y translate from previous block
            if(isUp) {
                newIndex = line.get(0).getYPos() - 1;
                yTranslate = line.get(0).getTranslateY() - 32;
            } else {
                newIndex = line.get(line.size() - 1).getYPos() + 1;
                yTranslate = line.get(line.size() - 1).getTranslateY() + 32;
            }

            int xIndex = line.get(0).getXPos(); //Get xIndex
            String key;
            if(newIndex >= 300) { //If below y level 300
                key = "3"; //Just bedrock below
            }  else if(newIndex < 0){
                key = "0"; //Above 0 is just air
            } else {
                key = map[xIndex][newIndex]; //get from map
            }

            //Initialise block
            Block block = new Block(GameController.lookupTable.get(key), xIndex, newIndex, this, inventory);
            //Set new block pos
            block.setPos(line.get(0).getX(), yTranslate);
            //Add to line
            line.add((isUp) ? 0 : line.size(), block);
            newBlocks.add(block);
        }
        //Add all new blocks to root
        root.getChildren().addAll(newBlocks);
    }


    public void deleteHorizontal(boolean isUp) {
        //Gatekeeping checks to avoid errors
        if (blocks.isEmpty() || blocks.get(0).isEmpty()) {
            return;
        }
        //Loop through blocks and remove top or bottom of the list
        for (List<Block> blockList : blocks) {
            Block block = blockList.get(isUp ? 0 : blockList.size() - 1);
            blockList.remove(block);
            root.getChildren().remove(block); //Remove from scenegraph
        }
    }

    public void translateBlocksByX(int offset) throws IOException {
        xOffset += offset; //Add to global offset
        //Move all blocks
        for (List<Block> blocks : blocks) {
            for (Block block : blocks) {
                block.addPos(offset, 0);
            }
        }
        //Move all dropped blocks
        for(ItemStack itemStack : droppedBlocks) {
            itemStack.addPos(offset, 0);
        }
        //Moves all zombies
        for(Zombie zombie : zombies) {
            zombie.addTranslateX(offset);
        }
        //Move rocks (for boss)
        rock.setTranslateX(rock.getTranslateX() + offset);
        for(ImageView rock1 : rocks) {
            rock1.setTranslateX(rock1.getTranslateX() + offset);
        }
        //Multiplayer only
        for(PseudoPlayer player : onlinePlayers) {
            player.getImageView().setTranslateX(player.getImageView().getTranslateX() + offset);
        }
        if(client == null) return;
        client.updatePositionOnServer(new int[]{character.getXPos(), character.getYPos(), offset, 0, getxOffset(), getyOffset()});
    }

    public void translateBlocksByY(int offset) throws IOException {
        yOffset += offset; //Add to global offset
        //Move all blocks
        for (List<Block> blocks : blocks) {
            for (Block block : blocks) {
                block.addPos(0, offset);
            }
        }
        //Move all dropped blocks
        for(ItemStack itemStack : droppedBlocks) {
            itemStack.addPos(0, offset);
        }
        //Move all zombies
        for(Zombie zombie : zombies) {
            zombie.addTranslateY(offset);
        }
        //Move Rocks for (boss)
        rock.setTranslateY(rock.getTranslateY() + offset);
        for(ImageView rock1 : rocks) {
            rock1.setTranslateY(rock1.getTranslateY() + offset);
        }
        //Multiplayer only
        for(PseudoPlayer player : onlinePlayers) {
            player.getImageView().setTranslateY(player.getImageView().getTranslateY() + offset);
        }
        if(client == null) return;

        client.updatePositionOnServer(new int[]{character.getXPos(), character.getYPos(), 0, offset, getxOffset(), getyOffset()});
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
        if(client != null) return;
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
        this.blockJustBroken = true;
    }

    //For breaking blocks
    public void removeBlock(Block remBlock, boolean isPacket) throws IOException { //Remove block and replaces with air block
        if(remBlock.getItemName().equals("air") || remBlock.getItemName().equals("bedrock")) return;
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
        if(newBlock.getXPos() == -1) return; //Not a valid block (for multiplayer not on screen)
        root.getChildren().remove(remBlock); //Removes old block
        root.getChildren().addAll(newBlock); //Adds air block in place to pane
        blocks.get(index[0]).set(index[1], newBlock); //Adds air block to blocks
        blockJustBroken = true; //Sets flag to true so blocks fall
        map[remBlock.getXPos()][remBlock.getYPos()] = "0"; //Update map to an air block

        if(client != null && isPacket) {
            client.removeBlock(remBlock);
            client.createDroppedItem(remBlock.getItemName(), 1, remBlock.getXPos(), remBlock.getYPos());
        } else if(client == null) {
            spawnItem(remBlock.getItemName(), 1, remBlock.getTranslateX(), remBlock.getTranslateY());
        }
    }

    public void removeBlock(int xPos, int yPos, boolean isPacket) throws IOException {
        map[xPos][yPos] = "0"; //Update the map in memory
        for (List<Block> blocks : blocks) {
            if(blocks.get(0).getXPos() != xPos) continue;
            //Find correct column
            for (Block block : blocks) {
                if(block.getYPos() == yPos) {
                    //Block with same xPos, yPos
                    removeBlock(block, isPacket);
                }
            }
        }
    }



    //For Placing Blocks
    public void placeBlock(Block block, String newBlockName, boolean isPacket) throws IOException {

        int[] index = new int[2];
        for (int i = 0; i < blocks.size(); i++) {
            for (int j = 0; j < blocks.get(i).size(); j++) {
                if(blocks.get(i).get(j) == block) {
                    index = new int[]{i, j};
                }
            }
        }
        Block placedBlock = new Block(newBlockName, block.getXPos(), block.getYPos(), this, inventory);
        map[placedBlock.getXPos()][placedBlock.getYPos()] = GameController.lookupTable.get(placedBlock.getItemName());
        placedBlock.setTranslateX(block.getTranslateX());
        placedBlock.setTranslateY(block.getTranslateY());
        root.getChildren().remove(block);
        root.getChildren().add(placedBlock);
        blocks.get(index[0]).set(index[1], placedBlock);
        if(client != null && isPacket) client.placeBlock(placedBlock);

        if(!isPacket) return;
        inventory.useBlockFromSelectedSlot();
        character.updateBlockInHand(inventory.getSelectedItemStack());
    }

    public void checkAttackIntersect() {
        character.getAttackTranslate().currentTimeProperty().addListener((observableValue, duration, t1) -> {
            if(t1.equals(Duration.millis(400))) {
                List<Zombie> deadZombies = new ArrayList<>();
                List<Node> zombieNodes = new ArrayList<>();
                for(Zombie zombie : zombies) {
                    if(character.getHandRectangle().intersects(zombie.getTranslateX(), zombie.getTranslateY(), 48, 72)) { // FIXME: 21/11/2023 could make this bound more precise
                        try {
                            checkZombieDamage(zombie, deadZombies, zombieNodes);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
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
                        try {
                            checkZombieDamage(zombie, deadZombies, zombieNodes);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                root.getChildren().removeAll(zombieNodes);
                zombies.removeAll(deadZombies);
            }
        });
    }

    private void checkZombieDamage(Zombie zombie, List<Zombie> deadZombies, List<Node> zombieNodes) throws IOException {
        int damage = (int) character.getAttackDamage();
        if(zombie.takeDamage(damage)) {
            deadZombies.add(zombie);
            zombieNodes.addAll(zombie.getNodes());
            spawnZombieDrop(zombie.getTranslateX(), zombie.getTranslateY());

            if(zombie instanceof Boss) {
                bossMusic.pause();
                gameController.setEventMessage("Boss Defeated!");
            }
        }
        if(client != null) client.damageZombie(zombie.getGameId(), damage);

    }

    private void spawnZombieDrop(double x, double y) {
        killCounter.set(killCounter.get() + 1);
        //Add Strength XP
        character.addStrengthXP(5);

        //Random Chance to start blood moon
        if(rand.nextDouble() < 0.01) {
            isBloodMoonActive.set(true);
        }

        double spawnChance = rand.nextDouble();
        if(spawnChance < 0.1) {
            return; //10% chance to spawn
        }

        double itemSpawnChance = rand.nextDouble();
        if(itemSpawnChance < 0.3) {
            //Spawn coal
            spawnItem("coal", 1, x, y);
        } else if(itemSpawnChance < 0.6) {
            //Spawn plank
            spawnItem("plank", 1, x, y);
        } else {
            //Spawn stick
            spawnItem("stick", 1, x, y);
        }
    }

    //For collisions
    // TODO: 10/11/2023 could maybe optimise using xPos and yPos
    public boolean isEntityTouchingBlock(Node collision, boolean isPlayer) {
        List<Block> blocks = new ArrayList<>();
        for(List<Block> blockArr : this.blocks) { //Loops through all blocks on screen
            for(Block block : blockArr) {
                if(collision.intersects(block.getBoundsInParent()) &&
                        !backgroundBlocks.contains(block.getItemName())) {
                    if(block.getItemName().equals("plank") && isPlayer) continue; //Walk through planks

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
                    || itemStack.getY() < -300 || itemStack.getY() > 644) {
                root.getChildren().remove(itemStack);
                droppedBlockIterator.remove();
            }
        }

        //Zombie Despawn
        for(Zombie zombie : zombies) {
            if(zombie.getTranslateX() < - 200 || zombie.getTranslateX() > 1224 || zombie.getTranslateY() < -200 || zombie.getTranslateY() > 764) {
                root.getChildren().removeAll(zombie.getNodes());
                zombies.remove(zombie);
            }
        }
    }

    public void checkBlockPickup() throws IOException {
        Iterator<ItemStack> droppedBlocksIterator = droppedBlocks.listIterator();
        while(droppedBlocksIterator.hasNext()) {
            ItemStack itemStack = droppedBlocksIterator.next();
            if(character.intersects(itemStack.getBoundsInParent())) {
                //Touching
                if(!inventory.addItem(itemStack)) continue;

                root.getChildren().remove(itemStack);
                droppedBlocksIterator.remove();

                //Check hand
                character.updateBlockInHand(inventory.getSelectedItemStack());

                if(client != null) client.pickupItem(itemStack);

            }
        }
    }

    public void createDroppedBlock(ItemStack itemStack, double x, double y) {
        itemStack.setPos(x, y);
        droppedBlocks.add(itemStack);
        root.getChildren().add(itemStack);
        character.updateBlockInHand(inventory.getSelectedItemStack());
        blockJustBroken = true;
    }

    public void respawn() {
        character.setHealth(100);
        sendToSpawn(false);

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

    public void sendToSpawn(boolean isMultiplayer) {
        character.setXPos(500);
        character.setYPos(findStartingY(map));
        if(isMultiplayer) addYOffset(-46);

        //Reset World
        if(blocks != null) {
            blocks.forEach(n -> root.getChildren().removeAll(n));
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

        data.add(String.valueOf(character.getAmmo()));
        data.add(String.valueOf(character.healthProperty().get()));
        data.add(String.valueOf(killCounter.get()));

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

    public void makeCrater(double x, double y, int length, int depth) throws IOException {

        //Find grass block
        Block block = null;
        for (int i = 0; i < blocks.size() - 1; i++) {
            List<Block> line = blocks.get(i);
            Block b = line.get(0);
            Block nextB = blocks.get(i + 1).get(0);
            if (x < b.getTranslateX() || x > nextB.getTranslateX()) continue;
            //Finds block within these values
            for (Block block1 : line) {
                //Loops down list to find first solid block
                if (block1.getTranslateY() < y ||
                        backgroundBlocks.contains(block1.getItemName())) continue;
                block = block1;
                break;
            }
        }

            if(block == null) return; //Null check

            for (int j = block.getYPos(); j < block.getYPos() + depth; j++) {
                for (int k = block.getXPos() - (length / 2); k < block.getXPos() + (length / 2) + 1; k++) {
                    removeBlock(k, j, true);
                }
                length--; //Must decrement for cone shape
            }
        this.blockJustBroken = true; // Flag to make all drops fall to ground
    }

    public void addNode(Node node) {
        this.root.getChildren().add(node);
    }

    public void setIsRainingRocks(boolean isRainingRocks) {
        this.isRainingRocks.set(isRainingRocks);
    }

    public void moveRock(double startX, double startY, double endX, double endY) {

        //Add rock to root
        root.getChildren().add(rock);

        //Path - Straight Line
        Path path = new Path();
        path.getElements().add(new MoveTo(startX, startY));
        path.getElements().add(new LineTo(endX, endY));

        //Animation
        PathTransition pathTransition = new PathTransition();
        pathTransition.setPath(path);
        pathTransition.setNode(rock);
        pathTransition.setDuration(Duration.millis(750));
        pathTransition.setOnFinished(e -> root.getChildren().remove(rock));
        pathTransition.play();
    }
    private ImageView initRock() {
        ImageView rock = new ImageView(rockImage);
        rock.setFitWidth(40);
        rock.setPreserveRatio(true);
        return rock;
    }

    private void spawnFallingRock() {
        double spawnX = rand.nextDouble(1000); // Get random spawn location
        //initialise rock
        ImageView rock = new ImageView(rockImage);
        rock.setFitWidth(40);
        rock.setPreserveRatio(true);
        rocks.add(rock); //Add to list so it's moved when player moves
        rock.setTranslateX(spawnX);
        rock.setTranslateY(-100);

        //Find target y - nearest solid block
        double y = 0;
        for (int i = 0; i < blocks.size() - 1; i++) {

            List<Block> line = blocks.get(i);
            List<Block> nextLine = blocks.get(i + 1);
            if (spawnX < line.get(0).getTranslateX() || spawnX > nextLine.get(0).getTranslateX()) continue;

            //Find block under rock spawn point
            for (Block block : line) {
                if (backgroundBlocks.contains(block.getItemName())) continue;
                //Find the nearest solid block
                y = block.getTranslateY();
                break;
            }
        }

        //Animation
        TranslateTransition fallingAnimation = new TranslateTransition();
        fallingAnimation.setNode(rock);
        fallingAnimation.setDuration(Duration.millis(2000));
        fallingAnimation.setToY(y-50); //-30 for height of rock
        fallingAnimation.play();
        fallingAnimation.setOnFinished(e -> {
            rocks.remove(rock); //Remove rock from rock list
            root.getChildren().remove(rock); //Remove rock from root
            try {
                makeCrater(rock.getTranslateX(), rock.getTranslateY(), 1, 1); //Make small crater
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            if(character.intersects(rock.getBoundsInParent())) {
                character.takeDamage(10); //If touching player deal damage
            }
        });
        root.getChildren().add(rock); //Add rock to root
    }

    public void setKillCounter(int killCounter) {
        this.killCounter.set(killCounter);
    }

    public List<List<Block>> getBlocks() {
        return blocks;
    }

    public void addOnlinePlayer(PseudoPlayer player) {
        onlinePlayers.add(player);
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Block getBlock(int xPos, int yPos) {
        for(List<Block> line : blocks) {
            for(Block block : line) {
                if(block.getXPos() == xPos && block.getYPos() == yPos) {
                    return block;
                }
            }
        }
        return null;
    }

    public void updateMap(int xPos, int yPos, String block) {
        map[xPos][yPos] = block;
    }
}
