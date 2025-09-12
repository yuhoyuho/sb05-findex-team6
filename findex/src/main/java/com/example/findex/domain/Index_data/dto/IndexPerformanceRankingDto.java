package com.example.findex.domain.Index_data.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class IndexPerformanceRankingDto {
    private int ranking;
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal change;
    private double changePercent;
}