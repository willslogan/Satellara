package com.satellaratech.satellara.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SatelliteInformationException.class)
    public ResponseEntity<ErrorResponse> handleSatelliteInfoException(SatelliteInformationException ex) {

        //Generate error response record
        ErrorResponse e = new ErrorResponse(ex.getErrorType(), ex.getMessage());

        //The Satellite already exists in the database
        if(ex.getErrorType() == ErrorType.ALREADY_EXISTS)
            return new ResponseEntity<ErrorResponse>(e, HttpStatus.CONFLICT);

        // The satellite was not found in the database
        else
            return new ResponseEntity<ErrorResponse>(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SatelliteLocationException.class)
    public ResponseEntity<ErrorResponse> handleSatelliteLocationException(SatelliteLocationException ex) {
        //Generate error response record
        ErrorResponse e = new ErrorResponse(ex.getErrorType(), ex.getMessage());

        //Script Error occured
        return new ResponseEntity<ErrorResponse>(e, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(SatelliteTLEException.class)
    public ResponseEntity<ErrorResponse> handleException(SatelliteTLEException ex) {
        //Generate error response record
        ErrorResponse e = new ErrorResponse(ex.getErrorType(), ex.getMessage());

        //IO Exception happened when generating data
        if(ex.getErrorType() == ErrorType.IO_EXCEPTION)
            return new ResponseEntity<ErrorResponse>(e, HttpStatus.INTERNAL_SERVER_ERROR);

        // Generator file couldn't be found
        if(ex.getErrorType() == ErrorType.FILE_NOT_FOUND)
            return new ResponseEntity<ErrorResponse>(e, HttpStatus.NOT_FOUND);

        // The satellite tle already exists in the database
        return new ResponseEntity<ErrorResponse>(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CountryCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCountryCodeNotFoundException(CountryCodeNotFoundException ex) {
        ErrorResponse e = new ErrorResponse(ex.getErrorType(), ex.getMessage());
        return new ResponseEntity<ErrorResponse>(e, HttpStatus.NOT_FOUND);
    }
    // Record for Error Responses
    public record ErrorResponse(ErrorType type, String message) {
    }
}
