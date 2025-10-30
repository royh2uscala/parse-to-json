package com.sc.sample.parsetojson.adapter.out.persistence.httprequest;

import com.sc.sample.parsetojson.adapter.infrastructure.httprequest.HttpRequestInfo;

public interface RequestInfoRepository {
    HttpRequestInfo save(HttpRequestInfo httpRequestInfo);
}
