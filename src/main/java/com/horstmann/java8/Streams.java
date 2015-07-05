package com.horstmann.java8;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Streams {
    public static Stream<Long> randomLongs(long seed, long a, long c, long m) {
	return Stream.iterate(seed, x_n -> (a * x_n + c) % m);
    }

    public static Stream<Character> characterStream(String s) {
	return IntStream.range(0, s.length()).mapToObj(s::charAt);
    }

    public static <T> boolean isFinite(Stream<T> stream) {
	AtomicLong count = new AtomicLong(Runtime.getRuntime().totalMemory() / 8);
	try {
	    stream.parallel().unordered().forEach(x -> {
		if (count.decrementAndGet() <= 0) {
		    throw new IllegalStateException("Infinite stream");
		}
	    });
	} catch (IllegalStateException e) {
	    return false;
	}
	return true;
    }

    public static <T> Stream<T> zip(Stream<T> first, Stream<T> second) {
	Queue<T> elementsA = first.collect(Collectors.toCollection(LinkedList::new));
	Queue<T> elementsB = second.limit(elementsA.size()).collect(Collectors.toCollection(LinkedList::new));
	return Stream.generate(new Supplier<T>() {
	    boolean first = true;

	    @Override
	    public T get() {
		Queue<T> queue = first ? elementsA : elementsB;
		first = !first;
		return queue.poll();
	    }
	}).limit(Math.min(elementsA.size(), elementsB.size()) * 2);
    }
}
