package com.jackson.io;

import com.jackson.game.ProceduralGenerator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TextIO {

    private TextIO() {
    }


    public static List<String> readFile(String dir) {

        List<String> data = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));

            String readLine;
            while(true) {
                readLine = reader.readLine();
                if(readLine == null) {
                    break;
                }
                data.add(readLine);
            }
            reader.close();
        } catch (IOException ignored) {
        }
        return data;
    }

    public static void updateFile(List<String> data, String dir) {

        if(data == null || data.isEmpty()) { //Gatekeeping
            return;
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
        }
    }




    public static void writeMap(String[][] bitmap, String dir) {
        if(bitmap == null || bitmap.length == 0) { //Gate keeping
            return;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(dir, false));
            for (int i = 0; i < 300; i++) {
                //From top to bottom of map
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < 1000; j++) {
                    //Adds characters to line
                    line.append(bitmap[j][i]);
                }
                //Writes whole line to file
                writer.write(line.toString());
                writer.newLine(); //New Line
            }
            writer.close(); //CLose writer
        } catch (IOException e) {
            System.err.println("Error: Saving Map File Failed");
        }
    }

    public static String[][] readMapFile() {
        String dir = "src/main/resources/saves/singleplayer.txt";

        if(Files.notExists(Path.of(dir))) {
            System.err.println("Save file not found");
            return null;
        }

        String[][] mapFile = new String[ProceduralGenerator.getWidth()][ProceduralGenerator.getHeight()];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String[] tempArray;
            for (int i = 0; i < ProceduralGenerator.getHeight(); i++) {

                tempArray = reader.readLine().split("");
                if(tempArray.length == 0) {
                    continue;
                }
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
