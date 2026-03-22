package com.bootcamp.ecommerce.exceptionalHandler;

public class DisabledException extends RuntimeException{
    public DisabledException(String message) {
        super(message);
    }
}
