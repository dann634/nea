package com.jackson.ui;

import com.jackson.main.Main;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class SettingsController {

    private Scene scene;
    private TextField displayNameTextField;
    private Button muteSoundEffectsButton;
    private Button muteBackgroundButton;



    public SettingsController() {

       //Load Settings from File

        VBox root = new VBox();
        Main.applyWindowSize(root); //Applies standard window size
        root.setId("root");

        addContent(root);

        this.scene = new Scene(root);
        this.scene.getStylesheets().add("file:src/main/resources/stylesheets/settings.css");
    }


    private void addContent(VBox root) {
        var title = new Label("Settings");
        title.setId("title");
        root.getChildren().add(title);

        addDisplayName(root);
        addMuteSoundEffects(root);
        addMuteBackground(root);
        deleteSinglePlayerSave(root);
        addButtons(root);
    }

    private void addDisplayName(VBox root) {
        //Display Name
        var displayNameLabel = new Label("Display Name:");
        displayNameLabel.getStyleClass().add("label");

        this.displayNameTextField = new TextField();
        this.displayNameTextField.setPromptText("Display Name");
        this.displayNameTextField.getStyleClass().add("textField");

        root.getChildren().add(createHBox(displayNameLabel, this.displayNameTextField));
    } //Adds option to change display name

    private void addMuteSoundEffects(VBox root) { //Adds mute sound effects option
        var muteSoundEffectsLabel = new Label("Mute Sound Effects:");
//        muteSoundEffectsLabel.getStyleClass().add("label");

        var muteSoundEffectsToggle = getToggleButton();
        root.getChildren().add(createHBox(muteSoundEffectsLabel, muteSoundEffectsToggle));
    } //Adds option to mute sound effects

    private void addMuteBackground(VBox root) {
        var muteBackgroundLabel = new Label("Mute Background Music:");
        var muteBackgroundToggle = getToggleButton();

        root.getChildren().add(createHBox(muteBackgroundLabel, muteBackgroundToggle));
    } //Adds option to mute background music

    private void deleteSinglePlayerSave(VBox root) {
        var deleteSaveLabel = new Label("Singleplayer Save:");
        var deleteSaveBtn = new Button("Delete");
        deleteSaveBtn.setId("redBtn");

        root.getChildren().add(createHBox(deleteSaveLabel, deleteSaveBtn));
    } //Adds option to delete singleplayer save

    private void addButtons(VBox root) {
        var saveButton = new Button("Save");
        saveButton.setId("confirmButton");
        var backButton = new Button("Back");
        backButton.setId("confirmButton");
        backButton.setOnAction(e -> Main.getStage().setScene(new MainMenuController().getScene()));
        root.getChildren().add(createHBox(saveButton, backButton));
    }



    private HBox createHBox(Node ... nodes) {
        HBox hBox = new HBox();
        hBox.getStyleClass().add("hbox");

        hBox.getChildren().addAll(nodes);

        return hBox;
    }

    private Button getToggleButton() {
        Button btn = new Button("Unmuted");
        btn.setId("greenBtn");
        btn.setOnAction(e -> {
            if(btn.getText().equals("Unmuted")) {
                btn.setId("redBtn");
                btn.setText("Muted");
            } else {
                btn.setId("greenBtn");
                btn.setText("Unmuted");
            }
        });
        return btn;
    }


    public Scene getScene() {
        return this.scene;
    }


}
