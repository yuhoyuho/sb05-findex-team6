package com.example.findex.domain.Index_data.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RankedIndexPerformanceDto {
    private IndexPerformanceDto performance;
    private int rank;
}