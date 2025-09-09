package com.example.findex.domain.Auto_Sync.repository;

import com.example.findex.domain.Auto_Sync.entity.AutoSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutoSyncRepository extends JpaRepository<AutoSync, Long> {

    @Query("""
        select a from AutoSync a
        where (:id is null or a.id = :id)
        and (:enabled is null or a.enabled = :enabled)
    """) // 조회 api에서 빈칸으로 들어온 필드에 대해 검색 처리 시 무시하고 싶을 때 : is null or
    List<AutoSync> findByConditions(Long id, Boolean enabled);

    Optional<AutoSync> findByIndexInfo_Id(Long id);
}