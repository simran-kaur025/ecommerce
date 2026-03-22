package com.bootcamp.ecommerce.exceptionalHandler;

public class AccessDeniedException extends RuntimeException{
    public AccessDeniedException(String message) {
        super(message);
    }

}
