package com.jackson.ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class GameController extends Scene {

    private AnchorPane root;

    public GameController() {
        super(new AnchorPane());
        this.root = new AnchorPane();
        this.root.setMinHeight(500);
        this.root.setMinWidth(1000);

        new NoiseGenerator().generateMap();

        setRoot(this.root);
        getStylesheets().add("file:src/main/resources/stylesheets/game.css");
    }

    public class NoiseGenerator {

        private static final int MAP_HEIGHT = 100;
        private static final int MAP_WIDTH = 1000;
        public String[][] generateMap() {
            String[][] map = new String[MAP_WIDTH][MAP_HEIGHT];
            for (int i = 0; i < MAP_WIDTH; i++) {
                double rawSineWave = MAP_HEIGHT * Math.sin(i);
                Circle circle = new Circle(1);
                circle.setCenterX(i);
                circle.setCenterY(rawSineWave + 100);
                root.getChildren().add(circle);
            }

            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    System.out.println(map[i][j]);
                }
            }
            return map;
        }
    }

}
