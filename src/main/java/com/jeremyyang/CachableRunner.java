package com.jeremyyang;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

public class CachableRunner {

    public interface Computable<A, V> {
        V compute(A arg) throws InterruptedException;
    }

    public class Memoizer<A, V> implements Computable<A, V> {

        private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();
        private final Computable<A, V> computable;

        public Memoizer(Computable<A, V> computable) {
            this.computable = computable;
        }

        public V compute(final A arg) throws InterruptedException {
            while (true) {
                Future<V> f = cache.get(arg);

                // compute
                if (f == null) {
                    Callable<V> eval = () -> computable.compute(arg);
                    FutureTask<V> ft = new FutureTask<V>(eval);
                    f = cache.putIfAbsent(arg, ft);
                    if (f == null) {
                        f = ft;
                        ft.run();
                    }
                }

                try {
                    return f.get();
                } catch (CancellationException e) {
                    cache.remove(arg, f);   // re-try
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    throw new RuntimeException(cause);
                }
            }
        }
    }


    private final Computable<BigInteger, BigInteger[]> c = this::factor;
    private final Computable<BigInteger, BigInteger[]> cache = new Memoizer<>(c);

    public static void main(String[] args) {
        Random rand = new Random(47);
        CachableRunner runner = new CachableRunner();

        for (int i = 0; i < 100; i++) {
            int input = rand.nextInt(5);
            BigInteger[] results = runner.cachableFactor(BigInteger.valueOf(input));
            System.out.println(String.format("Input: %s output: %s", input, Arrays.toString(results)));
        }
    }

    public BigInteger[] cachableFactor(BigInteger i) {
        try {
            return cache.compute(i);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new BigInteger[0];
        }
    }

    private BigInteger[] factor(BigInteger i) {
        // Doesn't really factor
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new BigInteger[]{i};
    }
}
