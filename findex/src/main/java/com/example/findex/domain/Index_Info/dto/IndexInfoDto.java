package com.example.findex.domain.Index_Info.dto;

import com.example.findex.common.base.SourceType;
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

}
