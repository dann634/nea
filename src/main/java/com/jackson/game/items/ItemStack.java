package com.jackson.game.items;

import com.jackson.ui.hud.Inventory;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;

public class ItemStack extends Entity { // TODO: 07/11/2023 Adjust everything for this
    private int durability;
    private SimpleIntegerProperty stackSize;
    private final int maxStackSize;
    private Label stackSizeLabel;


    public ItemStack(String itemName, double x, double y) {
        super(itemName);
        this.itemName = itemName;
        this.setMouseTransparent(true);

        initStackSize();
        this.maxStackSize = 100;

        this.setTranslateX(x);
        this.setTranslateY(y);

        this.getChildren().add(this.stackSizeLabel);

    }

    private void initStackSize() {
        this.stackSize = new SimpleIntegerProperty(0);
        this.stackSizeLabel = new Label();
        this.stackSizeLabel.textProperty().bind(this.stackSize.asString());
        this.stackSizeLabel.setMouseTransparent(false);
        this.stackSizeLabel.getStyleClass().add("outline");
        this.stackSizeLabel.setTranslateY(-10);
        this.stackSizeLabel.setTranslateX(5);
        this.stackSizeLabel.setPrefWidth(Inventory.getSlotSize() - 6);
        this.stackSizeLabel.opacityProperty().bind(this.stackSize.subtract(1)); //If value is stack size is 1 label is invisible
        this.stackSizeLabel.setStyle("-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: center-right;");
    }


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
