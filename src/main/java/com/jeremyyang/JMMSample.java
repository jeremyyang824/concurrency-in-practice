package com.jeremyyang;

public class JMMSample {
    int x = 0;
    volatile boolean v = false;

    public void writer() {
        x = 42;
        v = true;
    }

    public void reader() {
        if (v == true) {
            System.out.println(x);
        }
    }

    public static void main(String[] args) {
        int i = 0;
        while (i++ < 1000) {
            JMMSample jmmSample = new JMMSample();
            Thread threadWrite = new Thread(jmmSample::writer);
            Thread threadReader = new Thread(jmmSample::reader);

            threadWrite.start();
            threadReader.start();

            try {
                threadWrite.join();
                threadReader.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
