package com.jackson.ui;

import com.jackson.network.shared.Lobby;
import com.jackson.network.connections.GlobalServerConnection;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.List;

public class LobbyController {

    private Scene scene;

    private List<Lobby> lobbyList;

    public LobbyController() throws IOException, InterruptedException {
        this.lobbyList = GlobalServerConnection.getLobbyList();

    }







    public Scene getScene() {
        return this.scene;
    }

}
