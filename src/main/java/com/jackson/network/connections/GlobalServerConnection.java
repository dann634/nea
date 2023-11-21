package com.jackson.network.connections;

import com.jackson.network.shared.Lobby;
import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalServerConnection {

    private static Socket socket;
    private static ObjectOutputStream outStream;
    private static ObjectInputStream inStream;

    private static final int PORT = 4234;
    private static final String LAPTOP_IP = "192.168.0.36";
    private static final String SERVER_IP = "192.168.50.98";


    private static void connectToServer() throws IOException {
        socket = new Socket(SERVER_IP, PORT); //initialises socket (localhost for testing only)
        outStream = new ObjectOutputStream(socket.getOutputStream()); //initialises the outstream
        inStream = new ObjectInputStream(socket.getInputStream()); //initialises the instream
    }

    //Gets lobby list from main server
    public static List<Lobby> getLobbyList() throws IOException, InterruptedException {
        connectToServer(); //inits a connection to main server
        outStream.writeObject("ll"); //sends the request (ll = lobby list)
        List<Lobby> lobbies = new ArrayList<>();

        //Platform Thread
        Thread thread = new Thread(() -> {
            try {
                //will wait for response (may cause issues later)
                List<Lobby> tempList = (List<Lobby>) inStream.readObject();
                lobbies.addAll(tempList); //adds lobbies to list in scope
            } catch (IOException | ClassNotFoundException e) {
                //Handles error and outputs message
                System.err.println("Error: Failed to Process Response");
            }
        });
        thread.start(); //Starts thread
        thread.join(500); //Waits for thread to terminate first before returning
        // TODO: 19/08/2023 Holds up ui for 2 seconds if it doesnt connect 

        return lobbies;
    }

    public static double pingServer() throws IOException, InterruptedException { //Returns ping time in ms
        connectToServer();
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

}
