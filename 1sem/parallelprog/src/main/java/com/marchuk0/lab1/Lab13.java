package com.marchuk0.lab1;

public class Lab13 {
    private static class SharedCounter {
        private volatile int value = 0;

        private void increment() {
            value++;
        }

        private int read() {
            return value;
        }
    }

    public static void main(String[] args) {
        SharedCounter counter = new SharedCounter();

        Thread reader = new Thread(() -> {
            while (true) {
                System.out.println("Read result = " + counter.read());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread writer = new Thread(() -> {
            while (true) {
                System.out.println("Incrementing...");
                counter.increment();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        
        reader.start();
        writer.start();
    }
}
