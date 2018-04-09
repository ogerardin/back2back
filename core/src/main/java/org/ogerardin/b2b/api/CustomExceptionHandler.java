package org.ogerardin.b2b.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    ResponseEntity<?> handleMultipartException(HttpServletRequest request, Throwable ex) throws Throwable {
        Throwable cause = ex.getCause();
        if (cause instanceof IllegalStateException) {
            Throwable cause2 = cause.getCause();
            //Tomcat-specific exception
            if (cause2.getClass().getName().equals("org.apache.tomcat.util.http.fileupload.FileUploadBase.SizeLimitExceededException")) {
                throw new FileTooLargeException(cause2.toString());
            }
        }
        throw ex;
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<?> handleIOException(HttpServletRequest request, IOException e) {
        return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}