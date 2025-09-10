package com.example.findex.domain.Sync_Job_Log.dto;

import com.example.findex.common.base.JobResult;
import com.example.findex.domain.Index_Info.entity.IndexInfo;

import java.time.LocalDate;

public record SyncResult(
        JobResult result,
        IndexInfo indexInfo,
        String details,
        LocalDate targetDate) {
}
