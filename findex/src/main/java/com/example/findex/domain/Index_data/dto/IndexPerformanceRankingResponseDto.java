package com.example.findex.domain.Index_data.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class IndexPerformanceRankingResponseDto {
    private final List<IndexPerformanceRankingDto> rankings;
}