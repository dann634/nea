package com.jackson.main;

import com.jackson.ui.HelpController;
import com.jackson.ui.MainMenuController;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage stage;
    private static final double HEIGHT = 550.0; // Height of window
    private static final double WIDTH = 1024.0; //Width of window

    @Override
    public void start(Stage pstage)  { //Start point of program
        stage = pstage;
        stage.setTitle("Game"); //Set Window Title
        stage.getIcons().add(new Image("file:src/main/resources/images/grass.png")); //Add Window Icon
        stage.setScene(new MainMenuController().getScene()); //Opens Main Menu
        stage.setResizable(false); //Window cannot be resized
        stage.show(); //Show GUI
    }

    public static Stage getStage() {
        return stage;
    }

    public static void applyWindowSize(Parent root) {
        root.setStyle("-fx-pref-height: " + HEIGHT + ";" +
                "-fx-pref-width: " + WIDTH + ";");
    }


}
