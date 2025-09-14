package com.example.findex.domain.Auto_Sync.repository;

import com.example.findex.domain.Auto_Sync.entity.AutoSync;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AutoSyncRepository extends JpaRepository<AutoSync, Long> {

    @Query("""
    select a from AutoSync a
    join fetch a.indexInfo info
    where (:indexInfoId is null or info.id = :indexInfoId)
    and (:enabled is null or a.enabled = :enabled)
    """)
    Slice<AutoSync> findFirstPageByConditions(
            @Param("indexInfoId") Long indexInfoId,
            @Param("enabled") Boolean enabled,
            Pageable pageable
    );

    @Query("""
    select a from AutoSync a
    join fetch a.indexInfo info
    where (:indexInfoId is null or info.id = :indexInfoId)
    and (:enabled is null or a.enabled = :enabled)
    and info.id > :cursorIndexId
    """)
    Slice<AutoSync> findAfterCursorAsc(
            @Param("indexInfoId") Long indexInfoId,
            @Param("enabled") Boolean enabled,
            @Param("cursorIndexId") Long cursorIndexId,
            Pageable pageable
    );

    @Query("""
    select a from AutoSync a
    join fetch a.indexInfo info
    where (:indexInfoId is null or info.id = :indexInfoId)
    and (:enabled is null or a.enabled = :enabled)
    and info.id < :cursorIndexId
    """)
    Slice<AutoSync> findAfterCursorDesc(
            @Param("indexInfoId") Long indexInfoId,
            @Param("enabled") Boolean enabled,
            @Param("cursorIndexId") Long cursorIndexId,
            Pageable pageable
    );

    Optional<AutoSync> findByIndexInfo_Id(Long id);
}