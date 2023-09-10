package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.MovementRunnable;
import com.jackson.game.ProceduralGenerator;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
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
    private MovementRunnable movementRunnable;

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

        this.movementRunnable = new MovementRunnable(characters.get(0), this);
        Thread thread = new Thread(this.movementRunnable);
        thread.setDaemon(true);
        thread.start();

    }

    public void drawWorld() {
        /*
        32 blocks fit length
        17.1 blocks fit high
         */
        this.blocks = this.camera.getRenderBlocks(this.map, this.characters.get(0));
        for (int i = 0; i < this.blocks.length; i++) {
            for (int j = 0; j < this.blocks[i].length; j++) {
                this.blocks[i][j].setTranslateX((i-1) * 32);
               this.blocks[i][j].setTranslateY((j-1) * 32);
                this.root.getChildren().add(this.blocks[i][j]);
            }
        }

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
        for (int i = 0; i < this.blocks.length; i++) {
            for (int j = 0; j < this.blocks[i].length; j++) {
                if(!blocks[i][j].getImage().getUrl().contains("air") && character.getFeetCollision().intersects(blocks[i][j].getBoundsInParent())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void initOnKeyPressed() {
        setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A -> this.movementRunnable.setIsAPressed(true);
                case D -> this.movementRunnable.setIsDPressed(true);
                case W -> this.movementRunnable.setIsWPressed(true);
            }
        });

        setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A -> this.movementRunnable.setIsAPressed(false);
                case D -> this.movementRunnable.setIsDPressed(false);
                case W -> this.movementRunnable.setIsWPressed(false);
            }
        });

    }
}
