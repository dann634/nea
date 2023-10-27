package com.jackson.game;

import com.jackson.ui.hud.Inventory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public class ItemStack {
    private ImageView icon;
    private Block block;
    private int durability;
    private SimpleIntegerProperty stackSize;
    private int maxStackSize;
    private Label stackSizeLabel;

    public ItemStack(Block block) {
        this.block = block;

        initIcon();
        initStackSize();
        this.maxStackSize = 100;

    }

    private void initIcon() {
        this.icon = new ImageView(new Image("file:src/main/resources/images/" + this.block.getBlockName() + ".png"));
        this.icon.setFitHeight(16);
        this.icon.setFitWidth(16);
        this.icon.setTranslateX(((double) Inventory.getSlotSize() / 2) - this.icon.getFitWidth() / 2);
        this.icon.setTranslateY(((double) Inventory.getSlotSize() / 2) - this.icon.getFitHeight() / 2);
    }

    private void initStackSize() {
        this.stackSize = new SimpleIntegerProperty(0);
        this.stackSizeLabel = new Label();
        this.stackSizeLabel.textProperty().bind(this.stackSize.asString());
        this.stackSizeLabel.setPrefWidth(Inventory.getSlotSize() - 6);
        this.stackSizeLabel.setTranslateY(Inventory.getSlotSize() - 20);
        this.stackSizeLabel.setStyle("-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: center-right");
    }


    public String getItemName() {
        return this.block.getBlockName();
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

    public List<Node> getNodes() {
        return List.of(this.icon, this.stackSizeLabel);
    }

    public Block getBlock() {
        return this.block;
    }


}
