package com.horstmann.java8;

public class DoubleRunnable {
    public static Runnable andThen(Runnable first, Runnable second) {
	return () -> {
	    first.run();
	    second.run();
	};
    }
}
