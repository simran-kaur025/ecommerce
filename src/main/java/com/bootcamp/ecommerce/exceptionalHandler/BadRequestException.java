package com.bootcamp.ecommerce.exceptionalHandler;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String message) {
        super(message);
    }
}
