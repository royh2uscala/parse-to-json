package com.sc.sample.parsetojson;

import com.sc.sample.parsetojson.adapter.out.persistence.httprequest.RequestInfoRepository;
import com.sc.sample.parsetojson.adapter.out.persistence.httprequest.jpa.RequestInfoJPARepositoryImpl;
import com.sc.sample.parsetojson.adapter.out.persistence.httprequest.jpa.RequestInfoStringJpaRepository;
import com.sc.sample.parsetojson.application.port.in.textconvert.TextDataToDomainConverter;
import com.sc.sample.parsetojson.application.service.SimpleTextDataToDomainService;
import com.sc.sample.parsetojson.model.SomeBizDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Autowired
    RequestInfoStringJpaRepository requestInfoStringJpaRepository;

    @Bean
    TextDataToDomainConverter<SomeBizDomain> someBizDomainConverter() {

        return new SimpleTextDataToDomainService();
    }

    @Bean
    RequestInfoRepository requestInfoRepository() {
        return new RequestInfoJPARepositoryImpl(requestInfoStringJpaRepository);
    }
}
