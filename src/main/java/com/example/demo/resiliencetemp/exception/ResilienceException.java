package com.example.demo.resiliencetemp.exception;

import java.io.Serial;

public class ResilienceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6706860303479061691L;

    public ResilienceException() {
        super();
    }

    public ResilienceException(String message) {
        super(message);
    }

    public ResilienceException(Throwable cause) {
        super(cause);
    }

    public ResilienceException(String message, Throwable cause) {
        super(message, cause);
    }
}
