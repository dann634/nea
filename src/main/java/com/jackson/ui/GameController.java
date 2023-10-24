package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import com.jackson.game.MovementFactory;
import com.jackson.game.ProceduralGenerator;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.ui.hud.HealthBar;
import com.jackson.ui.hud.Inventory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class GameController extends Scene {

    private static int spawnYCoords;
    private final AnchorPane root;

    private final List<Character> characters;

    private final Inventory inventory;
    private final HealthBar healthBar;
    private final MovementFactory movementFactory;

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
        String[][] map = loadMap();

        spawnCharacter();

        //HUD
        this.inventory = new Inventory();
        this.healthBar = new HealthBar(this.characters.get(0).healthProperty());

        this.root.getChildren().add(this.inventory.getInventoryVbox());
        this.root.getChildren().add(this.healthBar.getHealthHud());

        //Movement
        this.isAPressed = false;
        this.isDPressed = false;
        this.isWPressed = false;


        Camera camera = new Camera(this.characters.get(0), map, this.root);
        camera.initWorld();
        this.characters.get(0).toFront();



        setRoot(this.root);
        this.root.setId("root");
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");

        this.movementFactory = new MovementFactory(this.characters.get(0), this, camera);
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000 / 60), e -> {

            this.movementFactory.calculateXProperties(this.isAPressed, this.isDPressed);
            this.movementFactory.calculateYProperties(this.isWPressed);

            if(camera.isBlockJustBroken()) {
                this.movementFactory.calculateDroppedBlockGravity();
            }


            //Everything to front (maybe make a method for it)
            this.inventory.getInventoryVbox().toFront();
            this.characters.get(0).toFront();
            this.healthBar.getHealthHud().toFront();

        }));
        timeline.play();


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

                    case I -> {
                        this.inventory.toggleInventory();
                    }
                    case DIGIT1 -> {
                        this.inventory.selectSlot(0);
                    }
                    case DIGIT2 -> {
                        this.inventory.selectSlot(1);
                    }
                    case DIGIT3 -> {
                        this.inventory.selectSlot(2);
                    }
                    case DIGIT4 -> {
                        this.inventory.selectSlot(3);
                    }
                    case DIGIT5 -> {
                        this.inventory.selectSlot(4);
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
