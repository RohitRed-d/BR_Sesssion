package com.gv.csc.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * PLMException - PLM Custom Exception for handling PLM service exceptions
 */
@Data
public class PLMException extends Exception {
    private HttpStatus statusCode;

    /**
     *
     * @param message
     * @param statusCode
     */
    public PLMException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
