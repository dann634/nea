package com.jackson.ui;

import com.jackson.game.ProceduralGenerator;
import com.jackson.main.Main;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.List;

public class GameController extends Scene {

    private AnchorPane root;

    public GameController() {
        super(new VBox());

        this.root = new AnchorPane();
        Main.applyWindowSize(this.root);

        ProceduralGenerator.createMapFile(true);



        setRoot(this.root);
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");
    }
}
