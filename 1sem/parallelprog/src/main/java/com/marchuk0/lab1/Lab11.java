package com.marchuk0.lab1;

public class Lab11 {
    public static void main(String[] args) {
        Runnable runnable1 = () -> {
            while (true) {
                System.out.println("I am runnable1");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Runnable runnable2 = () -> {
            while (true) {
                System.out.println("I am runnable2");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Thread thread1 = new Thread(runnable1);
        Thread thread2 = new Thread(runnable2);
        thread1.start();
        thread2.start();
    }
}
