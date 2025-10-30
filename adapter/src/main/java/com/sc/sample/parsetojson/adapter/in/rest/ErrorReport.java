package com.sc.sample.parsetojson.adapter.in.rest;

public record ErrorReport(int httpStatus, String errorMessage) {
}
