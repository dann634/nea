package com.jackson.game.characters;

import javafx.scene.image.Image;

public class Zombie extends Character {

    public Zombie() {
        super();
    }

    @Override
    public void setIdleImage() {
        setImage(new Image("file:src/main/resources/images/zombieRun1.png"));
    }
}
