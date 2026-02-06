package com.example.demo.exception;

import java.io.Serial;

public class RetryableRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private ErrorInfo errorInfo;

    public RetryableRuntimeException(String msg) {
        super(msg);
    }

    public RetryableRuntimeException(ErrorInfo errorInfo, Throwable rootCause) {
        super(getMessage(errorInfo), rootCause);
        this.errorInfo = errorInfo;
    }

    public RetryableRuntimeException(ErrorInfo errorInfo) {
        this(errorInfo, null);
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    private static String getMessage(ErrorInfo errorInfo) {
        if (errorInfo.getError() != null) {
            return errorInfo.getError().getRequestId() + ", " + errorInfo.getError().getCode() + ":"
                    + errorInfo.getError().getMessage();
        }
        return "No error details";
    }
}