package com.jackson.game;

import java.lang.reflect.Array;
import java.util.*;

public class ProceduralGenerator {

    //Fractal Terrain Generation
    //Midpoint displacement

    private static final int CHUNK_SIZE = 200;
    private static boolean isPositive;

    public static List<Integer> getHeightMapChunk(int startY, int range) {
        /*
        It works in integers for block height
        It takes the range of values for displacement
        It cuts the lines in half and changes the Y value
         */
        Random rand = new Random();
        isPositive = rand.nextBoolean(); //Does chunk slope up or down

        int endY = isPositive ? rand.nextInt(range) + startY : startY - rand.nextInt(range); //End Y location

        //Main breakup of loop
        List<Integer> inputList = new ArrayList<>();
        while (inputList.size() < CHUNK_SIZE) inputList.add(0); //Fills arraylist with 0s
        inputList.set(0, startY); //Adds starting height of chunk
        inputList.set(inputList.size() - 1, endY); //Adds ending height of chunk

        return getSmallChunk(inputList);
    }

    private static List<Integer> getSmallChunk(List<Integer> heights) {
        int midpoint = heights.size() / 2; //Gets midpoint of two points
        int lowerbound = heights.get(0); //Gets lowest point of line
        int upperbound = heights.get(heights.size() - 1); //Gets highest point of line
        int offset = 0;
        if(upperbound - lowerbound != 0) { //Range cannot be 0
            offset = new Random().nextInt(Math.abs(upperbound - lowerbound));
        }
        int midpointY = isPositive ? offset + lowerbound : lowerbound - offset; //Adds offset depending on gradient

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
