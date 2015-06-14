package com.horstmann.java8;

public interface RunnableEx {
    public void run() throws Exception;

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
