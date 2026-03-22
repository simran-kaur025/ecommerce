package com.bootcamp.ecommerce.exceptionalHandler;

public class BadCredentialException extends RuntimeException{
    public BadCredentialException(String message) {
        super(message);
    }

}
