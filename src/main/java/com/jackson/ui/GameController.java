package com.jackson.ui;

import com.jackson.io.TextIO;
import com.jackson.main.Main;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class GameController extends Scene {

    private AnchorPane root;

    public GameController() {
        super(new VBox());

        this.root = new AnchorPane();
        Main.applyWindowSize(this.root);


        setRoot(this.root);
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");
    }

    private void loadMap() {
        String[][] mapFile = TextIO.readMapFile(true);

    }
}
