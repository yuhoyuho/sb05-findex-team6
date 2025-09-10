package com.example.findex.domain.Sync_Job_Log.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class IndexDataScheduler {

    private final SyncJobLogService syncJobLogService;

    // 새벽 4시
    @Scheduled(cron = "0 0 4 * * *")
    public void runDailySyncJob() {
        syncJobLogService.syncAndLogLatestIndexData("SYSTEM", LocalDate.now());
    }
}
