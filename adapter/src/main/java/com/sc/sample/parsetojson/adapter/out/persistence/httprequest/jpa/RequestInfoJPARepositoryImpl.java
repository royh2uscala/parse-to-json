package com.sc.sample.parsetojson.adapter.out.persistence.httprequest.jpa;

import com.sc.sample.parsetojson.adapter.infrastructure.httprequest.HttpRequestInfo;
import com.sc.sample.parsetojson.adapter.out.persistence.httprequest.RequestInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class RequestInfoJPARepositoryImpl implements RequestInfoRepository {
    private final RequestInfoStringJpaRepository requestInfoStringJpaRepository;

    public RequestInfoJPARepositoryImpl(RequestInfoStringJpaRepository requestInfoStringJpaRepository) {
        this.requestInfoStringJpaRepository = requestInfoStringJpaRepository;
    }

    @Override
    @Transactional
    public HttpRequestInfo save(HttpRequestInfo httpRequestInfo) {
        RequestInfoJpaEntity jpaEntity =
                requestInfoStringJpaRepository.save(
                        RequestInfoMapper.toJpaEntity(httpRequestInfo));

        return RequestInfoMapper.toDomainModel(jpaEntity);
    }
}
