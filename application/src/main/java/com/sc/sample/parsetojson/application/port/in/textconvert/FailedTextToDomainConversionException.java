package com.sc.sample.parsetojson.application.port.in.textconvert;

public class FailedTextToDomainConversionException extends Exception {
    public FailedTextToDomainConversionException(Throwable ex) {
        super(ex);
    }
}
