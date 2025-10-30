package com.sc.sample.parsetojson.adapter.out.persistence.httprequest.jpa;

import com.sc.sample.parsetojson.adapter.infrastructure.httprequest.RequestInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "request_info")
public class RequestInfoJpaEntity implements RequestInfo {
    @Id
    private String requestId;
    @Column(nullable = false)
    private String requestUri;
    @Column(nullable = false)
    private Instant startTime;
    @Column
    private int httpResponseCode;
    @Column(nullable = false)
    private String requesterIp;
    @Column(nullable = true)
    private String requesterCountryCode;
    @Column(nullable = true)
    private String ispProviderIp;
    @Column
    private long timeLapsedMilli;

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

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public void setRequesterIp(String requesterIp) {
        this.requesterIp = requesterIp;
    }

    public void setRequesterCountryCode(String requesterCountryCode) {
        this.requesterCountryCode = requesterCountryCode;
    }

    public void setIspProviderIp(String ispProviderIp) {
        this.ispProviderIp = ispProviderIp;
    }

    public void setTimeLapsedMilli(long timeLapsedMilli) {
        this.timeLapsedMilli = timeLapsedMilli;
    }
}
