package com.jackson.ui;

import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.network.connections.Client;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SettingsController extends Scene {

    private TextField displayNameTextField;
    private Slider backgroundSlider;
    private Slider soundEffectsSlider;


    public SettingsController() {
        super(new VBox());

       //Load Settings from File

        VBox root = new VBox();
        Main.applyWindowSize(root); //Applies standard window size
        root.setId("root");

        addContent(root);
        loadSettings();

        setRoot(root);
        getStylesheets().add("file:src/main/resources/stylesheets/settings.css");
    }

    private void loadSettings() {
        List<String> settingsList = TextIO.readFile("src/main/resources/settings/settings.txt"); //Reads settings file which holds display name, and if muted
        if(settingsList.size() < 3) { //If there is less than 3 settings it will throw an ArrayOutOfBoundsException
            return;
        }
        this.displayNameTextField.setText(settingsList.get(0)); //Set text to current display name



        double backgroundVolume;
        double effectsVolume;
        try {
            backgroundVolume = Double.parseDouble(settingsList.get(2));
        } catch (NumberFormatException e) {
            backgroundVolume = 100;
        }
        try {
            effectsVolume = Double.parseDouble(settingsList.get(1));
        } catch (NumberFormatException e) {
            effectsVolume = 100;
        }
        this.backgroundSlider.setValue(backgroundVolume);
        this.soundEffectsSlider.setValue(effectsVolume);



    }

    private void addContent(VBox root) {
        Label title = new Label("Settings");
        title.setId("title");
        root.getChildren().add(title);

        addDisplayName(root);
        addMuteSoundEffects(root);
        addMuteBackground(root);
        deleteSinglePlayerSave(root);
        deleteMultiplayerSave(root);
        addButtons(root);
    }

    private void addDisplayName(VBox root) {
        //Display Name
        var displayNameLabel = new Label("Display Name:");
        displayNameLabel.getStyleClass().add("label");

        //Validity Check
        Circle circle = new Circle(); //Circle that changes colour to indicate if display name is valid
        circle.setRadius(10);


        this.displayNameTextField = new TextField();
        this.displayNameTextField.setPromptText("Display Name");
        this.displayNameTextField.textProperty().addListener((observableValue, number, t1) -> { //Changes color of circle
            if(t1.isEmpty() || t1.length() > 15) {
                circle.setFill(Color.web("#c7200e"));
            } else {
                circle.setFill(Color.web("#0aad07"));
            }
        });
        var hbox  = createHBox(displayNameLabel, this.displayNameTextField);
        hbox.getChildren().add(2, circle);
        root.getChildren().add(hbox);
    } //Adds option to change display name

    private void addMuteSoundEffects(VBox root) {
        var muteSoundEffectsLabel = new Label("Sound Effects Volume:");
        this.soundEffectsSlider = new Slider();
        var valueLabel = new Label(String.valueOf(this.soundEffectsSlider.getValue()));
        valueLabel.setId("value");
        valueLabel.textProperty().bind(soundEffectsSlider.valueProperty().asString("%.0f"));

        root.getChildren().add(createHBox(muteSoundEffectsLabel, valueLabel, soundEffectsSlider));
    } //Adds option to mute sound effects

    private void addMuteBackground(VBox root) {
        var muteBackgroundLabel = new Label("Background Music Volume:");
        this.backgroundSlider = new Slider();

        var valueLabel = new Label(String.valueOf(this.backgroundSlider.getValue()));
        valueLabel.setId("value");
        valueLabel.textProperty().bind(backgroundSlider.valueProperty().asString("%.0f"));


        root.getChildren().add(createHBox(muteBackgroundLabel, valueLabel, this.backgroundSlider));
    } //Adds option to mute background music

    private void deleteSinglePlayerSave(VBox root) {
        var deleteSaveLabel = new Label("Singleplayer Save:");
        var deleteSaveBtn = new Button("Delete");
        deleteSaveBtn.setId("redBtn");

        deleteSaveBtn.setOnAction(e -> {
            new File("src/main/resources/saves/singleplayer.txt").delete();
            new File("src/main/resources/saves/single_data.txt").delete();
            deleteSaveBtn.setDisable(true);
            deleteSaveBtn.setOpacity(0.7);
        });

        root.getChildren().add(createHBox(deleteSaveLabel, deleteSaveBtn));
    } //Adds option to delete single-player save

    private void deleteMultiplayerSave(VBox root) {
        var deleteSaveLabel = new Label("Multiplayer Save:");
        var deleteSaveButton = new Button("Delete");
        deleteSaveButton.setId("redBtn");
        deleteSaveButton.setOnAction(e -> {
            try {
                Client client = new Client();
                client.deleteSave();
            } catch (IOException ex) {
                deleteSaveButton.setText("Error");
            }
        });

        root.getChildren().addAll(deleteSaveLabel, deleteSaveButton);
    }

    private void addButtons(VBox root) {
        var saveButton = new Button("Save");
        saveButton.setId("confirmButton");
        saveButton.setOnAction(e -> {
            //Update Settings Text File
            List<String> newSettings = new ArrayList<>();
            String displayName = this.displayNameTextField.getText();
            if(displayName.length() < 3 || displayName.length() > 15) { //If display name is not valid, settings not updated
                return;
            }

            //Gets new data from ui
            newSettings.add(displayName);
            newSettings.add(String.valueOf(this.soundEffectsSlider.getValue()));
            newSettings.add(String.valueOf(this.backgroundSlider.getValue()));

            TextIO.updateFile(newSettings, "src/main/resources/settings/settings.txt"); //Updates text file with new settings

        });

        //Back Button
        var backButton = new Button("Back");
        backButton.setId("confirmButton");
        backButton.setOnAction(e -> Main.setScene(new MainMenuController())); //Back to main menu

        root.getChildren().add(createHBox(saveButton, backButton)); //Add Buttons to root
    }

    private HBox createHBox(Node ... nodes) { //template for each row of settings (varargs for re-usability)

        HBox hBox = new HBox();
        hBox.getStyleClass().add("hBox"); //Can't find css for Hbox name

        Region region = new Region(); //Used to push nodes to edge of hbox (alignment)

        hBox.getChildren().addAll(nodes);
        HBox.setHgrow(region, Priority.ALWAYS);
        hBox.getChildren().add(1, region); //Seperates label from button / text field

        return hBox;
    }




}
