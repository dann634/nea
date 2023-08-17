package com.jackson.network.connections;

import com.jackson.network.Lobby;
import com.jackson.network.packet.RequestPacket;

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
        socket = new Socket(LAPTOP_IP, PORT);
        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());
    }

    public static List<Lobby> getLobbyList() throws IOException {
        if(socket == null) {
            connectToServer();
        }
        final List<Lobby> lobbies = new ArrayList<>();
        Thread globalServerThread = new Thread(() -> {
            try {
                List<Lobby> tempList = (List<Lobby>) inStream.readObject();
                System.out.println(tempList.size());
                lobbies.addAll(tempList);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        globalServerThread.setDaemon(true);
        globalServerThread.start();
        return lobbies;
    }
}
