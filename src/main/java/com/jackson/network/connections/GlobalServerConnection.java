package com.jackson.network.connections;

import com.jackson.network.shared.Lobby;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GlobalServerConnection {

    private static Socket socket;
    private static ObjectOutputStream outStream;
    private static ObjectInputStream inStream;

    private static final int PORT = 4234;
    private static final String LAPTOP_IP = "192.168.0.36";



    private static void connectToServer() throws IOException {
        socket = new Socket("localhost", PORT); //initialises socket (localhost for testing only)
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
        thread.join(); //Waits for thread to terminate first before returning

        return lobbies;
    }

    public static double pingServer() { //Returns ping time in ms
        return -1;
    }

}
