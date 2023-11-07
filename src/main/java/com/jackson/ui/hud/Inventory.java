package com.jackson.ui.hud;

import com.jackson.game.items.ItemStack;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

public class Inventory {

    private static final int HOTBAR_SIZE = 5;
    private static final int INVENTORY_SIZE = 4;
    private static final int SLOT_SIZE = 40;

    private AnchorPane[][] inventoryArr;
    private ItemStack[][] itemArray;
    private VBox wholeInventoryVbox;
    private SimpleIntegerProperty selectedSlotIndex;
    private List<HBox> hboxRows;

    private boolean isInventoryOpen;
    private ImageView itemOnCursor;
    private ItemStack itemStackOnCursor;
    private boolean isCellHovered;

// FIXME: 06/11/2023 when player drops block it will place if block in hand

    public Inventory() {
        initHotBar();
        this.isCellHovered = false;
    }

    private void initHotBar() {
        this.selectedSlotIndex = new SimpleIntegerProperty(0);
        this.isInventoryOpen = false;

        this.itemArray = new ItemStack[HOTBAR_SIZE][INVENTORY_SIZE];

        this.wholeInventoryVbox = new VBox(10); //Whole vbox
        this.wholeInventoryVbox.setStyle("-fx-padding: 10");
        this.wholeInventoryVbox.setMouseTransparent(false);
        this.wholeInventoryVbox.setOnMouseEntered(e -> {
            this.isCellHovered = true;
        });
        this.wholeInventoryVbox.setOnMouseExited(e -> {
            this.isCellHovered = false;
        });

        this.itemOnCursor = new ImageView();
        this.itemOnCursor.setFitWidth(16);
        this.itemOnCursor.setFitHeight(16);

        this.hboxRows = new ArrayList<>(); //Array of all hbox rows

        this.inventoryArr = new AnchorPane[HOTBAR_SIZE][INVENTORY_SIZE]; //Array of all vbox squares

        HBox hotBarHBox = new HBox(3); //First row
        this.hboxRows.add(hotBarHBox);

        for (int i = 0; i < HOTBAR_SIZE ; i++) { //Hotbar
            AnchorPane pane = getInventorySquare(0, i);
            this.inventoryArr[i][0] = pane;
            hotBarHBox.getChildren().add(pane);
        }

        //Rest of inventory
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            HBox hBox = new HBox(3);
            this.hboxRows.add(hBox);
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                var vbox = getInventorySquare(i, j);
                hBox.getChildren().add(vbox);
                this.inventoryArr[j][i] = vbox;
            }
        }

        //Adds hbox to vbox
        for(HBox hBox : this.hboxRows) {
            this.wholeInventoryVbox.getChildren().add(hBox);
        }

        setHideInventory(true); //Should be hidden by default
        selectSlot(0); //Start with slot 1 selected

    }

    private AnchorPane getInventorySquare(int col, int row) {
        AnchorPane pane = new AnchorPane();
        pane.setId("inventory-unselected");

        pane.setOnMouseClicked(e -> {

            if((this.itemStackOnCursor == null && this.itemArray[row][col] == null)
            || (this.itemStackOnCursor != null && this.itemArray[row][col] != null)) {
                return;
            }
            if(this.itemStackOnCursor != null && this.itemArray[row][col] == null) { //Item on cursor and empty square
                this.itemArray[row][col] = this.itemStackOnCursor;
                this.itemStackOnCursor = null;
                this.itemOnCursor.setVisible(false);
                this.inventoryArr[row][col].getChildren().add(this.itemArray[row][col]);
                return;
            }
            //Picks up stack
            this.itemStackOnCursor = this.itemArray[row][col];
            setItemOnCursor(this.itemStackOnCursor.getItemName());
            this.inventoryArr[row][col].getChildren().clear();
            this.itemOnCursor.setVisible(true);
            this.itemArray[row][col] = null;


        });
        return pane;
    }

    public void toggleInventory() {
        if(this.isInventoryOpen) {
            setHideInventory(true);
            return;
        }
        setHideInventory(false);
    }

    private void setHideInventory(boolean isHidden) {
        this.isInventoryOpen = !isHidden;
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            this.hboxRows.get(i).setVisible(!isHidden);
        }
    }

    public VBox getInventoryVbox() {
        return this.wholeInventoryVbox;
    }

    public static int getSlotSize() {
        return SLOT_SIZE;
    }

    public void selectSlot(int index) {
        if(this.selectedSlotIndex.get() != -1) {
            this.inventoryArr[this.selectedSlotIndex.get()][0].setId("inventory-unselected");
        }
        this.inventoryArr[index][0].setId("inventory-selected");
        this.selectedSlotIndex.set(index);
    }

    public boolean addItem(ItemStack itemStack) { //May have to change to item after swords and stuff added
        ItemStack checkItemStack = doesItemExistAlready(itemStack); //Does block already exist
        int[] index;
        if(checkItemStack == null) {
            index = findNextFreeIndex(); //Is there a free slot
            if(index[0] == -1) { //No free slot
                return false;
            }
            //Is free slot and block doesnt already exist
            this.itemArray[index[0]][index[1]] = itemStack; //Update Backend
            itemStack.setPos((Inventory.SLOT_SIZE / 2) - (itemStack.getPrefWidth() / 2),(Inventory.SLOT_SIZE / 2) - (itemStack.getPrefHeight() / 2));
            itemStack.resetRotation();
            this.inventoryArr[index[0]][index[1]].getChildren().add(itemStack); //Updates front end
            return true;
        }

        checkItemStack.addStackValue(itemStack.getStackSize()); // FIXME: 07/11/2023 will break if goes over 100
        return true;
    }

    private int[] findNextFreeIndex() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                if(this.itemArray[j][i] == null) {
                    return new int[]{j, i};
                }
            }
        }
        return new int[]{-1, -1}; //Not found
    }

    private ItemStack doesItemExistAlready(ItemStack itemStack) {
        for (int i = 0; i < this.itemArray.length; i++) {
            for (int j = 0; j < this.itemArray[i].length; j++) {
                if(this.itemArray[i][j] != null && this.itemArray[i][j].getItemName().equals(itemStack.getItemName())) {
                    //Found
                    if(this.itemArray[i][j].getStackSize() < this.itemArray[i][j].getMaxStackSize()) { //Full
                        //Accept
                        return this.itemArray[i][j];
                    }

                }
            }
        }
        return null;
    }

    public SimpleIntegerProperty getSelectedSlotIndex() {
        return this.selectedSlotIndex;
    }

    public String getBlockNameInHotbar(int index) {
        if(this.itemArray[index][0] == null) {
            return "air";
        }
        return this.itemArray[index][0].getItemName();
    }

    public ItemStack getSelectedItemStack() {
        return this.itemArray[this.selectedSlotIndex.get()][0];
    }

    public void useBlockFromSelectedSlot() {
        ItemStack itemStack = this.itemArray[this.selectedSlotIndex.get()][0];
        itemStack.addStackValue(-1);
        if(itemStack.getStackSize() == 0) {
            //Remove from inventory
            this.itemArray[this.selectedSlotIndex.get()][0] = null;
            this.inventoryArr[this.selectedSlotIndex.get()][0].getChildren().clear();
        }

    }

    public ImageView getItemOnCursor() {
        return this.itemOnCursor;
    }

    private void setItemOnCursor(String blockName) {
        try {
            this.itemOnCursor.setImage(new Image("file:src/main/resources/images/" + blockName + ".png"));
        } catch (MissingResourceException ignored) {}
    }

    public ItemStack getItemStackOnCursor() {
        return this.itemStackOnCursor;
    }

    public void clearCursor() {
        this.itemStackOnCursor = null;
        this.itemOnCursor.setVisible(false);
    }

    public void setCellHovered(boolean value) {
        this.isCellHovered = value;
    }

    public boolean isCellHovered() {
        return isCellHovered;
    }
}
