package com.example.findex.domain.Index_data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class IndexChartResponse {
    private Long indexInfoId;
    private String indexClassification;
    private String indexName;
    private PeriodType periodType;
    private List<ChartDataPoint> dataPoints;
    private List<ChartDataPoint> ma5DataPoints;
    private List<ChartDataPoint> ma20DataPoints;
}
