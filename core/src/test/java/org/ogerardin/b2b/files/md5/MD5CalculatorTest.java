package org.ogerardin.b2b.files.md5;

import lombok.Data;
import org.springframework.util.StopWatch;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MD5CalculatorTest {

    private static final List<Class<? extends MD5Calculator>> MD5_CLASSES = Arrays.asList(
            JavaMD5Calculator.class,
            GuavaMD5Calculator.class,
            ApacheCommonsMD5Calculator.class,
            FastMD5Calculator.class,
            SpringMD5Calculator.class
    );

    public static void main(String args[]) throws IllegalAccessException, InstantiationException {
        benchmark(1000, "1K bytes");
        benchmark(10 * 1000, "10K bytes");
        benchmark(100 * 1000, "100K bytes");
        benchmark(1000 * 1000, "1M bytes");
        benchmark(10 * 1000 * 1000, "10M bytes");
        benchmark(100 * 1000 * 1000, "100M bytes");
        benchmark(1000 * 1000 * 1000, "1G bytes");
    }

    @Data
    private static class Result {
        final String md5;
        final long time;
    }

    private static void benchmark(int size, String description) throws IllegalAccessException, InstantiationException {
        System.out.println("Benchmarking: " + description);

        // generate a random byte array
        byte[] bytes = new byte[size];
        new Random().nextBytes(bytes);


        Map<Class<? extends MD5Calculator>, Result> results = new HashMap<>();
        StopWatch stopWatch = new StopWatch(description);

        // call and time each implementation
        for (Class<? extends MD5Calculator> md5Class : MD5_CLASSES) {
            MD5Calculator md5Calculator = md5Class.newInstance();

            stopWatch.start(md5Calculator.getClass().getSimpleName());
            String md5 = md5Calculator.hexMd5Hash(bytes);
            stopWatch.stop();

            Result result = new Result(md5, stopWatch.getLastTaskTimeMillis());
            results.put(md5Class, result);
        }

        System.out.println(stopWatch.prettyPrint());

/*
        for (Map.Entry<Class<? extends MD5Calculator>, Result> entry : results.entrySet()) {
            System.out.println(entry.getKey().getSimpleName() + " -> " + entry.getValue().getMd5());
        }
*/

        // assert all implementations return the same value
        long distinctResultsCount = results.values().stream()
                .map(Result::getMd5)
                .distinct()
                .count();
        assertThat(distinctResultsCount, is(1L));

        // get the fastest one
        Class<? extends MD5Calculator> first = results.entrySet().stream()
                .sorted(Comparator.comparingLong(entry -> entry.getValue().getTime()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(RuntimeException::new);
        System.out.println("Winner is: " + first);
        System.out.println();


    }

}