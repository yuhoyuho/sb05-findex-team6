package com.example.findex.service;

import com.example.findex.domain.Auto_Sync.entity.AutoSync;
import com.example.findex.domain.Auto_Sync.repository.AutoSyncRepository;
import com.example.findex.dto.AutoSyncConfigDto;
import com.example.findex.dto.AutoSyncConfigUpdateRequest;
import com.example.findex.mapper.AutoSyncMapper;
import lombok.RequiredArgsConstructor;
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
    public List<AutoSyncConfigDto> findAll(Long id, Boolean enabled) {
        if (id == null && enabled == null) {
            return autoSyncRepository.findAll().stream().map(autoSyncMapper::toDto).toList(); // ✅ 전체 조회
        }
        return autoSyncRepository.findByConditions(id, enabled).stream()
                .map(autoSyncMapper::toDto).toList();
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
