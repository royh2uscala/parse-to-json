package com.sc.sample.parsetojson.adapter.infrastructure.httprequest;

public interface WhiteListRestProviderConfig {
    String getName();
    String getRestBaseUrl();
    boolean isActiveProvider();
}
