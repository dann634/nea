package com.jackson.ui;

import com.jackson.main.Main;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainMenuController {

    private Scene scene;

    public MainMenuController() { //Constructor
        //Root
        VBox root = new VBox(); //Initialise the parent root
        Main.applyWindowSize(root); //Sets standard window size
        root.setId("root");

        addContent(root);

        //Scene
        this.scene = new Scene(root); //Initialise scene with root
        this.scene.getStylesheets().add("file:src/main/resources/stylesheets/mainMenu.css"); //Add stylesheets to scene
    }

    private void addContent(VBox root) {
        //Add Title
        var title = new Label("Main Menu");
        title.setId("title");
        root.getChildren().add(title);

        //Buttons (in order)
        var singlePlayerButton = new Button("Singleplayer");
        var multiPlayerButton = new Button("Multiplayer");
        var settingsButton = new Button("Settings");
        var helpButton = new Button("Help");
        var exitButton = new Button("Quit to Desktop");

        addFunctionality(singlePlayerButton, multiPlayerButton, settingsButton, helpButton, exitButton);

        root.getChildren().addAll(singlePlayerButton, multiPlayerButton, settingsButton, helpButton, exitButton);

    }

    private void addFunctionality(Button singlePlayerButton, Button multiplayerButton,
                                  Button settingsButton, Button helpButton, Button exitButton) { //Adds functionality to each individual button
        singlePlayerButton.setOnAction(e -> { //Goes to create world or loading scene

        });

        multiplayerButton.setOnAction(e -> {
            try {
                new LobbyController();//Goes to multiplayer lobby scene
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });

        settingsButton.setOnAction(e -> Main.setScene(new SettingsController().getScene())); //Goes to settings scene
        helpButton.setOnAction(e -> {}); //Goes to help scene
        exitButton.setOnAction(e -> System.exit(0)); //Exits Game


    }




   public Scene getScene() {
       return scene;
   }


}
