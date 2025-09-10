package com.example.findex.domain.Auto_Sync.service;

import com.example.findex.domain.Auto_Sync.entity.AutoSync;
import com.example.findex.domain.Auto_Sync.repository.AutoSyncRepository;
import com.example.findex.domain.Auto_Sync.dto.AutoSyncConfigDto;
import com.example.findex.domain.Auto_Sync.dto.AutoSyncConfigUpdateRequest;
import com.example.findex.domain.Auto_Sync.dto.CursorPageResponseAutoSyncConfigDto;
import com.example.findex.domain.Auto_Sync.mapper.AutoSyncMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AutoSyncService {
    private final AutoSyncRepository autoSyncRepository;
    private final AutoSyncMapper autoSyncMapper;

    /*
    export interface AutoSyncConfigQueryParams {
  indexInfoId?: number;
  enabled?: boolean;
  idAfter?: number;
  cursor?: string;
  sortField?: 'indexName' | 'enabled';
  sortDirection?: 'asc' | 'desc';
  size?: number;
}
     */

    @Transactional(readOnly = true)
    public CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto> findPage(
            Long indexInfoId, Boolean enabled, String cursor, Long idAfter, String sortField, String sortDirection, int size) {
            // sortDirection 포함하면 분기를 여러번 나눠야 해서 쿼리 복잡해짐.. 일단 기본값으로 대체.
        Long cursorIndexId = null;
        if(cursor != null && !cursor.isEmpty()) {
            cursorIndexId = Long.parseLong(cursor);
        } else if(idAfter != null) {
            cursorIndexId = idAfter;
        }

        Pageable pageable = PageRequest.of(0, size + 1);

        Slice<AutoSync> slice;
        if(cursorIndexId == null) {
            slice = autoSyncRepository.findFirstPageByConditions(indexInfoId, enabled, sortField, pageable);
        } else {
            slice = autoSyncRepository.findAfterCursorByConditions(indexInfoId, enabled, cursorIndexId, sortField, pageable);
        }

        List<AutoSync> autoSyncs = slice.getContent();
        boolean hasNext = autoSyncs.size() > size;

        String nextCursor = null;
        Long nextIdAfter = null;

        if(hasNext && !autoSyncs.isEmpty()) {
            AutoSync lastItem = autoSyncs.get(autoSyncs.size() - 1);
            nextIdAfter = lastItem.getIndexInfo().getId(); // indexInfo.id 사용
            nextCursor = String.valueOf(nextIdAfter);
        }

        List<AutoSyncConfigDto> content = autoSyncs.stream()
                .map(autoSyncMapper::toDto)
                .toList();

        return new CursorPageResponseAutoSyncConfigDto<>(
                content,
                nextCursor,      // indexInfo.id 문자열
                nextIdAfter,     // indexInfo.id 숫자값
                size,
                (long) content.size(),
                hasNext
        );
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
