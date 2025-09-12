package com.example.findex.domain.Index_data.entity;

import com.example.findex.common.base.BaseEntity;
import com.example.findex.common.base.SourceType;
import com.example.findex.common.openApi.dto.IndexApiResponseDto;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(
        name = "index_data",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_index_info_id_base_date",
                        columnNames = {"index_info_id", "base_date"}
                )
        }
)
public class IndexData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate; // 기준 일자

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "market_price", precision = 18, scale = 2)
    private BigDecimal marketPrice; // 시가

    @Column(name = "closing_price", precision = 18, scale = 2)
    private BigDecimal closingPrice; // 종가

    @Column(name = "high_price", precision = 18, scale = 2)
    private BigDecimal highPrice; // 고가

    @Column(name = "low_price", precision = 18, scale = 2)
    private BigDecimal lowPrice;

    @Column(name = "versus", precision = 18, scale = 2)
    private BigDecimal versus; // 대비

    @Column(name = "fluctuation_rate", precision = 5, scale = 2)
    private BigDecimal fluctuationRate; // 등락률

    @Column(name = "trading_quantity")
    private Long tradingQuantity; // 거래량

    @Column(name = "trading_price")
    private Long tradingPrice; // 거래 대금

    @Column(name = "market_total_amount")
    private Long marketTotalAmount; // 상장 시가 총액

    /**
     * 비즈니스 메서드
     * 외부 DTO를 기반으로 엔티티의 값을 업데이트
     */
    public void updateData(IndexApiResponseDto.Item item) {
        this.marketPrice = new BigDecimal(item.getMarketPrice().replace(",", ""));
        this.closingPrice = new BigDecimal(item.getClosingPrice().replace(",", ""));
        this.highPrice = new BigDecimal(item.getHighPrice().replace(",", ""));
        this.lowPrice = new BigDecimal(item.getLowPrice().replace(",", ""));
        this.versus = new BigDecimal(item.getVersus().replace(",", ""));
        this.fluctuationRate = new BigDecimal(item.getFluctuationRate().replace(",", ""));
        this.tradingQuantity = Long.parseLong(item.getTradingQuantity().replace(",", ""));
        this.tradingPrice = Long.parseLong(item.getTradingPrice().replace(",", ""));
        this.marketTotalAmount = Long.parseLong(item.getMarketTotalAmount().replace(",", ""));
    }
}
