package com.example.findex.domain.Index_Info.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoUpdateDto(
    String indexClassification,
    String indexName,
    Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    Boolean favorite
) {

}
