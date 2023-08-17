package com.jackson.ui;

import com.jackson.network.Lobby;
import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public class LobbyController {

    private Scene scene;

    private List<Lobby> lobbyList;

    public LobbyController() {
        this.lobbyList = loadLobbyData();
    }


    private List<Lobby> loadLobbyData() {
        List<Lobby> lobbies = new ArrayList<>();

        return lobbies;
    }



    public Scene getScene() {
        return this.scene;
    }

}
