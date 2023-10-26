package com.jackson.ui.hud;

import com.jackson.game.Block;
import com.jackson.game.ItemStack;
import com.jackson.ui.Camera;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private HBox hotBarHBox;
    private AnchorPane[] hotBarSlots;
    private AnchorPane[][] inventoryArr;
    private ItemStack[][] itemArray;
    private VBox wholeInventoryVbox;

    private int selectedSlotIndex;


    private List<HBox> hboxRows;


    private static final int HOTBAR_SIZE = 5;
    private static final int INVENTORY_SIZE = 4;
    private static final int SLOT_SIZE = 40;
    private boolean isInventoryOpen;

    public Inventory() {
        initHotBar();
    }

    private void initHotBar() {
        this.selectedSlotIndex = -1;
        this.isInventoryOpen = false;

        this.itemArray = new ItemStack[HOTBAR_SIZE][INVENTORY_SIZE];

        this.wholeInventoryVbox = new VBox(10); //Whole vbox
        this.wholeInventoryVbox.setStyle("-fx-padding: 10");

        this.hboxRows = new ArrayList<>(); //Array of all hbox rows

        this.inventoryArr = new AnchorPane[HOTBAR_SIZE][INVENTORY_SIZE]; //Array of all vbox squares

        this.hotBarHBox = new HBox(3); //First row
        this.hotBarSlots = new AnchorPane[HOTBAR_SIZE];
        this.hboxRows.add(this.hotBarHBox);

        for (int i = 0; i < HOTBAR_SIZE ; i++) { //Hotbar
            AnchorPane pane = getInventorySquare();
            this.hotBarSlots[i] = pane;
            this.hotBarHBox.getChildren().add(pane);
            this.inventoryArr[i][0] = pane;
        }

        //Rest of inventory
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            HBox hBox = new HBox(3);
            this.hboxRows.add(hBox);
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                var vbox = getInventorySquare();
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

        ItemStack itemStack = new ItemStack("dirt");
        itemStack.addStackValue(100);
        this.itemArray[0][0] = itemStack;
        this.inventoryArr[0][0].getChildren().addAll(itemStack.getNodes());

    }

    private AnchorPane getInventorySquare() {
        AnchorPane pane = new AnchorPane();
        pane.setId("inventory-unselected");

        pane.setOnMouseClicked(e -> {
            if(this.isInventoryOpen) {
                setHideInventory(true);
            } else {
                setHideInventory(false);
            }

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
        if(this.selectedSlotIndex != -1) {
            this.hotBarSlots[this.selectedSlotIndex].setId("inventory-unselected");
        }
        this.hotBarSlots[index].setId("inventory-selected");
        this.selectedSlotIndex = index;
    }

    public boolean addItem(Block block) { //May have to change to item after swords and stuff added
        ItemStack itemStack = doesItemExistAlready(block); //Does block already exist
        int[] index;
        if(itemStack == null) {
            index = findNextFreeIndex(); //Is there a free slot
            if(index[0] == -1) { //No free slot
                return false;
            }
            //Is free slot and block doesnt already exist
            ItemStack newItemStack = new ItemStack(block.getBlockName());
            newItemStack.addStackValue(1);
            this.itemArray[index[0]][index[1]] = newItemStack; //Update Backend
            this.inventoryArr[index[0]][index[1]].getChildren().addAll(newItemStack.getNodes()); //Updates front end
            return true;
        }

        itemStack.addStackValue(1);



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

    private ItemStack doesItemExistAlready(Block block) {
        for (int i = 0; i < this.itemArray.length; i++) {
            for (int j = 0; j < this.itemArray[i].length; j++) {
                if(this.itemArray[i][j] != null && this.itemArray[i][j].getItemName().equals(block.getBlockName())) {
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
}
