package com.example.findex.domain.Sync_Job_Log.dto;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        String nextCursor,
        int size,
        Long totalElements,
        boolean hasNext
) {}
