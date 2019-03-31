package com.jeremyyang.custom;

import java.util.Random;
import java.util.stream.IntStream;

/**
 * Version 01: throw exception if failed, client re-try
 */
public class SynchronizedBoundedBuffer<V> extends BaseBoundedBuffer<V> {

    public SynchronizedBoundedBuffer(int capacity) {
        super(capacity);
    }

    public synchronized void put(V v) throws BufferFullException {
        if (checkFull())
            throw new BufferFullException();
        this.doPut(v);
    }

    public synchronized V take() throws BufferEmptyException {
        if (checkEmpty())
            throw new BufferEmptyException();
        return this.doTake();
    }

    public synchronized boolean isFull() {
        return checkFull();
    }

    public synchronized boolean isEmpty() {
        return checkEmpty();
    }

    public static class BufferFullException extends RuntimeException {
    }

    public static class BufferEmptyException extends RuntimeException {
    }


    public static void main(String[] args) {
        int RETRY_TIME = 100;
        Random rnd = new Random(47);
        SynchronizedBoundedBuffer<String> buffer = new SynchronizedBoundedBuffer<>(10);

        Thread threadTake = new Thread(() -> {
            IntStream.rangeClosed(1, 100).forEach(i -> {
                while (true) {
                    try {
                        String item = buffer.take();
                        System.out.println(String.format("  Take item:[%s]", item));
                        break;  // take success
                    } catch (BufferEmptyException e) {
                        try {
                            Thread.sleep(RETRY_TIME);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }   // end while
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

                while (true) {
                    try {
                        String item = String.valueOf(i);
                        buffer.put(item);
                        System.out.println(String.format("Put item:[%s]", item));
                        break;  // put success
                    } catch (BufferFullException e) {
                        try {
                            Thread.sleep(RETRY_TIME);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }   // end while
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
