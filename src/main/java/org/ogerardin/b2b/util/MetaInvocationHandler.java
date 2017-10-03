package org.ogerardin.b2b.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * An implementation of {@link InvocationHandler} that tries to delegate each invocation to a set of candidate instances
 * sequentially until one of them succeeds (meaning: does not throw an exception).
 *
 * @param <T> the common type of candidate instances
 */
public class MetaInvocationHandler<T> implements InvocationHandler {

    private static final Log logger = LogFactory.getLog(MetaInvocationHandler.class);

    private final List<T> candidates;

    public MetaInvocationHandler(List<T> candidates) {
        this.candidates = candidates;
    }

    public <R> R invoke(Method method, Object... args) throws NoSuchMethodException {
        //TODO invocations of toString and such should not be delegated
        logger.debug("Handling : " + method.getName() + " " + Arrays.toString(args));
        // get an array of argument classes
        for (T candidate: candidates) {
            logger.debug("  Trying: " + candidate);
            Method candidateMethod;
            try {
                // lookup a method that matches called method profile
                candidateMethod = candidate.getClass().getMethod(method.getName(), method.getParameterTypes());
                // invoke it with actual parameters
                @SuppressWarnings("unchecked")
                R result = (R) candidateMethod.invoke(candidate, args);
                logger.debug("    Success!");
                return result;
            } catch (Exception e) {
                logger.debug("    " + e.toString());
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

