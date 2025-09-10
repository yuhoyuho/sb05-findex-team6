package com.example.findex.domain.Index_Info.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoCreateRequest(
    String indexClassification,
    String indexName,
    int employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    boolean favorite
    ) {

}
