package com.jeremyyang;

import java.util.concurrent.*;

/**
 * Task that deadlocks in a single-threaded Executor
 */
public class ThreadDeadLock {

    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    //private final ExecutorService exec = Executors.newFixedThreadPool(5);

    public class LoadFileTask implements Callable<String> {
        private final String fileName;

        public LoadFileTask(String fileName) {
            this.fileName = fileName;
        }

        public String call() throws Exception {
            return this.fileName + " loaded.";
        }
    }

    public class RenderPageTask implements Callable<String> {
        public String call() throws Exception {
            System.out.println("submit load header...");
            Future<String> header = exec.submit(new LoadFileTask("header.html"));

            System.out.println("submit load footer...");
            Future<String> footer = exec.submit(new LoadFileTask("footer.html"));

            System.out.println("render body...");
            String page = renderBody();

            System.out.println("begin to compose page content...");
            // Will deadlock -- task waiting for result of subtasks
            return header.get() + page + footer.get();
        }

        private String renderBody() {
            // Here's where we would actually render the page
            return " body content ";
        }
    }

    public static void main(String[] args) {
        ThreadDeadLock threadDeadLockRunner = new ThreadDeadLock();

        try {
            Future<String> content = threadDeadLockRunner.exec.submit(threadDeadLockRunner.new RenderPageTask());
            System.out.println(content.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {

            try {
                System.out.println("begin to shutdown ExecutorService...");
                threadDeadLockRunner.exec.shutdown();
                threadDeadLockRunner.exec.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
