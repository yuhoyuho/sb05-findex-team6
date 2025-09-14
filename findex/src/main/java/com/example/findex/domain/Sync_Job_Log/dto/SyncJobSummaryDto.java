package com.example.findex.domain.Sync_Job_Log.dto;

import java.time.LocalDateTime;

public record SyncJobSummaryDto(
        long successCount,
        long failedCount,
        LocalDateTime lastSyncTime
) {
}
