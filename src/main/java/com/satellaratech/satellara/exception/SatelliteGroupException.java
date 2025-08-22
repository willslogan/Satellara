package com.satellaratech.satellara.exception;

public class SatelliteGroupException extends RuntimeException {
    private final ErrorType errorType;
    public SatelliteGroupException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
      return errorType;
    }
}
