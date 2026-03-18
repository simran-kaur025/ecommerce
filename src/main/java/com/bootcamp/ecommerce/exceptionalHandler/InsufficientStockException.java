package com.bootcamp.ecommerce.exceptionalHandler;

public class InsufficientStockException extends RuntimeException{
    public InsufficientStockException(String message) {
        super(message);
    }
}
