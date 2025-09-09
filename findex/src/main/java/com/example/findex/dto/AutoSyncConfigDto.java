package com.example.findex.dto;

public record AutoSyncConfigDto (
        Long id,
        // 지수 정보 ID
        Long indexInfoId,
        // 지수 분류명
        String indexClassification,
        // 지수명
        String indexName,
        // 활성화 여부
        boolean enabled
){
}