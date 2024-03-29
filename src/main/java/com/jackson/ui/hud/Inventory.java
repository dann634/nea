package com.jackson.ui.hud;

import com.jackson.game.items.Entity;
import com.jackson.game.items.Item;
import com.jackson.game.items.ItemStack;
import com.jackson.ui.GameController;
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

    //Constants
    private static final int HOTBAR_SIZE = 5;
    private static final int INVENTORY_SIZE = 4;
    private static final int SLOT_SIZE = 40;

    //Fields
    private final AnchorPane[][] inventoryArr;
    private final ItemStack[][] itemArray;
    private final SimpleIntegerProperty selectedSlotIndex;
    private final ImageView itemOnCursor;
    private final SimpleIntegerProperty ammo;
    private VBox wholeInventoryVbox;
    private boolean isInventoryOpen;
    private ItemStack itemStackOnCursor;
    private boolean isCellHovered;


    public Inventory(SimpleIntegerProperty ammo) {
        //Initialises all fields
        this.ammo = ammo;
        isCellHovered = false;
        isInventoryOpen = false;
        itemArray = new ItemStack[HOTBAR_SIZE][INVENTORY_SIZE]; //2D array of all items
        inventoryArr = new AnchorPane[HOTBAR_SIZE][INVENTORY_SIZE]; //2D Array of all anchor pane squares
        selectedSlotIndex = new SimpleIntegerProperty(0);


        initInventory(); //Initialises all inventory squares

        //Item on cursor
        itemOnCursor = new ImageView();
        itemOnCursor.setFitWidth(16);
        itemOnCursor.setFitHeight(16);

        //Default appearance
        setHideInventory(true); //Should be hidden by default
        selectSlot(0); //Start with slot 1 selected
    }

    //get slot size
    public static int getSlotSize() {
        return SLOT_SIZE;
    }

    /*
    Creates the 5x4 inventory size
    Sets style of whole inventory
     */
    private void initInventory() {
        //Parent of whole inventory
        wholeInventoryVbox = new VBox(10); //Whole vbox
        wholeInventoryVbox.setStyle("-fx-padding: 10");
        wholeInventoryVbox.setMouseTransparent(false);
        wholeInventoryVbox.setOnMouseEntered(e -> isCellHovered = true);
        wholeInventoryVbox.setOnMouseExited(e -> isCellHovered = false);

        HBox hotBarHBox = new HBox(3); //First row
        List<HBox> tempHboxList = new ArrayList<>();
        tempHboxList.add(hotBarHBox);

        for (int i = 0; i < HOTBAR_SIZE; i++) { //Hotbar
            AnchorPane pane = getInventorySquare(0, i);
            inventoryArr[i][0] = pane;
            hotBarHBox.getChildren().add(pane);
        }

        //Rest of inventory
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            HBox hBox = new HBox(3);
            tempHboxList.add(hBox);
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                var vbox = getInventorySquare(i, j);
                hBox.getChildren().add(vbox);
                inventoryArr[j][i] = vbox;
            }
        }

        //Adds hbox to vbox
        wholeInventoryVbox.getChildren().addAll(tempHboxList);
    }

    //Returns an inventory square
    private AnchorPane getInventorySquare(int col, int row) {
        AnchorPane pane = new AnchorPane();
        pane.setId("unselected"); //ID for css
        pane.getStyleClass().addAll("inventory", "darkBackground");
        //On Click Listener
        pane.setOnMouseClicked(e -> {
            if ((itemStackOnCursor == null && itemArray[row][col] == null)) {
                //If no item in square and nothing on cursor
                //If item in square and item on cursor
                return;
            }
            //Item on cursor and empty square (puts block down)
            if (itemStackOnCursor != null && itemArray[row][col] == null) {
                itemArray[row][col] = itemStackOnCursor;
                itemStackOnCursor = null;
                itemOnCursor.setVisible(false);
                inventoryArr[row][col].getChildren().add(itemArray[row][col]);
                return;
            }

            //Item on cursor and full square
            //Swaps items
            if (itemStackOnCursor != null && itemArray[row][col] != null) {
                ItemStack tempItemStack = itemArray[row][col];
                itemArray[row][col] = itemStackOnCursor;
                inventoryArr[row][col].getChildren().set(0, itemArray[row][col]);
                itemStackOnCursor = tempItemStack;
                setItemOnCursor(itemStackOnCursor.getItemName());
                return;
            }

            //Picks up stack (takes block and puts on cursor)
            itemStackOnCursor = itemArray[row][col];
            setItemOnCursor(itemStackOnCursor.getItemName());
            inventoryArr[row][col].getChildren().clear();
            itemOnCursor.setVisible(true);
            itemArray[row][col] = null;
        });
        return pane;
    }

    //Toggles between inventory hidden and shown
    public void toggleInventory() {
        if (isInventoryOpen) {
            setHideInventory(true);
            return;
        }
        setHideInventory(false);
    }

    //Hides bottom 4 rows
    private void setHideInventory(boolean isHidden) {
        isInventoryOpen = !isHidden;
        for (int i = 1; i < INVENTORY_SIZE; i++) {
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                inventoryArr[j][i].setVisible(!isHidden);
            }
        }
    }

    //Get whole inventory
    public VBox getInventoryVbox() {
        return wholeInventoryVbox;
    }

    //Changes the visual indicator of what slot is selected
    public void selectSlot(int index) {
        if (selectedSlotIndex.get() != -1) {
            inventoryArr[selectedSlotIndex.get()][0].setId("inventory-unselected");
        }
        inventoryArr[index][0].setId("selected");
        selectedSlotIndex.set(index);
    }

    /*
    Adds item to inventory
    Sorts into next available slot
     */
    public boolean addItem(ItemStack itemStack) {
        ItemStack checkItemStack = doesItemExistAlready(itemStack); //Does item already exist in inventory
        int[] index;
        if (checkItemStack == null) {
            index = findNextFreeIndex(); //Is there a free slot
            if (index[0] == -1) { //No free slot
                return false;
            }
            //Is free slot and block doesn't already exist
            itemArray[index[0]][index[1]] = itemStack; //Update Backend
            //Resets itemstack for inventory
            itemStack.setPos((Inventory.SLOT_SIZE / 2) - (itemStack.getPrefWidth() / 2),
                    (Inventory.SLOT_SIZE / 2) - (itemStack.getPrefHeight() / 2));
            itemStack.resetRotation();
            inventoryArr[index[0]][index[1]].getChildren().add(itemStack); //Updates front end
            return true;
        }
        //Item already exists
        //Updates stack size (and label)
        checkItemStack.addStackValue(itemStack.getStackSize());
        return true;
    }

    //Finds next free slot in inventory and returns index
    private int[] findNextFreeIndex() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            for (int j = 0; j < HOTBAR_SIZE; j++) {
                if (itemArray[j][i] == null) {
                    return new int[]{j, i};
                }
            }
        }
        return new int[]{-1, -1}; //Not found
    }

    //Returns itemstack if item is found else returns null
    private ItemStack doesItemExistAlready(ItemStack itemStack) {
        for (ItemStack[] itemStacks : itemArray) {
            for (int j = 0; j < itemStacks.length; j++) {
                if (itemStacks[j] != null && itemStacks[j].getItemName().equals(itemStack.getItemName())) {
                    //Found
                    if (itemStacks[j].getStackSize() < itemStacks[j].getMaxStackSize()) { //Full
                        //Accept
                        return itemStacks[j];
                    }

                }
            }
        }
        return null;
    }

    public SimpleIntegerProperty getSelectedSlotIndex() {
        return selectedSlotIndex;
    }

    //Returns String Item Name for the character to hold
    public String getBlockNameInHotbar(int index) {
        if (itemArray[index][0] == null) {
            return "air";
        }
        return itemArray[index][0].getItemName();
    }

    //Selected item getter
    public ItemStack getSelectedItemStack() {
        return itemArray[selectedSlotIndex.get()][0];
    }

    public String getSelectedItemStackName() {
        return itemArray[selectedSlotIndex.get()][0] == null ? "air" : itemArray[selectedSlotIndex.get()][0].getItemName();
    }

    //Decrements item and removes if necessary
    public void useBlockFromSelectedSlot() {
        ItemStack itemStack = itemArray[selectedSlotIndex.get()][0];
        itemStack.addStackValue(-1);
        if (itemStack.getStackSize() == 0) {
            //Remove from inventory
            itemArray[selectedSlotIndex.get()][0] = null;
            inventoryArr[selectedSlotIndex.get()][0].getChildren().clear();
        }
    }

    public ImageView getItemOnCursor() {
        return itemOnCursor;
    }

    //Sets item on cursor to new item
    private void setItemOnCursor(String blockName) {
        try {
            itemOnCursor.setImage(new Image("file:src/main/resources/images/" + blockName + ".png"));
        } catch (MissingResourceException ignored) {
        }
    }

    public ItemStack getItemStackOnCursor() {
        return itemStackOnCursor;
    }

    //Removes item from cursor
    public void clearCursor() {
        itemStackOnCursor = null;
        itemOnCursor.setVisible(false);
    }

    public boolean isCellHovered() {
        return isCellHovered;
    }

    public ItemStack[][] getItemArray() {
        return itemArray;
    }

    //Returns if an item can be crafted from the items in the inventory
    public boolean canCraft(List<ItemStack> recipe) {
        if (recipe.isEmpty()) {
            return false;
        }

        for (ItemStack itemStack : recipe) {
            int amount = 0;
            ItemStack item = itemStack;
            for (ItemStack[] itemStacks : itemArray) {
                for (ItemStack targetItem : itemStacks) {
                    if (targetItem == null) continue;

                    if (targetItem.getItemName().equals(item.getItemName())) {
                        amount = amount + targetItem.getStackSize();
                    }
                }
            }
            if (amount < item.getStackSize()) {
                return false;
            }
        }

        return true;
    }

    //Takes items from inventory and puts target item back in
    public void craft(String item, List<ItemStack> recipe) {
        //Take items to craft
        for (ItemStack itemStack : recipe) {
            for (int i = 0; i < itemArray.length; i++) {
                for (int j = 0; j < itemArray[i].length; j++) {
                    ItemStack targetItem = itemArray[i][j];
                    if (targetItem == null || itemStack.getStackSize() == 0) {
                        continue;
                    }
                    if (targetItem.getItemName().equals(itemStack.getItemName())) {
                        //Is same so take away
                        if (targetItem.getStackSize() > itemStack.getStackSize()) {
                            //More than enough
                            targetItem.addStackValue(-itemStack.getStackSize());
                            itemStack.addStackValue(-itemStack.getStackSize());
                        } else if (targetItem.getStackSize() <= itemStack.getStackSize()) {
                            //Will take all so needs to be removed from inventory
                            //Not Enough
                            itemStack.addStackValue(-targetItem.getStackSize());
                            targetItem.addStackValue(-targetItem.getStackSize());

                            inventoryArr[i][j].getChildren().remove(targetItem);
                            itemArray[i][j] = null;
                        }
                    }
                }
            }
        }

        //Can craft so put item in inventory
        Entity entity;
        if (GameController.lookupTable.containsKey(item)) {
            entity = new Entity(item);
        } else {
            entity = new Item(item);
        }

        ItemStack craftedItem = new ItemStack(entity);
        craftedItem.addStackValue(1);
        if (item.equals("bullet")) {
            ammo.set(ammo.get() + 20);
            return;
        }
        addItem(craftedItem);
    }
}
