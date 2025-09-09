package com.example.findex.domain.Index_Info.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoCreateRequest(
    @NotBlank String indexClassification,
    @NotBlank String indexName,
    @Positive int employedItemsCount,
    @NotNull LocalDate basePointInTime,
    @NotNull BigDecimal baseIndex,
    boolean favorite
    ) {

}
