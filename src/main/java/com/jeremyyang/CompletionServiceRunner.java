package com.jeremyyang;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class CompletionServiceRunner {

    public static class Renderer {

        private final ExecutorService executor = Executors.newFixedThreadPool(5);

        public void renderPage() {
            final List<ImageInfo> info = scanForImageInfo();

            // submit tasks
            CompletionService<ImageData> completionService = new ExecutorCompletionService<>(executor);
            for (final ImageInfo imageInfo : info)
                completionService.submit(imageInfo::downloadImage);

            try {
                // retrieve other work result
                renderText();

                // retrieve task results
                for (int t = 0, n = info.size(); t < n; t++) {
                    Future<ImageData> f = completionService.take();
                    ImageData imageData = f.get();
                    renderImage(imageData);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                throw new RuntimeException(cause);
            } finally {

                try {
                    executor.shutdown();
                    executor.awaitTermination(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void renderText() throws InterruptedException {
            Thread.sleep(1000);
            String body = "this is main body";
            System.out.println(body);
        }

        private List<ImageInfo> scanForImageInfo() {
            return Arrays.asList(
                    new ImageInfo("a.png"),
                    new ImageInfo("b.png"),
                    new ImageInfo("c.png"));
        }

        private void renderImage(ImageData img) {
            System.out.println(img.getData());
        }

    }

    public static void main(String[] args) {
        Renderer render = new Renderer();
        render.renderPage();
    }


    static final class ImageData {
        private final String content;

        public String getData() {
            return content;
        }

        public ImageData(String content) {
            this.content = content;
        }
    }

    static class ImageInfo {
        private final String source;

        public ImageInfo(String source) {
            this.source = source;
        }

        public ImageData downloadImage() {
            try {
                Thread.sleep(1500);
                return new ImageData("data from " + this.source);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }
}
