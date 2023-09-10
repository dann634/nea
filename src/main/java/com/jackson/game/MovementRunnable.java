package com.jackson.game;

import com.jackson.ui.GameController;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class MovementRunnable implements Runnable {

    private static final double FPS = 60;
    private GameController gameController;
    private Character character;

    private SimpleBooleanProperty isAPressed;
    private SimpleBooleanProperty isDPressed;
    private SimpleBooleanProperty isWPressed;

    public MovementRunnable(Character character, GameController gameController) { //Start new thread for each character (Maybe change later)
        //Look into making a virtual thread
        this.gameController = gameController;
        this.character = character;

        this.isAPressed = new SimpleBooleanProperty(false);
        this.isDPressed = new SimpleBooleanProperty(false);
        this.isWPressed = new SimpleBooleanProperty(false);

        this.isWPressed.or(this.isAPressed).or(this.isDPressed).addListener((observable, oldValue, newValue) -> {
            //If character approaches borders update world
        });

    }

    @Override
    public void run() {
        double prevTime = System.currentTimeMillis();
        while (true) {

            if (System.currentTimeMillis() - prevTime < 1000 / FPS) { //FPS lock
                continue;
            }
            prevTime = System.currentTimeMillis();

            calculateYProperties();
            calculateXProperties();
        }
    }

    private void calculateYProperties() {
        if (!this.gameController.isEntityTouchingGround(this.character)) {
            Platform.runLater(() -> this.character.setY(this.character.getY() + 1)); //Gravity
        }
    }

    private void calculateXProperties() {
        System.out.printf("%s %s \n", this.isAPressed.get(), this.isDPressed.get());
        if (this.isAPressed.get() != this.isDPressed.get()) {
            Platform.runLater(() -> character.setX(character.getX() + (this.isAPressed.get() ? -3 : 3))); //Only works for left side
        }
    }

    public void setIsAPressed(boolean isAPressed) {
        this.isAPressed.set(isAPressed);
    }

    public void setIsDPressed(boolean isDPressed) {
        this.isDPressed.set(isDPressed);
    }

    public void setIsWPressed(boolean isWPressed) {
        this.isAPressed.set(isWPressed);
    }
}
