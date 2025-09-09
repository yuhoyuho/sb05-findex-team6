package com.example.findex.domain.Index_data.repository;

import com.example.findex.domain.Index_data.entity.IndexData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {

    boolean existsByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

    // 미완성 커서 컨트롤을 구현해야함
    @Query("""
        SELECT d FROM IndexData d
            JOIN FETCH d.indexInfo i
            WHERE (:indexInfoId IS NULL OR i.id = :indexInfoId)
            AND d.baseDate >= coalesce(:startDate, d.baseDate)
            AND d.baseDate <= coalesce(:endDate, d.baseDate)
        """)
    List<IndexData> findDataWithCondition(
            @Param("indexInfoId") Long indexInfoId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("sortField") String sortField,
            @Param("sortDirection") String sortDirection,
            @Param("cursor") String cursor,
            Pageable pageable
    );

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
}
