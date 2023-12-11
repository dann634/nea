package com.jackson.ui;

import com.jackson.game.MovementFactory;
import com.jackson.game.characters.Player;
import com.jackson.game.characters.Zombie;
import com.jackson.game.items.Item;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.ui.hud.CraftingMenu;
import com.jackson.ui.hud.Inventory;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;

public class GameController extends Scene {

    private final double ZOMBIE_SPAWN_RATE = 0.0;
    private final AnchorPane root;
    private Player character;
    private final List<Zombie> zombies;
    public static HashMap<String, String> lookupTable;
    private final Inventory inventory;
    private final HealthBar healthBar;
    private final StatMenu statMenu;
    private final CraftingMenu craftingMenu;
    private final MovementFactory movementFactory;
    private final Camera camera;
    private final Timeline gameTimeline;
    private final AudioPlayer audioplayer;
    private final AudioPlayer levelUpSound;
    private final AudioPlayer walkingEffects;
    private final AudioPlayer jumpingEffects;
    private final Random rand;
    private boolean blockDropped;
    private boolean isAPressed;
    private boolean isDPressed;
    private boolean isWPressed;

    // TODO: 24/10/2023 Add autosave feature -> on close and save

    public GameController() {
        super(new VBox());

        //Root
        root = new AnchorPane();
        Main.applyWindowSize(root);

        //Initialises fields
        zombies = new ArrayList<>();
        rand = new Random();
        String[][] map = TextIO.readMapFile(true);
        initLookupTable();

        //Sound
        audioplayer = new AudioPlayer("background");
        audioplayer.play();

        levelUpSound = new AudioPlayer("levelup");
        levelUpSound.setCycleCount(1);
        levelUpSound.setVolume(0.4);

        walkingEffects = new AudioPlayer("walking");
        jumpingEffects = new AudioPlayer("jump");

        //HUD
        inventory = new Inventory();
        craftingMenu = new CraftingMenu(inventory);
        spawnCharacter();
        camera = new Camera(character, map, root, this, inventory, zombies);
        healthBar = new HealthBar(character.healthProperty());
        statMenu = new StatMenu(character);
        EventMessage eventMessage = new EventMessage(character);

        root.getChildren().addAll(inventory.getInventoryVbox(), healthBar,
                statMenu, inventory.getItemOnCursor(), eventMessage, craftingMenu);

        //Movement
        isAPressed = false;
        isDPressed = false;
        isWPressed = false;

        blockDropped = false;

        //Send character
        List<String> playerData = TextIO.readFile("src/main/resources/saves/single_data.txt");
        if(playerData.isEmpty()) {
            camera.sendToSpawn();
        } else {
            character.setXPos(50); //Integer.parseInt(playerData.get(0))
            character.setYPos(140); //Integer.parseInt(playerData.get(1))
            camera.addXOffset(Integer.parseInt(playerData.get(2)));
            camera.addYOffset(Integer.parseInt(playerData.get(3)));
            camera.initWorld();
        }


        setRoot(root);
        root.setId("root");
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");

        Main.getStage().setOnCloseRequest(e -> saveGame()); //Saves when red cross clicked

        movementFactory = new MovementFactory(character,  camera);
        gameTimeline = new Timeline();
        gameTimeline.setCycleCount(Animation.INDEFINITE);
        gameTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(17), e -> {

            movementFactory.calculateXProperties(isAPressed, isDPressed, character); //character X movement
            movementFactory.calculateYProperties(isWPressed); //character y movement
            movementFactory.calculateZombieMovement(zombies);

            if(camera.isBlockJustBroken() || blockDropped) { //Check to save cpu
                movementFactory.calculateDroppedBlockGravity(); //Block dropping
                blockDropped = false;
            }

            camera.checkBlockPickup();
            camera.checkBlockBorder();
            spawnZombiePack();

            //Everything to front (maybe make a method for it)
            inventory.getInventoryVbox().toFront();
            inventory.getItemOnCursor().toFront();
            character.toFront();
            character.getHandRectangle().toFront();
            healthBar.toFront();
            statMenu.toFront();
            eventMessage.toFront();
            craftingMenu.toFront();

        }));
        gameTimeline.play();


    }

    private void spawnZombiePack() {
        //Do check first
        if(rand.nextDouble() > ZOMBIE_SPAWN_RATE) {
            return; //No spawn
        }
        //Spawn
        int packSize = 1;
                //(int) rand.nextGaussian(3, 1);
        int spawnTile = rand.nextInt(32) + 1;

        List<Zombie> pack = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < packSize; i++) {
            var zombie = new Zombie();
            zombie.setTranslateX(spawnTile * 32 + rand.nextDouble(25));
            zombie.setTranslateY(camera.getBlockTranslateY(spawnTile) - 48);
            zombie.translateXProperty().addListener((observableValue, number, t1) -> {
                if(zombie.canAttack() && character.intersects(zombie.getBoundsInParent())) {
                    zombie.attack(new Item("fist"));
                    character.takeDamage(rand.nextInt(5) + 1);
                }
            });

            zombie.translateYProperty().addListener((observableValue, number, t1) -> {
                if(zombie.canAttack() && character.intersects(zombie.getBoundsInParent())) {
                    zombie.attack(new Item("fist"));
                    character.takeDamage(rand.nextInt(5) + 1);
                }
            });

            pack.add(zombie);
            nodes.addAll(zombie.getNodes());
        }
        root.getChildren().addAll(nodes);
        zombies.addAll(pack);
    }



    private void spawnCharacter() {
        character = new Player();

        character.healthProperty().addListener((observableValue, number, t1) -> {
            if(t1.doubleValue() <= 0) {
                root.getChildren().add(new PauseMenuController(true));
            }
        });


        inventory.getSelectedSlotIndex().addListener((observableValue, number, t1) -> {
            character.updateBlockInHand(inventory.getSelectedItemStack());
        });

        root.getChildren().addAll(character, character.getDisplayNameLabel(), character.getHandRectangle());
        root.getChildren().addAll(character.getCollisions());
        character.toFront();

        initOnKeyPressed();
    }




    private void initOnKeyPressed() {

            setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case A -> {
                        isAPressed = true;
                        character.setIsModelFacingRight(false);
                    }
                    case D -> {
                        isDPressed = true;
                        character.setIsModelFacingRight(true);
                    }

                    case W -> {
                        isWPressed = true;
                        character.setIdleImage();
                        jumpingEffects.playFromBeginning();
                    }

                    case I -> inventory.toggleInventory();
                    case DIGIT1 -> inventory.selectSlot(0);
                    case DIGIT2 -> inventory.selectSlot(1);
                    case DIGIT3 -> inventory.selectSlot(2);
                    case DIGIT4 -> inventory.selectSlot(3);
                    case DIGIT5 -> inventory.selectSlot(4);
                    case K -> statMenu.toggleShown();
                    case C -> {
                        craftingMenu.toggleShown(gameTimeline);
                    }

                    case ESCAPE -> {
                        if(gameTimeline.getStatus() != Animation.Status.PAUSED) {
                            root.getChildren().add(new PauseMenuController(false));
                        }
                    }
                    case SPACE -> {
                        //Attack
                        character.attack(inventory.getSelectedItemStack());
                    }
                }
            });

        setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A -> isAPressed = false;
                case D -> isDPressed = false;
                case W -> {
                    isWPressed = false;
                    jumpingEffects.pause();
                }

            }
        });

        setOnMouseMoved(e -> {
            inventory.getItemOnCursor().setTranslateX(e.getSceneX() - 16);
            inventory.getItemOnCursor().setTranslateY(e.getSceneY() - 16);
        });

        setOnMouseClicked(e -> {
            //Move hand towards cursor

            if(inventory.getItemStackOnCursor() != null
            && !inventory.isCellHovered()) {
                //Drop item
                camera.createDroppedBlock(inventory.getItemStackOnCursor(), e.getSceneX(), e.getSceneY());
                inventory.clearCursor();
                blockDropped = true;
                character.updateBlockInHand(inventory.getSelectedItemStack());
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

    public Inventory getInventory() {
        return inventory;
    }

    private class PauseMenuController extends VBox {
        public PauseMenuController(boolean isDead) {
            String colour  = isDead ? "247, 45, 0" : "209, 222, 227";
            setStyle("-fx-background-color: rgba(" + colour + ",.5);" +
                    "-fx-min-height: 544;" +
                    "-fx-min-width: 1024;" +
                    "-fx-alignment: center;" +
                    "-fx-spacing: 12;");
            gameTimeline.pause();
            audioplayer.pause();

            Label title = new Label(isDead ? "You died" : "Paused");
            title.setStyle("-fx-font-weight: bold;" +
                    "-fx-font-size: 42");
            title.setTextFill(isDead ? Color.WHITE : Color.BLACK);


            getChildren().addAll(title, isDead ? addRespawnButton() : addResumeButton(), addSaveAndExitButton());
            toFront();


            if(isDead) {

                //Stupid way of doing it (concurrency issue)
                PauseTransition bringToFront = new PauseTransition();
                bringToFront.setDuration(Duration.millis(10));
                bringToFront.setOnFinished(e -> toFront());
                bringToFront.play();
            }


        }

        private Button addResumeButton() {
            Button button = new Button("Resume");
            button.setOnAction(e -> { //Resume game
                root.getChildren().remove(this);
                gameTimeline.play();
                audioplayer.play();
            });
            return button;
        }

        private Button addSaveAndExitButton() {
            Button button = new Button("Save and Exit");
            button.setOnAction(e -> {
                //Save first
                saveGame();
                Main.setScene(new MainMenuController());
            });
            return button;
        }

        private Button addRespawnButton() {
            Button button = new Button("Respawn");
            button.setOnAction(e -> {
                camera.respawn();
                root.getChildren().remove(this);
                gameTimeline.play();
                audioplayer.play();
            });
            button.toFront();
            return button;
        }
    }

    private class EventMessage extends Label {

        private final PauseTransition timer;

        public EventMessage(Player character) {
            timer = new PauseTransition();
            timer.setDuration(Duration.millis(2000));
            timer.setOnFinished(e -> setVisible(false));
            setId("eventMessage");




            character.strengthLevelProperty().addListener((observableValue, number, t1) -> {
                setVisible(true);
                setText("Strength Level Up!!");
                timer.play();
                levelUpSound.play();
            });

            character.agilityLevelProperty().addListener((observableValue, number, t1) -> {
                setVisible(true);
                setText("Agility Level Up!!");
                timer.play();
                levelUpSound.play();
            });

            character.defenceLevelProperty().addListener((observableValue, number, t1) -> {
                setVisible(true);
                setText("Defence Level Up!!");
                timer.play();
                levelUpSound.play();
            });
        }
    }

    private class HealthBar extends HBox {
        public HealthBar(SimpleDoubleProperty healthProperty) {
            //Initialising Values
            ProgressBar healthBar = new ProgressBar(1);
            Label healthLabel = new Label("");
            getStyleClass().add("darkBackground");

            //Binds label and progress bar to health
            healthBar.progressProperty().bind(healthProperty.divide(100));
            healthLabel.textProperty().bind(Bindings.format(new Locale("en", "uk")
                    , "%.0f",
                    healthProperty));

            setId("healthBox");
            healthBar.setId("healthBar");
            healthLabel.setId("healthLabel");

            this.getChildren().addAll(healthLabel, healthBar);
        }
    }


    private class StatMenu extends VBox {
        private final TranslateTransition translate;
        private boolean isVisible;

        public StatMenu(Player player) {
            //Initialise the hbox's
            HBox strengthHBox = createHBox("Strength", player.strengthLevelProperty(), player.strengthXPProperty());
            HBox agilityHBox = createHBox("Agility", player.agilityLevelProperty(), player.agilityXPProperty());
            HBox defenceHBox = createHBox("Defence", player.defenceLevelProperty(), player.defenceXPProperty());
            this.isVisible = false;

            Label title = new Label("Stats");
            title.getStyleClass().add("title");

            //Adds elements to main vbox
            getChildren().addAll(title, strengthHBox, agilityHBox, defenceHBox);
            setId("statMenu");
            getStyleClass().add("darkBackground");

            //Initialise animation
            this.translate = new TranslateTransition();
            this.translate.setNode(this);
            this.translate.setRate(1);
            this.translate.setInterpolator(Interpolator.EASE_BOTH);
            this.translate.setOnFinished(e -> isVisible = !isVisible);
        }

        private HBox createHBox(String statName, SimpleIntegerProperty stat, SimpleIntegerProperty currentXP) {
            Label statNameLabel = new Label(statName + ":"); //What the stat is called
            statNameLabel.getStyleClass().add("statNumbers");

            Pane spacer = new Pane(); //Invisible to account for different text lengths
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label currentLevel = new Label("1"); //Current level
            currentLevel.getStyleClass().add("statNumbers");
            currentLevel.textProperty().bind(stat.asString()); //Binds to current level property

            ProgressBar xpBar = new ProgressBar();
            xpBar.getStyleClass().add("xpBar");
            currentXP.addListener((observableValue, number, t1) -> {
                //When current xp changes, the progress bar is updated
                double currentexp = t1.doubleValue();
                double lastLevelReq = 50 * Math.pow(stat.get() - 1, 1.5);
                double nextLevelReq =  50 * Math.pow(stat.get(), 1.5);
                xpBar.setProgress((currentexp - lastLevelReq) / (nextLevelReq - lastLevelReq));
            });

            //Adds everything to a hbox
            HBox hBox = new HBox();
            hBox.getChildren().addAll(statNameLabel, spacer, currentLevel, xpBar);
            hBox.getStyleClass().add("stat");
            return hBox;
        }

        //Switches between shown and hidden
        public void toggleShown() {
            this.translate.setToX(this.isVisible ? 1324 : 714);
            this.translate.play();
        }
    }

    public void saveGame() {
        TextIO.writeMap(camera.getMap() , "src/main/resources/saves/singleplayer.txt");
        TextIO.updateFile(camera.getPlayerData(), "src/main/resources/saves/single_data.txt");
    }








}
