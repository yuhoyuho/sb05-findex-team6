package com.example.findex.domain.Index_Info.controller;

import com.example.findex.domain.Index_Info.dto.CursorPageResponseIndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoCreateRequest;
import com.example.findex.domain.Index_Info.dto.IndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoSummaryDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoUpdateDto;
import com.example.findex.domain.Index_Info.service.IndexInfoService;
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
  public ResponseEntity<IndexInfoDto> create(@RequestBody IndexInfoCreateRequest request) {
    IndexInfoDto created = service.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);

  }
  @GetMapping
  public ResponseEntity<CursorPageResponseIndexInfoDto> findAll(
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) String sortField,
      @RequestParam(required = false) String sortDirection,
      @RequestParam(required = false) String indexClassification,
      @RequestParam(required = false) String indexName,
      @RequestParam(required = false) Boolean favorite

  ) {


    CursorPageResponseIndexInfoDto response =
        service.findByCursorAndFilter(cursor, size, sortField, sortDirection,indexClassification,indexName,favorite);

    return ResponseEntity.ok(response);
  }

    @GetMapping("/{id}")
    public ResponseEntity<IndexInfoDto> findById (@PathVariable("id") Long id){
      return ResponseEntity.ok(service.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<IndexInfoDto> update (@PathVariable("id") Long id,
        @RequestBody IndexInfoUpdateDto request){
      return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete (@PathVariable("id") Long id){
      service.delete(id);
      return ResponseEntity.noContent().build();
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<IndexInfoSummaryDto>> getSummaries () {
      return ResponseEntity.ok(service.findSummaries());
    }




  }
