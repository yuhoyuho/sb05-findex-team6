package com.example.findex.domain.Sync_Job_Log.repository;

import com.example.findex.domain.Sync_Job_Log.dto.CursorPageResponse;
import com.example.findex.domain.Sync_Job_Log.dto.SyncJobQueryParams;
import com.example.findex.domain.Sync_Job_Log.entity.SyncJobLog;

public interface SyncJobLogRepositoryCustom {
    CursorPageResponse<SyncJobLog> search(SyncJobQueryParams params);
}
