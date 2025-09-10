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
            Long id, Boolean enabled, String cursor, int size) {

        Long cursorIndexId = null;
        if(cursor != null && !cursor.isEmpty()) {
            try {
                cursorIndexId = Long.parseLong(cursor);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid cursor format: " + cursor);
            }
        }

        Pageable pageable = PageRequest.of(0, size + 1);

        Slice<AutoSync> slice;
        if(cursorIndexId == null) {
            slice = autoSyncRepository.findFirstPageByConditions(id, enabled, pageable);
        } else {
            slice = autoSyncRepository.findAfterCursorByConditions(id, enabled, cursorIndexId, pageable);
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

    @Transactional
    public AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request) {
        AutoSync autoSync = autoSyncRepository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("해당 Id가 없습니다."));
        autoSync.updateEnabled(request.enabled());
        autoSyncRepository.save(autoSync);
        return autoSyncMapper.toDto(autoSync);
    }
}
