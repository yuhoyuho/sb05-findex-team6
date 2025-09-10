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

    private final AutoSyncService AutoSyncService;
    private final AutoSyncService autoSyncService;

    @GetMapping
    public ResponseEntity<CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto>> findAll(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = "30") int size) {

        if (size <= 0 || size > 100) {
            size = 30;
        }

        CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto> response =
                autoSyncService.findPage(id, enabled, cursor, size);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AutoSyncConfigDto> update(
            @PathVariable("id") Long id,
            @RequestBody AutoSyncConfigUpdateRequest request) {
        AutoSyncConfigDto updatedAutoSync = AutoSyncService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedAutoSync);
    }
}