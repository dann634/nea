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

    private final ObjectOutputStream outStream;
    private final ObjectInputStream inStream;
    private GameController gameController;
    private Camera camera;
    private final List<PseudoPlayer> players;
    private List<String> playerData;
    private final String displayName;
    private String[][] map;
    private static final int PORT = 4234;

    public Client() throws IOException {
        Socket socket = new Socket("localhost", PORT); //initialises socket
        outStream = new ObjectOutputStream(socket.getOutputStream()); //initialises the outstream
        inStream = new ObjectInputStream(socket.getInputStream()); //initialises the instream
        players = new ArrayList<>(); //Used to hold other players
        displayName = TextIO.readFile("src/main/resources/settings/settings.txt").get(0);
    }


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
                    gameController = new GameController((Difficulty) packet.getObject(), false, this);
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
                for(PseudoPlayer player : players) {
                    if(!player.getDisplayName().equals(targetName)) continue;
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
                    } catch (IOException ignored) {}
                });
            }

            case "place_block" -> {
                int[] blockPos = (int[]) packet.getObject();
                Platform.runLater(() -> {
                    camera.updateMap(blockPos[0], blockPos[1], packet.getExt());
                    Block block = camera.getBlock(blockPos[0], blockPos[1]);
                    if(block == null) return; //In case of invalid block
                    try {
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
                Platform.runLater(() -> gameController.damageZombie(
                        Integer.parseInt(packet.getExt()), (Integer) packet.getObject()));
            }

            case "update_zombie_pos" -> {
                Platform.runLater(() -> gameController.updateZombiePos(Integer.parseInt(packet.getExt()), (double[]) packet.getObject()));
            }

            case "create_dropped_item" -> {
                int[] data = (int[]) packet.getObject();
                Platform.runLater(() -> gameController.createDroppedBlock(data[3], packet.getExt(), data[2], data[0], data[1]));
            }

            case "pickup_item" -> {
                Platform.runLater(() -> gameController.pickupItem((int) packet.getObject()));
            }

            case "respawn" -> {
                Platform.runLater(() -> {
                    for(PseudoPlayer player : players) {
                        if(!player.getDisplayName().equals(packet.getExt())) continue;
                        player.setxOffset(0);
                        player.setyOffset(0);
                        player.setXPos(500);
                        player.setYPos(((int[]) packet.getObject())[1]);
                        gameController.setPseudoPlayerPos(player);
                        return;
                    }
                });
            }


            }

    }


    private void send(String msg, Object object) throws IOException {
        Packet packet = new Packet(msg, object);
        outStream.writeObject(packet);
    }

    private void send(String msg, String ext, Object object) throws IOException {
        outStream.writeObject(new Packet(msg, object, ext));
    }

    public boolean doesWorldExist() throws IOException, ClassNotFoundException {
        send("world_check", null);
        return (Boolean) ((Packet) inStream.readObject()).getObject(); //might cause errors
    }

    public boolean isUsernameUnique() throws IOException, ClassNotFoundException {
        send("username_check", displayName);
        return (Boolean) ((Packet) inStream.readObject()).getObject();
    }

    public void joinGame() throws IOException {
        send("join", displayName);
    }

    public void savePlayerData(List<String> data) throws IOException {
        send("save_player_data", displayName, data);
    }

    public void updatePositionOnServer(int[] data) throws IOException {
        send("pos_update", data);
    }

    public void disconnect() throws IOException {
        send("disconnect", null);
    }

    public void placeBlock(Block block) throws IOException { // TODO: 16/01/2024 maybe change to xPos, yPos, blockName
        send("place_block", GameController.lookupTable.get(block.getItemName()), new int[]{block.getXPos(), block.getYPos()});
    }

    public void removeBlock(Block block) throws IOException { // TODO: 16/01/2024 maybe change to xPos yPos
        send("remove_block", new int[]{block.getXPos(), block.getYPos()});
    }

    public void sendMap(String[][] map, Difficulty difficulty) throws IOException {
        send("map", map);
        send("difficulty", difficulty);
    }

    public void deleteSave() throws IOException {
        send("delete_save", null);
    }

    public void damageZombie(int id, int damage) throws IOException {
        send("damage_zombie", String.valueOf(id), damage);
    }

    public void updateZombiePos(int id, double x, double y) throws IOException {
        send("update_zombie_pos", String.valueOf(id), new double[]{x,y});
    }

    public void createDroppedItem(String itemName, int amount, int xPos, int yPos) throws IOException {
        send("create_dropped_item", itemName, new int[]{xPos, yPos, amount, 0});
    }

    public void pickupItem(ItemStack itemStack) throws IOException {
        send("pickup_item", itemStack.getGameId());
    }

    public void respawn() throws IOException {
        send("respawn", null);
    }

    public void saveAndExit() throws IOException {
        savePlayerData(camera.getPlayerData());
        disconnect();
    }

    public String[][] getMap() {
        return map;
    }


}
