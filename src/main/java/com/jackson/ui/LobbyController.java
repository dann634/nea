package com.jackson.ui;

import com.jackson.network.Lobby;
import com.jackson.network.connections.GlobalServerConnection;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LobbyController {

    private Scene scene;

    private List<Lobby> lobbyList;

    public LobbyController() throws IOException {
        this.lobbyList = GlobalServerConnection.getLobbyList();
//        System.out.println(this.lobbyList.size());
    }





    public Scene getScene() {
        return this.scene;
    }

}
