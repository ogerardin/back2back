package org.ogerardin.b2b.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * A generic factory that works by trying to instantiate in sequence a list of classes until
 * a suitable one if found.
 *
 * @param <T> the superclass of all candidate classes
 */
@Slf4j
public class StrategyFactory<T> {

    private final List<Class<? extends T>> candidateClasses;

    /**
     * @param candidateClasses list of candidate classes, in order of priority
     */
    public StrategyFactory(List<Class<? extends T>> candidateClasses) {
        this.candidateClasses = candidateClasses;
    }

    /**
     * Tries to obtain a new instance of T by examining each candidate class, obtaining a constructor that matches the
     * specified args and invoking it, until one succeeds or all candidate classes have failed.
     *
     * @param args arguments to pass to the class constructor
     * @return the instance of the first of the candidate classes for which a constructor exists and is successfully
     * invoked
     * @throws InstantiationException if all candidate classes have been examined unsuccessfully
     */
    public T newInstance(Object... args) throws InstantiationException {
        log.debug("Trying to instantiate for args: " + Arrays.toString(args));
        // get an array of argument classes
        Class<?> argClasses[] = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);

        log.debug("Arg classes: " + Arrays.toString(args));
        for (Class<? extends T> candidateClass : candidateClasses) {
            log.debug("  Trying class: " + candidateClass);
            try {
                // lookup a constructor that matches argument types
                Constructor<? extends T> candidateClassConstructor = candidateClass.getConstructor(argClasses);
                // invoke it with actual parameters
                T instance = candidateClassConstructor.newInstance(args);
                log.debug("  Successfully instantiated: " + instance);
                return instance;
            } catch (NoSuchMethodException e) {
                log.debug("    No suitable constructor");
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                log.debug("    Exception while invoking constructor", e);
            }
        }
        // all candidate classes examined -> failure
        throw new InstantiationException("No class could be instantiated for args: "+ Arrays.toString(args));    }

}
