package com.example.findex.domain.Sync_Job_Log.service;

import com.example.findex.common.base.JobType;
import com.example.findex.common.openApi.service.IndexSyncService;
import com.example.findex.domain.Sync_Job_Log.dto.SyncResult;
import com.example.findex.domain.Sync_Job_Log.entity.SyncJobLog;
import com.example.findex.domain.Sync_Job_Log.repository.SyncJobLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncJobLogService {

    private final IndexSyncService indexSyncService;
    private final SyncJobLogRepository syncJobLogRepository;

    @Transactional
    public List<SyncJobLog> syncAndLogLatestIndexData(String worker) {
        LocalDate today = LocalDate.now();

        List<SyncResult> syncResults = indexSyncService.syncDailyDataAndWithResults(today);
        List<SyncJobLog> createdLogs = new ArrayList<>();

        for(SyncResult result : syncResults) {
            if(result.indexInfo() == null) {
                log.warn("IndexInfo가 없습니다. Details : {}", result.details());
                continue;
            }

            SyncJobLog log = SyncJobLog.builder()
                    .jobType(JobType.INDEX_DATA)
                    .targetDate(today)
                    .worker(worker)
                    .jobTime(today)
                    .indexInfo(result.indexInfo())
                    .result(result.result())
                    .build();

            createdLogs.add(syncJobLogRepository.save(log));
        }

        log.info("{}건의 동기화 작업 로그가 기록되었습니다.", createdLogs.size());
        return createdLogs;
    }
}
