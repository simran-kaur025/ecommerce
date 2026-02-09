package com.bootcamp.ecommerce.exceptionalHandler;

public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
