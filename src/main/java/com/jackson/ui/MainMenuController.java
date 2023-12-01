package com.jackson.ui;

import com.jackson.main.Main;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController extends Scene implements Initializable {

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.gc(); //Advises Garbage Collection
    }

    public MainMenuController() {
        super(new VBox());
        VBox root = new VBox(); //Initialise the parent root
        setRoot(root);
        //Root
        Main.applyWindowSize(root); //Sets standard window size
        root.setId("root");

        addContent(root);

        getStylesheets().add("file:src/main/resources/stylesheets/mainMenu.css"); //Add stylesheets to scene
    }

    private void addContent(VBox root) {
        //Add Title
        var title = new Label("Main Menu");
        title.setId("title");
        root.getChildren().add(title);

        //Buttons (in order)
        var singlePlayerButton = new Button("Singleplayer");

        //Does save exist
        if(new File("src/main/resources/saves/singleplayer.txt").exists()) {
            singlePlayerButton.setText("Continue Singleplayer");
        }

        var multiPlayerButton = new Button("Multiplayer");
        var settingsButton = new Button("Settings");
        var helpButton = new Button("Help");
        var exitButton = new Button("Quit to Desktop");

        addFunctionality(singlePlayerButton, multiPlayerButton, settingsButton, helpButton, exitButton);

        root.getChildren().addAll(singlePlayerButton, multiPlayerButton, settingsButton, helpButton, exitButton);

    }

    private void addFunctionality(Button singlePlayerButton, Button multiplayerButton,
                                  Button settingsButton, Button helpButton, Button exitButton) { //Adds functionality to each individual button
        singlePlayerButton.setOnAction(e -> {
            if(singlePlayerButton.getText().equals("Singleplayer")) {
                Main.setScene(new CreateWorldController());
                return;
            }

            Main.setScene(new GameController());
        });

        multiplayerButton.setOnAction(e -> Main.setScene(new LobbyController()));

        settingsButton.setOnAction(e -> Main.setScene(new SettingsController())); //Goes to settings scene
        helpButton.setOnAction(e -> {}); //Goes to help scene
        exitButton.setOnAction(e -> System.exit(0)); //Exits Game


    }






}
