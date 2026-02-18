package com.bootcamp.ecommerce.exceptionalHandler;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
