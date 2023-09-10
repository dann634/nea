package com.jackson.io;

import com.jackson.game.ProceduralGenerator;

import java.io.*;
import java.time.chrono.HijrahEra;
import java.util.ArrayList;
import java.util.List;

public class TextIO {


    public static List<String> readFile(String dir) {

        List<String> data = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));

            String readLine = "";
            while(true) {
                readLine = reader.readLine();
                if(readLine == null) {
                    break;
                }
                data.add(readLine);
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error: Reading File Failed");
        }
        return data;
    }

    public static boolean updateFile(String dir, List<String> data) {

        if(data == null || data.isEmpty()) { //Gatekeeping
            return false;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dir, false));

            writer.write(""); //Clears file
            for(String line : data) {
                writer.write(line + "\n"); //Writes new data and adds line break
            }

            writer.close(); //Closes bufferedWriter
        } catch (IOException e) {
            System.err.println("Error: File Writing Failed");
            return false;
        }
        return true; // returns true if file write was successful
    }




    public static boolean writeMap(int[][] bitmap, String dir) {
        if(bitmap == null) { //Gatekeeping
            return false;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dir, false));
            for (int i = 0; i < bitmap[0].length; i++) {
                for (int j = 0; j < bitmap.length; j++) {
                    writer.write(String.valueOf(bitmap[j][i]));
                }
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static String[][] readMapFile(boolean isSingleplayer) {
        String dir = "src/main/resources/saves/";
        dir += isSingleplayer ? "singleplayer.txt" : "multiplayer.txt";

        if(!new File(dir).exists()) {
            System.err.println("Save file not found");
        }

        String[][] mapFile = new String[ProceduralGenerator.getWidth()][ProceduralGenerator.getHeight()];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String[] tempArray;
            for (int i = 0; i < ProceduralGenerator.getHeight(); i++) {

                tempArray = reader.readLine().split("");
                for (int j = 0; j < ProceduralGenerator.getWidth(); j++) {
                    mapFile[j][i] = tempArray[j];
                }
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return mapFile;
    }

}
