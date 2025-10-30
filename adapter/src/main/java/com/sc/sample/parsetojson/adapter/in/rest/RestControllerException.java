package com.sc.sample.parsetojson.adapter.in.rest;

import org.springframework.http.ResponseEntity;

public class RestControllerException extends RuntimeException{
    private final ResponseEntity<ErrorReport> errorReportResponseEntity;

    public RestControllerException(ResponseEntity<ErrorReport> errorReportResponseEntity) {
        super(getMessage(errorReportResponseEntity));
        this.errorReportResponseEntity = errorReportResponseEntity;
    }

    private static String getMessage(ResponseEntity<ErrorReport> errorReportResponse) {
        ErrorReport errorResponseBody = errorReportResponse.getBody();
        return errorResponseBody != null ? errorResponseBody.errorMessage() : null;
    }

    public ResponseEntity<ErrorReport> getErrorReportResponseEntity() {
        return errorReportResponseEntity;
    }
}
