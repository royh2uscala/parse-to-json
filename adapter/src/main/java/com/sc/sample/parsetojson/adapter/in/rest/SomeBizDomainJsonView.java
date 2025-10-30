package com.sc.sample.parsetojson.adapter.in.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sc.sample.parsetojson.model.SomeBizDomain;

public record SomeBizDomainJsonView(
        @JsonProperty("Name") String name,
        @JsonProperty("Transport") String transport,
        @JsonProperty("Top Speed") String topSpeed) {

    public static SomeBizDomainJsonView transitFrom(SomeBizDomain domain) {
        return new SomeBizDomainJsonView(
                domain.name(), domain.transport(), String.valueOf(domain.topSpeed()));
    }
}
