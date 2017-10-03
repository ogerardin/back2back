package org.ogerardin.b2b.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class MetaInvocationHandler<T> implements InvocationHandler {

    private static final Log logger = LogFactory.getLog(MetaInvocationHandler.class);

    private final List<T> candidates;

    public MetaInvocationHandler(List<T> candidates) {
        this.candidates = candidates;
    }

    public <R> R invoke(Method method, Object... args) throws NoSuchMethodException {
        logger.debug("Trying to find candidate to handle : " + method.getName());
        // get an array of argument classes
        for (T candidate: candidates) {
            logger.debug("  Trying: " + candidate);
            try {
                // lookup a method that matches called method profile
                Method candidateMethod = candidate.getClass().getMethod(method.getName(), method.getParameterTypes());
                // invoke it with actual parameters
                @SuppressWarnings("unchecked")
                R result = (R) candidateMethod.invoke(candidate, args);
                logger.debug("  Successfully invoked method on: " + candidate);
                return result;
            } catch (NoSuchMethodException e) {
                logger.debug("    No such method");
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.debug("    Exception while invoking " + method, e);
            }
        }
        // all candidate classes examined -> failure
        throw new NoSuchMethodException("No suitable candidate can handle " + method.getName());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoke(method, args);
    }
}

