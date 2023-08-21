package com.jackson.ui;

import com.jackson.main.Main;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class CreateWorldController extends Scene {

    private static final String EASY_DESCRIPTION = "A relaxed experience, mobs have lowered HP and deal less damage (Recommended for Casual Players)";
    private static final String MEDIUM_DESCRIPTION = "A balanced experience, mobs have a fair amount of HP and deal some damage (Recommended for Intermediate Players)";
    private static final String HARD_DESCRIPTION = "A challenging experience, mobs have lots of HP and deal insane damage (Recommended for Veteran Players)";


    // TODO: 21/08/2023 Maybe break this down into methods
    public CreateWorldController() {
        super(new VBox()); //Super constructor with placeholder pane

        VBox root = new VBox(); //Actual pane for this scene
        setRoot(root); //Pane is added to scene
        root.setId("root");
        Main.applyWindowSize(root); //Standard window size is applied

        var title = new Label("Create World"); //Title of scene
        title.setId("title");

        var difficultyLabel = new Label("Difficulty:"); //Label for combobox

        var difficultyComboBox = new ComboBox<>(); //Drop down menu for selecting difficulty
        difficultyComboBox.getItems().addAll("Easy", "Medium", "Hard"); //Different difficulties added
        difficultyComboBox.getSelectionModel().select(1); // Medium is the default choice

        var description = new Label(MEDIUM_DESCRIPTION); //Description of difficulty
        description.setId("descriptionLabel");

        //Combobox changes description to match difficulty chosen
        difficultyComboBox.setOnAction(e -> { // FIXME: 21/08/2023 BUG: Combobox won't close first time clicking item
            switch (difficultyComboBox.getValue().toString()) {
                case "Easy":
                    description.setText(EASY_DESCRIPTION);
                    break;
                case "Medium":
                    description.setText(MEDIUM_DESCRIPTION);
                    break;
                case "Hard":
                    description.setText(HARD_DESCRIPTION);
                    break;
            }
        });
        var difficultyHbox = new HBox(); //hbox for label and combobox
        difficultyHbox.getStyleClass().add("hbox");
        difficultyHbox.getChildren().addAll(difficultyLabel, difficultyComboBox);

        //Generate world button
        var generateWorldButton = new Button("Generate World");
        generateWorldButton.setId("generateWorldButton");

        //Back button (back to main menu)
        var backButton = new Button("Back");
        backButton.setOnAction(e -> Main.setScene(new MainMenuController()));

        //Hbox for generate world and back button
        var hbox = new HBox();
        hbox.getChildren().addAll(generateWorldButton, backButton);
        hbox.getStyleClass().add("hbox");

        //All elements added to scene graph
        root.getChildren().addAll(title, difficultyHbox, description, hbox);

        //Css stylesheet added
        getStylesheets().add("file:src/main/resources/stylesheets/createWorld.css");
    }


}
