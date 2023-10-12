package com.marchuk0.lab1;

import java.util.function.Consumer;

public class Lab12 {
    private static interface ParametrizedRunnable extends Consumer<String> {
        
    }

    public static void main(String[] args) {
        ParametrizedRunnable parametrizedRunnable = s -> {
            System.out.println(s);
        };

        parametrizedRunnable.accept("test1");
        parametrizedRunnable.accept("test2");
        parametrizedRunnable.accept("test3");
        
        Thread thread = new Thread(() -> parametrizedRunnable.accept("Current thread " + Thread.currentThread()));
        thread.start();
    }
}
