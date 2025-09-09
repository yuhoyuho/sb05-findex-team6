package com.example.findex.controller;

import com.example.findex.dto.AutoSyncConfigDto;
import com.example.findex.dto.AutoSyncConfigUpdateRequest;
import com.example.findex.service.AutoSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auto-sync-configs")
public class AutoSyncController {

    private final AutoSyncService AutoSyncService;

    @GetMapping
    public ResponseEntity<List<AutoSyncConfigDto>> findAll(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "enabled", required = false) Boolean enabled) {
        List<AutoSyncConfigDto> result = AutoSyncService.findAll(id, enabled);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AutoSyncConfigDto> update(
            @PathVariable("id") Long id,
            @RequestBody AutoSyncConfigUpdateRequest request) {
        AutoSyncConfigDto updatedAutoSync = AutoSyncService.update(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedAutoSync);
    }
}