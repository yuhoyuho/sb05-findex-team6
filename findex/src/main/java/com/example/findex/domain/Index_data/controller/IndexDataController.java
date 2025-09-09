package com.example.findex.domain.Index_data.controller;

import com.example.findex.domain.Index_data.dto.CursorPageResponseIndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexDataCreateRequest;
import com.example.findex.domain.Index_data.dto.IndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexDataUpdateRequest;
import com.example.findex.domain.Index_data.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
}
