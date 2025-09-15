package com.example.findex.domain.Auto_Sync.service;

import com.example.findex.domain.Auto_Sync.entity.AutoSync;
import com.example.findex.domain.Auto_Sync.repository.AutoSyncRepository;
import com.example.findex.domain.Auto_Sync.dto.AutoSyncConfigDto;
import com.example.findex.domain.Auto_Sync.dto.AutoSyncConfigUpdateRequest;
import com.example.findex.domain.Auto_Sync.dto.CursorPageResponseAutoSyncConfigDto;
import com.example.findex.domain.Auto_Sync.mapper.AutoSyncMapper;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AutoSyncService {
    private final AutoSyncRepository autoSyncRepository;
    private final AutoSyncMapper autoSyncMapper;

    @Transactional(readOnly = true)
    public CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto> findPage(
            Long indexInfoId, Boolean enabled, String cursor, Long idAfter, String sortField, String sortDirection, int size) {
        /*
        sortField?: 'indexName' | 'enabled';
        sortDirection?: 'asc' | 'desc';
         */
        Long cursorIndexId = null;

        if(cursor != null && !cursor.isEmpty()) {
            cursorIndexId = Long.parseLong(cursor);
            // 현재 커서 위치의 정렬 필드값 조회
        } else if(idAfter != null) {
            cursorIndexId = idAfter;
        }

        String sortProperty = sortField.equals("indexName")
                ? "indexInfo.indexName"
                : "enabled";

        Pageable pageable = PageRequest.of(0, size+1,
                Sort.by(Sort.Direction.ASC,sortProperty));

        Slice<AutoSync> slice;
        if (cursorIndexId == null) {
            slice = autoSyncRepository.findFirstPageByConditions(indexInfoId, enabled, pageable);
        } else {
            slice = autoSyncRepository.findAfterCursorAsc(indexInfoId, enabled, cursorIndexId,  pageable);
        }

        List<AutoSync> autoSyncs = slice.getContent();
        boolean hasNext = autoSyncs.size() > size;

        List<AutoSync> actualContent = hasNext ?
                autoSyncs.subList(0, size) : autoSyncs;

        String nextCursor = null;
        Long nextIdAfter = null;

        if(hasNext && !actualContent.isEmpty()) {
            AutoSync lastItem = actualContent.get(actualContent.size() - 1);
            nextIdAfter = lastItem.getIndexInfo().getId();
            nextCursor = String.valueOf(nextIdAfter);
        }

        Long totalElements = autoSyncRepository.count();

        List<AutoSyncConfigDto> ActualContents = actualContent.stream()
                .map(autoSyncMapper::toDto)
                .toList();

        return new CursorPageResponseAutoSyncConfigDto<>(
                ActualContents,
                nextCursor,      // indexInfo.id 문자열
                nextIdAfter,     // indexInfo.id 숫자값
                size,
                totalElements,
                hasNext
        );
    }

    @Transactional
    public void create(IndexInfo indexInfo) {
        if(autoSyncRepository.findByIndexInfo_Id(indexInfo.getId()).isPresent()) {
            return;
        }
        AutoSync autoSync = AutoSync.builder()
                .indexInfo(indexInfo)
                .enabled(false)
                .build();
        autoSyncRepository.save(autoSync);
    }

    @Transactional
    public AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request) {
        AutoSync autoSync = autoSyncRepository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("해당 Id가 없습니다."));
        autoSync.updateEnabled(request.enabled());
        autoSyncRepository.save(autoSync);
        return autoSyncMapper.toDto(autoSync);
    }
}
