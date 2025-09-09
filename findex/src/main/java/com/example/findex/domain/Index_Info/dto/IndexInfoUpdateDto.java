package com.example.findex.domain.Index_Info.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoUpdateDto(
    String indexClassification,
    String indexName,
    @Positive Integer employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    Boolean favorite
) {

}
