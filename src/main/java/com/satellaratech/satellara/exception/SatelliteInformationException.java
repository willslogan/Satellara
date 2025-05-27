package com.satellaratech.satellara.exception;

public class SatelliteInformationException extends RuntimeException {
    private ErrorType errorType;

    public SatelliteInformationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
