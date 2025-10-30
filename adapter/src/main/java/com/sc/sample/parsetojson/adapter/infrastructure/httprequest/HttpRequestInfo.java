package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;

import java.time.Instant;

public record HttpRequestInfo(
        String requestId,
        String requestUri,
        Instant startTime,
        int httpResponseCode,

        String requesterIp,
        String requesterCountryCode,
        String ispProviderIp,
        long timeLapsedMilli

) implements RequestInfo {
    @Override
    public String getRequestId() {
        return requestId;
    }

    @Override
    public String getRequestUri() {
        return requestUri;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    @Override
    public String getRequesterIp() {
        return requesterIp;
    }

    @Override
    public String getRequesterCountryCode() {
        return requesterCountryCode;
    }

    @Override
    public String getIspProviderIp() {
        return ispProviderIp;
    }

    @Override
    public long getTimeLapsedMilli() {
        return timeLapsedMilli;
    }

    @Override
    public String toString() {
        return "HttpRequestInfo{" +
                "requestId='" + requestId + '\'' +
                ", requestUri='" + requestUri + '\'' +
                ", startTime=" + startTime +
                ", httpResponseCode=" + httpResponseCode +
                ", requesterIp='" + requesterIp + '\'' +
                ", requesterCountryCode='" + requesterCountryCode + '\'' +
                ", ispProviderIp='" + ispProviderIp + '\'' +
                ", timeLapsedMilli=" + timeLapsedMilli +
                '}';
    }
}
