package com.jeremyyang.custom;

import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

/**
 * Version 04: using explicit condition queue,
 * so that we can create 2 condition queues and use signal instead of signalAll
 */
public class ExplicitConditionBoundedBuffer<V> extends BaseBoundedBuffer<V> {

    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    public ExplicitConditionBoundedBuffer(int capacity) {
        super(capacity);
    }

    public void put(V v) throws InterruptedException {
        lock.lock();
        try {
            while (checkFull())
                notFull.await();

            doPut(v);

            notEmpty.signal();  // here we can use signal instead of signalAll
        } finally {
            lock.unlock();
        }
    }

    public V take() throws InterruptedException {
        lock.lock();
        try {
            while (checkEmpty())
                notEmpty.await();

            V v = doTake();

            notFull.signal();
            return v;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return checkFull();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return checkEmpty();
        } finally {
            lock.unlock();
        }
    }


    public static void main(String[] args) {
        Random rnd = new Random(47);
        ExplicitConditionBoundedBuffer<String> buffer = new ExplicitConditionBoundedBuffer<>(10);

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
