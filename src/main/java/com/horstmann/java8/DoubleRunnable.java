package com.horstmann.java8;

public class DoubleRunnable {

    /**
     * Takes as parameters two Runnable instances and returns a Runnable that runs the first, then the second.
     *
     * @param first
     *            - Runnable that runs first
     * @param second
     *            - Runnable that runs second
     * @return Runnable that runs the first, then the second
     */
    public static Runnable andThen(Runnable first, Runnable second) {
	return () -> {
	    first.run();
	    second.run();
	};
    }
}
