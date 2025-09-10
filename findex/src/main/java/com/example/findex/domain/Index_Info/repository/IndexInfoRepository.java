package com.example.findex.domain.Index_Info.repository;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

    // 지수 분류명과 지수명으로 IndexInfo를 찾는 메서드
    Optional<IndexInfo> findByIndexNameAndIndexClassification(String indexName, String indexClassification);

    // 커서 이후 데이터를 가져오는 메서드
    List<IndexInfo> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);

    // 처음 조회
    List<IndexInfo> findAllByOrderByIdAsc(Pageable pageable);
}
