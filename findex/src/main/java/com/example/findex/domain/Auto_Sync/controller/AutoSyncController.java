package com.example.findex.domain.Auto_Sync.controller;

import com.example.findex.domain.Auto_Sync.dto.AutoSyncConfigDto;
import com.example.findex.domain.Auto_Sync.dto.AutoSyncConfigUpdateRequest;
import com.example.findex.domain.Auto_Sync.dto.CursorPageResponseAutoSyncConfigDto;
import com.example.findex.domain.Auto_Sync.service.AutoSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncController {

    private final AutoSyncService autoSyncService;

    @GetMapping
    public ResponseEntity<CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto>> findAll(
            @RequestParam(value = "indexInfoId", required = false) Long indexInfoId,
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "idAfter", required = false) Long idAfter,
            @RequestParam(value = "sortField", defaultValue = "indexName") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "desc") String sortDirection,
            @RequestParam(value = "size", defaultValue = "30") int size) {

        // 파라미터 검증
        if (size <= 0 || size > 100) {
            size = 30;
        }

        // sortField 검증 (프론트엔드에서 정의한 값만 허용)
        if (!"indexName".equals(sortField) && !"enabled".equals(sortField)) {
            sortField = "indexName";
        }

        // sortDirection 검증
        if (!"asc".equals(sortDirection) && !"desc".equals(sortDirection)) {
            sortDirection = "desc";
        }

        CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto> response =
                autoSyncService.findPage(indexInfoId, enabled, cursor, idAfter, sortField, sortDirection, size);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AutoSyncConfigDto> update(
            @PathVariable("id") Long id,
            @RequestBody AutoSyncConfigUpdateRequest request) {
        AutoSyncConfigDto updatedAutoSync = autoSyncService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedAutoSync);
    }
}