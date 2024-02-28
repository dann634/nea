package com.jackson.network.connections;

import com.jackson.game.Difficulty;
import com.jackson.game.items.Block;
import com.jackson.game.items.ItemStack;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.network.shared.Packet;
import com.jackson.ui.Camera;
import com.jackson.ui.GameController;
import com.jackson.ui.MainMenuController;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Client {

    private static final int PORT = 4234;
    private final ObjectOutputStream outStream;
    private final ObjectInputStream inStream;
    private final List<PseudoPlayer> players;
    private final String displayName;
    private GameController gameController;
    private Camera camera;
    private List<String> playerData;
    private String[][] map;

    public Client() throws IOException {
        Socket socket = new Socket("localhost", PORT); //initialises socket
        outStream = new ObjectOutputStream(socket.getOutputStream()); //initialises the outstream
        inStream = new ObjectInputStream(socket.getInputStream()); //initialises the instream
        players = new ArrayList<>(); //Used to hold other players
        displayName = TextIO.readFile("src/main/resources/settings/settings.txt").get(0);
    }

    //Starts the loop to listen for packets from the server
    public void startListening() {
        //Listen for object and pass it for processing
        //Error
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    //Listen for object and pass it for processing
                    Packet packet = (Packet) inStream.readObject();
                    processPacket(packet);
                }
            } catch (IOException | ClassNotFoundException e) {
                //Error
                Platform.runLater(() -> Main.setScene(new MainMenuController()));
            }
        });
        thread.setDaemon(true); //So thread closes with program
        thread.start(); //Start thread
    }

    /*
    Receives a packet as a parameter
    Does appropriate action based on packet header
     */
    private void processPacket(Packet packet) {
        switch (packet.getMsg()) {
            case "map" -> {
                //Receiving multiplayer map
                map = (String[][]) packet.getObject();
            }
            case "player_data" -> {
                //Store Player Data
                playerData = (ArrayList<String>) packet.getObject();
            }

            case "difficulty" -> {
                Platform.runLater(() -> {
                    try {
                        gameController = new GameController((Difficulty) packet.getObject(), false, this);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    gameController.loadSaveData(playerData); //Load Player Data
                    gameController.setClient(this); //Set Client
                    camera = gameController.getGameCamera();
                    Main.setScene(gameController); //Update Screen
                });
            }

            case "player_join" -> {
                int[] data = (int[]) packet.getObject();
                //Adds (fake) player that moves to show other players
                PseudoPlayer player = new PseudoPlayer(packet.getExt());
                players.add(player); //Adds player to list
                //Sets the players position
                player.setXPos(data[0]);
                player.setYPos(data[1]);
                player.setxOffset(-data[2]);
                player.setyOffset(data[3]);

                //Adds player to screen
                Platform.runLater(() -> {
                    gameController.addPlayer(player);
                    gameController.setEventMessage(player.getDisplayName() + " joined!");
                });
            }

            case "pos_update" -> {
                String targetName = packet.getExt();
                int[] posData = (int[]) packet.getObject();
                for (PseudoPlayer player : players) {
                    if (!player.getDisplayName().equals(targetName)) continue;
                    Platform.runLater(() -> {
                        player.translateX(-posData[2]);
                        player.translateY(-posData[3]);
                        player.setXPos(posData[0]);
                        player.setYPos(posData[1]);
                    });
                    return;
                }
            }

            case "disconnect" -> {
                Iterator<PseudoPlayer> playerIterator = players.listIterator();
                while (playerIterator.hasNext()) {
                    //Get next player in list
                    PseudoPlayer player = playerIterator.next();
                    if (!player.getDisplayName().equals(packet.getObject())) continue;
                    //If display name matches
                    playerIterator.remove(); //Remove from list
                    Platform.runLater(() -> {
                        //remove player from screen
                        gameController.removePlayer(player);
                        //alert other players
                        gameController.setEventMessage(player.getDisplayName() + " disconnected");
                    });

                }
            }

            case "remove_block" -> {
                int[] blockPos = (int[]) packet.getObject();
                Platform.runLater(() -> {
                    try {
                        camera.removeBlock(blockPos[0], blockPos[1], false);
                    } catch (IOException ignored) {
                    }
                });
            }

            case "place_block" -> {
                int[] blockPos = (int[]) packet.getObject();
                Platform.runLater(() -> {
                    camera.updateMap(blockPos[0], blockPos[1], packet.getExt());
                    Block block = camera.getBlock(blockPos[0], blockPos[1]);
                    if (block == null) return; //In case of invalid block
                    try {
                        //Update Screen
                        camera.placeBlock(block, GameController.lookupTable.get(packet.getExt()), false);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            case "zombie_spawn" -> {
                int[][] data = (int[][]) packet.getObject();
                Platform.runLater(() -> gameController.spawnZombiePack(data, packet.getExt().equals(displayName)));
            }

            case "damage_zombie" -> {
                try {
                    Platform.runLater(() -> gameController.damageZombie(
                            Integer.parseInt(packet.getExt()), (Integer) packet.getObject()));
                } catch (UnsupportedOperationException e) {
                    e.getStackTrace();
                } catch (ClassCastException ignored) {
                }
            }

            case "update_zombie_pos" -> {
                Platform.runLater(() -> gameController.updateZombiePos(Integer.parseInt(packet.getExt()), (double[]) packet.getObject()));
            }

            case "create_dropped_item" -> {
                int[] data = (int[]) packet.getObject();
                Platform.runLater(() -> gameController.createDroppedBlock(
                        data[3], packet.getExt(), data[2], data[0], data[1]));
            }

            case "pickup_item" -> {
                Platform.runLater(() -> gameController.pickupItem((int) packet.getObject()));
            }

            case "respawn" -> {
                Platform.runLater(() -> {
                    for (PseudoPlayer player : players) {
                        if (!player.getDisplayName().equals(packet.getExt())) continue;
                        player.setxOffset(0);
                        player.setyOffset(0);
                        player.setXPos(500);
                        player.setYPos(((int[]) packet.getObject())[1]);
                        gameController.setPseudoPlayerPos(player);
                        return;
                    }
                });
            }

            case "kick" -> {
                Platform.runLater(() -> {
                    Main.setScene(new MainMenuController());
                });
            }

            case "spawn_boss" -> {
                Platform.runLater(() -> gameController.spawnBoss(packet.getExt().equals(displayName), (int[]) packet.getObject()));
            }

            case "start_blood_moon" -> {
                gameController.setBloodMoon(true);
            }


        }

    }

    //Send methods create a packet and send it to the server
    private void send(String msg, Object object) throws IOException {
        Packet packet = new Packet(msg, object);
        outStream.writeObject(packet);
    }

    private void send(String msg, String ext, Object object) throws IOException {
        outStream.writeObject(new Packet(msg, object, ext));
    }

    //Sends a packet to the server asking if a world already exists
    public boolean doesWorldExist() throws IOException, ClassNotFoundException {
        send("world_check", null);
        return (Boolean) ((Packet) inStream.readObject()).getObject(); //might cause errors
    }

    //Sends a packet to the server asking if a display name is being used or not
    public boolean isUsernameUnique() throws IOException, ClassNotFoundException {
        send("username_check", displayName);
        return (Boolean) ((Packet) inStream.readObject()).getObject();
    }

    //Sends a packet to the server to join the game
    public void joinGame() throws IOException {
        send("join", displayName);
    }

    //Sends player data to the server in a packet
    public void savePlayerData(List<String> data) throws IOException {
        send("save_player_data", displayName, data);
    }

    //Updates position of player on server
    public void updatePositionOnServer(int[] data) throws IOException {
        send("pos_update", data);
    }

    //Sends a disconnect packet to the server
    public void disconnect() throws IOException {
        send("disconnect", null);
    }

    //Sends a place block packet to the server
    public void placeBlock(Block block) throws IOException {
        send("place_block", GameController.lookupTable.get(block.getItemName()), new int[]{block.getXPos(), block.getYPos()});
    }

    //Sends a remove block packet to the server
    public void removeBlock(Block block) throws IOException {
        send("remove_block", new int[]{block.getXPos(), block.getYPos()});
    }

    //Sends the server file to the server
    public void sendMap(String[][] map, Difficulty difficulty) throws IOException {
        send("map", map);
        send("difficulty", difficulty);
    }

    //Sends a packet to the server which deletes the save files
    public void deleteSave() throws IOException {
        send("delete_save", null);
    }

    //Sends a packet to the server when a zombie has taken damage
    public void damageZombie(int id, int damage) throws IOException {
        send("damage_zombie", String.valueOf(id), damage);
    }

    //Sends a packet to the server updating a zombie position
    public void updateZombiePos(int id, double x, double y) throws IOException {
        send("update_zombie_pos", String.valueOf(id), new double[]{x, y});
    }

    //Sends a packet to the server when a block has been dropped
    public void createDroppedItem(String itemName, int amount, int xPos, int yPos) throws IOException {
        send("create_dropped_item", itemName, new int[]{xPos, yPos, amount, 0});
    }

    //Sends a packet to the server when an item has been picked up by a player
    public void pickupItem(ItemStack itemStack) throws IOException {
        send("pickup_item", itemStack.getGameId());
    }

    //Sends a packet to the server when a player needs to respawn
    public void respawn() throws IOException {
        send("respawn", null);
    }

    //Saves player data and disconnects
    public void saveAndExit() throws IOException {
        savePlayerData(camera.getPlayerData());
        disconnect();
    }

    //Sends a packet to the server which spawns the boss
    public void spawnBoss(int[] data) throws IOException {
        send("spawn_boss", data);
    }

    //Sends a packet to the server which starts the blood moon
    public void startBloodMoon() throws IOException {
        send("start_blood_moon", null);
    }

    //Map getter
    public String[][] getMap() {
        return map;
    }
}
