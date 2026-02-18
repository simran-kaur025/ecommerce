package com.bootcamp.ecommerce.exceptionalHandler;


public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
