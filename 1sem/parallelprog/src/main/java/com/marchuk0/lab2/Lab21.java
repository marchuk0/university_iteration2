package com.marchuk0.lab2;

import java.util.Random;

public class Lab21 {
    public static class Philosopher implements Runnable {
        private final Random random = new Random();
        private final Object left;
        private final Object right;

        Philosopher(Object left, Object right) {
            this.left = left;
            this.right = right;
        }

        private void doAction(String message) throws InterruptedException {
            System.out.println(Thread.currentThread() + " time:" + System.nanoTime() + " | " +  message);
            Thread.sleep(((int) (random.nextDouble() * 50)));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    doAction("Thinking...");
                    synchronized (left) {
                        doAction("Lock left!");
                        synchronized (right) {
                            doAction("Lock right. Success!");
                            doAction("Unlock right!");
                        }
                        doAction("Unlock left!");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void deadlockMain() {
        Philosopher[] philosophers = new Philosopher[5];
        Object[] locks = new Object[philosophers.length];

        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }

        for (int i = 0; i < philosophers.length; i++) {
            var left = locks[i];
            var right = locks[(i + 1) % locks.length];

            if (i == philosophers.length - 1) {
                philosophers[i] = new Philosopher(left, right);
            } else {
                philosophers[i] = new Philosopher(left, right);
            }

            new Thread(philosophers[i], "" + i).start();
        }
    }
    
    private static void noDeadlockMain() {
        Philosopher[] philosophers = new Philosopher[5];
        Object[] locks = new Object[philosophers.length];

        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Object();
        }

        for (int i = 0; i < philosophers.length; i++) {
            var left = locks[i];
            var right = locks[(i + 1) % locks.length];

            if (i == philosophers.length - 1) {
                philosophers[i] = new Philosopher(right, left);
            } else {
                philosophers[i] = new Philosopher(left, right);
            }

            new Thread(philosophers[i], "" + i).start();
        }
    }

    public static void main(String[] args) {
        deadlockMain();
//        noDeadlockMain();
    }
}
