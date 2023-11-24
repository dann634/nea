package com.jackson.ui;

import com.jackson.game.*;
import com.jackson.game.characters.Player;
import com.jackson.game.characters.Zombie;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.ui.hud.HealthBar;
import com.jackson.ui.hud.Inventory;
import com.jackson.ui.hud.StatMenu;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GameController extends Scene {

    private final double ZOMBIE_SPAWN_RATE = 0.01;
    private static int spawnYCoords;
    private final AnchorPane root;
    private final List<Player> characters;
    private final List<Zombie> zombies;
    public static HashMap<String, String> lookupTable;
    private final Inventory inventory;
    private final HealthBar healthBar;
    private final StatMenu statMenu;
    private final MovementFactory movementFactory;
    private final Camera camera;
    private final Timeline gameTimeline;
    private final AudioPlayer audioPlayer;
    private final AudioPlayer walkingEffects;
    private final AudioPlayer jumpingEffects;
    private final Random rand;
    private boolean blockDropped;
    private boolean isAPressed;
    private boolean isDPressed;
    private boolean isWPressed;
    private boolean isSpacePressed;

    // TODO: 24/10/2023 Add autosave feature -> on close and save

    public GameController() {
        super(new VBox());

        //Root
        this.root = new AnchorPane();
        Main.applyWindowSize(this.root);

        //Initialises fields
        this.characters = new ArrayList<>();
        this.zombies = new ArrayList<>();
        this.rand = new Random();
        String[][] map = loadMap();
        initLookupTable();

        //Sound
        this.audioPlayer = new AudioPlayer("background");
        this.audioPlayer.play();

        this.walkingEffects = new AudioPlayer("walking");
        this.jumpingEffects = new AudioPlayer("jump");

        //HUD
        this.inventory = new Inventory();
        spawnCharacter();
        this.camera = new Camera(this.characters.get(0), map, this.root, this, this.inventory, this.zombies);
        this.healthBar = new HealthBar(this.characters.get(0).healthProperty());
        this.statMenu = new StatMenu(this.characters.get(0));

        this.root.getChildren().addAll(this.inventory.getInventoryVbox(), this.healthBar.getHealthHud(),
                statMenu, this.inventory.getItemOnCursor());

        //Movement
        this.isAPressed = false;
        this.isDPressed = false;
        this.isWPressed = false;
        this.isSpacePressed = false;

        this.blockDropped = false;

        this.camera.initWorld();
        this.characters.get(0).toFront();

        setRoot(this.root);
        this.root.setId("root");
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");

        this.movementFactory = new MovementFactory(this.characters.get(0),  this.camera);
        this.gameTimeline = new Timeline();
        this.gameTimeline.setCycleCount(Animation.INDEFINITE);
        this.gameTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(1000 / 60), e -> {

            this.movementFactory.calculateXProperties(this.isAPressed, this.isDPressed, this.walkingEffects); //Player X movement
            this.movementFactory.calculateYProperties(this.isWPressed); //Player y movement
            this.movementFactory.calculateZombieMovement(this.zombies);

            if(this.camera.isBlockJustBroken() || this.blockDropped) { //Check to save cpu
                this.movementFactory.calculateDroppedBlockGravity(); //Block dropping
                this.blockDropped = false;
            }

            this.camera.checkBlockPickup();
            this.camera.checkBlockBorder();
            spawnZombiePack();

            //Everything to front (maybe make a method for it)
            this.inventory.getInventoryVbox().toFront();
            this.inventory.getItemOnCursor().toFront();
            this.characters.get(0).toFront();
            this.characters.get(0).getHandRectangle().toFront();
            this.healthBar.getHealthHud().toFront();
            statMenu.toFront();

        }));
        this.gameTimeline.play();


    }

    private void spawnZombiePack() {
        //Do check first
        if(this.rand.nextDouble() > ZOMBIE_SPAWN_RATE) {
            return; //No spawn
        }
        //Spawn
        int packSize = (int) this.rand.nextGaussian(3, 1);
        int spawnTile = this.rand.nextInt(32) + 1;

        List<Zombie> pack = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < packSize; i++) {
            var zombie = new Zombie();
            zombie.setTranslateX(spawnTile * 32 + this.rand.nextDouble(25));
            zombie.setTranslateY(this.camera.getBlockTranslateY(spawnTile) - 48);
            pack.add(zombie);
            nodes.addAll(zombie.getNodes());
        }
        this.root.getChildren().addAll(nodes);
        this.zombies.addAll(pack);
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
                        this.jumpingEffects.playFromBeginning();
                    }

                    case I -> this.inventory.toggleInventory();
                    case DIGIT1 -> this.inventory.selectSlot(0);
                    case DIGIT2 -> this.inventory.selectSlot(1);
                    case DIGIT3 -> this.inventory.selectSlot(2);
                    case DIGIT4 -> this.inventory.selectSlot(3);
                    case DIGIT5 -> this.inventory.selectSlot(4);
                    case K -> this.statMenu.toggleShown();

                    case ESCAPE -> {
                        if(this.gameTimeline.getStatus() != Animation.Status.PAUSED) {
                            this.root.getChildren().add(new PauseMenuController());
                        }
                    }
                    case SPACE -> {
                        //Attack
                        this.isSpacePressed = true;
                        this.characters.get(0).attack(this.inventory.getSelectedItemStack());
                    }
                }
            });

        setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A -> this.isAPressed = false;
                case D -> this.isDPressed = false;
                case W -> {
                    this.isWPressed = false;
                    this.jumpingEffects.pause();
                }
                case SPACE -> this.isSpacePressed = false;

            }
        });

        setOnMouseMoved(e -> {
            this.inventory.getItemOnCursor().setTranslateX(e.getSceneX() - 16);
            this.inventory.getItemOnCursor().setTranslateY(e.getSceneY() - 16);
        });

        setOnMouseClicked(e -> {

            //Move hand towards cursor
//            this.characters.get(0).moveHand(e.getSceneX(), e.getSceneY());

            if(this.inventory.getItemStackOnCursor() != null
            && !this.inventory.isCellHovered()) {
                //Drop item
                this.camera.createDroppedBlock(this.inventory.getItemStackOnCursor(), e.getSceneX(), e.getSceneY());
                this.inventory.clearCursor();
                this.blockDropped = true;
                String blockInHand;
                try {
                    blockInHand = this.inventory.getSelectedItemStack().getItemName();
                } catch (NullPointerException error) {
                    blockInHand = "air";
                }
                this.characters.get(0).updateBlockInHand(blockInHand);
            }
        });

    }

    private void initLookupTable() {
        lookupTable = new HashMap<>();
        lookupTable.put("0", "air");
        lookupTable.put("1", "dirt");
        lookupTable.put("2", "grass");
        lookupTable.put("3", "bedrock");
        lookupTable.put("4", "stone");
        lookupTable.put("5", "wood");
        lookupTable.put("6", "leaves");
        lookupTable.put("air", "0");
        lookupTable.put("dirt", "1");
        lookupTable.put("grass", "2");
        lookupTable.put("bedrock", "3");
        lookupTable.put("stone", "4");
        lookupTable.put("wood", "5");
        lookupTable.put("leaves", "6");
    }

    private class PauseMenuController extends VBox {
        public PauseMenuController() {
            setStyle("-fx-background-color: rgba(209, 222, 227, .5);" +
                    "-fx-min-height: 544;" +
                    "-fx-min-width: 1024;" +
                    "-fx-alignment: center;" +
                    "-fx-spacing: 12;");
            gameTimeline.pause();
            audioPlayer.pause();

            getChildren().addAll(addResumeButton(), addSaveAndExitButton());
            toFront();
        }

        private Button addResumeButton() {
            Button button = new Button("Resume");
            button.setOnAction(e -> { //Resume game
                root.getChildren().remove(this);
                gameTimeline.play();
                audioPlayer.play();
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






}
