package com.horstmann.java8;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Collection2<T> extends Collection<T> {

    /**
     * Applies action to each element for which filter returns true.
     *
     * @param action
     *            - action to be applied
     * @param filter
     *            - filter to be matched
     */
    public default void forEachIf(Consumer<T> action, Predicate<T> filter) {
	this.stream().filter(filter).forEach(action);
    }
}
