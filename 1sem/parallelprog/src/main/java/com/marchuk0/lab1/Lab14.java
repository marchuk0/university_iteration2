package com.marchuk0.lab1;

import java.util.Random;

import static java.lang.Math.sqrt;

public class Lab14 {
    private static long msTime() {
        return System.nanoTime() / 1000_000;
    }

    public static class SleepRunnable implements Runnable {
        private final int sleepMs;

        public SleepRunnable(int sleepMs) {
            this.sleepMs = sleepMs;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread() + " before sleep " + msTime());
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread() + " after sleep " + msTime());
        }
    }

    public static class YieldRunnable implements Runnable {
        Random random = new Random();
        int operations = 200;
        @Override
        public void run() {
            while (operations-- > 0) {
                if (random.nextInt() % 5 == 0) {
                    System.out.println(Thread.currentThread() + " before yield " + msTime());
                    Thread.yield();
                    System.out.println(Thread.currentThread() + " after yield " + msTime());
                } else {
                    compute();
                }
            }
        }

        private double compute() {
            double result = 0;
            for (int i = 0; i < 100000; i++) {
                result += sqrt(random.nextInt());
            }

            return result;
        }
    }
    
    public static void main(String[] args) {
//        for (int i = 1; i < 4; i++) {
//            new Thread(new SleepRunnable(i * 1000)).start();
//        }

        for (int i = 0; i < 10; i++) {
            new Thread(new YieldRunnable()).start();
        }
    }
}
