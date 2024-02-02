package com.jackson.ui;

import com.jackson.io.TextIO;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioPlayer {

    private final MediaPlayer mediaPlayer;

    /*
 Adventure by Alexander Nakarada | https://creatorchords.com
Music promoted by https://www.chosic.com/free-music/all/
Attribution 4.0 International (CC BY 4.0)
https://creativecommons.org/licenses/by/4.0/

     */

    public AudioPlayer(String sound) {
        Media media = new Media(getClass().getResource("/sound/" + sound + ".mp3").toExternalForm());
        this.mediaPlayer = new MediaPlayer(media);
        this.mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

        //volume
        if(sound.equals("background") || sound.equals("boss")) {
            setVolume(Double.parseDouble(TextIO.readFile("src/main/resources/settings/settings.txt").get(2)) / 100);
        } else {
            setVolume(Double.parseDouble(TextIO.readFile("src/main/resources/settings/settings.txt").get(1)) / 100);
        }
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

    public void setCycleCount(int value) {
        this.mediaPlayer.setCycleCount(value);
    }

    public boolean isPlaying() {
        return this.mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public void playFromBeginning() {
        this.mediaPlayer.seek(Duration.millis(0));
        this.mediaPlayer.play();
    }
}
