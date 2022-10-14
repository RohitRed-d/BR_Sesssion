package com.gv.csc.exceptions;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.springframework.http.HttpStatus;

/**
 * CLOSETException - CLO-SET Custom Exception for handling CLOSET service exceptions
 */

@Setter @Getter
public class CLOSETException extends Exception {
    private HttpStatus statusCode;

    /**
     *
     * @param message
     * @param statusCode
     */
    public CLOSETException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
