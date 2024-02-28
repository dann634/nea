package com.jackson.ui;

import com.jackson.io.TextIO;
import com.jackson.main.Main;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;

public class HelpController extends Scene {

    private final HashMap<String, String> map;

    private final Label sectionTitle;
    private final Label descriptionLabel;

    public HelpController() {
        super(new VBox());
        BorderPane root = new BorderPane();
        map = new HashMap<>();

        initMap();

        //Title
        Label title = new Label("Help");
        title.setId("title");

        //Back Button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> Main.setScene(new MainMenuController()));
        HBox hBox = new HBox();
        hBox.setId("bottomHbox");
        hBox.getChildren().add(backButton);

        VBox selectorMenu = getSelectorMenu();
        VBox descriptionVbox = new VBox();
        descriptionVbox.setId("descriptionVbox");
        //Section Title
        sectionTitle = new Label();
        sectionTitle.setId("sectionTitle");

        //Label
        descriptionLabel = new Label();
        descriptionLabel.setId("descriptionLabel");
        descriptionLabel.setWrapText(true);
        descriptionVbox.getChildren().addAll(sectionTitle, descriptionLabel);

        root.setTop(title);
        root.setBottom(hBox);
        root.setLeft(selectorMenu);
        root.setCenter(descriptionVbox);

        getStylesheets().add("file:src/main/resources/stylesheets/helpController.css");
        Main.applyWindowSize(root);
        setRoot(root);
    }

    //creates a map for title to help text
    private void initMap() {
        List<String> helpInfo = TextIO.readFile("src/main/resources/settings/help.txt");
        for (String line : helpInfo) {
            //Split line up
            String[] splitLine = line.split(" ", 2);
            if (splitLine.length < 2) continue;
            map.put(splitLine[0], splitLine[1]);
        }
    }

    //Creates the menu to select each heading
    private VBox getSelectorMenu() {
        VBox vBox = new VBox();
        vBox.setId("selectorVbox");

        map.keySet().forEach(n -> {
            Label label = new Label(n);
            label.getStyleClass().add("selectorLabel");
            label.setOnMouseClicked(e -> updateText(n));
            vBox.getChildren().add(label);
        });

        return vBox;
    }

    //Updates the text in the middle of the screen
    private void updateText(String menuName) {
        sectionTitle.setText(menuName);
        descriptionLabel.setText(map.get(menuName));
    }
}
