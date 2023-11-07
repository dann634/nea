package com.jackson.game;

import com.jackson.ui.hud.Inventory;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public class ItemStack extends AnchorPane { // TODO: 07/11/2023 Adjust everything for this
    private ImageView icon;
    private int durability;
    private SimpleIntegerProperty stackSize;
    private int maxStackSize;
    private Label stackSizeLabel;

    private String itemName;

    private SimpleDoubleProperty x;
    private SimpleDoubleProperty y;

    public ItemStack(String itemName, double x, double y) {
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
        this.itemName = itemName;
        initIcon();
        initStackSize();
        this.maxStackSize = 100;

    }

    private void initIcon() {
        this.icon = new ImageView(new Image("file:src/main/resources/images/" + this.itemName + ".png"));
        this.icon.setFitHeight(16);
        this.icon.setFitWidth(16);
        this.icon.setMouseTransparent(false);
        this.icon.translateXProperty().bind(this.x);
        this.icon.translateYProperty().bind(this.y);

    }

    private void initStackSize() {
        this.stackSize = new SimpleIntegerProperty(0);
        this.stackSizeLabel = new Label();
        this.stackSizeLabel.textProperty().bind(this.stackSize.asString());
        this.stackSizeLabel.setMouseTransparent(false);
        this.stackSizeLabel.getStyleClass().add("outline");
        this.stackSizeLabel.setPrefWidth(Inventory.getSlotSize() - 6);
        this.stackSizeLabel.translateXProperty().bind(x);
        this.stackSizeLabel.translateYProperty().bind(y.subtract(10));
        this.stackSizeLabel.opacityProperty().bind(this.stackSize.subtract(1)); //If value is stack size is 1 label is invisible
        this.stackSizeLabel.setStyle("-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: center-right;");
    }


    public String getItemName() {
        return this.itemName;
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

    public ImageView getIcon() {
        return this.icon;
    }

    public void setPos(double x, double y) {
        this.x.set(x);
        this.y.set(y);
    }

    public void addPos(double x, double y) {
        this.x.set(this.x.get() + x);
        this.y.set(this.y.get() + y);
    }

    public double getX() {
        return this.x.get();
    }

    public double getY() {
        return this.y.get();
    }


}
