package com.horstmann.java8;

import static java.lang.System.*;
import static java.util.stream.Collectors.*;
import static org.hamcrest.Matchers.*;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class StreamsTest {

    private static final URL BOOK_URL = StreamsTest.class.getResource("war-and-peace.txt");

    private static String[] WORDS;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	Path path = Paths.get(BOOK_URL.toURI());

	// Read file into string
	String contents = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

	// Split into words; non-letters are delimiters
	WORDS = contents.split("[\\P{L}]");
    }

    /**
     * Write a parallel version of the for loop in Section 2.1, “From Iteration to Stream Operations,” on page 22. Obtain the number of processors. Make that
     * many separate threads, each working on a segment of the list, and total up the results as they come in.
     */
    @Test
    public void test1() {
	// Obtain the number of processors
	int cores = Runtime.getRuntime().availableProcessors();
	out.println("There are " + cores + " processors available");

	// Count the number of words concurrently
	LongAdder count = new LongAdder();
	ExecutorService executorService = Executors.newFixedThreadPool(cores);
	for (int i = 0; i < cores; i++) {
	    int from = i * WORDS.length / cores;
	    int to = i + 1 < cores ? (i + 1) * WORDS.length / cores : WORDS.length;
	    executorService.submit(() -> {
		for (int j = from; j < to; j++) {
		    if (WORDS[j].length() > 12) {
			count.increment();
		    }
		}
	    });
	}
	executorService.shutdown();
	while (!executorService.isTerminated()) {
	    // Wait for threads to complete their job
	}

	// Verify the results
	Assert.assertThat(count.intValue(), is(1946));
    }

    /**
     * Verify that asking for the first five long words does not call the filter method once the fifth long word has been found. Simply log each method call.
     */
    @Test
    public void test2() {
	LongAdder count = new LongAdder();
	List<String> longWords = Stream.of(WORDS).filter(w -> {
	    if (w.length() > 12) {
		count.increment();
		return true;
	    }
	    return false;
	}).limit(5).collect(toList());
	out.println(longWords);
	Assert.assertThat(count.intValue(), is(5));
    }

    /**
     * Measure the difference when counting long words with a parallelStream instead of a stream. Call currentTimeMillis before and after the call, and print
     * the difference. Switch to a larger document (such as War and Peace) if you have a fast computer.
     */
    @Test
    public void test3() {
	long snapshot1 = currentTimeMillis();
	Stream.of(WORDS).filter(w -> w.length() > 12).count();
	long snapshot2 = currentTimeMillis();
	Stream.of(WORDS).parallel().unordered().filter(w -> w.length() > 12).count();
	long snapshot3 = currentTimeMillis();
	long sequentialTime = snapshot2 - snapshot1;
	long parallelTime = snapshot3 - snapshot2;
	out.println("Sequential time: " + sequentialTime);
	out.println("Parallel time: " + parallelTime);
	Assert.assertThat(parallelTime, lessThan(sequentialTime));
    }

    /**
     * Suppose you have an array int[] values = { 1, 4, 9, 16 }. What is Stream.of( values)? How do you get a stream of int instead?
     */
    @Test
    public void test4() {
	int[] values = { 1, 4, 9, 16 };
	Stream.of(values).forEach(x -> Assert.assertThat(x, instanceOf(int[].class)));
	IntStream.of(values).forEach(x -> Assert.assertThat(x, instanceOf(int.class)));
    }

    /**
     * Using Stream.iterate, make an infinite stream of random numbers— not by calling Math.random but by directly implementing a linear congruential generator.
     * In such a generator, you start with x_0 = seed and then produce x_n+1 = (a*x_n+c) % m, for appropriate values of a, c, and m. You should implement a
     * method with parameters a, c, m, and seed that yields a Stream<Long>. Try out a = 25214903917, c = 11, and m = 2^48.
     */
    @Test
    public void test5() {
	List<Long> fistSequence = Streams.randomLongs(currentTimeMillis(), 25214903917L, 11L, 2L ^ 48).limit(100).collect(toList());
	out.println("First sequence: " + fistSequence);
	List<Long> secondSequence = Streams.randomLongs(currentTimeMillis(), 25214903917L, 11L, 2L ^ 48).limit(100).collect(toList());
	out.println("Second sequence: " + secondSequence);
	Assert.assertThat(fistSequence, not(secondSequence));
    }

    /**
     * The characterStream method in Section 2.3, “The filter, map, and flatMap Methods,” on page 25, was a bit clumsy, first filling an array list and then
     * turning it into a stream. Write a stream-based one-liner instead. One approach is to make a stream of integers from 0 to s.length() - 1 and map that with
     * the s:: charAt method reference.
     */
    @Test
    public void test6() {
	Assert.assertThat(Streams.characterStream("Character Stream").count(), is(16L));
    }

    /**
     * Your manager asks you to write a method public static <T> boolean isFinite(Stream <T> stream). Why isn’t that such a good idea? Go ahead and write it
     * anyway.
     */
    @Test
    public void test7() {
	Assert.assertThat(Streams.isFinite(Stream.generate(() -> 1)), is(false));
	Assert.assertThat(Streams.isFinite(Stream.of(1, 2, 3, 4, 5)), is(true));
    }

    /**
     * Write a method public static <T> Stream <T> zip( Stream <T> first, Stream <T> second) that alternates elements from the streams first and second,
     * stopping when one of them runs out of elements.
     */
    @Test
    public void test8() {
	Stream<Integer> odd = IntStream.iterate(1, x -> x + 2).limit(10).boxed();
	Stream<Integer> even = IntStream.iterate(2, x -> x + 2).limit(13).boxed();
	List<Integer> actual = Streams.zip(odd, even).collect(toList());
	List<Integer> expected = IntStream.rangeClosed(1, 20).boxed().collect(toList());
	Assert.assertThat(actual, is(expected));
    }

    /**
     * Join all elements in a Stream<ArrayList<T>> to one ArrayList<T>. Show how to do this with the three forms of reduce.
     */
    @Test
    public void test9() {
	ArrayList<Integer> first = IntStream.range(1, 11).boxed().collect(toCollection(ArrayList::new));
	ArrayList<Integer> second = IntStream.range(11, 21).boxed().collect(toCollection(ArrayList::new));
	ArrayList<Integer> third = IntStream.rangeClosed(21, 30).boxed().collect(toCollection(ArrayList::new));

	ArrayList<Integer> merged1 = Stream.of(first, second, third).reduce(new ArrayList<>(), (result, element) -> {
	    result.addAll(element);
	    return result;
	});

	ArrayList<Integer> merged2 = Stream.of(first, second, third).reduce((a, b) -> {
	    ArrayList<Integer> result = new ArrayList<>(a);
	    result.addAll(b);
	    return result;
	}).get();

	ArrayList<Integer> merged3 = Stream.of(first, second, third).reduce(new ArrayList<>(), (result, element) -> {
	    result.addAll(element);
	    return result;
	}, (result1, result2) -> {
	    result1.addAll(result2);
	    return result1;
	});

	ArrayList<Integer> expected = IntStream.rangeClosed(1, 30).boxed().collect(toCollection(ArrayList::new));

	Assert.assertThat(merged1, is(expected));
	Assert.assertThat(merged2, is(expected));
	Assert.assertThat(merged3, is(expected));
    }

    /**
     * Write a call to reduce that can be used to compute the average of a Stream<Double>. Why can’t you simply compute the sum and divide by count()?
     */
    @Test
    public void test10() {
	List<Double> sequence = Stream.generate(Math::random).limit(20).collect(toList());
	Double expected = sequence.stream().collect(Collectors.averagingDouble(x -> x));

	AtomicLong count = new AtomicLong();
	Double average = sequence.stream().reduce(0D, (result, element) -> result += element / count.incrementAndGet() - result / count.get());

	Assert.assertThat(average, closeTo(expected, 0.0001D));
    }


    /**
     * It should be possible to concurrently collect stream results in a single ArrayList, instead of merging multiple array lists, provided it has been
     * constructed with the stream’s size, since concurrent set operations at disjoint positions are thread safe. How can you achieve that?
     */
    @Test
    public void test11() {
	int size = 1000;

	// Assuming that we know the size of the stream, prepare the ArrayList to hold elements
	List<Integer> list = new ArrayList<Integer>(size);
	for (int i = 0; i < size; i++) {
	    list.add(null);
	}

	// Collect elements to an ArrayList concurrently
	AtomicInteger index = new AtomicInteger();
	IntStream.rangeClosed(1, size).parallel().boxed().collect(() -> null, (x, y) -> list.set(index.getAndIncrement(), y), (a, b) -> {});

	// Verify that list contains all the elements
	Assert.assertThat(list, hasSize(size));
	Assert.assertThat(list, Matchers.containsInAnyOrder(IntStream.rangeClosed(1, size).boxed().toArray(Integer[]::new)));
    }

    /**
     * Count all short words in a parallel Stream<String>, as described in Section 2.13, “Parallel Streams” on page 40, by updating an array of AtomicInteger.
     * Use the atomic getAndIncrement method to safely increment each counter.
     */
    @Test
    public void test12() {
	AtomicInteger[] shortWords = Stream.generate(AtomicInteger::new).limit(12).toArray(AtomicInteger[]::new);
	Stream.of(WORDS).parallel().forEach(s -> {
	    if (s.length() < 12) {
		shortWords[s.length()].getAndIncrement();
	    }
	});
	Assert.assertThat(shortWords[3].get(), is(143619));
    }

    /**
     * Repeat the preceding exercise, but filter out the short strings and use the collect method with Collectors.groupingBy and Collectors.counting.
     */
    @Test
    public void test13() {
	ConcurrentMap<Integer, Long> lengths = Stream.of(WORDS).parallel().collect(Collectors.groupingByConcurrent(String::length, Collectors.counting()));
	Assert.assertThat(lengths.get(3), is(143619L));
    }

}
