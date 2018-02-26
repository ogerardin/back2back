package org.ogerardin.b2b.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of {@link InvocationHandler} that tries to delegate each invocation to a set of candidate instances
 * sequentially until one of them succeeds (meaning: does not throw an exception).
 *
 * @param <T> the common type of candidate instances
 */
@Slf4j
public class MetaInvocationHandler<T> implements InvocationHandler {

    private final List<T> candidates;

    public MetaInvocationHandler(List<T> candidates) {
        this.candidates = candidates;
    }

    public <R> R invoke(Method method, Object... args) throws NoSuchMethodException {
        //TODO invocations of toString and such should not be delegated
        log.debug("Handling : " + method.getName() + " " + Arrays.toString(args));
        // get an array of argument classes
        for (T candidate: candidates) {
            log.debug("  Trying: " + candidate);
            Method candidateMethod;
            try {
                // lookup a method that matches called method profile
                candidateMethod = candidate.getClass().getMethod(method.getName(), method.getParameterTypes());
                // invoke it with actual parameters
                @SuppressWarnings("unchecked")
                R result = (R) candidateMethod.invoke(candidate, args);
                log.debug("    Success!");
                return result;
            } catch (InvocationTargetException e) {
                log.debug("    " + e.getCause().toString());
            } catch (Exception e) {
                log.debug("    " + e.toString());
            }
        }
        // all candidate classes examined -> failure
        throw new NoSuchMethodException("No candidate could handle " + method.getName());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoke(method, args);
    }
}

