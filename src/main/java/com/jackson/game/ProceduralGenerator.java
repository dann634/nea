package com.jackson.game;

import com.jackson.io.TextIO;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class ProceduralGenerator {

    //Fractal Terrain Generation
    //Midpoint displacement

    private static final int CHUNK_SIZE = 100;
    private static boolean isPositive;
    private static int START_Y = 150;
    private static int RANGE = 50;

    private static int WIDTH = 1000;
    private static int HEIGHT = 300;

    /*
    0 - Air
    1 - Dirt
    2 - Grass
    3 - Bedrock
     */

    public static void createMapFile(boolean isSingleplayer)  {
        List<Integer> fullHeightMap = new ArrayList<>();
        while (fullHeightMap.size() < WIDTH) { // Loops until map is 1000 blocks wide
            fullHeightMap.addAll(getHeightMapChunk(RANGE));
        }
        int[][] heightMapArray = new int[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            try {
                //Air blocks
                for (int j = 0; j < fullHeightMap.get(i); j++) {
                    heightMapArray[i][j] = 0;

                }

                //Grass layer
                heightMapArray[i][fullHeightMap.get(i)] = 2;

                //Dirt Layer
                for (int j = fullHeightMap.get(i)+1; j <= HEIGHT - 2; j++) {
                    heightMapArray[i][j] = 1;
                }

                //Bedrock layer
                heightMapArray[i][HEIGHT-1] = 3;

                // TODO: 07/09/2023 Add stone
            } catch (IndexOutOfBoundsException e) {
                System.out.println(fullHeightMap.get(i));
            }
        }

        //Add to file
        String dir = "src/main/resources/saves/";
        dir += isSingleplayer ? "singleplayer.txt" : "multiplayer.txt";
        try {
            new File(dir).createNewFile();
        } catch (IOException e) {
            // TODO: 07/09/2023 Add error message
            System.err.println("Map could not be created");
        }

        TextIO.writeMap(heightMapArray, dir);

    }

    private static List<Integer> getHeightMapChunk(int range) {
        /*
        It works in integers for block height
        It takes the range of values for displacement
        It cuts the lines in half and changes the Y value
         */
        Random rand = new Random();
        isPositive = rand.nextBoolean(); //Does chunk slope up or down

        int endY = isPositive ? rand.nextInt(range) + START_Y : START_Y - rand.nextInt(range); //End Y location

        //Main breakup of loop
        List<Integer> inputList = new ArrayList<>();
        while (inputList.size() < CHUNK_SIZE) inputList.add(0); //Fills arraylist with 0s
        inputList.set(0, START_Y); //Adds starting height of chunk
        inputList.set(inputList.size() - 1, endY); //Adds ending height of chunk

        List<Integer> list = getSmallChunk(inputList);
        START_Y = list.get(list.size()-1);
        return list;
    }

    private static List<Integer> getSmallChunk(List<Integer> heights) {

        // FIXME: 07/09/2023 Creates values >300 or <0

        int midpoint = heights.size() / 2; //Gets midpoint of two points
        int lowerbound = heights.get(0); //Gets lowest point of line
        int upperbound = heights.get(heights.size() - 1); //Gets highest point of line
        int offset = 0;
        if(upperbound - lowerbound != 0) { //Range cannot be 0
            offset = new Random().nextInt(Math.abs(upperbound - lowerbound));
        }
        int midpointY = isPositive ? offset + lowerbound : lowerbound - offset; //Adds offset depending on gradient

        if(midpointY < 1) {
            midpointY = 1;
        } else if(midpointY > 300) {
            midpointY = 299;
        }
        heights.set(midpoint, midpointY); //Adds new value to list

        if (!isHeightsFull(heights)) { //Checks if all values are full
            List<Integer> firstHalf = heights.subList(0, midpoint + 1); //Gets first half of array
            List<Integer> secondHalf = heights.subList(midpoint, heights.size()); //Gets second half of array

            getSmallChunk(firstHalf); //Recursive call for first half
            getSmallChunk(secondHalf); //Recursive call for second half
        }

        return heights;
    }

    private static boolean isHeightsFull(List<Integer> heights) { //Checks if list is full
        for (int i : heights) {
            if (i == 0) { //Not changed
                return false;
            }
        }
        return true;
    }


}
