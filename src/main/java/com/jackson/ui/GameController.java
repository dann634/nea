package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.MovementFactory;
import com.jackson.game.ProceduralGenerator;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import com.jackson.game.Character;

import java.util.ArrayList;
import java.util.List;

public class GameController extends Scene {

    private static int spawnYCoords;
    private AnchorPane root;

    private List<Character> characters;

    private Camera camera;
    private String[][] map;
    private Block[][] blocks;
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

        spawnCharacter();
        drawWorld();

        setRoot(this.root);
        this.root.setId("root");
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");

        this.movementFactory = new MovementFactory(this.characters.get(0), this);
        Timeline movementTimeline = this.movementFactory.getMovementTimeline();
        movementTimeline.play();

    }

    public void drawWorld() {
        /*
        32 blocks fit length
        17.1 blocks fit high
         */
        if (this.blocks != null) {
            for(Block[] blockArr : this.blocks) {
                for (Block block : blockArr) {
                    this.root.getChildren().remove(block);
                }
            }
        }
        this.blocks = this.camera.getRenderBlocks(this.map, this.characters.get(0));
        for (int i = 0; i < this.blocks.length; i++) {
            for (int j = 0; j < this.blocks[i].length; j++) {
                this.blocks[i][j].setTranslateX((i-1) * 32);
               this.blocks[i][j].setTranslateY((j-1) * 32);
                this.root.getChildren().add(this.blocks[i][j]);
            }
        }
        characters.get(0).toFront();
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
        initOnKeyPressed();

        root.getChildren().add(character);
        root.getChildren().addAll(character.getCollisions());
        character.toFront();

        this.characters.add(character);
    }

    private int findStartingY(String[][] map) {
        for (int i = 0; i < ProceduralGenerator.getHeight(); i++) {
            if(map[500][i].equals("2")) {
                return i;
            }
        }
        return -1;
    }

    public boolean isEntityTouchingGround(Character character) { //Can be optimised
        List<Block> blocks = getBlocksTouchingPlayer(character);
        blocks.removeIf(n -> n.getImage().getUrl().contains("air"));

        return !blocks.isEmpty();
    }

    public boolean isEntityTouchingSide(Character character) {
        List<Block> blocks = getBlockTouchingSide(character);
        blocks.removeIf(n -> n.getImage().getUrl().contains("air"));
        return !blocks.isEmpty();
    }

    public List<Block> getBlockTouchingSide(Character character) {
        List<Block> blocks = new ArrayList<>();
        for(Block[] blockArr : this.blocks) {
            for(Block block : blockArr) {
                if(character.getBodyCollision().intersects(block.getBoundsInParent())) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    public List<Block> getBlocksTouchingPlayer(Character character) {
        List<Block> blocks = new ArrayList<>();
        for (Block[] block : this.blocks) {
            for (Block value : block) {
                if (character.getFeetCollision().intersects(value.getBoundsInParent())) {
                    blocks.add(value);
                }
            }
        }
        return blocks; //Should never happen
    }

    private void initOnKeyPressed() {
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
