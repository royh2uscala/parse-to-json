package com.sc.sample.parsetojson.adapter.out.persistence.httprequest.jpa;

import com.sc.sample.parsetojson.adapter.infrastructure.httprequest.HttpRequestInfo;
import com.sc.sample.parsetojson.adapter.infrastructure.httprequest.RequestInfo;

public class RequestInfoMapper {

    public static RequestInfoJpaEntity toJpaEntity(HttpRequestInfo requestInfoModel) {
        RequestInfoJpaEntity jpaEntity = new RequestInfoJpaEntity();
        jpaEntity.setRequestId(requestInfoModel.getRequestId());
        jpaEntity.setRequestUri(requestInfoModel.getRequestUri());
        jpaEntity.setStartTime(requestInfoModel.getStartTime());
        jpaEntity.setHttpResponseCode(requestInfoModel.getHttpResponseCode());
        jpaEntity.setRequesterIp(requestInfoModel.getRequesterIp());
        jpaEntity.setRequesterCountryCode(requestInfoModel.getRequesterCountryCode());
        jpaEntity.setIspProviderIp(requestInfoModel.getIspProviderIp());
        jpaEntity.setTimeLapsedMilli(requestInfoModel.getTimeLapsedMilli());
        return jpaEntity;
    }

    public static HttpRequestInfo toDomainModel(RequestInfoJpaEntity jpaEntity) {
        HttpRequestInfo requestInfoModel = new HttpRequestInfo(
                jpaEntity.getRequestId(),jpaEntity.getRequestUri(),jpaEntity.getStartTime(),
                jpaEntity.getHttpResponseCode(), jpaEntity.getRequesterIp(),
                jpaEntity.getRequesterCountryCode(), jpaEntity.getIspProviderIp(),
                jpaEntity.getTimeLapsedMilli()
        );
        return requestInfoModel;
    }
}
