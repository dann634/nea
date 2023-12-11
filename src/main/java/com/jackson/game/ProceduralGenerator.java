package com.jackson.game;

import com.jackson.io.TextIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProceduralGenerator {

    //Fractal Terrain Generation
    //Midpoint displacement

    private static final int CHUNK_SIZE = 100;
    private static boolean isPositive;
    private static int START_Y = 150;
    private static final int RANGE = 30;
    private static final int WIDTH = 1000; //Map Width
    private static final int HEIGHT = 300; //Map Height
    private static final double TREE_SPAWN_CHANCE = 0.15; //On any grass block
    private static final double NUMBER_OF_CHUNKS = 10;

    /*
    0 - Air
    1 - Dirt
    2 - Grass
    3 - Bedrock
    4 - Stone
    5 - Wood
    6 - Leaves
     */

    //Creates the 2D array of numbers to indicate block type
    public static void createMapFile(boolean isSingleplayer)  {
        List<Integer> fullHeightMap = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
            fullHeightMap.addAll(getHeightMapChunk(i+1)); //Adds chunks to height map until its 1000 in size
        }

        String[][] heightMapArray = new String[WIDTH][HEIGHT];
        for (int i = 0; i < heightMapArray.length; i++) { //Loops through each X coordinate
            //Air blocks
            for (int j = 0; j < fullHeightMap.get(i); j++) heightMapArray[i][j] = "0";

            //Grass layer
            heightMapArray[i][fullHeightMap.get(i)] = "2";

            try {
                //Dirt Layer
                for (int j = fullHeightMap.get(i)+1; j <= fullHeightMap.get(i)+21; j++) heightMapArray[i][j] = "1";

                //Stone layer
                for (int j = fullHeightMap.get(i)+22; j <= HEIGHT - 2; j++) heightMapArray[i][j] = "4";
            } catch (ArrayIndexOutOfBoundsException e) {

            }

            //Bedrock layer
            heightMapArray[i][HEIGHT-1] = "3";
        }
//        System.out.println();
        spawnTrees(fullHeightMap, heightMapArray);

                // FIXME: 01/12/2023 bedrock is overwritten


        //Add to file
        String dir = "src/main/resources/saves/";
        dir += isSingleplayer ? "singleplayer.txt" : "multiplayer.txt"; //Adds correct file name
        try {
            new File(dir).createNewFile(); //Creates file
        } catch (IOException e) {
            System.err.println("Map file could not be created");
        }

        TextIO.writeMap(heightMapArray, dir); //Writes 2D array to file

    }


    private static List<Integer> getHeightMapChunk(int chunkNumber) {
        /*
        It works in integers for block height
        It takes the range of values for displacement
        It cuts the lines in half and changes the Y value
         */
        Random rand = new Random();
        isPositive = rand.nextBoolean(); //Does chunk slope up or down
        List<Integer> inputList = new ArrayList<>();

        int endY = isPositive ? rand.nextInt(RANGE) + START_Y : START_Y - rand.nextInt(RANGE); //End Y location
        if(chunkNumber == NUMBER_OF_CHUNKS) {
            endY = 150;
            isPositive = (START_Y - 150) < 0;
        }

        //Main breakup of loop

        while (inputList.size() < CHUNK_SIZE) inputList.add(0); //Fills arraylist with 0s
        inputList.set(0, START_Y); //Adds starting height of chunk
        inputList.set(inputList.size() - 1, endY); //Adds ending height of chunk

        List<Integer> list = getChunk(inputList); //Gets chunk
        START_Y = list.get(list.size()-1); //start location is updated for next chunk
        return list;
    }

    private static List<Integer> getChunk(List<Integer> heights) {

        int midpoint = heights.size() / 2; //Gets midpoint of two points
        int lowerbound = heights.get(0); //Gets the lowest point of line
        int upperbound = heights.get(heights.size() - 1); //Gets the highest point of line
        int offset = 0;
        if(upperbound - lowerbound != 0) { //Range cannot be 0
            //New offset is created from the difference of upper and lower bounds
            offset = new Random().nextInt(Math.abs(upperbound - lowerbound));
        }
        int midpointY = isPositive ? offset + lowerbound : lowerbound - offset; //Adds offset depending on gradient

        //Range check so array doesn't throw ArrayOutOfBoundsException
        if(midpointY < 10) {
            midpointY = 11;
        } else if(midpointY > 290) {
            midpointY = 289;
        }
        heights.set(midpoint, midpointY); //Adds new value to list

        if (!isHeightsFull(heights)) { //Checks if all values are full
            List<Integer> firstHalf = heights.subList(0, midpoint + 1); //Gets first half of array
            List<Integer> secondHalf = heights.subList(midpoint, heights.size()); //Gets second half of array

            getChunk(firstHalf); //Recursive call for first half
            getChunk(secondHalf); //Recursive call for second half
        }

        return heights; //Returns array to calling method
    }

    private static boolean isHeightsFull(List<Integer> heights) { //Checks if list is full
        for (int i : heights) {
            if (i == 0) { //Not changed
                return false;
            }
        }
        return true;
    }

    private static void spawnTrees(List<Integer> heightMap, String[][] map) {
        Random rand = new Random();
        boolean treeProximityFlag = false; //Flag
        for (int i = 0; i < WIDTH; i++) { //Runs entire length of map
            if(rand.nextDouble() < TREE_SPAWN_CHANCE && !treeProximityFlag) {
                //Does random number fall within range and is there no tree next to this
                treeProximityFlag = true; //Set true for next block
                //Spawn tree
                //Wood
                //Goes to top of grass and then gets a random truncated number from the normal distribution
                int topOfTree = heightMap.get(i) - 1 - (int) rand.nextGaussian(5, 1);
                for (int j = heightMap.get(i)-1; j > topOfTree; j--) { //Creates trunk
                    try {
                        map[i][j] = "5"; //5 is code for wood
                    } catch (ArrayIndexOutOfBoundsException ignored) {}
                }

                //Leaves (Pyramid Pattern)
                int length = 5;
                for (int j = topOfTree; j > topOfTree - 3; j--) {
                    for (int k = i - (length / 2); k < i + (length / 2) + 1; k++) {
                        try{
                            map[k][j] = "6"; //6 is code for leaves
                        } catch (ArrayIndexOutOfBoundsException ignored) {
                        }
                    }
                    length--; //Must decrement for cone shape
                }

                continue;
            }
            treeProximityFlag = false;
        }
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getHeight() {
        return HEIGHT;
    }


}
