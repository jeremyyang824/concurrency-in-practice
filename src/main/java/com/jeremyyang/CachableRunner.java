package com.jeremyyang;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

public class CachableRunner {

    public interface Computable<A, V> {
        V compute(A arg) throws InterruptedException;
    }

    /**
     * version 1 (sequence computing)
     *
     * @param <A> input value
     * @param <V> output value
     */
    public class MemoizerV1<A, V> implements Computable<A, V> {

        private final Map<A, V> cache = new HashMap<>();
        private final Computable<A, V> computable;

        public MemoizerV1(Computable<A, V> computable) {
            this.computable = computable;
        }

        @Override
        public synchronized V compute(A arg) throws InterruptedException {
            V result = cache.get(arg);
            if (result == null) {
                result = computable.compute(arg);
                cache.put(arg, result);
            }
            return result;
        }
    }

    /**
     * version 2 (race condition)
     *
     * @param <A> input value
     * @param <V> output value
     */
    public class MemoizerV2<A, V> implements Computable<A, V> {

        private final Map<A, V> cache = new ConcurrentHashMap<>();  // use concurrent container
        private final Computable<A, V> computable;

        public MemoizerV2(Computable<A, V> computable) {
            this.computable = computable;
        }

        @Override
        public V compute(A arg) throws InterruptedException {
            V result = cache.get(arg);
            if (result == null) {   // check-then-act cause duplicate computing
                result = computable.compute(arg);
                cache.put(arg, result);
            }
            return result;
        }
    }

    /**
     * version 3 (race condition)
     *
     * @param <A> input value
     * @param <V> output value
     */
    public class MemoizerV3<A, V> implements Computable<A, V> {

        private final Map<A, Future<V>> cache = new ConcurrentHashMap<>();  // put Future in the cache
        private final Computable<A, V> computable;

        public MemoizerV3(Computable<A, V> computable) {
            this.computable = computable;
        }

        @Override
        public V compute(A arg) throws InterruptedException {
            Future<V> f = cache.get(arg);
            if (f == null) {    // check-then-act cause duplicate computing
                Callable<V> eval = () -> computable.compute(arg);
                FutureTask<V> ft = new FutureTask<>(eval);
                f = ft;
                cache.put(arg, ft);
                ft.run();
            }

            try {
                return f.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                throw new RuntimeException(cause);
            }
        }
    }

    /**
     * final version
     *
     * @param <A> input value
     * @param <V> output value
     */
    public class Memoizer<A, V> implements Computable<A, V> {

        private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();
        private final Computable<A, V> computable;

        public Memoizer(Computable<A, V> computable) {
            this.computable = computable;
        }

        @Override
        public V compute(final A arg) throws InterruptedException {
            while (true) {
                Future<V> f = cache.get(arg);

                // compute
                if (f == null) {
                    Callable<V> eval = () -> computable.compute(arg);
                    FutureTask<V> ft = new FutureTask<>(eval);
                    f = cache.putIfAbsent(arg, ft); // avoid duplicate computing
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


    private final Computable<BigInteger, BigInteger[]> computable = this::factor;
    private final Computable<BigInteger, BigInteger[]> cachedComputable = new Memoizer<>(computable);

    public static void main(String[] args) {
        Random rand = new Random(47);
        CachableRunner runner = new CachableRunner();

        for (int i = 0; i < 100; i++) {
            int input = rand.nextInt(5);

            long startTime = System.currentTimeMillis();
            BigInteger[] results = runner.cachableFactor(BigInteger.valueOf(input));
            long endTime = System.currentTimeMillis();

            System.out.println(String.format("Input: %s output: %s, compute spend: %s ms",
                    input, Arrays.toString(results), endTime - startTime));
        }
    }

    public BigInteger[] cachableFactor(BigInteger i) {
        try {
            return cachedComputable.compute(i);
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
