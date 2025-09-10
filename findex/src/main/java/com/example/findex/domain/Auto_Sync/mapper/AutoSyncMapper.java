package com.example.findex.domain.Auto_Sync.mapper;

import com.example.findex.domain.Auto_Sync.entity.AutoSync;
import com.example.findex.domain.Auto_Sync.dto.AutoSyncConfigDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AutoSyncMapper {
    /*
    Dto
    Long id,
    Long indexInfoId,
    String indexClassification,
    String indexName,
    boolean enabled
     */
    /*
    Entity
    private Long id;
    private IndexInfo indexInfo; // 지수 정보
    private boolean enabled = false;  /
     */
    @Mapping(target = "indexInfoId", source = "indexInfo.id")
    @Mapping(target = "indexClassification", source = "indexInfo.indexClassification")
    @Mapping(target = "indexName", source = "indexInfo.indexName")
    AutoSyncConfigDto toDto(AutoSync autoSync);
}