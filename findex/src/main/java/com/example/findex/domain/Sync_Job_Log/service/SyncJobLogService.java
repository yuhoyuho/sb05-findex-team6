package com.example.findex.domain.Sync_Job_Log.service;

import com.example.findex.common.base.JobResult;
import com.example.findex.common.base.JobType;
import com.example.findex.common.openApi.service.IndexSyncService;
import com.example.findex.domain.Sync_Job_Log.dto.*;
import com.example.findex.domain.Sync_Job_Log.entity.QSyncJobLog;
import com.example.findex.domain.Sync_Job_Log.entity.SyncJobLog;
import com.example.findex.domain.Sync_Job_Log.mapper.SyncJobLogMapper;
import com.example.findex.domain.Sync_Job_Log.repository.SyncJobLogRepository;
import com.example.findex.domain.Sync_Job_Log.repository.SyncJobLogRepositoryCustom;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncJobLogService implements SyncJobLogRepositoryCustom {

    private final IndexSyncService indexSyncService;
    private final SyncJobLogRepository syncJobLogRepository;
    private final SyncJobLogMapper syncJobLogMapper;
    private final JPAQueryFactory queryFactory;

    /**
     * 오늘 날짜의 모든 지수/정보를 연동하고 로그를 기록하는 메서드
     */
    @Transactional
    public List<SyncJobLog> syncAndLogLatestIndexData(String worker, LocalDate date) {
        LocalDate target = date == null ? LocalDate.now() : date;

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
     *
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<SyncJobLogDto> getSyncJobList(SyncJobQueryParams params) {
        CursorPageResponse<SyncJobLog> result = this.search(params);

        // entity -> dto
        List<SyncJobLogDto> list = syncJobLogMapper.toDtoList(result.content());

        return new CursorPageResponse<>(list, result.nextCursor(), result.size(), result.hasNext());
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
                    .jobTime(LocalDateTime.now())
                    .indexInfo(syncResult.indexInfo())
                    .result(syncResult.result())
                    .build();

            createdLogs.add(syncJobLogRepository.save(log));
        }

        log.info("{}건의 동기화 작업 로그를 기록하였습니다.", createdLogs.size());
        return createdLogs;
    }

    @Override
    public CursorPageResponse<SyncJobLog> search(SyncJobQueryParams params) {
        QSyncJobLog syncJobLog = QSyncJobLog.syncJobLog;

        int size = params.getSize();
        List<SyncJobLog> content = queryFactory
                .selectFrom(syncJobLog)
                .where(
                        jobTypeEq(params.getJobType()),
                        indexInfoIdEq(params.getIndexInfoId()),
                        baseDateBetween(params.getBaseDateFrom(), params.getBaseDateTo()),
                        workerEq(params.getWorker()),
                        jobTimeBetween(params.getJobTimeFrom(), params.getJobTimeTo()),
                        statusEq(params.getStatus()),
                        cursorCondition(params.getCursor(), params.getSortField(), params.getSortDirection())
                )
                .orderBy(getOrderSpecifiers(params.getSortField(), params.getSortDirection()))
                .limit(size + 1)
                .fetch();

        boolean hasNext = content.size() > size;
        String nextCursor = null;

        if(hasNext) {
            SyncJobLog lastElement = content.remove(size);
            nextCursor = generateCursor(lastElement, params.getSortField());
        }

        return new CursorPageResponse<>(content, nextCursor, size, hasNext);
    }

    // --- 동적 WHERE 절을 위한 헬퍼 메서드들 ---

    private BooleanExpression jobTypeEq(JobType jobType) {
        return jobType != null ? QSyncJobLog.syncJobLog.jobType.eq(jobType) : null;
    }

    private BooleanExpression indexInfoIdEq(Long indexInfoId) {
        return indexInfoId != null ? QSyncJobLog.syncJobLog.indexInfo.id.eq(indexInfoId) : null;
    }

    private BooleanExpression baseDateBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) return null;
        return QSyncJobLog.syncJobLog.targetDate.between(from, to);
    }

    private BooleanExpression workerEq(String worker) {
        return worker != null && !worker.isBlank() ? QSyncJobLog.syncJobLog.worker.eq(worker) : null;
    }

    private BooleanExpression jobTimeBetween(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) return null;
        return QSyncJobLog.syncJobLog.jobTime.between(from, to);
    }

    private BooleanExpression statusEq(JobResult status) {
        return status != null ? QSyncJobLog.syncJobLog.result.eq(status) : null;
    }

    private BooleanExpression cursorCondition(String cursor, String sortField, String sortDirection) {
        if (cursor == null) {
            return null;
        }

        String[] parts = cursor.split("_");
        if (parts.length != 2) throw new IllegalArgumentException("Invalid cursor format");

        long lastId = Long.parseLong(parts[1]);
        QSyncJobLog syncJobLog = QSyncJobLog.syncJobLog;
        boolean isDesc = "desc".equalsIgnoreCase(sortDirection);

        if ("jobTime".equals(sortField)) {
            LocalDateTime lastJobTime = LocalDateTime.parse(parts[0]);
            if (isDesc) {
                return syncJobLog.jobTime.lt(lastJobTime)
                        .or(syncJobLog.jobTime.eq(lastJobTime).and(syncJobLog.id.lt(lastId)));
            } else {
                return syncJobLog.jobTime.gt(lastJobTime)
                        .or(syncJobLog.jobTime.eq(lastJobTime).and(syncJobLog.id.gt(lastId)));
            }
        } else { // "targetDate"
            LocalDate lastTargetDate = LocalDate.parse(parts[0]);
            if (isDesc) {
                return syncJobLog.targetDate.lt(lastTargetDate)
                        .or(syncJobLog.targetDate.eq(lastTargetDate).and(syncJobLog.id.lt(lastId)));
            } else {
                return syncJobLog.targetDate.gt(lastTargetDate)
                        .or(syncJobLog.targetDate.eq(lastTargetDate).and(syncJobLog.id.gt(lastId)));
            }
        }
    }

    // --- 동적 ORDER BY 절을 위한 헬퍼 메서드 ---

    private OrderSpecifier<?>[] getOrderSpecifiers(String sortField, String sortDirection) {
        QSyncJobLog syncJobLog = QSyncJobLog.syncJobLog;
        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);
        Order order = isAsc ? Order.ASC : Order.DESC;
        OrderSpecifier<Long> idOrder = new OrderSpecifier<>(order, syncJobLog.id);

        if ("jobTime".equals(sortField)) {
            OrderSpecifier<LocalDateTime> jobTimeOrder = new OrderSpecifier<>(order, syncJobLog.jobTime);
            return new OrderSpecifier[]{jobTimeOrder, idOrder};
        } else { // "targetDate"
            OrderSpecifier<LocalDate> targetDateOrder = new OrderSpecifier<>(order, syncJobLog.targetDate);
            return new OrderSpecifier[]{targetDateOrder, idOrder};
        }
    }

    // --- 다음 커서 생성을 위한 헬퍼 메서드 ---
    private String generateCursor(SyncJobLog lastElement, String sortField) {
        if (lastElement == null) return null;

        String valuePart = "jobTime".equals(sortField)
                ? lastElement.getJobTime().toString()
                : lastElement.getTargetDate().toString();
        return valuePart + "_" + lastElement.getId();
    }
}
