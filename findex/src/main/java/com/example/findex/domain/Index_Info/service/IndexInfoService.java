package com.example.findex.domain.Index_Info.service;

import com.example.findex.common.base.SourceType;
import com.example.findex.domain.Index_Info.dto.IndexInfoCreateRequest;
import com.example.findex.domain.Index_Info.dto.IndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoSummaryDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoUpdateDto;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

  private final IndexInfoRepository repository;

  // 전체조회
  public List<IndexInfoDto> findAll() {
    return repository.findAll().stream()
        .map(IndexInfoDto::fromEntity)
        .toList();
  }

  // 단일조회
  public IndexInfoDto findById(Long id) {
    IndexInfo entity = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("지수 정보를 찾을 수 없습니다. id=" + id));
    return IndexInfoDto.fromEntity(entity);
  }

  // 등록
  public IndexInfoDto create(IndexInfoCreateRequest request) {
    IndexInfo entity = IndexInfo.builder()
        .indexClassification(request.indexClassification())
        .indexName(request.indexName())
        .employedItemsCount(request.employedItemsCount())
        .basePointInTime(request.basePointInTime())
        .baseIndex(request.baseIndex())
        .favorite(request.favorite())
        .sourceType(SourceType.사용자)
        .build();

    return IndexInfoDto.fromEntity(repository.save(entity));
  }

  // 수정
  public IndexInfoDto update(Long id, IndexInfoUpdateDto request) {
    IndexInfo entity = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("지수 정보를 찾을 수 없습니다. id=" + id));

    if (request.indexClassification() != null)entity.setIndexClassification(request.indexClassification());
    if (request.indexName() != null)entity.setIndexName(request.indexName());
    if (request.employedItemsCount() != null)entity.setEmployedItemsCount(request.employedItemsCount());
    if (request.basePointInTime() != null)entity.setBasePointInTime(request.basePointInTime());
    if (request.baseIndex() != null)entity.setBaseIndex(request.baseIndex());
    if (request.favorite() != null)entity.setFavorite(request.favorite());

    return IndexInfoDto.fromEntity(repository.save(entity));
  }

  // 삭제
  public void delete(Long id) {
    repository.deleteById(id);
  }

  // summaries (id, classification, name)
  public List<IndexInfoSummaryDto> findSummaries() {
    return repository.findAll().stream()
        .map(IndexInfoSummaryDto::fromEntity)
        .toList();
  }
}
