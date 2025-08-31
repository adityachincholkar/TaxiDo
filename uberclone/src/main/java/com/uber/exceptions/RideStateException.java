package com.uber.exceptions;

public class RideStateException extends RuntimeException {
    public RideStateException(String message) {
        super(message);
    }
}
