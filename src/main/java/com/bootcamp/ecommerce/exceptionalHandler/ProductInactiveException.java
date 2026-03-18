package com.bootcamp.ecommerce.exceptionalHandler;

public class ProductInactiveException extends RuntimeException{
    public ProductInactiveException(String msg) {
        super(msg);
    }

}
