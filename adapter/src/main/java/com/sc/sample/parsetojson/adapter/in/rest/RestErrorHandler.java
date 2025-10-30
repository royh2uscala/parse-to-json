package com.sc.sample.parsetojson.adapter.in.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class RestErrorHandler {

    @ExceptionHandler(RestControllerException.class)
    public ResponseEntity<ErrorReport> handleRestControllerException(RestControllerException ex) {
        return ex.getErrorReportResponseEntity();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .badRequest()
                .body("Uploaded file too large - exceed Max Size of:%d".formatted(ex.getMaxUploadSize()));
    }

    public static RestControllerException restControllerException(HttpStatus httpStatus, String message) {
        return new RestControllerException(buildResponseErrorReport(httpStatus, message));
    }

    private static ResponseEntity<ErrorReport> buildResponseErrorReport(HttpStatus httpStatus, String message) {
        ErrorReport errorReport = new ErrorReport(httpStatus.value(), message);
        return ResponseEntity.status(httpStatus.value()).body(errorReport);
    }
}
