package org.ogerardin.b2b.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

/**
 * Custom exception designed to map to a HTTP 413
 */
@ResponseStatus(code = HttpStatus.PAYLOAD_TOO_LARGE)
public class FileTooLargeException extends Exception {

    public FileTooLargeException(String s) {
        super(s);
    }

    public FileTooLargeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FileTooLargeException(Throwable throwable) {
        super(throwable);
    }

    public FileTooLargeException(Class clazz, String id) {
        super (MessageFormat.format("{0}@{1}", clazz.getName(), id));
    }
}
