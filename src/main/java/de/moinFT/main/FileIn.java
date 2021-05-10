package de.moinFT.main;


import java.io.*;

public class FileIn {
    public static String read(String filePath) {
        StringBuilder fileContent = new StringBuilder();

        try {
            InputStream in = Main.class.getResourceAsStream(filePath);
            if (in != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                while (reader.ready()) {
                    fileContent.append(reader.readLine());
                }
            } else {
                System.out.println("An error occurred.");
                throw new IllegalArgumentException("file not found! " + filePath);
            }
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }
}
