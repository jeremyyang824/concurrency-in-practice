package com.jeremyyang.custom;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Version 02: crude blocking
 */
public class BlockingBoundedBuffer<V> extends BaseBoundedBuffer<V> {

    private final int SLEEP_GRANULARITY = 100;

    public BlockingBoundedBuffer(int capacity) {
        super(capacity);
    }

    public void put(V v) throws InterruptedException {
        while (true) {
            synchronized (this) {
                if (!checkFull()) {
                    doPut(v);
                    return;
                }
            }
            Thread.sleep(SLEEP_GRANULARITY);
        }
    }

    public V take() throws InterruptedException {
        while (true) {
            synchronized (this) {
                if (!checkEmpty())
                    return doTake();
            }
            Thread.sleep(SLEEP_GRANULARITY);
        }
    }

    public synchronized boolean isFull() {
        return checkFull();
    }

    public synchronized boolean isEmpty() {
        return checkEmpty();
    }

    public static void main(String[] args) {
        Random rnd = new Random(47);
        BlockingBoundedBuffer<String> buffer = new BlockingBoundedBuffer<>(10);

        Thread threadTake = new Thread(() -> {
            IntStream.rangeClosed(1, 100).forEach(i -> {
                try {
                    String item = buffer.take();
                    System.out.println(String.format("  Take item:[%s]", item));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }, "thread-take");

        Thread threadPut = new Thread(() -> {
            IntStream.rangeClosed(1, 100).forEach(i -> {
                try {
                    if (rnd.nextInt(10) == 10)
                        Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                try {
                    String item = String.valueOf(i);
                    buffer.put(item);
                    System.out.println(String.format("Put item:[%s]", item));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }, "thread-put");

        threadTake.start();
        threadPut.start();

        try {
            threadTake.join();
            threadPut.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
