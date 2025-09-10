package com.example.findex.domain.Sync_Job_Log.service;

import com.example.findex.common.base.JobType;
import com.example.findex.common.openApi.service.IndexSyncService;
import com.example.findex.domain.Sync_Job_Log.dto.IndexDataSyncRequest;
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

    /**
     * 오늘 날짜의 모든 지수/정보를 연동하고 로그를 기록하는 메서드
     */
    @Transactional
    public List<SyncJobLog> syncAndLogLatestIndexData(String worker, LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;
        LocalDate today = LocalDate.now();

//        List<SyncResult> syncResults = indexSyncService.syncDailyDataAndWithResults(today);
//        List<SyncJobLog> createdLogs = new ArrayList<>();
//
//        for(SyncResult result : syncResults) {
//            if(result.indexInfo() == null) {
//                log.warn("IndexInfo가 없습니다. Details : {}", result.details());
//                continue;
//            }
//
//            SyncJobLog log = SyncJobLog.builder()
//                    .jobType(JobType.INDEX_DATA)
//                    .targetDate(today)
//                    .worker(worker)
//                    .jobTime(today)
//                    .indexInfo(result.indexInfo())
//                    .result(result.result())
//                    .build();
//
//            createdLogs.add(syncJobLogRepository.save(log));
//        }
//
//        log.info("{}건의 동기화 작업 로그가 기록되었습니다.", createdLogs.size());
//        return createdLogs;

        List<SyncResult> result = indexSyncService.syncIndexDataByFilter(target, target, null);
        return createLogsFromResult(result, worker);
    }

    /**
     * 특정 조건에 맞는 지수 데이터를 연동하고 로그를 기록하는 메서드
     */
    @Transactional
    public List<SyncJobLog> syncSpecificIndexDataAndLog(IndexDataSyncRequest request, String worker) {
        List<SyncResult> result = indexSyncService.syncIndexDataByFilter(request.baseDateFrom(), request.baseDateTo(), request.indexInfoIds());

        return createLogsFromResult(result, worker);
    }

    /**
     * 중복 제거용 헬퍼 메서드
     * 동기화 결과를 받아서 SyncJobLog 엔티티를 만들고 db에 저장
     */
    private List<SyncJobLog> createLogsFromResult(List<SyncResult> result, String worker) {
        List<SyncJobLog> createdLogs = new ArrayList<>();

        for(SyncResult syncResult : result) {
            if(syncResult.indexInfo() == null) {
                log.warn("로그 기록 실패 : IndexInfo가 없습니다. Details : {}", syncResult.details());
                continue;
            }

            SyncJobLog log = SyncJobLog.builder()
                    .jobType(JobType.INDEX_DATA)
                    .targetDate(syncResult.targetDate())
                    .worker(worker)
                    .jobTime(LocalDate.now())
                    .indexInfo(syncResult.indexInfo())
                    .result(syncResult.result())
                    .build();

            createdLogs.add(syncJobLogRepository.save(log));
        }

        log.info("{}건의 동기화 작업 로그를 기록하였습니다.", createdLogs.size());
        return createdLogs;
    }
}
