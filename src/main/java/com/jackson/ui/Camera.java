package com.jackson.ui;

import com.jackson.game.Block;
import com.jackson.game.Character;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Camera {

    public static final int RENDER_WIDTH = 17;
    public static final int RENDER_HEIGHT = 9;
    private boolean isWorldLoadable;

    public Camera() {
        this.isWorldLoadable = true;
    }

    public Block[][] getRenderBlocks(String[][] map, Character character) { //Render 34 wide and 19 high
        //Get blocks around character
        Block[][] renderArray = new Block[RENDER_WIDTH * 2][RENDER_HEIGHT * 2];
        int blockXIndex = 0;
        int blockYIndex = 0;

        for (int i = character.getXPos() - RENDER_WIDTH; i < character.getXPos() + RENDER_WIDTH; i++) {
            blockYIndex = 0;
            for (int j = character.getYPos() - RENDER_HEIGHT; j < character.getYPos() + RENDER_HEIGHT; j++) {
                renderArray[blockXIndex][blockYIndex] = new Block(map[i][j], i, j);
                blockYIndex++;
            }
            blockXIndex++;
        }


        return renderArray;
    }

    public void checkForEdgeOfScreen(Character character, GameController gameController) {
        if(character.getX() < 100 || character.getX() > 924) {
            List<Block> blocksTouchingPlayer = gameController.getBlocksTouchingPlayer(character);
            //Just take first

            if(!blocksTouchingPlayer.isEmpty()) {
                character.setXPos(blocksTouchingPlayer.get(0).getXPos());
                character.setYPos(blocksTouchingPlayer.get(0).getYPos());
            }


           //Move world
            if(this.isWorldLoadable) {
                gameController.moveWorld();
                this.isWorldLoadable = false;
            }


        }

    }

    public Timeline getPanningTimeline(Block[][] oldBlocks, Block[][] screenBlocks, Character character, GameController gameController) {
        Timeline timeline = new Timeline();


//        for (int i = 0; i < oldBlocks.length; i++) { //Shift old world
//            for (int j = 0; j < oldBlocks[i].length; j++) {
//                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000), new KeyValue(oldBlocks[i][j].translateXProperty(), (i-1-RENDER_WIDTH) * 32, Interpolator.EASE_BOTH)));
//            }
//        }

        for(int i = 0; i< screenBlocks.length; i++) { //Shift new World
            for (int j = 0; j< screenBlocks[i].length; j++) {
//                System.out.println((i-1) * 32);
                timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000), new KeyValue(screenBlocks[i][j].translateXProperty(), (i-1) * 32, Interpolator.EASE_BOTH)));            }
        }
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000), new KeyValue(screenBlocks[0][0].translateXProperty(), 0, Interpolator.EASE_BOTH)));

        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000), new KeyValue(character.xProperty(), 100, Interpolator.EASE_BOTH)));
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(3000), new KeyValue(character.yProperty(), character.getY(), Interpolator.EASE_BOTH)));

        return timeline;
    }

}
