package org.ogerardin.b2b.api;

import org.apache.tomcat.util.http.fileupload.FileUploadBase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ResponseEntityExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    ResponseEntity<?> handleMultipartException(HttpServletRequest request, Throwable ex) throws Throwable {
        Throwable cause = ex.getCause();
        if (cause instanceof IllegalStateException) {
            Throwable cause2 = cause.getCause();
            if (cause2 instanceof FileUploadBase.SizeLimitExceededException) {
                ResponseEntity<String> responseEntity = new ResponseEntity<>(cause2.toString(), HttpStatus.PAYLOAD_TOO_LARGE);
                return responseEntity;
            }
        }

        throw ex;
    }
}