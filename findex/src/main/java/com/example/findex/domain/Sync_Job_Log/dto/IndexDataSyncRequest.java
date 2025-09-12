package com.example.findex.domain.Sync_Job_Log.dto;

import java.time.LocalDate;
import java.util.List;

public record IndexDataSyncRequest(
        List<Long> indexInfoIds,
        LocalDate baseDateFrom,
        LocalDate baseDateTo) {
}
