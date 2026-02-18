package com.bootcamp.ecommerce.exceptionalHandler;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
