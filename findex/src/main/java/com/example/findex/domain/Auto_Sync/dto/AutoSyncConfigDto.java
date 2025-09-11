package com.example.findex.domain.Auto_Sync.dto;

public record AutoSyncConfigDto (
        Long id,
        Long indexInfoId,
        String indexClassification,
        String indexName,
        boolean enabled
){
}