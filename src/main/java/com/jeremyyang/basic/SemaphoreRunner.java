package com.jeremyyang.basic;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class SemaphoreRunner {

    public static class BoundedHashSet<T> {

        private final Set<T> set;
        private final Semaphore sem;

        public BoundedHashSet(int bound) {
            this.set = Collections.synchronizedSet(new HashSet<>());
            this.sem = new Semaphore(bound);
        }

        public boolean add(T o) throws InterruptedException {
            sem.acquire();  // operation P
            boolean wasAdded = false;
            try {
                wasAdded = set.add(o);
                System.out.println(String.format(
                        "Add [%s] %s. Current size:%s", o, wasAdded ? "Success" : "Failure", set.size()));
                return wasAdded;
            } finally {
                if (!wasAdded)
                    sem.release();  // operation V
            }
        }

        public boolean remove(T o) {
            boolean wasRemoved = set.remove(o);
            if (wasRemoved)
                sem.release();  // operation V
            System.out.println(String.format(
                    "Remove [%s] %s. Current size:%s", o, wasRemoved ? "Success" : "Failure", set.size()));
            return wasRemoved;
        }
    }


    public static void main(String[] args) {
        BoundedHashSet<String> boundedHashSet = new BoundedHashSet<>(3);
        String[] items = {"A1", "A1", "A2", "A3", "A4", "A2", "A5", "A6", "A3", "A7", "A8"};

        // add items
        new Thread(() -> {
            for (String item : items) {
                try {
                    boundedHashSet.add(item);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        // remove item
        new Thread(() -> {
            for (String item : items) {
                try {
                    Thread.sleep(1000);
                    boundedHashSet.remove(item);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
