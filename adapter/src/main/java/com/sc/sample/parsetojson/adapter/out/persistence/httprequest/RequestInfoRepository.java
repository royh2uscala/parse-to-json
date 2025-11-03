package com.sc.sample.parsetojson.adapter.out.persistence.httprequest;

import com.sc.sample.parsetojson.adapter.infrastructure.httprequest.HttpRequestInfo;

import java.util.List;

public interface RequestInfoRepository {
    HttpRequestInfo save(HttpRequestInfo httpRequestInfo);
    List<HttpRequestInfo> findAll();
}
