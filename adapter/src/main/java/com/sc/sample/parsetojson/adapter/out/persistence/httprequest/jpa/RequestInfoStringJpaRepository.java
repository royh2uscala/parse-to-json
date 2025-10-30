package com.sc.sample.parsetojson.adapter.out.persistence.httprequest.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestInfoStringJpaRepository extends JpaRepository<RequestInfoJpaEntity, String> {
}
