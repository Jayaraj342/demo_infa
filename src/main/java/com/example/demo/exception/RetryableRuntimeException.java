package com.example.demo.exception;

public class RetryableRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RetryableRuntimeException(String msg) {
        super(msg);
    }
}