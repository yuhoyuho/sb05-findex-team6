package com.example.findex.domain.Index_Info.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record FavoriteIndexDto(
    Long id,
    String indexName,
    BigDecimal closingPrice,
    BigDecimal versus,
    BigDecimal fluctuationRate
) {
}