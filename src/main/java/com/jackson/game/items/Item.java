package com.jackson.game.items;

public class Item extends Entity {

    public Item(String itemName) {
        super(itemName);
        this.isUsable = true;
        fixGuns();
    }

    public Item(String itemName, double x, double y) {
        super(itemName, x, y);
        this.isUsable = true;
        fixGuns();
    }

    private void fixGuns() {
        if(itemName.equals("rifle") || itemName.equals("sniper")) {
            setSize(100);
        }
    }


}
