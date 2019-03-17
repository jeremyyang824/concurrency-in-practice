package com.jeremyyang;

import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumer {

    /**
     * This is a Producer
     */
    static class FileCrawler implements Runnable {

        private final BlockingQueue<File> fileQueue;
        private final FileFilter fileFilter;
        private final File root;

        FileCrawler(
                BlockingQueue<File> fileQueue,
                final FileFilter fileFilter,
                File root) {
            this.fileQueue = fileQueue;
            this.root = root;
            this.fileFilter = f -> f.isDirectory() || fileFilter.accept(f);
        }

        public void run() {
            try {
                crawl(root);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void crawl(File root) throws InterruptedException {
            File[] entries = root.listFiles(fileFilter);
            if (entries != null) {
                for (File entry : entries)
                    if (entry.isDirectory())
                        crawl(entry);
                    else if (!alreadyIndexed(entry)) {
                        fileQueue.put(entry);
                        System.out.println(String.format("Put file into queue: [%s]", entry.getAbsolutePath()));
                    }

            }
        }

        private boolean alreadyIndexed(File f) {
            return false;
        }
    }

    /**
     * This is a Consumer
     */
    static class Indexer implements Runnable {

        private final BlockingQueue<File> queue;

        Indexer(BlockingQueue<File> queue) {
            this.queue = queue;
        }

        public void run() {
            try {
                while (true) {
                    File file = queue.take();
                    System.out.println(String.format("Take file from queue: [%s]", file.getAbsolutePath()));
                    indexFile(file);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void indexFile(File file) throws InterruptedException {
            Thread.sleep(1000);
            // TODO: Index the file...
        }
    }

    private static final int BOUND = 10;
    private static final int N_CONSUMERS = Runtime.getRuntime().availableProcessors();
    private static final File[] roots = {new File("/")};

    public static void main(String[] args) {
        BlockingQueue<File> queue = new LinkedBlockingQueue<>(BOUND);
        FileFilter filter = file -> true;

        for (File root : roots)
            new Thread(new FileCrawler(queue, filter, root)).start();

        for (int i = 0; i < N_CONSUMERS; i++)
            new Thread(new Indexer(queue)).start();
    }
}
