package com.example.findex.domain.Index_data.repository;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_data.entity.IndexData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>, IndexDataRepositoryCustom{

    boolean existsByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

    @Query("""
            SELECT COUNT(d) FROM IndexData d 
            JOIN d.indexInfo i
            WHERE (:indexInfoId IS NULL OR i.id = :indexInfoId)
            AND d.baseDate >= coalesce(:startDate, d.baseDate)
            AND d.baseDate <= coalesce(:endDate, d.baseDate)
            """)
    long countDataWithCondition(
            @Param("indexInfoId") Long indexInfoId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // OpenAPI 데이터 연동 시 id, base_date 기준으로 데이터 존재 여부 확인
    Optional<IndexData> findByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

    List<IndexData> findAllByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(Long indexInfoId, LocalDate startDate, LocalDate endDate);

    // 랭킹용

    // 차트용: index_info_id로 기간 조회
    List<IndexData> findAllByIndexInfo_IdAndBaseDateBetweenOrderByBaseDateAsc(Long indexInfoId, LocalDate startDate, LocalDate endDate);
}
