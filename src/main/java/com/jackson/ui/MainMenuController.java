package com.jackson.ui;

import com.jackson.main.Main;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

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

        List<Button> btnList = new ArrayList<>(); //Adding buttons to a list to remove duplicate code
        btnList.add(singlePlayerButton);
        btnList.add(multiPlayerButton);
        btnList.add(settingsButton);
        btnList.add(helpButton);
        btnList.add(exitButton);

        for(Button btn : btnList) {
            btn.getStyleClass().add("button");
        }
        root.getChildren().addAll(btnList);

    }

    private void addFunctionality(Button singlePlayerButton, Button multiplayerButton,
                                  Button settingsButton, Button helpButton, Button exitButton) { //Adds functionality to each individual button
        singlePlayerButton.setOnAction(e -> { //Goes to create world or loading scene

        });

        multiplayerButton.setOnAction(e -> { //Goes to multiplayer lobby scene
        });

        settingsButton.setOnAction(e -> Main.getStage().setScene(new SettingsController().getScene())); //Goes to settings scene
        helpButton.setOnAction(e -> {}); //Goes to help scene
        exitButton.setOnAction(e -> System.exit(0)); //Exits Game


    }




   public Scene getScene() {
       return scene;
   }


}
