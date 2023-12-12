package com.jackson.game.items;

public class Item extends Entity {

    public Item(String itemName) {
        super(itemName);
        this.isUsable = true;
        if(itemName.contains("axe") || itemName.contains("shovel") || itemName.contains("sword")) {

        }
    }

    public Item(String itemName, double x, double y) {
        super(itemName, x, y);
        this.isUsable = true;
    }


}
