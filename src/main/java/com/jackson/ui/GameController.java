package com.jackson.ui;

import com.jackson.game.Difficulty;
import com.jackson.game.MovementHandler;
import com.jackson.game.characters.Boss;
import com.jackson.game.characters.Player;
import com.jackson.game.characters.Zombie;
import com.jackson.game.items.Block;
import com.jackson.game.items.Entity;
import com.jackson.game.items.Item;
import com.jackson.game.items.ItemStack;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.network.connections.Client;
import com.jackson.network.connections.PseudoPlayer;
import com.jackson.ui.hud.CraftingMenu;
import com.jackson.ui.hud.Inventory;
import javafx.animation.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController extends Scene {

    public static HashMap<String, String> lookupTable;
    private final AnchorPane root;
    private final List<Zombie> zombies;
    private final Inventory inventory;
    private final HealthBar healthBar;
    private final StatMenu statMenu;
    private final CraftingMenu craftingMenu;
    private final MovementHandler movementHandler;
    private final Camera camera;
    private final Timeline gameTimeline;
    private final AudioPlayer audioplayer;
    private final AudioPlayer levelUpSound;
    private final AudioPlayer bossMusic;
    private final AudioPlayer walkingEffects;
    private final AudioPlayer jumpingEffects;
    private final Random rand;
    private final EventMessage eventMessage;
    private final PauseTransition bloodMoonTimer;
    private final Difficulty difficulty;
    private final boolean isSingleplayer;
    private double ZOMBIE_SPAWN_RATE;
    private Player character;
    private boolean blockDropped;
    private boolean isGamePaused;
    private boolean isAPressed;
    private boolean isDPressed;
    private boolean isWPressed;
    private boolean isSpacePressed;
    private Client client;

    // TODO: 24/10/2023 Add autosave feature -> on close and save

    public GameController(Difficulty difficulty, boolean isSingleplayer, Client client) {
        super(new VBox());

        this.client = client;
        this.difficulty = difficulty;
        this.isSingleplayer = isSingleplayer;

        //Root
        root = new AnchorPane();
        Main.applyWindowSize(root);

        //Initialises fields
        zombies = new CopyOnWriteArrayList<>();
        rand = new Random();
        String[][] map = isSingleplayer ? TextIO.readMapFile() : client.getMap(); //May produce NPE
        initLookupTable();
        this.isGamePaused = false;

        //Sound
        audioplayer = new AudioPlayer("background");
        audioplayer.play();

        levelUpSound = new AudioPlayer("levelup");
        levelUpSound.setCycleCount(1);
        levelUpSound.setVolume(0.4);

        walkingEffects = new AudioPlayer("walking");
        jumpingEffects = new AudioPlayer("jump");
//        bossMusic = new AudioPlayer("boss_music");
        bossMusic = new AudioPlayer("jump");

        //HUD
        SimpleBooleanProperty isHoldingGun = new SimpleBooleanProperty(false);
        SimpleIntegerProperty ammo = new SimpleIntegerProperty(0);
        SimpleIntegerProperty killCounter = new SimpleIntegerProperty(0);
        killCounter.addListener((observableValue, number, t1) -> {
            //Must kill 50 zombies for boss to spawn
            if (t1.intValue() == 50) spawnBoss();
        });

        SimpleBooleanProperty isBloodMoonActive = new SimpleBooleanProperty(false);
        isBloodMoonActive.addListener((observableValue, aBoolean, t1) -> setBloodMoon(t1));
        SimpleBooleanProperty isRainingRocks = new SimpleBooleanProperty(false);

        inventory = new Inventory(ammo);
        craftingMenu = new CraftingMenu(inventory);
        spawnCharacter(isHoldingGun, ammo);
        camera = new Camera(character, map, root, inventory, zombies, isBloodMoonActive, isRainingRocks, killCounter, bossMusic, this);
        healthBar = new HealthBar(character.healthProperty());
        statMenu = new StatMenu(character);
        eventMessage = new EventMessage(character);

        HBox ammoHbox = createCounterHBox(ammo, "bullet", false, 920);
        ammoHbox.visibleProperty().bind(character.isHoldingGunProperty());
        HBox killCounterHBox = createCounterHBox(killCounter, "zombieRun1", true, 10);

        root.getChildren().addAll(inventory.getInventoryVbox(), healthBar,
                statMenu, inventory.getItemOnCursor(), eventMessage, craftingMenu, ammoHbox, killCounterHBox);

        //Movement
        isAPressed = false;
        isDPressed = false;
        isWPressed = false;

        blockDropped = false;

        if (isSingleplayer) loadSaveData(TextIO.readFile("src/main/resources/saves/single_data.txt"));

        bloodMoonTimer = new PauseTransition();
        bloodMoonTimer.setDuration(Duration.minutes(2));
        bloodMoonTimer.setOnFinished(e -> setBloodMoon(false));
        setBloodMoon(false);


        setRoot(root);
        root.setId("root");
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");

        Main.getStage().setOnCloseRequest(e -> {
            if (isSingleplayer) {
                saveGame();
            } else {
                try {
                    //Save multiplayer
                    client.savePlayerData(camera.getPlayerData());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        movementHandler = new MovementHandler(character, camera);
        gameTimeline = new Timeline();
        gameTimeline.setCycleCount(Animation.INDEFINITE);
        gameTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(17), e -> {

            try {
                movementHandler.calculateXProperties(isAPressed, isDPressed, character); //character X movement
                movementHandler.calculateYProperties(isWPressed); //character y movement
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            movementHandler.calculateZombieMovement(zombies);

            if (camera.isBlockJustBroken() || blockDropped) { //Check to save cpu
                movementHandler.calculateDroppedBlockGravity(); //Block dropping
                blockDropped = false;
            }

            if (isSpacePressed) {
                character.attack(inventory.getSelectedItemStack());
            }

            camera.checkBlockPickup();
            camera.checkBlockBorder();
            spawnZombiePack();

            pushElementsToFront(eventMessage, ammoHbox, killCounterHBox);
        }));
        gameTimeline.play();
    }

    private void pushElementsToFront(EventMessage eventMessage, HBox ammoHbox, HBox killCounterHBox) {
        //Everything to front (maybe make a method for it)
        if (isGamePaused) return;
        inventory.getInventoryVbox().toFront();
        inventory.getItemOnCursor().toFront();
        character.toFront();
        character.getHandRectangle().toFront();
        healthBar.toFront();
        statMenu.toFront();
        eventMessage.toFront();
        craftingMenu.toFront();
        ammoHbox.toFront();
        killCounterHBox.toFront();
    }

    private void spawnZombiePack() {
        //Do check first
        if (!isSingleplayer) return;
        if (rand.nextDouble() > ZOMBIE_SPAWN_RATE) {
            return; //No spawn
        }
        //Spawn
        int packSize = (int) rand.nextGaussian(3, 1);
        int spawnTile = rand.nextInt(32) + 1;

        List<Zombie> pack = new ArrayList<>();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < packSize; i++) {
            var zombie = new Zombie(difficulty);
            zombie.setTranslateX(spawnTile * 32 + rand.nextDouble(25));
            zombie.setTranslateY(camera.getBlockTranslateY(spawnTile) - 48);
            zombie.translateXProperty().addListener((observableValue, number, t1) -> {
                if (zombie.canAttack() && character.intersects(zombie.getBoundsInParent())) {
                    zombie.attack(new Item("fist"));
                    character.takeDamage(zombie.getAttack());
                }
            });

            zombie.translateYProperty().addListener((observableValue, number, t1) -> {
                if (zombie.canAttack() && character.intersects(zombie.getBoundsInParent())) {
                    zombie.attack(new Item("fist"));
                    character.takeDamage(zombie.getAttack());
                }
            });

            pack.add(zombie);
            nodes.addAll(zombie.getNodes());
        }
        root.getChildren().addAll(nodes);
        zombies.addAll(pack);
    }

    private void spawnBoss() {
        Boss boss = new Boss(camera, character, rand.nextInt(400) + 300, 0, difficulty);
        boss.translateXProperty().addListener((observableValue, number, t1) -> {
            if (boss.canAttack()) {
                boss.attack(new Item("fist"));
            }
        });

        boss.translateYProperty().addListener((observableValue, number, t1) -> {
            if (boss.canAttack()) {
                boss.attack(new Item("fist"));
            }
        });

        //Start Boss Music
        if (!bossMusic.isPlaying()) bossMusic.playFromBeginning();

        root.getChildren().addAll(boss.getNodes());
        boss.toFront();
        zombies.add(boss);
    }

    public void loadSaveData(List<String> playerData) {
        if (playerData.isEmpty()) {
            camera.sendToSpawn();
        } else {
            character.setXPos(Integer.parseInt(playerData.get(0)));
            character.setYPos(Integer.parseInt(playerData.get(1)));
            camera.addXOffset(Integer.parseInt(playerData.get(2)));
            camera.addYOffset(Integer.parseInt(playerData.get(3)));
            CopyOnWriteArrayList<Zombie> e = new CopyOnWriteArrayList<>();

            String[] strength = playerData.get(4).split(" ");
            String[] agility = playerData.get(5).split(" ");
            String[] defence = playerData.get(6).split(" ");

            character.setStrength(Integer.parseInt(strength[0]), Integer.parseInt(strength[1]));
            character.setAgility(Integer.parseInt(agility[0]), Integer.parseInt(agility[1]));
            character.setDefence(Integer.parseInt(defence[0]), Integer.parseInt(defence[1]));
            character.setAmmo(Integer.parseInt(playerData.get(7)));
            character.setHealth(Double.parseDouble(playerData.get(8)));
            camera.setKillCounter(Integer.parseInt(playerData.get(isSingleplayer ? 10 : 9)));

            //Load Inventory

            for (int i = isSingleplayer ? 11 : 10; i < playerData.size(); i++) {
                String line = playerData.get(i);
                if (line.equals("null")) continue;

                String[] splitLine = line.split(" ");
                Entity entity;
                if (lookupTable.containsKey(splitLine[0])) {
                    entity = new Block(splitLine[0], -1, -1, camera, inventory);
                } else {
                    entity = new Item(splitLine[0]);
                }
                ItemStack itemStack = new ItemStack(entity);
                itemStack.addStackValue(Integer.parseInt(splitLine[1]));
                inventory.addItem(itemStack);
            }
            camera.initWorld();
        }
    }


    private void spawnCharacter(SimpleBooleanProperty isHoldingGun, SimpleIntegerProperty ammo) {
        character = new Player(isHoldingGun, ammo);

        character.healthProperty().addListener((observableValue, number, t1) -> {
            if (t1.doubleValue() <= 0) {
                root.getChildren().add(new PauseMenuController(true));
            } else if (t1.intValue() > 100) {
                System.err.println("Error: Health cannot be set above 100");
            }
        });


        inventory.getSelectedSlotIndex().addListener((observableValue, number, t1) -> {
            character.updateBlockInHand(inventory.getSelectedItemStack());
        });

        root.getChildren().addAll(character, character.getHandRectangle(), character.getAimingLine());
        root.getChildren().addAll(character.getCollisions());
        character.toFront();

        initOnKeyPressed();
    }

    public void addPlayer(PseudoPlayer player) {
        addOnlinePlayerIfOnScreen(player);
        root.getChildren().addAll(player.getImageView(), player.getDisplayNameLabel());
        camera.addOnlinePlayer(player);
    }

    public void removePlayer(PseudoPlayer player) {
        root.getChildren().removeAll(player.getImageView(), player.getDisplayNameLabel());
    }

    public void addOnlinePlayerIfOnScreen(PseudoPlayer player) {

        if (player.isOnScreen()) return;

        List<List<Block>> blocks = camera.getBlocks();
        int leftBorder = blocks.get(0).get(0).getXPos();
        int rightBorder = blocks.get(blocks.size() - 1).get(0).getXPos();
        int topBorder = blocks.get(0).get(0).getYPos();
        int bottomBorder = blocks.get(0).get(blocks.get(0).size() - 1).getYPos();
        if (player.getXPos() < leftBorder || player.getXPos() > rightBorder || player.getYPos() < topBorder || player.getYPos() > bottomBorder) {
            player.setOnScreen(false);
            return; //Not on screen
        }
        player.setOnScreen(true);
        for (List<Block> line : blocks) {
            if (line.get(0).getXPos() == player.getXPos()) {
                for (Block block : line) {
                    if (block.getYPos() == player.getYPos()) {
                        player.getImageView().setTranslateY(block.getTranslateY() + player.getyOffset() - 48);
                        player.getImageView().setTranslateX(block.getTranslateX() - 32);
                    }
                }
            }
        }
    }


    private void initOnKeyPressed() {

        setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A -> {
                    if (isGamePaused) return;
                    isAPressed = true;
                    character.setIsModelFacingRight(false);
                }
                case D -> {
                    if (isGamePaused) return;
                    isDPressed = true;
                    character.setIsModelFacingRight(true);
                }

                case W -> {
                    if (isGamePaused) return;
                    isWPressed = true;
                    character.setIdleImage();
                    jumpingEffects.playFromBeginning();
                }

                case DIGIT1 -> inventory.selectSlot(0);
                case DIGIT2 -> inventory.selectSlot(1);
                case DIGIT3 -> inventory.selectSlot(2);
                case DIGIT4 -> inventory.selectSlot(3);
                case DIGIT5 -> inventory.selectSlot(4);

                case I -> inventory.toggleInventory();
                case K -> statMenu.toggleShown();
                case C -> craftingMenu.toggleShown(gameTimeline);

                case ESCAPE -> {
                    if (gameTimeline.getStatus() != Animation.Status.PAUSED && !this.isGamePaused) {
                        root.getChildren().add(new PauseMenuController(false));
                    }
                }
                case SPACE -> {
                    if (isGamePaused) return;
                    isSpacePressed = true;
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
                case SPACE -> this.isSpacePressed = false;

            }
        });

        setOnMouseMoved(e -> {
            inventory.getItemOnCursor().setTranslateX(e.getSceneX() - 16);
            inventory.getItemOnCursor().setTranslateY(e.getSceneY() - 16);
            character.updateAimingLine((int) e.getSceneX(), (int) e.getSceneY());
        });

        setOnMouseClicked(e -> {
            //Move hand towards cursor

            if (inventory.getItemStackOnCursor() != null
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
        lookupTable.put("7", "plank");
        lookupTable.put("8", "metal_ore");
        lookupTable.put("9", "coal_ore");

        lookupTable.put("air", "0");
        lookupTable.put("dirt", "1");
        lookupTable.put("grass", "2");
        lookupTable.put("bedrock", "3");
        lookupTable.put("stone", "4");
        lookupTable.put("wood", "5");
        lookupTable.put("leaves", "6");
        lookupTable.put("plank", "7");
        lookupTable.put("metal_ore", "8");
        lookupTable.put("coal_ore", "9");
    }

    public void saveGame() {
        TextIO.writeMap(camera.getMap(), "src/main/resources/saves/singleplayer.txt");
        List<String> playerData = camera.getPlayerData();
        playerData.add(9, difficulty.name()); //Add difficulty
        TextIO.updateFile(playerData, "src/main/resources/saves/single_data.txt");
    }

    private void setBloodMoon(boolean value) {
        String backgroundColour = value ? "red, indianred" : "whitesmoke, deepskyblue";
        root.setStyle("-fx-background-color: linear-gradient(to top," + backgroundColour + ");" +
                "-fx-min-width: 1024;" +
                "-fx-min-height: 544;");
        //Blood moon vs Regular spawn rate
        ZOMBIE_SPAWN_RATE = value ? 0.003 : switch (difficulty) {
            case EASY -> 0.001;
            case MEDIUM -> 0.0012;
            case HARD -> 0.0015;
        };
        if (value) bloodMoonTimer.play();
    }

    private HBox createCounterHBox(SimpleIntegerProperty property, String imageName, boolean imageOnLeft, double x) {
        //Label styling
        Label label = new Label();
        label.textProperty().bind(property.asString());
        label.setStyle("-fx-font-size: 24;" +
                "-fx-font-weight: bold;" +
                "-fx-min-width: 50");
        label.setAlignment(imageOnLeft ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

        //Image
        ImageView imageView = new ImageView("file:src/main/resources/images/" + imageName + ".png");
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        imageView.setPreserveRatio(true);

        //Hbox that contains both
        HBox hbox = new HBox(12);
        if (imageOnLeft) {
            hbox.getChildren().addAll(imageView, label);
        } else {
            hbox.getChildren().addAll(label, imageView);
        }
        hbox.setMouseTransparent(true);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setTranslateX(x);
        hbox.setTranslateY(500);
        return hbox;
    }

    public void setClient(Client client) {
        this.client = client;
        camera.setClient(client);
    }

    public void updatePositionOnServer(int[] pos) throws IOException {
        client.updatePositionOnServer(pos);
    }

    public Camera getGameCamera() {
        return camera;
    }

    private class PauseMenuController extends VBox {

        // TODO: 29/12/2023 remove all dropped blocks on death

        public PauseMenuController(boolean isDead) {
            String colour = isDead ? "247, 45, 0" : "209, 222, 227";
            setStyle("-fx-background-color: rgba(" + colour + ",.5);" +
                    "-fx-min-height: 544;" +
                    "-fx-min-width: 1024;" +
                    "-fx-alignment: center;" +
                    "-fx-spacing: 12;");
            if (gameTimeline != null && isSingleplayer) {
                gameTimeline.pause();
            }
            audioplayer.pause();
            isGamePaused = true;

            Label title = new Label(isDead ? "You died" : "Paused");
            title.setStyle("-fx-font-weight: bold;" +
                    "-fx-font-size: 42");
            title.setTextFill(isDead ? Color.WHITE : Color.BLACK);


            getChildren().addAll(title, isDead ? addRespawnButton() : addResumeButton(), addSaveAndExitButton());
            toFront();


            if (isDead) {
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
                isGamePaused = false;
            });
            return button;
        }

        private Button addSaveAndExitButton() {
            Button button = new Button("Save and Exit");
            button.setOnAction(e -> {
                //Save first
                if (isSingleplayer) {
                    saveGame();
                } else {
                    try {
                        client.savePlayerData(camera.getPlayerData());
                        client.disconnect();
//                        client.closeClient();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                Main.setScene(new MainMenuController());
            });
            return button;
        }

        private Button addRespawnButton() {
            Button button = new Button("Respawn");
            button.setOnAction(e -> {
                camera.respawn();
                root.getChildren().removeAll(camera.getDroppedBlocks());
                camera.getDroppedBlocks().clear();
                root.getChildren().remove(this);
                gameTimeline.play();
                audioplayer.play();
                isGamePaused = false;
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

            setMouseTransparent(true);

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
            healthLabel.textProperty().bind(healthProperty.asString("%.0f"));

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
                double nextLevelReq = 50 * Math.pow(stat.get(), 1.5);
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
    public void setEventMessage(String text) {
        eventMessage.setText(text);
        eventMessage.setVisible(true);
        eventMessage.timer.play();
    }
}
