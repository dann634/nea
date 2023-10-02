package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import com.jackson.game.MovementFactory;
import com.jackson.game.ProceduralGenerator;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameController extends Scene {

    private static int spawnYCoords;
    private AnchorPane root;

    private List<Character> characters;

    private Camera camera;
    private String[][] map;
    private static List<List<Block>> blocks;
    private MovementFactory movementFactory;

    private boolean isAPressed;
    private boolean isDPressed;
    private boolean isWPressed;


    public GameController() {
        super(new VBox());

        //Root
        this.root = new AnchorPane();
        Main.applyWindowSize(this.root);

        //Initialises fields
        this.characters = new ArrayList<>();
        this.blocks = new ArrayList<>();
        this.map = loadMap();

        this.isAPressed = false;
        this.isDPressed = false;
        this.isWPressed = false;

        spawnCharacter();
        this.camera = new Camera(this.characters.get(0), this.map, this.root, this.blocks);
        this.camera.initWorld();
        this.characters.get(0).toFront();

        setRoot(this.root);
        this.root.setId("root");
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");

        this.movementFactory = new MovementFactory(this.characters.get(0), this, this.camera);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(25), e -> {

            this.movementFactory.calculateXProperties(this.isAPressed, this.isDPressed, this.isWPressed);
//            this.movementFactory.calculateYProperties(this.isWPressed);

        }));
        timeline.play();

    }

    private String[][] loadMap() {
        String[][] map = TextIO.readMapFile(true);
        spawnYCoords = findStartingY(map);
        return map;
    }

    public static List<List<Block>> getBlocks() {
        return blocks;
    }

    private void spawnCharacter() {
        Character character = new Character();
        character.setXPos(500);
        character.setYPos(spawnYCoords);


        root.getChildren().add(character);
        root.getChildren().addAll(character.getCollisions());
        root.getChildren().add(character.getDisplayNameLabel());
        character.toFront();

        this.characters.add(character);
        initOnKeyPressed();
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

    public boolean isEntityTouchingSide(Rectangle collision) {
        List<Block> blocks = getBlockTouchingSide(collision);
        blocks.removeIf(n -> n.getImage().getUrl().contains("air"));
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

    private void initOnKeyPressed() {

            setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case A -> {
                        this.isAPressed = true;
                        this.characters.get(0).setIsModelFacingRight(false);
                    }
                    case D -> {
                        this.isDPressed = true;
                        this.characters.get(0).setIsModelFacingRight(true);
                    }

                    case W -> {
                        this.isWPressed = true;
                        this.characters.get(0).setIdleImage();

                    }
                }
            });

        setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A -> this.isAPressed = false;
                case D -> this.isDPressed = false;
                case W -> this.isWPressed = false;
            }
        });



    }
}
