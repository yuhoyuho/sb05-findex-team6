package com.example.findex.domain.Sync_Job_Log.dto;

import com.example.findex.common.base.JobResult;
import com.example.findex.common.base.JobType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SyncJobQueryParams {
    JobType jobType;
    Long indexInfoId;
    LocalDate baseDateFrom;
    LocalDate baseDateTo;
    String worker;
    LocalDateTime jobTimeFrom;
    LocalDateTime jobTimeTo;
    JobResult status;
    String cursor;
    @Builder.Default String sortField = "jobTime";
    @Builder.Default String sortDirection = "desc";
    @Builder.Default Integer size = 10;
}
