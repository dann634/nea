package com.jackson.network.shared;

import java.util.List;

public class LobbyPacket extends Packet {
    private List<Lobby> lobbyList;

    public void addLobby(Lobby lobby) {
        this.lobbyList.add(lobby);
    }

//    public List<Lo>
}
