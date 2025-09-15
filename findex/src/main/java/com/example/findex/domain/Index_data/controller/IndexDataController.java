package com.example.findex.domain.Index_data.controller;

import com.example.findex.domain.Index_data.dto.*;
import com.example.findex.domain.Index_data.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-data")
public class IndexDataController {

    private final IndexDataService indexDataService;

    @GetMapping
    public ResponseEntity<CursorPageResponseIndexDataDto> getIndexDataList(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "baseDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "10") Integer size) {

        CursorPageResponseIndexDataDto response = indexDataService.getIndexDataList(
                indexInfoId, startDate, endDate, idAfter, cursor, sortField, sortDirection, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IndexDataDto> createIndexData(@RequestBody IndexDataCreateRequest request) {
        IndexDataDto created = indexDataService.createIndexData(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IndexDataDto> updateIndexData(
            @PathVariable Long id,
            @RequestBody IndexDataUpdateRequest request
    ) {
        IndexDataDto updated = indexDataService.updateIndexData(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIndexData(@PathVariable Long id) {
        indexDataService.deleteIndexData(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> downloadIndexData(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "baseDate") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        String csvContent = indexDataService.exportToCsv(indexInfoId, startDate, endDate, sortField, sortDirection);

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=index_data.csv")
                .body("\uFEFF" + csvContent);
    }

    @GetMapping("/{id}/chart")
    public ResponseEntity<IndexChartResponse> getIndexChart(
            @PathVariable Long id,
            @RequestParam(defaultValue = "DAILY") PeriodType periodType) {

        IndexChartResponse response = indexDataService.getIndexChart(id, periodType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/performance/rank")
    public ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRanking(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(defaultValue = "DAILY") PeriodType periodType,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<RankedIndexPerformanceDto> responseDto = indexDataService.getRankedPerformance2(indexInfoId, periodType, limit);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/performance/favorite")
    public ResponseEntity<List<IndexPerformanceDto>> getFavoritePerformance(
            @RequestParam(defaultValue = "DAILY") PeriodType periodType) {
        List<IndexPerformanceDto> responseDto = indexDataService.getFavoritePerformance(periodType);
        return ResponseEntity.ok(responseDto);
    }
}
