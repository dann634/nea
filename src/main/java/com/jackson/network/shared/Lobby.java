package com.jackson.network.shared;

import com.jackson.game.Difficulty;

import java.io.Serializable;

public class Lobby implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String hostIP;
    private int maxPlayers;
    private int currentPlayers;
    private Difficulty difficulty;
    private String password;

    public String getName() {
        return name;
    }

    public String getHostIP() {
        return hostIP;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getCurrentPlayers() {
        return currentPlayers;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getPassword() {
        return password;
    }
}
