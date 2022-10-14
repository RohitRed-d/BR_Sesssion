package com.gv.csc.aspects;

import com.gv.csc.exceptions.PLMException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Capture PLM Related exceptions using AOP
 */
@ControllerAdvice
public class PLMAspects {

    /**
     * This method captures any PLM related exceptions thrown in the service layer
     *
     * @param e exception
     * @return response
     */
    @ExceptionHandler(PLMException.class)
    public ResponseEntity<?> handlePLMException(PLMException e, HttpStatus status) {
        return ResponseEntity.status(status).body(e.getMessage());
    }

}
