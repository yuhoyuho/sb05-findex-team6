package com.example.findex.domain.Index_data.controller;

import com.example.findex.domain.Index_data.dto.IndexChartResponse;
import com.example.findex.domain.Index_data.dto.IndexPerformanceRankingResponseDto;
import com.example.findex.domain.Index_data.dto.PeriodType;
import com.example.findex.domain.Index_data.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IndexDataController {

    private final IndexDataService indexDataService;

    @GetMapping("/test-data/setup")
    public ResponseEntity<String> setupTestData() {
        Long indexId = indexDataService.setupTestData();
        return ResponseEntity.ok("Test data created for Index ID: " + indexId);
    }

    @GetMapping("/index-data/{id}/chart")
    public ResponseEntity<IndexChartResponse> getIndexChart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "DAILY") PeriodType periodType) {

        IndexChartResponse response = indexDataService.getIndexChart(id, periodType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/indices/ranking")
    public ResponseEntity<IndexPerformanceRankingResponseDto> getPerformanceRanking() {
        IndexPerformanceRankingResponseDto responseDto = indexDataService.getPerformanceRanking();
        return ResponseEntity.ok(responseDto);
    }
}
