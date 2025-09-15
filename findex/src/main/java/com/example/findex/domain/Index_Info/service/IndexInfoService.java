package com.example.findex.domain.Index_Info.service;

import com.example.findex.domain.Auto_Sync.service.AutoSyncService;
import com.example.findex.domain.Index_Info.dto.CursorPageResponseIndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoCreateRequest;
import com.example.findex.domain.Index_Info.dto.IndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoSummaryDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoUpdateDto;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.mapper.IndexInfoMapper;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import jakarta.persistence.EntityNotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexInfoService {

  private final AutoSyncService autoSyncService;
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

    repository.save(entity);
    autoSyncService.create(entity); // [자동 연동 설정] 생성

    return mapper.toDto(entity);
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

    public CursorPageResponseIndexInfoDto findByCursorAndFilter(
            String cursor,
            int size,
            String sortField,
            String sortDirection,
            String indexClassification,
            String indexName,
            Boolean favorite
    ) {

        List<IndexInfo> entities = repository.findByCursorAndFilter(
                cursor, size, sortField, sortDirection,
                indexClassification, indexName, favorite
        );

        boolean hasNext = entities.size() > size;
        if (hasNext) {
            entities = entities.subList(0, size);
        }

        List<IndexInfoDto> content = entities.stream()
                .map(mapper::toDto)
                .toList();

        Long nextIdAfter = content.isEmpty() ? null : content.get(content.size() - 1).id();

        String nextCursor = null;
        if (!content.isEmpty()) {
            IndexInfo last = entities.get(entities.size() - 1);
            String sortValue = extractSortValue(last, sortField);
            String json = String.format(
                    "{\"%s\":\"%s\",\"id\":%d}",
                    sortField, sortValue, last.getId()
            );
            nextCursor = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        }

        String cls = indexClassification == null ? "" : indexClassification;
        String name = indexName == null ? "" : indexName;

        long totalElements;
        if (cls.isEmpty() && name.isEmpty() && favorite == null) {
            totalElements = repository.count(); // 전체
        } else {
            totalElements = repository.countByFilter(cls, name, favorite);
        }

        return CursorPageResponseIndexInfoDto.builder()
                .content(content)
                .nextCursor(nextCursor)
                .nextIdAfter(nextIdAfter)
                .size(size)
                .totalElements(totalElements)
                .hasNext(hasNext)
                .build();
    }

    private String extractSortValue(IndexInfo entity, String sortField) {
        return switch (sortField) {
            case "indexClassification" -> entity.getIndexClassification();
            default -> String.valueOf(entity.getId());
        };
    }
}
