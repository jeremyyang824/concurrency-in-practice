package com.jeremyyang.custom;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Version 03: using condition queue
 */
public class ConditionQueueBoundedBuffer<V> extends BaseBoundedBuffer<V> {

    public ConditionQueueBoundedBuffer(int capacity) {
        super(capacity);
    }

    public synchronized void put(V v) throws InterruptedException {
        while (checkFull())
            wait();

        doPut(v);

        notifyAll();    // // we use notifyAll instead of notify
    }

    public synchronized V take() throws InterruptedException {
        while (checkEmpty())
            wait();

        V v = doTake();

        notifyAll();    // we use notifyAll instead of notify
        return v;
    }

    public synchronized boolean isFull() {
        return checkFull();
    }

    public synchronized boolean isEmpty() {
        return checkEmpty();
    }


    public static void main(String[] args) {
        Random rnd = new Random(47);
        ConditionQueueBoundedBuffer<String> buffer = new ConditionQueueBoundedBuffer<>(10);

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
