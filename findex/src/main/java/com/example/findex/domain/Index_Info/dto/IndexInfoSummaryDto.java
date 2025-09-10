package com.example.findex.domain.Index_Info.dto;

import com.example.findex.domain.Index_Info.entity.IndexInfo;

public record IndexInfoSummaryDto(
    Long id,
    String indexClassification,
    String indexName
) {

}
