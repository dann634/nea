package com.jackson.game.items;

import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.Random;

public class Entity extends VBox {

    protected ImageView imageView; //Imageview can be accessed from same package
    protected String itemName; //Item name can be accessed from same package
    protected boolean isUsable; //Tools + Weapons

    public Entity(String itemName) {
        init(itemName);
    }

    //Can initialise object with x and y positions
    public Entity(String itemName, double x, double y) {
        init(itemName);

        //Init Pos
        this.setTranslateX(x);
        this.setTranslateY(y);
    }

    private void init(String itemName) {
        //Init icon
        this.itemName = itemName;

        //Init imageview
        this.imageView = new ImageView(new Image("file:src/main/resources/images/" + this.itemName + ".png"));
        this.imageView.setPreserveRatio(true);
        setSize(16);

        //Optimisation
        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);
        this.imageView.setSmooth(false);

        //Add Nodes to Vbox
        this.getChildren().add(this.imageView);
    }

    //Item name getter
    public String getItemName() {
        return itemName;
    }

    //Sets entity size
    public void setSize(double size) {
        this.setPrefHeight(size);
        this.setPrefWidth(size);
        this.imageView.setFitHeight(size);
        this.imageView.setFitWidth(size);
    }

    //Sets position on screen
    public void setPos(double x, double y) {
        this.setTranslateX(x);
        this.setTranslateY(y);
    }

    //Updates the position on screen
    public void addPos(double x, double y) {
        this.setTranslateX(getTranslateX() + x);
        this.setTranslateY(getTranslateY() + y);
    }

    //Screen position getters
    public double getX() {
        return this.getTranslateX();
    }

    public double getY() {
        return this.getTranslateY();
    }

    //Resets entity rotation back to 0
    public void resetRotation() {
        this.imageView.setRotate(0);
    }

    //Gives the entity a random rotation
    public void randomRotation() {
        this.imageView.setRotate(new Random().nextDouble(360));
    }

    //is entity usable getter
    public boolean isUsable() {
        return isUsable;
    }
}
