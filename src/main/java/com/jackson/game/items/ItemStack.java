package com.jackson.game.items;

import com.jackson.ui.hud.Inventory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;

public class ItemStack extends Entity { //Child of Entity
    private int durability;
    private SimpleIntegerProperty stackSize;
    private final int maxStackSize;
    private Label stackSizeLabel;


    public ItemStack(String itemName, double x, double y) {
        super(itemName, x, y); //Parent constructor
        this.setMouseTransparent(true); //Needs to not block mouse events

        initStackSize(); //Inits Label
        this.maxStackSize = 100; //Sets default max size to 100

        //Adds label to vbox
        this.getChildren().add(this.stackSizeLabel);

    }

    private void initStackSize() {
        this.stackSize = new SimpleIntegerProperty(0); //Stack size starts at 0
        this.stackSizeLabel = new Label(); //Initialises Label
        this.stackSizeLabel.textProperty().bind(this.stackSize.asString()); //Binds label text to the stack size
        this.stackSizeLabel.setMouseTransparent(false);
        //Repositions Label in relation to image
        this.stackSizeLabel.setTranslateY(-10);
        this.stackSizeLabel.setTranslateX(5);
        //Width makes easier to manage
        this.stackSizeLabel.setPrefWidth(Inventory.getSlotSize() - 6);
        //If value is stack size is 1 label is invisible
        this.stackSizeLabel.opacityProperty().bind(this.stackSize.subtract(1));
        //Styling
        this.stackSizeLabel.getStyleClass().add("outline");
        this.stackSizeLabel.setStyle("-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: center-right;");
    }


    //Getters and Setters
    public int getDurability() {
        return durability;
    }

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
