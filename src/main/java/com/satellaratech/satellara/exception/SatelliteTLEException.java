package com.satellaratech.satellara.exception;

public class SatelliteTLEException extends RuntimeException {
    private ErrorType errorType;

    public SatelliteTLEException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
