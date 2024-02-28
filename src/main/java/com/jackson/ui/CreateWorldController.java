package com.jackson.ui;

import com.jackson.game.Difficulty;
import com.jackson.game.ProceduralGenerator;
import com.jackson.main.Main;
import com.jackson.network.connections.Client;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CreateWorldController extends Scene {

    private static final String EASY_DESCRIPTION = "A relaxed experience, mobs have lowered HP and deal less damage (Recommended for Casual Players)";
    private static final String MEDIUM_DESCRIPTION = "A balanced experience, mobs have a fair amount of HP and deal some damage (Recommended for Intermediate Players)";
    private static final String HARD_DESCRIPTION = "A challenging experience, mobs have lots of HP and deal insane damage (Recommended for Veteran Players)";
    private final VBox root;
    private final Label title;
    private final Button generateWorldButton;
    private final ComboBox<String> difficultyComboBox;


    public CreateWorldController() {
        super(new VBox()); //Super constructor with placeholder pane

        root = new VBox(); //Actual pane for this scene
        root.setSpacing(120);
        setRoot(root); //Pane is added to scene
        root.setId("root");
        Main.applyWindowSize(root); //Standard window size is applied

        title = new Label("Create World"); //Title of scene
        title.setId("title");

        var difficultyLabel = new Label("Difficulty:"); //Label for combobox

        difficultyComboBox = new ComboBox<>(); //Drop down menu for selecting difficulty
        difficultyComboBox.getItems().addAll("Easy", "Medium", "Hard"); //Different difficulties added
        difficultyComboBox.getSelectionModel().selectFirst(); // Easy is the default choice

        var description = new Label(EASY_DESCRIPTION); //Description of difficulty
        description.setId("descriptionLabel");

        //Combobox changes description to match difficulty chosen
        difficultyComboBox.setOnAction(e -> {
            switch (difficultyComboBox.getValue().toString()) {
                case "Easy" -> description.setText(EASY_DESCRIPTION);
                case "Medium" -> description.setText(MEDIUM_DESCRIPTION);
                case "Hard" -> description.setText(HARD_DESCRIPTION);
            }
        });
        var difficultyHbox = new HBox(); //hbox for label and combobox
        difficultyHbox.getStyleClass().add("hbox");
        difficultyHbox.getChildren().addAll(difficultyLabel, difficultyComboBox);

        var difficultyVbox = new VBox();
        difficultyVbox.setId("difficultyVbox");
        difficultyVbox.getChildren().addAll(difficultyHbox, description);

        //Generate world button
        generateWorldButton = new Button("Generate World");
        generateWorldButton.setId("generateWorldButton");
        generateWorldButton.setOnAction(e -> {
            ProceduralGenerator.saveMapToFile(ProceduralGenerator.createMapArray());
            try {
                Files.createFile(Path.of("src/main/resources/saves/single_data.txt"));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            try {
                Main.setScene(new GameController(Difficulty.valueOf(difficultyComboBox.getValue().toUpperCase()), true, null));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        //Back button (back to main menu)
        var backButton = new Button("Back");
        backButton.setOnAction(e -> Main.setScene(new MainMenuController()));

        //Hbox for generate world and back button
        var hbox = new HBox();
        hbox.getChildren().addAll(generateWorldButton, backButton);
        hbox.getStyleClass().add("hbox");

        //All elements added to scene graph
        root.getChildren().addAll(title, difficultyVbox, hbox);

        //Css stylesheet added
        getStylesheets().add("file:src/main/resources/stylesheets/createWorld.css");
    }

    //Changes for multiplayer
    public void multiplayer() {
        title.setText("Host Game");
        root.setSpacing(40);

        //hbox
        generateWorldButton.setOnAction(e -> {
            try {
                Client client = new Client();
                client.sendMap(ProceduralGenerator.createMapArray(), Difficulty.valueOf(difficultyComboBox.getValue().toUpperCase()));
                client.joinGame();
                client.startListening();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
