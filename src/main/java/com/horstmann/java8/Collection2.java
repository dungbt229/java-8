package com.horstmann.java8;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Collection2<T> extends Collection<T> {

    public default void forEachIf(Consumer<T> action, Predicate<T> filter) {
	this.stream().filter(filter).forEach(action);
    }
}
