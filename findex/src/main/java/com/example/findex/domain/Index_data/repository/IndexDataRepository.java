package com.example.findex.domain.Index_data.repository;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_data.entity.IndexData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {
    // 랭킹용: IndexInfo 객체로 최신 2개 조회
    Page<IndexData> findTop2ByIndexInfoOrderByBaseDateDesc(IndexInfo indexInfo, Pageable pageable);

    // 차트용: index_info_id로 기간 조회
    List<IndexData> findAllByIndexInfo_IdAndBaseDateBetweenOrderByBaseDateAsc(Long indexInfoId, LocalDate startDate, LocalDate endDate);
}
