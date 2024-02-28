package com.jackson.game;

import com.jackson.io.TextIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProceduralGenerator {

    //Fractal Terrain Generation
    //Midpoint displacement

    private static final int CHUNK_SIZE = 100;
    private static final int RANGE = 30;
    private static final int MAP_WIDTH = 1000; //Map Width
    private static final int MAP_HEIGHT = 300; //Map Height
    private static final double TREE_SPAWN_CHANCE = 0.15; //On any grass block
    private static final double ORE_SPAWN_CHANCE = 0.03; //On any stone block
    private static final int NUMBER_OF_CHUNKS = 10;
    private static final String AIR_BLOCK = "0";
    private static final String DIRT_BLOCK = "1";
    private static final String GRASS_BLOCK = "2";
    private static final String BEDROCK_BLOCK = "3";
    private static final String STONE_BLOCK = "4";
    private static final String WOOD_BLOCK = "5";
    private static final String LEAVES_BLOCK = "6";
    private static final String METAL_BLOCK = "8";
    private static final String COAL_BLOCK = "9";
    private static final Random random = new Random();
    private static boolean isPositive;
    private static int START_Y = 150;

    /*
    Gets heightmap of integers
    Creates full map from heightmap
    Adds trees and ores
     */
    public static String[][] createMapArray() {
        List<Integer> fullHeightMap = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
            fullHeightMap.addAll(getHeightMapChunk(i + 1));
        }

        String[][] heightMapArray = initializeHeightMapArray(fullHeightMap);

        spawnTrees(fullHeightMap, heightMapArray);
        spawnOres(heightMapArray);
        return heightMapArray;
    }

    /*
    Fill in other dimension of world from the heightmap
    Adds air, dirt, grass, stone and bedrock
     */
    private static String[][] initializeHeightMapArray(List<Integer> fullHeightMap) {
        String[][] heightMapArray = new String[MAP_WIDTH][MAP_HEIGHT];

        for (int i = 0; i < MAP_WIDTH; i++) {
            int currentHeight = fullHeightMap.get(i);

            for (int j = 0; j < currentHeight; j++) {
                heightMapArray[i][j] = AIR_BLOCK;
            }

            heightMapArray[i][currentHeight] = GRASS_BLOCK;

            try {
                for (int j = currentHeight + 1; j <= currentHeight + 21; j++) {
                    heightMapArray[i][j] = DIRT_BLOCK;
                }

                for (int j = currentHeight + 22; j <= MAP_HEIGHT - 2; j++) {
                    heightMapArray[i][j] = STONE_BLOCK;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }

            heightMapArray[i][MAP_HEIGHT - 1] = BEDROCK_BLOCK;
        }

        return heightMapArray;
    }

    public static void saveMapToFile(String[][] heightMapArray) {
        //Filepath
        String dir = "src/main/resources/saves/singleplayer.txt";

        try {
            Files.createFile(Paths.get(dir)); //Create file
        } catch (IOException e) {
            System.err.println("Error: Map file could not be created");
        }

        TextIO.writeMap(heightMapArray, dir); //Write map to file
    }

    /*
    Generates a chunk (100 block)
    Uses the previous chunks end Y location
     */
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
        if (chunkNumber == NUMBER_OF_CHUNKS) {
            endY = 150;
            isPositive = (START_Y - 150) < 0;
        }

        //Main breakup of loop
        while (inputList.size() < CHUNK_SIZE) inputList.add(0); //Fills arraylist with 0s
        inputList.set(0, START_Y); //Adds starting height of chunk
        inputList.set(inputList.size() - 1, endY); //Adds ending height of chunk

        List<Integer> list = getChunk(inputList); //Gets chunk
        START_Y = list.get(list.size() - 1); //start location is updated for next chunk
        return list;
    }

    /*
    Recursive call
    Finds midpoint of line and generates a new random y between start and end
    Sublists and calls itself
     */
    private static List<Integer> getChunk(List<Integer> heights) {
        int midpoint = heights.size() / 2; //Gets midpoint of two points
        int lowerbound = heights.get(0); //Gets the lowest point of line
        int upperbound = heights.get(heights.size() - 1); //Gets the highest point of line
        int offset = 0;
        if (upperbound - lowerbound != 0) { //Range cannot be 0
            //New offset is created from the difference of upper and lower bounds
            offset = random.nextInt(Math.abs(upperbound - lowerbound));
        }
        //Adds offset depending on gradient
        int midpointY = isPositive ? offset + lowerbound : lowerbound - offset;
        midpointY = Math.max(11, Math.min(midpointY, 289)); // Range check
        heights.set(midpoint, midpointY); //Adds new value to list

        if (!isHeightsFull(heights)) { //Checks if all values are full
            List<Integer> firstHalf = heights.subList(0, midpoint + 1); //Gets first half of array
            List<Integer> secondHalf = heights.subList(midpoint, heights.size()); //Gets second half of array

            getChunk(firstHalf); //Recursive call for first half
            getChunk(secondHalf); //Recursive call for second half
        }

        return heights; //Returns array to calling method
    }

    //Returns false if there are any 0's
    private static boolean isHeightsFull(List<Integer> heights) { //Checks if list is full
        return heights.stream().allMatch(n -> n != 0);
    }

    /*
    Adds trees
    Trees have a chance to spawn on each grass block
    Trees cannot spawn directly next to each other
    They have a pyramid pattern for leaves
     */
    private static void spawnTrees(List<Integer> heightMap, String[][] map) {
        Random rand = new Random();
        boolean treeProximityFlag = false; //Flag
        for (int i = 0; i < MAP_WIDTH; i++) { //Runs entire length of map
            if (rand.nextDouble() < TREE_SPAWN_CHANCE && !treeProximityFlag) {
                //Does random number fall within range and is there no tree next to this
                treeProximityFlag = true; //Set true for next block
                //Spawn tree
                //Goes to top of grass and then gets a random truncated number from the normal distribution
                int topOfTree = heightMap.get(i) - 1 - (int) rand.nextGaussian(5, 1);
                for (int j = heightMap.get(i) - 1; j > topOfTree; j--) { //Creates trunk
                    try {
                        map[i][j] = WOOD_BLOCK;
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }
                }

                //Leaves (Pyramid Pattern)
                int length = 5;
                for (int j = topOfTree; j > topOfTree - 3; j--) {
                    for (int k = i - (length / 2); k < i + (length / 2) + 1; k++) {
                        try {
                            map[k][j] = LEAVES_BLOCK;
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

    /*
    Adds coal and metal ore
    Each stone block has a chance to turn into an ore block
     */
    private static void spawnOres(String[][] map) {
        //Loop through whole map
        for (int i = 0; i < MAP_WIDTH; i++) {
            for (int j = 0; j < MAP_HEIGHT; j++) {
                //If not stone
                if (!map[i][j].equals(STONE_BLOCK)) continue;
                //If not ore block
                if (random.nextDouble() > ORE_SPAWN_CHANCE) continue;

                //Turn stone into ore
                map[i][j] = random.nextBoolean() ? METAL_BLOCK : COAL_BLOCK;

            }
        }
    }

    //Getters
    public static int getWidth() {
        return MAP_WIDTH;
    }

    public static int getHeight() {
        return MAP_HEIGHT;
    }
}
