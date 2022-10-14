package com.gv.csc.aspects;

import com.gv.csc.exceptions.CLOSETException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Capture CLO-SET Related exceptions using AOP
 */
@ControllerAdvice
public class CLOSETAspects {

    /**
     * This method captures any CLO-SET related exceptions thrown in the service layer
     *
     * @param e exception
     * @return response
     */
    @ExceptionHandler(CLOSETException.class)
    public ResponseEntity<?> handleCLOSETException(CLOSETException e) {

        return null;
    }
}
