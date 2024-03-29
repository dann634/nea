package com.jackson.main;

import com.jackson.ui.MainMenuController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private static final double HEIGHT = 544.0; // Height of window
    private static final double WIDTH = 1024.0; //Width of window
    private static Stage stage;

    public static void setScene(Scene scene) {
        stage.setScene(scene);
    }

    //Applies correct width and height to scenes
    public static void applyWindowSize(Parent root) {
        root.setStyle("-fx-min-height: " + HEIGHT + ";" +
                "-fx-min-width: " + WIDTH + ";");

    }

    //getter
    public static Stage getStage() {
        return stage;
    }

    @Override
    public void start(Stage pstage) { //Start point of program
        stage = pstage;
        stage.setTitle("Game"); //Set Window Title
        stage.getIcons().add(new Image("file:src/main/resources/images/grass.png")); //Add Window Icon
        stage.setScene(new MainMenuController()); //Opens Main Menu
        stage.setResizable(false); //Window cannot be resized
        stage.show(); //Show GUI

    }
}
