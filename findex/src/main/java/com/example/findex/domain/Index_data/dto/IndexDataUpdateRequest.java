package com.example.findex.domain.Index_data.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndexDataUpdateRequest {
    private BigDecimal marketPrice;
    private BigDecimal closingPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal versus;
    private BigDecimal fluctuationRate;
    private Long tradingQuantity;
    private Long tradingPrice;
    private Long marketTotalAmount;
}
