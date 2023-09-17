package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import com.jackson.game.MovementFactory;
import com.jackson.game.ProceduralGenerator;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class GameController extends Scene {

    private static int spawnYCoords;
    private AnchorPane root;

    private List<Character> characters;

    private Camera camera;
    private String[][] map;
    //    private Block[][] blocks;
    private List<Block> blocks;
    private MovementFactory movementFactory;

    public GameController() {
        super(new VBox());

        //Root
        this.root = new AnchorPane();
        Main.applyWindowSize(this.root);

        //Initialises fields
        this.characters = new ArrayList<>();
        this.camera = new Camera();
        this.map = loadMap();
        this.blocks = new ArrayList<>();

        spawnCharacter();
        drawWorld();

        setRoot(this.root);
        this.root.setId("root");
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");

        this.movementFactory = new MovementFactory(this.characters.get(0), this);
        Timeline movementTimeline = this.movementFactory.getMovementTimeline(this.camera, map);
        movementTimeline.play();

    }


    public void drawWorld() {
        /*
        32 blocks fit length
        17.1 blocks fit high
         */
        characters.get(0).toFront();
    }

    public void drawBlock(Block block) {
        this.root.getChildren().add(block);
    }

    public void clearWorld() {
        this.root.getChildren().removeIf(n -> n instanceof ImageView && !((ImageView)n).getImage().getUrl().contains("player"));
    }


    private String[][] loadMap() {
        String[][] map = TextIO.readMapFile(true);
        spawnYCoords = findStartingY(map);
        return map;
    }

    private void spawnCharacter() {
        Character character = new Character();
        character.setXPos(500);
        character.setYPos(spawnYCoords);
        System.out.println(spawnYCoords);


        root.getChildren().add(character);
        root.getChildren().addAll(character.getCollisions());
        character.toFront();

        this.characters.add(character);
        initOnKeyPressed();
    }

    private int findStartingY(String[][] map) {
        for (int i = 0; i < ProceduralGenerator.getHeight(); i++) {
            if (map[500][i].equals("2")) {
                return i;
            }
        }
        return -1;
    }

    public boolean isEntityTouchingGround(Character character) {
        List<Block> blocks = getBlocksTouchingPlayer(character); //Gets list of blocks touching feet
        blocks.removeIf(n -> n.getImage().getUrl().contains("air")); //Removes all air blocks
        return !blocks.isEmpty(); //If any solid blocks are left
    }

    public boolean isEntityTouchingSide(Rectangle collision) {
        List<Block> blocks = getBlockTouchingSide(collision);
        blocks.removeIf(n -> n.getImage().getUrl().contains("air"));
        return !blocks.isEmpty();
    }

    public List<Block> getBlockTouchingSide(Rectangle collision) {
        List<Block> blocks = new ArrayList<>();
//        for(Block[] blockArr : this.blocks) {
//
//            }

        for (Block block : this.blocks) {
            if (collision.intersects(block.getBoundsInParent())) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    public List<Block> getBlocksTouchingPlayer(Character character) {
        List<Block> blocks = new ArrayList<>(); // Arraylist to store blocks touching player
//        for (Block[] blockArr : this.blocks) //Loops through all blocks shown on screen
//
//               }

        for (Block block : this.blocks) {
            //If the rectangle at the feet intersects with the block
            if (character.getFeetCollision().intersects(block.getBoundsInParent())) {
                blocks.add(block); //Add to list
            }
        }
            return blocks;
    }

        private void initOnKeyPressed () {
            setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case A -> {
                        this.movementFactory.setIsAPressed(true);
                        this.characters.get(0).setIsModelFacingRight(false);
                    }
                    case D -> {
                        this.movementFactory.setIsDPressed(true);
                        this.characters.get(0).setIsModelFacingRight(true);
                    }
                    case W -> {
                        this.movementFactory.setIsWPressed(true);
                        this.characters.get(0).setIdleImage();
                    }
                }
            });

            setOnKeyReleased(e -> {
                switch (e.getCode()) {
                    case A -> this.movementFactory.setIsAPressed(false);
                    case D -> this.movementFactory.setIsDPressed(false);
                    case W -> this.movementFactory.setIsWPressed(false);
                }
            });


        }


    }

