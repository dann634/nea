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



}
