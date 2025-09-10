package com.example.findex.domain.Auto_Sync.repository;

import com.example.findex.domain.Auto_Sync.entity.AutoSync;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AutoSyncRepository extends JpaRepository<AutoSync, Long> {

    @Query("""
    select a from AutoSync a
    where (:id is null or a.id = :id)
    and (:enabled is null or a.enabled = :enabled)
    order by a.indexInfo.id desc
    """)
    Slice<AutoSync> findFirstPageByConditions(Long id, Boolean enabled, Pageable pageable);

    @Query("""
    select a from AutoSync a
    where (:id is null or a.id = :id)
    and (:enabled is null or a.enabled = :enabled)
    and a.indexInfo.id < :cursorIndexId
    order by a.indexInfo.id desc
    """)
    Slice<AutoSync> findAfterCursorByConditions(Long id, Boolean enabled, Long cursorIndexId, Pageable pageable);

    Optional<AutoSync> findByIndexInfo_Id(Long id);
}