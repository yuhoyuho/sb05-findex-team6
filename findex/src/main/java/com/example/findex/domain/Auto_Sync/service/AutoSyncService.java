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

    @Transactional(readOnly = true)
    public CursorPageResponseAutoSyncConfigDto<AutoSyncConfigDto> findPage(
            Long indexInfoId, Boolean enabled, String cursor, Long idAfter, String sortField, String sortDirection, int size) {
        Long cursorIndexId = null;
        if(cursor != null && !cursor.isEmpty()) {
            try {
                cursorIndexId = Long.parseLong(cursor);

                // 커서 유효성 검사: 해당 커서가 현재 필터 조건에 맞는지 확인
                if (!isCursorValidForFilter(cursorIndexId, indexInfoId, enabled, sortField)) {
                    System.out.println("Invalid cursor for current filter conditions, resetting to first page");
                    cursorIndexId = null; // 첫 페이지로 리셋
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid cursor format: " + cursor);
            }
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
        boolean hasNext = slice.hasNext();

        // size만큼만 반환
        if(autoSyncs.size() > size) {
            autoSyncs = autoSyncs.subList(0, size);
        }

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
                nextCursor,      // indexInfo.id를 문자열로
                nextIdAfter,     // indexInfo.id 숫자값
                content.size(),
                (long) content.size(),
                hasNext
        );
    }

    private boolean isCursorValidForFilter(Long cursorId, Long indexInfoId, Boolean enabled, String sortField) {
        // 해당 커서 ID가 현재 필터 조건에 존재하는지 확인
        return autoSyncRepository.existsByCursorAndFilter(cursorId, indexInfoId, enabled, sortField);
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
