package com.jeremyyang.cancel;

/**
 * Cancelable task
 */
public class CancelableTask {

    public void doSomething() {

        Thread thread = Thread.currentThread();

        while (true) {
            if (thread.isInterrupted()) {
                System.out.println("return from Runnable status");
                break;
            }

            //TODO: other works

            try {
                Thread.sleep(1000); // any blocking method
            } catch (InterruptedException e) {
                System.out.println("restore interrupt flag");
                thread.interrupt(); // !!!
            }
        }
    }

    public static void main(String[] args) {
        Thread thread = new Thread(() -> new CancelableTask().doSomething());
        thread.start();

        System.out.println("try to interrupt thread:" + thread.getId());
        thread.interrupt();

        try {
            thread.join();
            System.out.println("thread:" + thread.getId() + " was interrupted.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
