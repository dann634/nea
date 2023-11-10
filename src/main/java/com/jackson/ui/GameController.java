package com.jackson.ui;

import com.jackson.game.*;
import com.jackson.game.characters.Player;
import com.jackson.game.characters.Zombie;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.ui.hud.HealthBar;
import com.jackson.ui.hud.Inventory;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameController extends Scene {

    private static int spawnYCoords;
    private final AnchorPane root;
    private final List<Player> characters;
    private final List<Zombie> zombies;
    public static HashMap<String, String> lookupTable;
    private final Inventory inventory;
    private final HealthBar healthBar;
    private final MovementFactory movementFactory;
    private final Camera camera;
    private final Timeline gameTimeline;
    private boolean blockDropped;
    private boolean isAPressed;
    private boolean isDPressed;
    private boolean isWPressed;

    // TODO: 24/10/2023 Add autosave feature -> no close and save

    public GameController() {
        super(new VBox());

        //Root
        this.root = new AnchorPane();
        Main.applyWindowSize(this.root);

        //Initialises fields
        this.characters = new ArrayList<>();
        this.zombies = new ArrayList<>();
        String[][] map = loadMap();
        initLookupTable();


        //HUD
        this.inventory = new Inventory();
        spawnCharacter();
        this.root.getChildren().add(this.inventory.getItemOnCursor());
        this.healthBar = new HealthBar(this.characters.get(0).healthProperty());

        this.root.getChildren().add(this.inventory.getInventoryVbox());
        this.root.getChildren().add(this.healthBar.getHealthHud());

        //Movement
        this.isAPressed = false;
        this.isDPressed = false;
        this.isWPressed = false;

        this.blockDropped = false;


        this.camera = new Camera(this.characters.get(0), map, this.root, this, this.inventory, this.zombies);
        this.camera.initWorld();
        this.characters.get(0).toFront();

        setRoot(this.root);
        this.root.setId("root");
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");

        this.movementFactory = new MovementFactory(this.characters.get(0),  this.camera);
        this.gameTimeline = new Timeline();
        this.gameTimeline.setCycleCount(Animation.INDEFINITE);
        this.gameTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000 / 60), e -> {

            this.movementFactory.calculateXProperties(this.isAPressed, this.isDPressed); //Player X movement
            this.movementFactory.calculateYProperties(this.isWPressed); //Player y movement
            this.movementFactory.calculateZombieMovement(this.zombies);

            if(this.camera.isBlockJustBroken() || this.blockDropped) { //Check to save cpu
                this.movementFactory.calculateDroppedBlockGravity(); //Block dropping
                this.blockDropped = false;
            }

            this.camera.checkBlockPickup();

            //Everything to front (maybe make a method for it)
            this.inventory.getInventoryVbox().toFront();
            this.inventory.getItemOnCursor().toFront();
            this.characters.get(0).toFront();
            this.characters.get(0).getHandRectangle().toFront();
            this.healthBar.getHealthHud().toFront();

        }));
        this.gameTimeline.play();


    }

    private String[][] loadMap() {
        String[][] map = TextIO.readMapFile(true);
        spawnYCoords = findStartingY(map);
        return map;
    }


    private void spawnCharacter() {
        Player character = new Player();
        character.setXPos(500);
        character.setYPos(spawnYCoords);

        this.inventory.getSelectedSlotIndex().addListener((observableValue, number, t1) -> {
            character.updateBlockInHand(this.inventory.getBlockNameInHotbar(t1.intValue())); // FIXME: 27/10/2023 when block is picked up players hand not updated
        });

        spawnZombie(800);
//        spawnZombie(600);

        root.getChildren().addAll(character, character.getDisplayNameLabel(), character.getHandRectangle());
        root.getChildren().addAll(character.getCollisions());
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

                    case ESCAPE -> {
                        if(this.gameTimeline.getStatus() != Animation.Status.PAUSED) {
                            this.root.getChildren().add(new PauseMenuController());
                        }
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

        setOnMouseMoved(e -> {
            this.inventory.getItemOnCursor().setTranslateX(e.getSceneX() - 16);
            this.inventory.getItemOnCursor().setTranslateY(e.getSceneY() - 16);
        });

        setOnMouseClicked(e -> {
            if(this.inventory.getItemStackOnCursor() != null
            && !this.inventory.isCellHovered()) {
                //Drop item
                this.camera.createDroppedBlock(this.inventory.getItemStackOnCursor(), e.getSceneX(), e.getSceneY());
                this.inventory.clearCursor();
                this.blockDropped = true;
            }

        });

    }

    private void initLookupTable() {
        lookupTable = new HashMap<>();
        lookupTable.put("0", "air");
        lookupTable.put("1", "dirt");
        lookupTable.put("2", "grass");
        lookupTable.put("3", "bedrock");
    }

    private class PauseMenuController extends VBox {
        public PauseMenuController() {
            setStyle("-fx-background-color: rgba(209, 222, 227, .5);" +
                    "-fx-min-height: 544;" +
                    "-fx-min-width: 1024;" +
                    "-fx-alignment: center;" +
                    "-fx-spacing: 12;");
            gameTimeline.pause();

            getChildren().addAll(addResumeButton(), addSaveAndExitButton());
            toFront();
        }

        private Button addResumeButton() {
            Button button = new Button("Resume");
            button.setOnAction(e -> { //Resume game
                root.getChildren().remove(this);
                gameTimeline.play();
            });
            return button;
        }

        private Button addSaveAndExitButton() {
            Button button = new Button("Save and Exit");
            button.setOnAction(e -> {
                //Save first
                Main.setScene(new MainMenuController());
            });
            return button;
        }
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    private void spawnZombie(int x) {
        Zombie zombie = new Zombie();
        //setup zombie stuff
        zombie.setTranslateX(x);
        this.zombies.add(zombie);
        this.root.getChildren().addAll(zombie.getNodes());

    }




}
