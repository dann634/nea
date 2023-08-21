package com.jackson.ui;

import com.jackson.io.TextIO;
import com.jackson.main.Main;
import com.jackson.network.connections.GlobalServerConnection;
import com.jackson.network.shared.Lobby;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LobbyController extends Scene {

    //Center
    private GridPane updatableGrid;
    private VBox centerVbox;
    private List<Lobby> lobbyList;


    //Left menu
    private Label connectedToServerLabel;
    private Label pingLabel;
    private Label lobbiesActiveLabel;
    public LobbyController() {

        super(new VBox());


        BorderPane root = new BorderPane();
        Main.applyWindowSize(root);

        root.setLeft(getLeftMenu());
        root.setCenter(getCenterMenu());
        root.setBottom(getBottomMenu());

        updateUI("none");

        setRoot(root);
        getStylesheets().add("file:src/main/resources/stylesheets/lobbyMenu.css");

    }

    private VBox getLeftMenu() {
        VBox vBox = new VBox();
        vBox.setId("leftVbox");

        var searchBar = new TextField();
        searchBar.setPromptText("Search");
        searchBar.textProperty().addListener((observableValue, s, t1) -> { //search filter
            updateUI(t1);
        });

        this.connectedToServerLabel = new Label();
        this.pingLabel = new Label();
        setConnectedToServerLabel(true);

        this.lobbiesActiveLabel = new Label();



        var displayNameLabel = new Label("Display Name: " +  TextIO.readFile("src/main/resources/settings/settings.txt").get(0)); //Get display name from file
        displayNameLabel.setStyle("-fx-font-size: 17");



        vBox.getChildren().addAll(searchBar, this.connectedToServerLabel, this.pingLabel, lobbiesActiveLabel ,displayNameLabel);

        return vBox;
    }

    private HBox getBottomMenu() {
        HBox hBox = new HBox();
        hBox.getStyleClass().add("hbox");

        var backButton = new Button("Back");
        var refreshButton = new Button("Refresh");
        var hostButton = new Button("Host");
        var connectButton = new Button("Connect");

        backButton.setOnAction(e -> Main.setScene(new MainMenuController()));
        refreshButton.setOnAction(e -> updateUI("none"));

        hBox.getChildren().addAll(backButton, refreshButton, hostButton, connectButton);

        return hBox;
    }

    private VBox getCenterMenu() {
        this.centerVbox = new VBox();
        this.centerVbox.getStyleClass().add("vbox");

        var title = new Label("Lobby Selector");
        title.setId("title");

        var headerHBox = new HBox();
        headerHBox.setId("headerHbox");
        var nameHeaderLabel = new Label("Name");
        var difficultyHeaderLabel = new Label("Difficulty");
        var playerHeaderLabel = new Label("Players");
        var hasPasswordLabel = new Label("Has Password");
        headerHBox.getChildren().addAll(nameHeaderLabel, difficultyHeaderLabel, playerHeaderLabel, hasPasswordLabel);

        // TODO: 19/08/2023 add columnn contrainsts to match headers to info


        var scrollpane = new ScrollPane();
        this.updatableGrid = new GridPane();
        this.updatableGrid.getStyleClass().add("gridpane");
        this.updatableGrid.setGridLinesVisible(true);

        //Column Constraints
        var nameColumn = new ColumnConstraints();
        nameColumn.setMinWidth(80);
        var difficultyColumn = new ColumnConstraints();
        difficultyColumn.setMinWidth(80);
        var playerColumn = new ColumnConstraints();
        playerColumn.setMinWidth(30);
        var passwordColumn = new ColumnConstraints();
        passwordColumn.setMinWidth(30);
        var joinColumn = new ColumnConstraints();
        joinColumn.setMinWidth(30);


        this.updatableGrid.getColumnConstraints().addAll(nameColumn, difficultyColumn, playerColumn, passwordColumn, joinColumn);


        scrollpane.setContent(this.updatableGrid);

        scrollpane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        this.centerVbox.getChildren().addAll(title, headerHBox, scrollpane);

        return this.centerVbox;
    }

    private void updateUI(String filter) {
        this.updatableGrid.getChildren().clear();
        try {
            setConnectedToServerLabel(true);
            this.lobbyList = GlobalServerConnection.getLobbyList();
        } catch (IOException | InterruptedException e) {
            setConnectedToServerLabel(false);
            this.lobbyList = new ArrayList<>();
        }

        if(filter.equals("none")) {
            try {
                this.pingLabel.setText("Ping: " + GlobalServerConnection.pingServer() + "ms");
                this.lobbiesActiveLabel.setText("Lobbies Active: " + this.lobbyList.size());
            } catch (IOException | InterruptedException ignored) {
            }
        }

        for(int i = 0; i < this.lobbyList.size(); i++) {
            var lobby = this.lobbyList.get(i);


            //toLowerCase so its not case sensitive
            if(!filter.equals("none") && !lobby.getName().toLowerCase().contains(filter.toLowerCase())) { //Search Bar filter
                continue;
            }

            List<Node> nodeList = new ArrayList<>();

            var name = new Label(lobby.getName());
            var difficulty = new Label(lobby.getDifficulty().name());
            var players = new Label(lobby.getCurrentPlayers() + "/" + lobby.getMaxPlayers());
            var hasPassword = new Label((lobby.getPassword().equals("") ? "No" : "Yes"));
            var joinButton = new Button("Join");
            joinButton.setId("joinButton");

            // TODO: 18/08/2023 change vbox to grid pane and just apply the styling to whole row
            // TODO: 18/08/2023 2d arraylist of node and just update it 


            this.updatableGrid.addRow(i, name, difficulty, players, hasPassword, joinButton);
        }
        this.centerVbox.getChildren().removeIf(n -> n instanceof Label && ((Label) n).getText().equals("No Lobbies Found"));
        if(this.updatableGrid.getChildren().isEmpty()) {
            //No lobbies found
            var noLobbiesLabel = new Label("No Lobbies Found");
            noLobbiesLabel.setId("noLobbies");
            this.centerVbox.getChildren().add(2, noLobbiesLabel);
        }

    }


    private void setConnectedToServerLabel(boolean isConnected) {
        this.connectedToServerLabel.setText(isConnected ? "Connected to Main Server" : "Not Connected to Main Server");
        this.connectedToServerLabel.setStyle("-fx-text-fill: " + (isConnected ? "#0aad07" : "#c7200e"));

        this.pingLabel.setStyle("-fx-text-fill: " + (isConnected ? "#0aad07" : "#c7200e"));
        if(!isConnected) {
            this.pingLabel.setText("Ping: N/A");
        }

    }

}
