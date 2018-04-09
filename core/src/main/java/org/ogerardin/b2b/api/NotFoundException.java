package org.ogerardin.b2b.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

/**
 * Custom exception designed to map to a HTTP 404
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NotFoundException extends Exception {

    public NotFoundException(String s) {
        super(s);
    }

    public NotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NotFoundException(Throwable throwable) {
        super(throwable);
    }

    public NotFoundException(Class clazz, String id) {
        super (MessageFormat.format("{0}@{1}", clazz.getName(), id));
    }
}
