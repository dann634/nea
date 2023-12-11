package com.jackson.game.items;

import com.jackson.game.items.Entity;

public class Item extends Entity {

    public Item(String itemName) {
        super(itemName);
        this.isUsable = true;
    }

    public Item(String itemName, double x, double y) {
        super(itemName, x, y);
        this.isUsable = true;
    }


}
