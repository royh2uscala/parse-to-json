package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;

import java.time.Instant;

public interface RequestInfo {
    String getRequestId();
    String getRequestUri();
    Instant getStartTime();
    int getHttpResponseCode();
    String getRequesterIp();
    String getRequesterCountryCode();
    String getIspProviderIp();
    long getTimeLapsedMilli();

}
