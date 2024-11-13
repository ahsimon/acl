package com.lasvegas.library.functions;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A {@link java.util.function.Supplier}-like interface which allows throwing Error.
 */
@FunctionalInterface
public interface CheckedSupplier<T> {
    T get() throws Throwable;

    default <V> CheckedSupplier<V> andThen(CheckedFunction<? super T, ? extends V> after) {
        Objects.requireNonNull(after, "after is null");
        return () -> after.apply(get());
    }
    default Supplier<T> unchecked() {
        return () -> {
            try {
                return get();
            } catch(Throwable t) {
                return sneakyThrow(t);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}