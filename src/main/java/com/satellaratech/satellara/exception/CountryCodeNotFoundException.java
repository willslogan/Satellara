package com.satellaratech.satellara.exception;

public class CountryCodeNotFoundException extends RuntimeException {
    private ErrorType errorType;

    public CountryCodeNotFoundException(ErrorType type, String message) {
        super(message);
        this.errorType = type;
    }

    public ErrorType getErrorType() {
      return errorType;
    }
}
