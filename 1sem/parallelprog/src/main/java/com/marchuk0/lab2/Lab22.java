package com.marchuk0.lab2;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

//TODO:
public class Lab22 {
    private static class Library {
        Map<String, Integer> books = new HashMap<>();
        Map<Student, String> borrowedBooks = new HashMap<>();
        Map<String, Integer> borrowedBookCounter = new HashMap<>();
        Map<String, Integer> returnedBookCounter = new HashMap<>();

        ReentrantLock lock = new ReentrantLock();

        public Library() {
            books.put("Book1", 2);
            books.put("Book2", 1);
            books.put("Book3", 1);
            books.put("Book4", 1);
            books.put("Book5", 2);
            books.put("Book6", 2);
        }

        public boolean borrowBook(Student student, String bookTitle) {
            lock.lock();
            try {
                int bookCounter = books.getOrDefault(bookTitle, 0); 
                if (bookCounter > 0) {
                    books.put(bookTitle, bookCounter - 1);
                    borrowedBooks.put(student, bookTitle);
                    incrementCounter(borrowedBookCounter, bookTitle);
                    return true;
                } else {
                    return false;
                }
            } finally {
                lock.unlock();
            }
        }

        public boolean returnBook(Student student, String bookTitle) {
            lock.lock();
            try {
                if (!bookTitle.equals(borrowedBooks.get(student))) {
                    return false;
                }

                int bookCounter = books.getOrDefault(bookTitle, 0);
                books.put(bookTitle, bookCounter + 1);
                borrowedBooks.remove(student);
                incrementCounter(returnedBookCounter, bookTitle);

                return true;
            } finally {
                lock.unlock();
            }
        }

        public List<String> getAvailableBooks() {
            lock.lock();
            try {
                return books.entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .map(Map.Entry::getKey)
                        .toList();
            } finally {
                lock.unlock();
            }
        }

        private void incrementCounter(Map<String, Integer> map, String bookTitle) {
            int counter = map.getOrDefault(bookTitle, 0) + 1;
            map.put(bookTitle, counter);
        }
        
        public void printStatistics() {
            System.out.println("Borrowed counter: ");
            for (var bookToCounter : borrowedBookCounter.entrySet()) {
                System.out.println(bookToCounter.getKey() + " " + bookToCounter.getValue());
            }

            System.out.println("Returned counter: ");
            for (var bookToCounter : returnedBookCounter.entrySet()) {
                System.out.println(bookToCounter.getKey() + " " + bookToCounter.getValue());
            }
        }

    }

    private static class Student implements Runnable {

        private final Library library;
        private final String studentName;
        private final Random random = new Random();

        public Student(Library library, String studentName) {
            this.library = library;
            this.studentName = studentName;
        }

        @Override
        public void run() {
            int cnt = random.nextInt(5);
            while (cnt-- > 0) {
                try {
                    getReadAndReturn();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void log(String msg) {
            System.out.println("Student " + studentName + " | " + msg);
        }

        private void getReadAndReturn() throws InterruptedException {
            List<String> books = library.getAvailableBooks();
            if (!books.isEmpty()) {
                int index = (int) (random.nextDouble() * books.size());
                String book = books.get(index);
                boolean borrowed = library.borrowBook(this, book);

                if (borrowed) {
                    readAndReturn(book);
                } else {
                    log("Cannot borrow book " + book);
                }
            } else {
                log("Cannot borrow book, list is empty");
            }
        }

        private void readAndReturn(String bookTitle) throws InterruptedException {
            log("Borrowed book, started reading: " + bookTitle);
            Thread.sleep((int) (random.nextDouble() * 3000));
            log("Finised reading, returing: " + bookTitle);

            boolean returned = library.returnBook(this, bookTitle);
            log("Returned:" + returned);
        }
    }

    private static class LibrarySimulator {
        private final Library library = new Library();

        List<Thread> threads = new ArrayList<>();
        public void run() throws InterruptedException {
            for (int i = 0; i < 5; i++) {
                Student student = new Student(library, "Student" + i);
                Thread thread = new Thread(student);
                threads.add(thread);
                thread.start();
            }
            
            for (var thread : threads) {
                thread.join();
            }

            library.printStatistics();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LibrarySimulator simulator = new LibrarySimulator();
        simulator.run();
    }
    
}
