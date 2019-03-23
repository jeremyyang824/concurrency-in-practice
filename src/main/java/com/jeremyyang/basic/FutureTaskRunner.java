package com.jeremyyang.basic;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class FutureTaskRunner {

    private final FutureTask<ProductInfo> future =
            new FutureTask<>(new Callable<ProductInfo>() {
                public ProductInfo call() throws DataLoadException {
                    return loadProductInfo();
                }
            });

    public void start() {
        new Thread(future).start();
    }

    public ProductInfo get()
            throws DataLoadException, InterruptedException {
        try {
            return future.get();
        } catch (ExecutionException e) {
            // ExecutionException thrown when attempting to retrieve the result of a task
            // that aborted by throwing an exception. This exception can be
            // inspected using the getCause() method.
            Throwable cause = e.getCause();
            if (cause instanceof DataLoadException)
                throw (DataLoadException) cause;
            else
                throw new RuntimeException(cause);
        }
    }

    private ProductInfo loadProductInfo() throws DataLoadException {
        // return () -> "TestProduct";
        throw new DataLoadException(String.format("Executing error! Current thread is:[%s]", Thread.currentThread().getId()));
    }

    interface ProductInfo {
        String getName();
    }

    private static class DataLoadException extends Exception {
        public DataLoadException(String message) {
            super(message);
        }
    }


    public static void main(String[] args) {
        System.out.println(String.format("Start at thread:[%s]", Thread.currentThread().getId()));
        FutureTaskRunner task = new FutureTaskRunner();
        task.start();

        try {
            ProductInfo productInfo = task.get();   // get Future result
            System.out.println(String.format("Success. Result is: %s", productInfo.getName()));
        } catch (DataLoadException e) {
            System.out.println(String.format("Failure. %s", e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
