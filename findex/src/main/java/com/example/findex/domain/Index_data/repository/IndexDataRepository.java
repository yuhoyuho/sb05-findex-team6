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

    @Query(value = """
    WITH ranked AS (
        SELECT 
            id,
            index_info_id,
            base_date,
            closing_price,
            ROW_NUMBER() OVER (PARTITION BY index_info_id ORDER BY base_date DESC) AS rn_latest,
            ROW_NUMBER() OVER (PARTITION BY index_info_id ORDER BY base_date ASC) AS rn_earliest
        FROM index_data
        WHERE base_date BETWEEN :start AND :end
          AND (:indexInfoId IS NULL OR index_info_id = :indexInfoId)
    ),
    latest AS (
        SELECT * FROM ranked WHERE rn_latest = 1
    ),
    earliest AS (
        SELECT * FROM ranked WHERE rn_earliest = 1
    )
    SELECT 
        l.index_info_id          AS index_info_id,
        ii.index_classification  AS index_classification,
        ii.index_name             AS index_name,
        l.closing_price           AS current_price,
        e.closing_price           AS before_price,
        (l.closing_price - e.closing_price)         AS versus,
        CASE 
            WHEN e.closing_price = 0 THEN 0
            ELSE ((l.closing_price - e.closing_price) / e.closing_price) * 100
        END AS fluctuation_rate
    FROM latest l
    JOIN earliest e ON l.index_info_id = e.index_info_id
    JOIN index_info ii ON ii.id = l.index_info_id
    ORDER BY fluctuation_rate DESC
    LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> getRankedPerformance2Raw(@Param("indexInfoId") Long indexInfoId,
                                            @Param("start") LocalDate start,
                                            @Param("end") LocalDate end,
                                            @Param("limit") int limit);


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
