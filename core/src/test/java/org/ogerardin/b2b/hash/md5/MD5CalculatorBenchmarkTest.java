package org.ogerardin.b2b.hash.md5;

import lombok.Data;
import org.junit.Test;
import org.ogerardin.b2b.hash.ByteArrayHashCalculator;
import org.ogerardin.b2b.hash.DigestingInputStream;
import org.ogerardin.b2b.hash.DigestingInputStreamProvider;
import org.ogerardin.b2b.hash.InputStreamHashCalculator;
import org.ogerardin.b2b.hash.md5.apache.ApacheCommonsMD5Calculator;
import org.ogerardin.b2b.hash.md5.fast.FastMD5Calculator;
import org.ogerardin.b2b.hash.md5.guava.GuavaMD5Calculator;
import org.ogerardin.b2b.hash.md5.java.JavaMD5Calculator;
import org.ogerardin.b2b.hash.md5.spring.SpringMD5Calculator;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MD5CalculatorBenchmarkTest {

    private static final List<?> MD5_INSTANCES = Arrays.asList(
            new JavaMD5Calculator(),
            new GuavaMD5Calculator(),
            new ApacheCommonsMD5Calculator(),
            new FastMD5Calculator(),
            new SpringMD5Calculator()
    );

    @Data
    private static final class BenchSize {
        private final int size;
        private final String desc;
    }

    private static final List<BenchSize> SIZES = Arrays.asList(
//            new BenchSize(1000, "1K bytes"),
//            new BenchSize(10 * 1000, "10K bytes"),
//            new BenchSize(100 * 1000, "100K bytes"),
            new BenchSize(1000 * 1000, "1M bytes"),
            new BenchSize(10 * 1000 * 1000, "10M bytes"),
            new BenchSize(100 * 1000 * 1000, "100M bytes"),
            new BenchSize(1000 * 1000 * 1000, "1G bytes")
    );

    @Test
    public void benchmarkByteArray() {
        SIZES.forEach(s -> benchmark(s.size, s.desc));
    }

    @Test
    public void benchmarkInputStream() {
        // adapter for InputStreamHashCalculator
        Function<InputStreamHashCalculator, ByteArrayHashCalculator> asMD5Calculator = isc -> bytes -> {
            try {
                return isc.hash(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        SIZES.forEach(s -> benchmark(s.size, s.desc, asMD5Calculator));
    }

    @Test
    public void benchmarkDigestInputStream() {
        // adapter for DigestingInputStreamProvider
        Function<DigestingInputStreamProvider, ByteArrayHashCalculator> asMD5Calculator = uisp -> bytes -> {
            try {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                DigestingInputStream hpis = uisp.digestingInputStream(byteArrayInputStream);
                //noinspection StatementWithEmptyBody
                while (hpis.read() > 0) ;
                hpis.close();
                return hpis.hash();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        SIZES.forEach(s -> benchmark(s.size, s.desc, asMD5Calculator));
    }


    @Data
    private static class Result {
        final String md5;
        final long time;
    }

    private static void benchmark(int size, String description) {
        benchmark(size, description, Function.identity());
    }

    /**
     * all MD5 implementations are tested using the {@link ByteArrayHashCalculator} interface with a random byte array as source.
     */
    private static <C> void benchmark(int size, String description, Function<C, ByteArrayHashCalculator> toMd5Calculator) {
        System.out.println("Benchmarking: " + description);

        // generate a random byte array
        byte[] bytes = new byte[size];
        new Random().nextBytes(bytes);

        Map<Class<?>, Result> results = new HashMap<>();
        StopWatch stopWatch = new StopWatch(description);

        // call and time each implementation
        for (Object o : MD5_INSTANCES) {
            Class<?> md5Class = o.getClass();

            try {
                ByteArrayHashCalculator md5Calculator = toMd5Calculator.apply((C) o);
                stopWatch.start(md5Class.getSimpleName());
                String md5 = md5Calculator.hexHash(bytes);
                stopWatch.stop();

                Result result = new Result(md5, stopWatch.getLastTaskTimeMillis());
                results.put(md5Class, result);
            } catch (ClassCastException e) {
                System.out.println(e.toString());
            }
        }

        System.out.println(stopWatch.prettyPrint());

        // assert all implementations return the same value
        long distinctResultsCount = results.values().stream()
                .map(Result::getMd5)
                .distinct()
                .count();
        if (distinctResultsCount != 1) {
            for (Map.Entry<Class<?>, Result> entry : results.entrySet()) {
                System.out.println(entry.getKey().getSimpleName() + " -> " + entry.getValue().getMd5());
            }
        }
        assertThat(distinctResultsCount, is(1L));

        // get the fastest one
        Class<?> first = results.entrySet().stream()
                .sorted(Comparator.comparingLong(entry -> entry.getValue().getTime()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(RuntimeException::new);
        System.out.println("Winner is: " + first);
        System.out.println();


    }


}