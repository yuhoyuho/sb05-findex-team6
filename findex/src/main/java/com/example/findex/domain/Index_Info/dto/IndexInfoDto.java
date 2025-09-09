package com.example.findex.domain.Index_Info.dto;

import com.example.findex.common.base.SourceType;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoDto(
    Long id,
    String indexClassification,
    String indexName,
    int employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    SourceType sourceType,
    boolean favorite
) {
  public static IndexInfoDto fromEntity(IndexInfo entity) {
    return new IndexInfoDto(
        entity.getId(),
        entity.getIndexClassification(),
        entity.getIndexName(),
        entity.getEmployedItemsCount(),
        entity.getBasePointInTime(),
        entity.getBaseIndex(),
        entity.getSourceType(),
        entity.isFavorite()
    );
  }
}
