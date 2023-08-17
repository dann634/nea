package com.jackson.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TextReader {


    public static List<String> readFile(String dir) throws IOException {
        if(!new File(dir).exists()) {
            System.err.println("Error: File Not Found");
            return null;
        }
        BufferedReader reader = new BufferedReader(new FileReader(dir));
        List<String> data = new ArrayList<>();

        String readLine = "";
        while(true) {
            readLine = reader.readLine();
            if(readLine == null || readLine.isEmpty()) {
                break;
            }
            data.add(readLine);
        }
        reader.close();
        return data;
    }

}
