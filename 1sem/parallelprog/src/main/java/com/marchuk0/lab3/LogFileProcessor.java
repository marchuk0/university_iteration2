package com.marchuk0.lab3;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogFileProcessor {
    private static final String FILE = "file.log";
    private static final int NUM_THREADS = 1;

    public static void main(String[] args) {
        try {
            long startTime = System.nanoTime();
            run();
            long endTime = System.nanoTime();

            long totalMs = (endTime - startTime) / 1_000_000;
            System.out.println("Time: " + totalMs + "ms");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void run() throws InterruptedException {
        File logFile = new File(FILE);
        long logEntryNumber = logFile.length() / LogEntry.SIZE;
        long fragmentSize = logEntryNumber / NUM_THREADS;

        List<LogProcessor> logProcessors = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            long start = i * fragmentSize;
            long end = (i == NUM_THREADS - 1) ? logEntryNumber : start + fragmentSize;
            logProcessors.add(new LogProcessor(start, end));
        }

        runThreads(logProcessors);

        Statistics result = new Statistics();
        for (int i = 0; i < NUM_THREADS; i++) {
            result = Statistics.aggregate(result, logProcessors.get(i).statistics);
        }

        result.print();
    }

    private static void runThreads(List<LogProcessor> logProcessors) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            Thread thread = new Thread(logProcessors.get(i));
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.join();
        }
    }


    static class LogProcessor implements Runnable {
        private final long start;
        private final long length;
        final Statistics statistics = new Statistics();

        public LogProcessor(long start, long end) {
            this.start = start;
            this.length = end - start;
        }

        private void processFile() throws IOException {
            try (FileInputStream filestream = new FileInputStream(FILE)) {
                long bytesToSkip = start * LogEntry.SIZE;
                long skipped = filestream.skip(bytesToSkip);
                if (skipped != bytesToSkip) {
                    throw new RuntimeException("skipped != bytesToSkip " + skipped + " " + bytesToSkip);
                }

                DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(filestream));
                for (int i = 0; i < length; i++) {
                    LogEntry logEntry = LogEntry.from(dataInputStream);
                    onLogEntry(logEntry);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void onLogEntry(LogEntry logEntry) {
            statistics.onLogEntry(logEntry);
        }

        @Override
        public void run() {
            try {
                processFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class Statistics {
        Map<Integer, Long> typeToCounter = new HashMap<>();
        Map<Integer, Long> typeToSumOfTimeDiff = new HashMap<>();
        Map<Integer, Long> typeToFirstTime = new HashMap<>();
        Map<Integer, Long> typeToLastTime = new HashMap<>();

        public void onLogEntry(LogEntry logEntry) {
            long cnt = typeToCounter.getOrDefault(logEntry.type, 0L);
            typeToCounter.put(logEntry.type, cnt + 1);

            if (typeToFirstTime.get(logEntry.type) != null) {
                long lastTime = typeToLastTime.get(logEntry.type);
                long newValue = typeToSumOfTimeDiff.getOrDefault(logEntry.type, 0L) + logEntry.timestamp - lastTime;
                typeToSumOfTimeDiff.put(logEntry.type, newValue);
            } else {
                typeToFirstTime.put(logEntry.type, logEntry.timestamp);
            }
            typeToLastTime.put(logEntry.type, logEntry.timestamp);
        }
        
        public void print() {
            System.out.println("Counter: ");
            for (var entry : typeToCounter.entrySet()) {
                System.out.println("Type: " + entry.getKey() + " value: " + entry.getValue());
            }

            System.out.println("Average distance: ");
            for (var entry : typeToSumOfTimeDiff.entrySet()) {
                double sum = entry.getValue();
                long cnt = typeToCounter.get(entry.getKey());

                System.out.println("Type: " + entry.getKey() + " value: " + (sum) / cnt);
            }

        }

        public static Statistics aggregate(Statistics left, Statistics right) {
            Statistics statistics = new Statistics();

            addCounter(statistics, left);
            addCounter(statistics, right);

            addSum(statistics, left);
            addSum(statistics, right);

            addSumBetweenStatistics(statistics, left, right);

            addFirstTime(statistics, left);
            addFirstTime(statistics, right);

            addLastTime(statistics, left);
            addLastTime(statistics, right);

            return statistics;
        }

        private static void addSum(Statistics to, Statistics from) {
            for (var typeAndCounter : from.typeToSumOfTimeDiff.entrySet()) {
                var newValue = to.typeToSumOfTimeDiff.getOrDefault(typeAndCounter.getKey(), 0L) + typeAndCounter.getValue();
                to.typeToSumOfTimeDiff.put(typeAndCounter.getKey(), newValue);
            }
        }

        private static void addSumBetweenStatistics(Statistics to, Statistics left, Statistics right) {
            for (var typeAndLastTime : left.typeToLastTime.entrySet()) {
                var rightFirstTime = right.typeToFirstTime.get(typeAndLastTime.getKey());
                if (rightFirstTime != null) {
                    var add = rightFirstTime - typeAndLastTime.getValue();
                    var newValue = to.typeToSumOfTimeDiff.getOrDefault(typeAndLastTime.getKey(), 0L) + add;
                    to.typeToSumOfTimeDiff.put(typeAndLastTime.getKey(), newValue);
                }
            }
        }

        private static void addCounter(Statistics to, Statistics from) {
            for (var typeAndCounter : from.typeToCounter.entrySet()) {
                var newValue = to.typeToCounter.getOrDefault(typeAndCounter.getKey(), 0L) + typeAndCounter.getValue();
                to.typeToCounter.put(typeAndCounter.getKey(), newValue);
            }
        }

        private static void addFirstTime(Statistics to, Statistics from) {
            for (var typeAndFirstTime : from.typeToFirstTime.entrySet()) {
                to.typeToFirstTime.putIfAbsent(typeAndFirstTime.getKey(), typeAndFirstTime.getValue());
            }
        }

        private static void addLastTime(Statistics to, Statistics from) {
            to.typeToLastTime.putAll(from.typeToLastTime);
        }

    }
}
