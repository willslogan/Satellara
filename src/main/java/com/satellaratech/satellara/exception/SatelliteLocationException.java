package com.satellaratech.satellara.exception;

public class SatelliteLocationException extends RuntimeException {
    private ErrorType errorType;

    public SatelliteLocationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
