package com.example.findex.domain.Index_Info.dto;

import com.example.findex.domain.Index_Info.entity.IndexInfo;

public record IndexInfoSummaryDto(
    Long id,
    String indexClassification,
    String indexName
) {
  public static IndexInfoSummaryDto fromEntity(IndexInfo entity) {
    return  new IndexInfoSummaryDto(
        entity.getId(),
        entity.getIndexClassification(),
        entity.getIndexName()
    );
  }

}
