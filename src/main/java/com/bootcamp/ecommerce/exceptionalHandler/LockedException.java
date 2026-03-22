package com.bootcamp.ecommerce.exceptionalHandler;

public class LockedException extends RuntimeException{
    public LockedException(String msg) {
        super(msg);
    }

}
