package com.jackson.network.connections;

import com.jackson.network.shared.Lobby;
import com.jackson.network.shared.RequestPacket;

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
        socket = new Socket("localhost", PORT);
        System.out.println("Connected to server");
        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());
    }

    public static List<Lobby> getLobbyList() throws IOException { //Gets lobby list from main server
        connectToServer(); //inits a connection to main server
        outStream.writeObject(new RequestPacket("ll")); //sends the request (ll = lobby list)
        final List<Lobby> lobbies = new ArrayList<>();
        Thread thread = new Thread(() -> { //new thread to not block ui thread
            try {
                List<Lobby> tempList = (List<Lobby>) inStream.readObject(); //will wait for response (may cause issues later)
                System.out.println("Response Recieved");
                lobbies.addAll(tempList);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        thread.setDaemon(true);
        thread.start();
        return lobbies;
    }
}
