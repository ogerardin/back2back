package org.ogerardin.b2b.util;

public class Container<T> {

    private final T object;

    public Container(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }
}
