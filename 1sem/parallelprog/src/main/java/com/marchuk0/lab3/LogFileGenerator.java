package com.marchuk0.lab3;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class LogFileGenerator {
    private static final String FILE = "file.log";

    public static void main(String[] args) throws IOException {
        int numberOfEntries = 10_000_000;
        generateLogFile(FILE, numberOfEntries);
        System.out.println("File generated: " + FILE);
    }

    public static void generateLogFile(String filePath, int numberOfEntries) throws IOException {
        Random random = new Random();
        DataOutputStream output = new DataOutputStream(new FileOutputStream(filePath));
        long timestamp = System.currentTimeMillis();

        for (int i = 0; i < numberOfEntries; i++) {
            int eventType = random.nextInt(5);
            timestamp += random.nextInt(1000);

            LogEntry logEntry = new LogEntry(eventType, timestamp);
            LogEntry.write(output, logEntry);
        }

        output.close();
    }
}
