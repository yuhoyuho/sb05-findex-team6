package com.example.findex.domain.Index_Info.service;

import com.example.findex.domain.Index_Info.dto.CursorPageResponseIndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoCreateRequest;
import com.example.findex.domain.Index_Info.dto.IndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoSummaryDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoUpdateDto;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.mapper.IndexInfoMapper;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

  private final IndexInfoRepository repository;
  private final IndexInfoMapper mapper;

  // 전체조회
  public List<IndexInfoDto> findAll() {

    return mapper.toDtoList(repository.findAll());
  }

  // 단일조회
  public IndexInfoDto findById(Long id) {
    IndexInfo entity = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("지수 정보를 찾을 수 없습니다. id=" + id));
    return mapper.toDto(entity);
  }

  // 등록
  public IndexInfoDto create(IndexInfoCreateRequest request) {
    IndexInfo entity = mapper.toEntity(request);

    return mapper.toDto(repository.save(entity));
  }

  // 수정
  public IndexInfoDto update(Long id, IndexInfoUpdateDto request) {
    IndexInfo entity = repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("지수 정보를 찾을 수 없습니다. id=" + id));

    mapper.updateEntityFromRequest(request, entity);

    // favorite 값 업데이트
    if (request.favorite() != null) {
      entity.setFavorite(request.favorite());
    }

    return mapper.toDto(repository.save(entity));
  }

  // 삭제
  public void delete(Long id) {
    repository.deleteById(id);
  }

  // summaries (id, classification, name)
  public List<IndexInfoSummaryDto> findSummaries() {
    return repository.findAll().stream()
        .map(mapper::toSummaryDto)
        .toList();
  }

  public CursorPageResponseIndexInfoDto findByCursorAndSortAndFilter(Long cursor, int size,
      String sortField, String sortDirection, String indexClassification, String indexName, Boolean favorite) {

    List<IndexInfo> entities = repository.findByCursorAndFilter(cursor,size,sortField,sortDirection,indexClassification,indexName,favorite);

    List<IndexInfoDto> content = entities.stream()
        .map(mapper::toDto)
        .toList();

    Long nexIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).id();
    boolean hasNext = entities.size() == size;

    return CursorPageResponseIndexInfoDto.builder()
        .content(content)
        .nextCursor(nexIdAfter != null ? String.valueOf(nexIdAfter) : null)
        .nextIdAfter(nexIdAfter)
        .size(content.size())
        .totalElements(repository.count())
        .hasNext(hasNext)
        .build();
  }
}
