package com.jackson.ui;

import com.jackson.game.Difficulty;
import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.network.connections.Client;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
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


            Main.setScene(new GameController(Difficulty.valueOf(TextIO.readFile("src/main/resources/saves/single_data.txt").get(9)), true, null));
        });


        multiplayerButton.setOnAction(e -> {
            //Does multiplayer world already exist
            try {
                Client client = new Client();
                if(!client.doesWorldExist()) {
                    CreateWorldController createWorldController = new CreateWorldController();
                    createWorldController.multiplayer();
                    Platform.runLater(() -> Main.setScene(createWorldController));
                } else {
                    if(!client.isUsernameUnique()) {
                        Main.setScene(new ErrorScreen("Username already in use. Please Change"));
                    } else {
                        client.joinGame();
                        client.startListening();
                    }

                }
            } catch (IOException | ClassNotFoundException ex) {
                Main.setScene(new ErrorScreen("Couldn't Connect to Server. Please try again later."));
            }
        });

        settingsButton.setOnAction(e -> Main.setScene(new SettingsController())); //Goes to settings scene
        helpButton.setOnAction(e -> {Main.setScene(new HelpController());}); //Goes to help scene
        exitButton.setOnAction(e -> System.exit(0)); //Exits Game


    }

    private class ErrorScreen extends Scene {

        public ErrorScreen(String errorMessage) {
            super(new VBox());
            VBox root = new VBox();
            root.setStyle("-fx-alignment: center;" +
                    "-fx-spacing: 40;" +
                    "-fx-pref-height: 544;" +
                    "-fx-pref-width: 1024;");

            //Title
            Label title = new Label("Error");
            title.setStyle("-fx-font-size: 42;" +
                    "-fx-font-weight: bold");

            //Error Message
            Label errorText = new Label(errorMessage);
            errorText.setStyle("-fx-font-size: 24;" +
                    "-fx-text-fill: red;");

            //Back Button
            Button backButton = new Button("Back");
            backButton.setStyle("-fx-pref-width: 150;" +
                    "-fx-pref-height: 40;" +
                    "-fx-font-size: 18");
            backButton.setOnAction(e -> Main.setScene(new MainMenuController()));

            root.getChildren().addAll(title, errorText, backButton);


            setRoot(root);
        }
    }






}
