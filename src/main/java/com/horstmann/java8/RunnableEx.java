package com.horstmann.java8;

public interface RunnableEx {
    public void run() throws Exception;

    /**
     * Catches all checked exceptions that may occur in {@link Runnable#run()} method and turns them into unchecked exceptions.
     *
     * @param r
     *            - Runnable to convert exceptions for
     * @return Runnable that does not throw any checked exceptions.
     */
    public static Runnable uncheck(RunnableEx r) {
	return () -> {
	    try {
		r.run();
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	};
    }
}
