package de.moinFT.main;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class FileIn {
    private static final Logger log = LogManager.getLogger(FileIn.class.getName());

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
                log.error("An error occurred.");
                throw new IllegalArgumentException("file not found! " + filePath);
            }
        } catch (FileNotFoundException e) {
            log.error("An error occurred.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent.toString();
    }
}
