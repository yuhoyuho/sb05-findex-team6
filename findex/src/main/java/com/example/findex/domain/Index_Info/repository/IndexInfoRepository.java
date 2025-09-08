package com.example.findex.domain.Index_Info.repository;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

    // 지수 분류명과 지수명으로 IndexInfo를 찾는 메서드
    Optional<IndexInfo> findByIndexNameAndIndexClassification(String indexName, String indexClassification);
}
