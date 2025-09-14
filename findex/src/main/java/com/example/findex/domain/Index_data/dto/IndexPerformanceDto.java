package com.example.findex.domain.Index_data.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class IndexPerformanceDto {
    private Long indexInfoId;
    private String indexClassification;
    private String indexName;
    private BigDecimal versus;
    private BigDecimal fluctuationRate;
    private BigDecimal currentPrice;
    private BigDecimal beforePrice;
}