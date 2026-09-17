package com.habiterra.application.exception;

// Application personnalized exception
public class ApplicationException extends RuntimeException {
    private final int status;
    private final String code;

    public ApplicationException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int getStatus() { return status; }
    public String getCode() { return code; }
}
