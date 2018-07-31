package org.ogerardin.b2b.files.md5;

import lombok.Data;
import org.junit.Test;
import org.ogerardin.b2b.files.md5.apache.ApacheCommonsMD5Calculator;
import org.ogerardin.b2b.files.md5.fast.FastMD5Calculator;
import org.ogerardin.b2b.files.md5.guava.GuavaMD5Calculator;
import org.ogerardin.b2b.files.md5.java.JavaMD5Calculator;
import org.ogerardin.b2b.files.md5.spring.SpringMD5Calculator;
import org.springframework.util.StopWatch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ByteArrayMD5CalculatorTest {

    private static final List<?> MD5_INSTANCES = Arrays.asList(
            new JavaMD5Calculator(),
            new GuavaMD5Calculator(),
            new ApacheCommonsMD5Calculator(),
            new FastMD5Calculator(),
            new SpringMD5Calculator()
    );

    @Test
    public void benchmarkByteArray() {
        benchmark(1000, "1K bytes");
        benchmark(10 * 1000, "10K bytes");
        benchmark(100 * 1000, "100K bytes");
        benchmark(1000 * 1000, "1M bytes");
        benchmark(10 * 1000 * 1000, "10M bytes");
        benchmark(100 * 1000 * 1000, "100M bytes");
        benchmark(1000 * 1000 * 1000, "1G bytes");
    }

    @Test
    public void benchmarkInputStream() {
        // adapter for InputStreamMD5Calculator
        Function<InputStreamMD5Calculator, ByteArrayMD5Calculator> asMD5Calculator = isc -> bytes -> {
            try {
                return isc.md5Hash(new ByteArrayInputStream(bytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        benchmark(1000, "1K bytes", asMD5Calculator);
        benchmark(10 * 1000, "10K bytes", asMD5Calculator);
        benchmark(100 * 1000, "100K bytes", asMD5Calculator);
        benchmark(1000 * 1000, "1M bytes", asMD5Calculator);
        benchmark(10 * 1000 * 1000, "10M bytes", asMD5Calculator);
        benchmark(100 * 1000 * 1000, "100M bytes", asMD5Calculator);
        benchmark(1000 * 1000 * 1000, "1G bytes", asMD5Calculator);
    }

    @Test
    public void benchmarkDigestInputStream() {
        // adapter for MD5UpdatingInputStreamProvider
        Function<MD5UpdatingInputStreamProvider, ByteArrayMD5Calculator> asMD5Calculator = uisp -> bytes -> {
            try {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                HashProviderInputStream hpis = uisp.md5UpdatingInputStream(byteArrayInputStream);
                //noinspection StatementWithEmptyBody
                while (hpis.read() > 0) ;
                hpis.close();
                return hpis.hash();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        benchmark(1000, "1K bytes", asMD5Calculator);
        benchmark(10 * 1000, "10K bytes", asMD5Calculator);
        benchmark(100 * 1000, "100K bytes", asMD5Calculator);
        benchmark(1000 * 1000, "1M bytes", asMD5Calculator);
        benchmark(10 * 1000 * 1000, "10M bytes", asMD5Calculator);
        benchmark(100 * 1000 * 1000, "100M bytes", asMD5Calculator);
        benchmark(1000 * 1000 * 1000, "1G bytes", asMD5Calculator);
    }


    @Data
    private static class Result {
        final String md5;
        final long time;
    }

    private static <C> void benchmark(int size, String description) {
        benchmark(size, description, Function.identity());

    }

    /**
     * all MD5 implementations are tested using the {@link ByteArrayMD5Calculator} interface with a random byte array as source.
     * @param size
     * @param description
     * @param toMd5Calculator
     * @param <C>
     */
    private static <C> void benchmark(int size, String description, Function<C, ByteArrayMD5Calculator> toMd5Calculator) {
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
                ByteArrayMD5Calculator md5Calculator = toMd5Calculator.apply((C) o);
                stopWatch.start(md5Class.getSimpleName());
                String md5 = md5Calculator.hexMd5Hash(bytes);
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