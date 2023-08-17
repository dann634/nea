package com.jackson.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TextReader {


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

}
