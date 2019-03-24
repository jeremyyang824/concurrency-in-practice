package com.jeremyyang.cancel;

/**
 * None-cancelable task that restores interruption before exit
 */
public class NoneCancelableTask {

    public void doSomething() {

        boolean interrupted = false;

        try {
            while (true) {
                try {
                    Thread.sleep(1000); // any blocking method (can be interrupted while in Waiting status)
                    return;

                } catch (InterruptedException e) {
                    interrupted = true; // record interrupt request
                    // fall through and retry in while
                }
            }
        } finally {
            // restore interrupt before return
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        Thread thread = new Thread(() -> new NoneCancelableTask().doSomething());
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
