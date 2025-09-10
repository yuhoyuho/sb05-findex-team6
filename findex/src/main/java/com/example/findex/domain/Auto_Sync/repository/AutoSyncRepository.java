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
    where (:indexInfoId is null or a.indexInfo.id = :indexInfoId)
    and (:enabled is null or a.enabled = :enabled)
    order by 
        case when :sortField = 'indexName' then a.indexInfo.indexName end desc,
        case when :sortField = 'enabled' then a.enabled end desc,
        a.indexInfo.id desc
""")
    Slice<AutoSync> findFirstPageByConditions(
            @Param("indexInfoId") Long indexInfoId,
            @Param("enabled") Boolean enabled,
            @Param("sortField") String sortField,
            Pageable pageable
    );

    @Query("""
    select a from AutoSync a
    where (:indexInfoId is null or a.indexInfo.id = :indexInfoId)
    and (:enabled is null or a.enabled = :enabled)
    and a.indexInfo.id < :cursorIndexId
    order by 
        case when :sortField = 'indexName' then a.indexInfo.indexName end desc,
        case when :sortField = 'enabled' then a.enabled end desc,
        a.indexInfo.id desc
""")
    Slice<AutoSync> findAfterCursorByConditions(
            @Param("indexInfoId") Long indexInfoId,
            @Param("enabled") Boolean enabled,
            @Param("cursorIndexId") Long cursorIndexId,
            @Param("sortField") String sortField,
            Pageable pageable
    );

    Optional<AutoSync> findByIndexInfo_Id(Long id);

    @Query("""
    select count(a) > 0 from AutoSync a
    where a.indexInfo.id = :cursorId
    and (:indexInfoId is null or a.indexInfo.id = :indexInfoId)
    and (:enabled is null or a.enabled = :enabled)
    """)
    boolean existsByCursorAndFilter(@Param("cursorId") Long cursorId,
                                    @Param("indexInfoId") Long indexInfoId,
                                    @Param("enabled") Boolean enabled,
                                    @Param("sortField") String sortField);
}