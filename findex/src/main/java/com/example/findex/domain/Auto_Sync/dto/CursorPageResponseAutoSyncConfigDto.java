package com.example.findex.domain.Auto_Sync.dto;

import java.util.List;

public record CursorPageResponseAutoSyncConfigDto<T>(
        List<T> content,
        String nextCursor,
        Long nextIdAfter,
        int size,
        Long totalElements,
        boolean hasNext
) {
}
