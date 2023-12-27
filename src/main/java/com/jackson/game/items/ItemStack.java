package com.jackson.game.items;

import com.jackson.ui.hud.Inventory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;

public class ItemStack extends Entity { //Child of Entity
    private SimpleIntegerProperty stackSize;
    private int maxStackSize;
    private Label stackSizeLabel;


    public ItemStack(Entity item) {
        super(item.getItemName(), item.getTranslateX(), item.getTranslateY()); //Parent constructor
        this.setMouseTransparent(true); //Needs to not block mouse events

        this.isUsable = item.isUsable;

        initStackSize(); //Inits Label
        if(item instanceof Item) {
            if(itemName.contains("axe") || itemName.contains("shovel") || itemName.contains("sword")
            || itemName.contains("pistol") || itemName.contains("rifle") || itemName.contains("sniper")) {
                maxStackSize = 1;
            } else {
                maxStackSize = 20;
            }
        } else {
            maxStackSize = 100;
        }

        //Adds label to vbox
        this.getChildren().add(this.stackSizeLabel);

    }

    private void initStackSize() {
        this.stackSize = new SimpleIntegerProperty(0); //Stack size starts at 0
        this.stackSizeLabel = new Label(); //Initialises Label
        this.stackSizeLabel.textProperty().bind(this.stackSize.asString()); //Binds label text to the stack size
        this.stackSizeLabel.setMaxWidth(15);
        this.stackSizeLabel.setMouseTransparent(false);
        //Repositions Label in relation to image
        this.stackSizeLabel.setTranslateY(-10);
        this.stackSizeLabel.setTranslateX(12);
        //Width makes easier to manage
        this.stackSizeLabel.setPrefWidth(Inventory.getSlotSize() - 6);
        //If value is stack size is 1 label is invisible
        this.stackSizeLabel.opacityProperty().bind(this.stackSize.subtract(1));
        //Styling
        this.stackSizeLabel.getStyleClass().addAll("outline", "itemStack");
    }


    //Getters and Setters

    public int getStackSize() {
        return stackSize.get();
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public void addStackValue(int value) {
        this.stackSize.set(this.stackSize.get() + value);
    }





}
