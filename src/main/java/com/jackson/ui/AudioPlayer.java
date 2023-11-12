package com.jackson.ui;

import com.jackson.io.TextIO;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioPlayer {

    private final MediaPlayer mediaPlayer;
    private Media media;

    /*
 Adventure by Alexander Nakarada | https://creatorchords.com
Music promoted by https://www.chosic.com/free-music/all/
Attribution 4.0 International (CC BY 4.0)
https://creativecommons.org/licenses/by/4.0/

     */

    public AudioPlayer() {
        this.media = new Media(getClass().getResource("/sound/background.mp3").toExternalForm());
        this.mediaPlayer = new MediaPlayer(media);
        this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        setVolume(Double.parseDouble(TextIO.readFile("src/main/resources/settings/settings.txt").get(2)) / 100);
    }

    public void play() {
        this.mediaPlayer.play();
    }

    public void pause() {
        this.mediaPlayer.pause();
    }

    public void setVolume(double volume) {
        this.mediaPlayer.setVolume(volume);
    }
}
