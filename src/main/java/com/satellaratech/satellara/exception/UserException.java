package com.satellaratech.satellara.exception;

public class UserException extends RuntimeException {
    private final ErrorType errorType;
    public UserException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
      return errorType;
    }
}
