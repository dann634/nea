package com.jackson.network.connections;

import com.jackson.game.Difficulty;
import com.jackson.game.characters.Player;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.network.shared.Lobby;
import com.jackson.network.shared.Packet;
import com.jackson.ui.GameController;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Client {

    private final Socket socket;
    private final ObjectOutputStream outStream;
    private final ObjectInputStream inStream;
    private GameController gameController;
    private final List<PseudoPlayer> players;
    private List<String> playerData;
    private final String displayName;
    private Thread thread;
    private static final int PORT = 4234;
    private static final String LAPTOP_IP = "192.168.0.36";
    private static final String SERVER_IP = "192.168.50.98";

    public Client() throws IOException {
        socket = new Socket("localhost", PORT); //initialises socket (localhost for testing only)
        outStream = new ObjectOutputStream(socket.getOutputStream()); //initialises the outstream
        inStream = new ObjectInputStream(socket.getInputStream()); //initialises the instream
        players = new ArrayList<>();
        displayName = TextIO.readFile("src/main/resources/settings/settings.txt").get(0);
    }


    public void startListening() {
        Runnable runnable = () -> {
            try {
                while(true) {
                    Packet packet = (Packet) inStream.readObject();
                    processPacket(packet);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        };
        thread = Thread.ofVirtual().start(runnable);

    }

    private void processPacket(Packet packet) throws IOException {
        switch (packet.getMsg()) {
            case "map" -> {
                //Recieving multiplayer map
                String dir = "src/main/resources/saves/multiplayer.txt";
                Files.deleteIfExists(Path.of(dir));
                Files.createFile(Path.of(dir));
                TextIO.writeMap((String[][]) packet.getObject(), dir);
            }
            case "player_data" -> {
                playerData = (ArrayList<String>) packet.getObject();
            }

            case "difficulty" -> {
                Platform.runLater(() -> {
                    gameController = new GameController((Difficulty) packet.getObject(), false);
                    gameController.loadSaveData(playerData);
                    gameController.setClient(this);
                    Main.setScene(gameController);
                });
            }

            case "player_join" -> {
                int[] data = (int[]) packet.getObject();
                PseudoPlayer player = new PseudoPlayer(packet.getExt());
                players.add(player);
                player.setXPos(data[0]);
                player.setYPos(data[1]);
                player.setxOffset(data[2]);
                player.setyOffset(data[3]);
                Platform.runLater(() -> {
                    gameController.addPlayer(player);
                });
            }

            case "pos_update" -> {
                String targetName = packet.getExt();
                int[] posData = (int[]) packet.getObject();
                for(PseudoPlayer player : players) {
                    if(player.getDisplayName().equals(targetName)) {
                        Platform.runLater(() -> {
                            gameController.addOnlinePlayerIfOnScreen(player);
                            player.translateX(-posData[2]);
                            player.translateY(-posData[3]);
                            player.setXPos(posData[0]);
                            player.setYPos(posData[1]);
                        });
                        return;
                    }
                }
            }
        }
    }

    public double pingServer() throws IOException, InterruptedException { //Returns ping time in ms
        Date beforePacket = new Date();
        outStream.writeObject("ping"); //Send Request#
        AtomicLong ping = new AtomicLong(-1);
        Thread thread = new Thread(() -> {
            try {
                inStream.readObject(); //We don't care about response packet
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            ping.set(new Date().getTime() - beforePacket.getTime());
        });
        thread.start();
        thread.join(500);
        return ping.get();
    }

    public void send(String msg, Object object) throws IOException {
        Packet packet = new Packet(msg, object);
        outStream.writeObject(packet);
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
        data.add(displayName);
        send("save_player_data", data);
    }

    public void updatePositionOnServer(int[] data) throws IOException {
        send("pos_update", data);
    }

    public void closeClient() throws IOException {
        thread.interrupt();
        inStream.close();
        outStream.close();
        socket.close();
    }


}
