package com.example.findex.domain.Index_Info.controller;

import com.example.findex.domain.Index_Info.dto.IndexInfoCreateRequest;
import com.example.findex.domain.Index_Info.dto.IndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoSummaryDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoUpdateDto;
import com.example.findex.domain.Index_Info.service.IndexInfoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/index-infos")
public class IndexInfoController {

  private final IndexInfoService service;

  @PostMapping
  public ResponseEntity<IndexInfoDto> create(@Valid @RequestBody IndexInfoCreateRequest request) {
    IndexInfoDto created = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);

  }

  @GetMapping
  public ResponseEntity<?> findAll(
      @RequestParam(required = false) Long cursor,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (cursor == null) {
      return ResponseEntity.ok(service.findAll());
    } else {
      return ResponseEntity.ok(service.findByCursor(cursor, size));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> findById(@PathVariable("id") Long id) {
    return ResponseEntity.ok(service.findById(id));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<IndexInfoDto> update(@PathVariable("id") Long id,
      @Valid @RequestBody IndexInfoUpdateDto request) {
    return ResponseEntity.ok(service.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> getSummaries() {
    return ResponseEntity.ok(service.findSummaries());
  }


}
