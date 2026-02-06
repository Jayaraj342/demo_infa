package com.example.demo.exception;

import lombok.Data;

@Data
public class ErrorData {
    private String code;
    private String message;
    private String debugMessage;
    private String requestId;
    private ErrorDetail[] details;
}
