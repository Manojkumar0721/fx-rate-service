package com.fxservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception that will return an HTTP 404 Not Found error
 * when a required currency rate cannot be found in the database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Sets the HTTP status code
public class RateNotFoundException extends RuntimeException {

    public RateNotFoundException(String message) {
        super(message);
    }
}