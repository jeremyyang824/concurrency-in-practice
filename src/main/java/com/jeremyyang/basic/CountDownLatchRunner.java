package com.jeremyyang.basic;

import java.util.concurrent.CountDownLatch;

public class CountDownLatchRunner {

    public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {

        final CountDownLatch startGate = new CountDownLatch(1);
        final CountDownLatch endGate = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        System.out.println(String.format("  Thread:[%s] wait to begin...", Thread.currentThread().getId()));
                        startGate.await();  // wait to start...

                        try {
                            task.run();
                        } finally {
                            System.out.println(String.format("  Thread:[%s] done.", Thread.currentThread().getId()));
                            endGate.countDown();    // one thread done
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            };
            t.start();
        }

        Thread.sleep(1000);

        long start = System.nanoTime();
        startGate.countDown();  // begin at the same time
        System.out.println(String.format("Start at time: %s", start));

        endGate.await();    // wait to end...
        long end = System.nanoTime();
        System.out.println(String.format("End at time: %s", end));

        return end - start;
    }


    public static void main(String[] args) {
        try {
            new CountDownLatchRunner()
                    .timeTasks(5, () -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
